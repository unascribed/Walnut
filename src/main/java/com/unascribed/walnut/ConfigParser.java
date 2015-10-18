package com.unascribed.walnut;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.unascribed.walnut.value.ArrayValue;
import com.unascribed.walnut.value.BooleanValue;
import com.unascribed.walnut.value.DoubleValue;
import com.unascribed.walnut.value.IntValue;
import com.unascribed.walnut.value.LongValue;
import com.unascribed.walnut.value.NullValue;
import com.unascribed.walnut.value.StringValue;
import com.unascribed.walnut.value.Value;

public class ConfigParser {
	private Reader src;
	private int idx;

	private int bufPrevious = -1;
	private int bufCurrent = -1;
	private int bufNext = -1;
	private int bufNextNext = -1;
	
	private boolean processComments = true;
	private String lastDocumentationComment;
	
	public ConfigParser(Reader src) {
		this.src = new BufferedReader(src);
	}
	
	/**
	 * Public to test units or something.
	 */
	public int getPosition() {
		return idx;
	}
	
	/**
	 * Blah blah unit testing public blah.
	 */
	public String getLastDocumentationComment() {
		return lastDocumentationComment;
	}
	
	public ConfigParser prepare() throws IOException, ParseException {
		bufNext = read();
		return this;
	}
	
	
	/**
	 * Parse the contents of a Reader into a WalnutConfig.
	 * <p>
	 * Using the from* methods in WalnutConfig is much preferred to using
	 * this method directly.
	 * @return a newly created WalnutConfig from the contents of the passed reader
	 * @throws IOException if an underlying IO error occurs
	 * @throws ParseException if the reader does not contain a proper Walnut config
	 */
	public WalnutConfig parse() throws IOException, ParseException {
		WalnutConfig conf = new WalnutConfig();
		while (hasMore()) {
			String k = readKey();
			if (k == null) break;
			conf.map.put(new Key(k, lastDocumentationComment), readValue());
			lastDocumentationComment = null;
		}
		return conf;
	}
	
	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public String readKey() throws IOException, ParseException {
		skipWhitespace();
		String rtrn = trim(allUntil(':', '=', '{', '(', '['));
		if (rtrn == null) return null;
		int sep = bufCurrent;
		//System.out.print("q: ");
		//System.out.println(Character.toChars(sep));
		// In the case of a spanned string, section, or array, we want to keep
		// the initial character so it is properly detected by readValue
		if (sep == '{' || sep == '(' || sep == '[') {
			withdraw();
		}
		return rtrn;
	}
	
	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public Value readValue() throws IOException, ParseException {
		skipWhitespace();
		int first = advance();
		if (first == '"' || first == '(') {
			boolean spanning = (first == '(');
			int endChar = (spanning ? ')' : '"');
			return readString(spanning, first, endChar);
		} else if (first == '[') {
			List<Value> li = new ArrayList<Value>();
			StringBuilder sb = new StringBuilder();
			sb.appendCodePoint(first);
			while (true) {
				int c = advance();
				if (c == ']') {
					sb.appendCodePoint(c);
					break;
				} else {
					withdraw();
				}
				Value v = readValue();
				li.add(v);
				sb.append(v.getRawValue());
				sb.append(allWhitespace());
				int n = advance();
				sb.appendCodePoint(n);
				if (n == ']') break;
				else if (n != ',') throw new ParseException("Expected comma, but was "+new String(Character.toChars(n)), idx);
				sb.append(allWhitespace());
			}
			return new ArrayValue(sb.toString(), li.toArray(new Value[li.size()]));
		} else if (first == '{') {
			WalnutConfig conf = new WalnutConfig();
			while (true) {
				skipWhitespace();
				int c = advance();
				if (c == '}') {
					break;
				} else {
					withdraw();
				}
				String k = readKey();
				if (k == null) throw new EOFException("section was not closed before EOF at "+idx);
				conf.map.put(new Key(k, lastDocumentationComment), readValue());
				lastDocumentationComment = null;
			}
			return conf;
		} else {
			withdraw();
			String token = allUntilWhitespaceOr(',', '}', ']', '\n');
			//System.out.println("W: "+token);
			if (first == '-' || first == 'I' || first == 'N' || isBasicDigit(first)) {
				String n = token.startsWith("0x") ? token : trimLeadingZeroes(token);
				try {
					return new IntValue(token, Integer.decode(n));
				} catch (IllegalArgumentException e) {
					try {
						return new LongValue(token, Long.decode(n));
					} catch (IllegalArgumentException e1) {
						e1.initCause(e);
						try {
							return new DoubleValue(token, Double.parseDouble(n));
						} catch (IllegalArgumentException e2) {
							e2.initCause(e1);
							throw (ParseException) new ParseException("Invalid number: "+token, idx).initCause(e2);
						}
					}
				}
			} else if (token.equals("on") || token.equals("true") || token.equals("enabled")) {
				return new BooleanValue(token, true);
			} else if (token.equals("off") || token.equals("false") || token.equals("disabled")) {
				return new BooleanValue(token, false);
			} else if (token.equals("null") || token.equals("nil") || token.equals("undefined")) {
				return new NullValue(token);
			} else {
				throw new ParseException("Cannot parse unknown value type", idx);
			}
		}
	}

	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public StringValue readString(boolean spanning, int beginChar, int endChar) throws IOException, ParseException {
		boolean oldSkipComments = processComments;
		processComments = false;
		try {
			int begin = idx;
			StringBuilder accumulator = new StringBuilder();
			StringBuilder origAccumulator = new StringBuilder();
			origAccumulator.appendCodePoint(beginChar);
			if (spanning) {
				origAccumulator.append(allWhitespace());
			}
			while (true) {
				int next = advance();
				origAccumulator.appendCodePoint(next);
				if (next == endChar) break;
				if (next == '\n') {
					if (spanning) {
						accumulator.appendCodePoint(next);
						origAccumulator.append(allWhitespace());
						continue;
					} else {
						throw new ParseException("String extends into infinity", begin);
					}
				}
				if (next == '\\') {
					int nextNext = advance();
					origAccumulator.appendCodePoint(nextNext);
					String s = null;
					switch (nextNext) {
						case '"':
						case '(':
						case '\\':
							accumulator.appendCodePoint(nextNext);
							break;
						case 'x':
							accumulator.appendCodePoint(Integer.parseInt(s = next(2), 16));
							break;
						case 'u':
							accumulator.appendCodePoint(Integer.parseInt(s = next(4), 16));
							break;
						case 'U':
							accumulator.appendCodePoint(Integer.parseInt(s = next(8), 16));
							break;
						default:
							throw new ParseException("Unknown escape "+String.copyValueOf(Character.toChars(nextNext)), idx);
					}
					if (s != null) origAccumulator.append(s);
					continue;
				} else {
					accumulator.appendCodePoint(next);
				}
			}
			return new StringValue(origAccumulator.toString(), accumulator.toString());
		} finally {
			processComments = oldSkipComments;
		}
	}

