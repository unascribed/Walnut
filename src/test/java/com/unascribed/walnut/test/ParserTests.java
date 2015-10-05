package com.unascribed.walnut.test;

import static org.junit.Assert.*;

import java.io.EOFException;
import java.io.StringReader;

import org.junit.Test;

import com.unascribed.walnut.ConfigParser;
import com.unascribed.walnut.Key;
import com.unascribed.walnut.WalnutConfig;
import com.unascribed.walnut.value.ArrayValue;
import com.unascribed.walnut.value.BooleanValue;
import com.unascribed.walnut.value.DoubleValue;
import com.unascribed.walnut.value.IntValue;
import com.unascribed.walnut.value.LongValue;
import com.unascribed.walnut.value.NullValue;
import com.unascribed.walnut.value.StringValue;
import com.unascribed.walnut.value.Value;

import sun.security.krb5.internal.ktab.KeyTabEntry;

public class ParserTests {
	private ConfigParser parser(String text) throws Exception {
		return new ConfigParser(new StringReader(text));
	}
	
	@Test
	public void testRead() throws Exception {
		assertEquals('a', parser("a").read());
		assertEquals('\t', parser("\t").read());
		assertEquals(0x1F431, parser("\uD83D\uDC31").read());
	}
	
	@Test
	public void testAdvance() throws Exception {
		String s = "abc foo a b c d e qwerty \uD83D\uDC31";
		StringBuilder sb = new StringBuilder();
		ConfigParser p = parser(s).prepare();
		while (p.hasMore()) {
			sb.appendCodePoint(p.advance());
		}
		assertEquals(s, sb.toString());
	}
	
	@Test
	public void testSkipWhitespace() throws Exception {
		ConfigParser p = parser(" \r \r\t\tfoo").prepare();
		p.skipWhitespace();
		assertEquals('f', p.advance());
		assertEquals('o', p.advance());
		assertEquals('o', p.advance());
	}
	
	@Test
	public void testOverflow() throws Exception {
		ConfigParser p = parser("abc\uD83D\uDC31").prepare();
		p.advance(); // a
		p.advance(); // b
		p.advance(); // c
		p.advance(); // cat face
		try {
			p.advance(); // EOF
			fail("did not throw EOF after seeking past end of reader");
		} catch (EOFException e) {}
	}
	
	@Test
	public void testWithdraw() throws Exception {
		StringBuilder sb = new StringBuilder();
		ConfigParser p = parser("abc foo a b c d e qwerty \uD83D\uDC31").prepare();
		sb.appendCodePoint(p.advance()); // a
		sb.appendCodePoint(p.advance()); // b
		sb.appendCodePoint(p.advance()); // c
		sb.appendCodePoint(p.advance()); // space
		sb.appendCodePoint(p.advance()); // f
		sb.appendCodePoint(p.withdraw()); // space
		sb.appendCodePoint(p.advance()); // f
		sb.appendCodePoint(p.advance()); // o
		sb.appendCodePoint(p.advance()); // o
		sb.appendCodePoint(p.advance()); // space
		sb.appendCodePoint(p.advance()); // a
		sb.appendCodePoint(p.advance()); // space
		sb.appendCodePoint(p.withdraw()); // a
		assertEquals("abc f foo a a", sb.toString());
	}
	
	@Test
	public void testDoubleWithdraw() throws Exception {
		ConfigParser p = parser("abcdef").prepare();
		p.advance();
		p.advance();
		p.withdraw();
		try {
			p.withdraw();
			fail("did not throw exception for double withdraw");
		} catch (AssertionError e) {}
	}
	
	@Test
	public void testReadKey() throws Exception {
		ConfigParser p = parser("potato: sandwich = mango :").prepare();
		assertEquals("potato", p.readKey());
		assertEquals("sandwich", p.readKey());
		assertEquals("mango", p.readKey());
	}
	
	@Test
	public void testTrim() throws Exception {
		String a = "a long string with\twhitespace\rin the\f\f  middle";
		assertEquals(a, ConfigParser.trim("\t\t\t\f\r    \r\t "+a+"\t\r\r \f\r\t"));
	}
	
	@Test
	public void testSkipComment() throws Exception {
		ConfigParser p = parser("/*Hi, I'm a comment!*/foo").prepare();
		assertEquals("f", new String(Character.toChars(p.advance())));
		assertEquals("o", new String(Character.toChars(p.advance())));
		assertEquals("o", new String(Character.toChars(p.advance())));
	}
	
	@Test
	public void testReadDocumentationComment() throws Exception {
		ConfigParser p = parser("/**Hi, I'm a documentation comment!*/a").prepare();
		p.advance();
		assertEquals("Hi, I'm a documentation comment!", p.getLastDocumentationComment());
	}
	
