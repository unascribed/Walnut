package com.unascribed.walnut.test;

import static org.junit.Assert.*;

import java.io.EOFException;
import java.io.StringReader;

import org.junit.Test;

import com.unascribed.walnut.ConfigParser;

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
	public void testTrim() {
		String a = "a long string with\twhitespace\rin the\f\f  middle";
		assertEquals(a, ConfigParser.trim("\t\t\t\f\r    \r\t "+a+"\t\r\r \f\r\t"));
	}
}