	/**
	 * Remove leading zeroes from a string for safe parsing as a non-octal value.
	 * <p>
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public String trimLeadingZeroes(String s) {
		int start = 0;
		for (; start < s.length(); start++) {
			if (s.codePointAt(start) != '0') {
				break;
			}
		}
		return s.substring(start);
	}

	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public String next(int count) throws IOException, ParseException {
		StringBuilder accumulator = new StringBuilder();
		for (int i = 0; i < count; i++) {
			accumulator.appendCodePoint(advance());
		}
		return accumulator.toString();
	}

	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public boolean isBasicDigit(int codePoint) {
		return codePoint >= 0x30 && codePoint <= 0x39;
	}

	/**
	 * Alternate version of String::trim that properly handles
	 * whitespace outside of ASCII.
	 * <p>
	 * Public only to allow unit testing.
	 */
	public static String trim(String s) {
		if (s == null) return null;
		int start = 0;
		for (; start < s.length(); start++) {
			if (!Character.isWhitespace(s.codePointAt(start))) {
				break;
			}
		}
		int end = s.length();
		for (; end > 0; end--) {
			if (!Character.isWhitespace(s.codePointAt(end-1))) {
				break;
			}
		}
		return s.substring(start, end);
	}

	/**
	 * Behaves as if calling allUntil with every possible whitespace character as an
	 * argument. Except not implemented like that, because that would be horrible.
	 * <p>
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public String allUntilWhitespace() throws IOException, ParseException {
		return allUntilWhitespaceOr();
	}
	public String allUntilWhitespaceOr(int... end) throws IOException, ParseException {
		StringBuilder accumulator = new StringBuilder();
		while (true) {
			int c = tryAdvance();
			if (c == -1 || Character.isWhitespace(c)) {
				withdraw();
				return accumulator.toString();
			} else {
				for (int i : end) {
					if (i == c) {
						withdraw();
						return accumulator.toString();
					}
				}
			}
			accumulator.appendCodePoint(c);
		}
	}
	
	/**
	 * Read all characters, starting from the current position in the stream. The character that
	 * halted reading will be skipped, but can be retrieved from the {@code current} field.
	 * <p>
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public String allUntil(int... end) throws IOException, ParseException {
		StringBuilder accumulator = new StringBuilder();
		while (true) {
			int c = tryAdvance();
			if (c == -1) return null;
			for (int i : end) {
				if (c == i) {
					return accumulator.toString();
				}
			}
			accumulator.appendCodePoint(c);
		}
	}
	
	public String all(int... incl) throws IOException, ParseException {
		StringBuilder accumulator = new StringBuilder();
		while (true) {
			int c = tryAdvance();
			if (c == -1) break;
			boolean any = (c == -1);
			for (int i : incl) {
				if (c == i) {
					accumulator.appendCodePoint(c);
					any = true;
				}
			}
			if (!any) {
				break;
			}
		}
		return accumulator.toString();
	}
	
	public void skipUntil(int... end) throws IOException, ParseException {
		while (true) {
			int c = advance();
			for (int i : end) {
				if (c == i) {
					return;
				}
			}
		}
	}

	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public void skipWhitespace() throws IOException, ParseException {
		while (Character.isWhitespace(tryAdvance())) {}
		withdraw();
	}
	
	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public String allWhitespace() throws IOException, ParseException {
		StringBuilder sb = new StringBuilder();
		while (true) {
			int c = advance();
			if (Character.isWhitespace(c)) {
				sb.appendCodePoint(c);
			} else {
				break;
			}
		}
		withdraw();
		return sb.toString();
	}

	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public boolean hasMore() {
		return bufNext != -1;
	}
	
	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public int withdraw() throws IOException, ParseException {
		if (bufNextNext != -1) {
			throw new AssertionError("Already withdrawn!");
		}
		//System.out.println("Withdrawing");
		idx--;
		bufNextNext = bufNext;
		bufNext = bufCurrent;
		bufCurrent = bufPrevious;
		bufPrevious = -1;
		return bufCurrent;
	}
	
	/**
	 * Advances the stream forward a character, properly handling withdraws
	 * and processing comments. If EOF is reached, an exception is thrown.
	 * <p>
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public int advance() throws IOException, ParseException {
		int rtrn = tryAdvance();
		if (rtrn == -1) throw new EOFException("unexpected EOF at "+idx);
		return rtrn;
	}
	/**
	 * Advances the stream forward a character, properly handling withdraws
	 * and processing comments. If EOF is reached, -1 is returned.
	 * <p>
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public int tryAdvance() throws IOException, ParseException {
		boolean processingLineComment = false;
		boolean processingBlockComment = false;
		
		boolean firstBlockCommentChar = false;
		boolean firstDocCommentChar = false;
		
		StringBuilder documentationBuilder = null;
		//System.out.println("Entering advance");
		int skip = 0;
		while (true) {
			bufPrevious = bufCurrent;
			bufCurrent = bufNext;
			
			if (bufNextNext == -1) {
				bufNext = read();
			} else {
				bufNext = bufNextNext;
				bufNextNext = -1;
			}
			if (bufCurrent == -1) {
				break;
			}
			//System.out.println(Character.toChars(bufCurrent));
			
			if (skip > 0) {
				//System.out.println("Skipping");
				skip--;
				continue;
			}
			
			if (processComments) {
				if (processingLineComment) {
					if (bufCurrent == '\n') {
						processingLineComment = false;
					}
				} else if (processingBlockComment) {
					if (firstBlockCommentChar) {
						if (bufCurrent == '*') {
							documentationBuilder = new StringBuilder();
							firstDocCommentChar = true;
						}
						firstBlockCommentChar = false;
					}
					if (bufCurrent == '*' && bufNext == '/') {
						processingBlockComment = false;
						skip = 1;
					} else if (documentationBuilder != null) {
						if (firstDocCommentChar) {
							firstDocCommentChar = false;
						} else {
							documentationBuilder.appendCodePoint(bufCurrent);
						}
					}
				} else if (bufCurrent == '/') {
					if (bufNext == '/') {
						processingLineComment = true;
						skip = 1;
					} else if (bufNext == '*') {
						lastDocumentationComment = null;
						processingBlockComment = true;
						firstBlockCommentChar = true;
						skip = 1;
					}
				}
			}
			
			if (!processingLineComment && !processingBlockComment && skip <= 0) {
				break;
			}
		}
		
		if (documentationBuilder != null) {
			lastDocumentationComment = documentationBuilder.toString();
		}
		
		return bufCurrent;
	}

	/**
	 * Reads a character from the underlying stream, automatically combining
	 * surrogate pairs.
	 * <p>
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public int read() throws IOException, ParseException {
		int a = src.read();
		if (a == -1) {
			return -1;
		} else {
			idx++;
			if (Character.isHighSurrogate((char)a)) {
				int b = src.read();
				if (b == -1) throw new EOFException("EOF while reading second half of surrogate pair");
				idx++;
				if (Character.isSurrogatePair((char)a, (char)b)) {
					return Character.toCodePoint((char)a, (char)b);
				} else {
					throw new ParseException("invalid surrogate pair", idx);
				}
			} else {
				return a;
			}
		}
	}
}