	@Test
	public void testReadDecimal() throws Exception {
		assertEquals(new IntValue("501", 501), parser("501").prepare().readValue());
		assertEquals(new LongValue("4294967295", 4294967295L), parser("4294967295").prepare().readValue());
		assertEquals(new IntValue("-1821", -1821), parser("-1821").prepare().readValue());
		assertEquals(new DoubleValue("0.532123", 0.532123), parser("0.532123").prepare().readValue());
	}
	
	@Test
	public void testReadHex() throws Exception {
		assertEquals(new IntValue("0xFE", 0xFE), parser("0xFE").prepare().readValue());
		// negative hex numbers are not a thing that should happen, but test it anyway
		assertEquals(new IntValue("-0xFFEFEF", -0xFFEFEF), parser("-0xFFEFEF").prepare().readValue());
		assertEquals(new LongValue("0xFFFFFFFF", 0xFFFFFFFFL), parser("0xFFFFFFFF").prepare().readValue());
	}
	
	@Test
	public void testReadScientific() throws Exception {
		assertEquals(new DoubleValue("1e17", 1e17), parser("1e17").prepare().readValue());
		assertEquals(new DoubleValue("4.75e20", 4.75e20), parser("4.75e20").prepare().readValue());
	}
	
	@Test
	public void testReadSpecial() throws Exception {
		assertEquals(new DoubleValue("Infinity", Double.POSITIVE_INFINITY), parser("Infinity").prepare().readValue());
		assertEquals(new DoubleValue("-Infinity", Double.NEGATIVE_INFINITY), parser("-Infinity").prepare().readValue());
		Value v = parser("NaN").prepare().readValue();
		assertTrue(v instanceof DoubleValue);
		assertEquals("NaN", v.getRawValue());
		assertTrue(Double.isNaN(((DoubleValue)v).value));
	}
	
	@Test
	public void testReadBoolean() throws Exception {
		assertEquals(new BooleanValue("on", true), parser("on").prepare().readValue());
		assertEquals(new BooleanValue("enabled", true), parser("enabled").prepare().readValue());
		assertEquals(new BooleanValue("true", true), parser("true").prepare().readValue());
		
		assertEquals(new BooleanValue("off", false), parser("off").prepare().readValue());
		assertEquals(new BooleanValue("disabled", false), parser("disabled").prepare().readValue());
		assertEquals(new BooleanValue("false", false), parser("false").prepare().readValue());
	}
	
	@Test
	public void testReadNull() throws Exception {
		assertEquals(new NullValue("nil"), parser("nil").prepare().readValue());
		assertEquals(new NullValue("null"), parser("null").prepare().readValue());
		assertEquals(new NullValue("undefined"), parser("undefined").prepare().readValue());
	}
	
	@Test
	public void testReadString() throws Exception {
		assertEquals(new StringValue("\"hello\"", "hello"), parser("\"hello\"").prepare().readValue());
		assertEquals(new StringValue("\"abc foo a b c d e qwerty \uD83D\uDC31\"", "abc foo a b c d e qwerty \uD83D\uDC31"), parser("\"abc foo a b c d e qwerty \uD83D\uDC31\"").prepare().readValue());
	}
	
	@Test
	public void testReadSpannedString() throws Exception {
		assertEquals(new StringValue("(\thello\n\tworld\n)", "hello\nworld\n"), parser("(\thello\n\tworld\n)").prepare().readValue());
	}
	
	@Test
	public void testReadArray() throws Exception {
		String s = "[1, 2, 3, 4, 5, 6, \"potato\", (sandwich), [\"mango\"]]";
		assertEquals(new ArrayValue(s, new Value[] {
				new IntValue("1", 1),
				new IntValue("2", 2),
				new IntValue("3", 3),
				new IntValue("4", 4),
				new IntValue("5", 5),
				new IntValue("6", 6),
				new StringValue("\"potato\"", "potato"),
				new StringValue("(sandwich)", "sandwich"),
				new ArrayValue("[\"mango\"]", new Value[] {
						new StringValue("\"mango\"", "mango")
				})
		}), parser(s).prepare().readValue());
		assertEquals(new ArrayValue("[]", new Value[0]), parser("[]").prepare().readValue());
	}
	
	@Test
	public void testReadSection() throws Exception {
		String s = "{a:\"b\" c:4}";
		WalnutConfig control = new WalnutConfig();
		control.put(new Key("a", null), new StringValue("\"b\"", "b"));
		control.put(new Key("c", null), new IntValue("4", 4));
		assertEquals(control, parser(s).prepare().readValue());
		assertEquals(new WalnutConfig(), parser("{}").prepare().readValue());
	}
}
