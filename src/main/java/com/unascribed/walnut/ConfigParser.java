package com.unascribed.walnut;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unascribed.walnut.value.BooleanValue;
import com.unascribed.walnut.value.DoubleValue;
import com.unascribed.walnut.value.IntValue;
import com.unascribed.walnut.value.LongValue;
import com.unascribed.walnut.value.Value;

public class ConfigParser {
	private static final Logger log = LoggerFactory.getLogger(ConfigParser.class);
	
	private Reader src;
	private int idx;

	private int previous = -1;
	private int current = -1;
	private int next = -1;
	private int nextNext = -1;
	
	public ConfigParser(Reader src) {
		this.src = new BufferedReader(src);
	}
	
	public ConfigParser prepare() throws IOException, ParseException {
		next = read();
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
			// TODO
		}
		return conf;
	}
	
	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public String readKey() throws IOException, ParseException {
		skipWhitespace();
		return trim(allUntil(':', '='));
	}
	
	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public Value readValue() throws IOException, ParseException {
		skipWhitespace();
		int first = advance();
		if (first == '"') {
			int begin = idx;
			StringBuilder accumulator = new StringBuilder();
			while (true) {
				int next = advance();
				if (next == '"') break;
				if (next == '\n') throw new ParseException("String extends into infinity", begin);
				if (next == '\\') {
					int nextNext = advance();
					switch (nextNext) {
						case '"':
						case '(':
						case '\\':
							accumulator.appendCodePoint(nextNext);
							break;
						case 'x':
							accumulator.appendCodePoint(Integer.parseInt(next(2), 16));
							break;
						case 'u':
							accumulator.appendCodePoint(Integer.parseInt(next(4), 16));
							break;
						case 'U':
							accumulator.appendCodePoint(Integer.parseInt(next(8), 16));
							break;
						default:
							throw new ParseException("Unknown escape "+String.copyValueOf(Character.toChars(nextNext)), idx);
					}
				}
			}
		} else {
			String token = allUntilWhitespace();
			if (token.isEmpty()) throw new ParseException("Expected value, got nothing", idx);
			if (token.startsWith("-") || isBasicDigit(token.codePointAt(0))) {
				String n = trimLeadingZeroes(token);
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
			} else if (token.equals("Infinity") || token.equals("+Infinity")) {
				return new DoubleValue(token, Double.POSITIVE_INFINITY);
			} else if (token.equals("-Infinity")) {
				return new DoubleValue(token, Double.NEGATIVE_INFINITY);
			} else if (token.equals("NaN")) {
				return new DoubleValue(token, Double.NaN);
			} else if (token.equals("yes") || token.equals("true") || token.equals("enabled")) {
				return new BooleanValue(token, true);
			}
			// TODO arrays, spanned strings, sections, false booleans, ...
		}
		return null;
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
		StringBuilder accumulator = new StringBuilder();
		while (true) {
			int c = advance();
			if (Character.isWhitespace(c)) return accumulator.toString();
			accumulator.appendCodePoint(c);
		}
	}
	
	/**
	 * Read all characters, starting from the current position in the stream,
	 * until a character in the passed list is encountered. The character that
	 * halted reading will be skipped, but can be retrieved with a {@link #withdraw}.
	 * <p>
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public String allUntil(int... end) throws IOException, ParseException {
		StringBuilder accumulator = new StringBuilder();
		while (true) {
			int c = advance();
			for (int i : end) {
				if (c == i) {
					return accumulator.toString();
				}
			}
			accumulator.appendCodePoint(c);
		}
	}

	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public void skipWhitespace() throws IOException, ParseException {
		while (Character.isWhitespace(advance())) {}
		withdraw();
	}

	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public boolean hasMore() {
		return next != -1;
	}
	
	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public int withdraw() throws IOException, ParseException {
		if (previous == -1) {
			throw new AssertionError("Cannot withdraw twice in a row");
		}
		idx--;
		nextNext = next;
		next = current;
		current = previous;
		previous = -1;
		return current;
	}
	
	/**
	 * Public only to allow unit testing.
	 * @see #parse()
	 */
	public int advance() throws IOException, ParseException {
		previous = current;
		current = next;
		
		if (nextNext == -1) {
			next = read();
		} else {
			next = nextNext;
			nextNext = -1;
		}
		if (current == -1) {
			throw new EOFException("unexpected EOF at "+idx);
		}
		return current;
	}

	/**
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
