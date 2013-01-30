package com.edugility.objexj.parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.edugility.objexj.Program;

public class TestCaseParser {

  public TestCaseParser() {
    super();
  }

  @Test
  public void testParseSimpleCatenation() throws IOException {
    final PushbackReader reader = new PushbackReader(new StringReader("java.lang.Character(charValue() == 'a')/java.lang.Character(charValue() == 'b')"));
    final PostfixTokenizer tokenizer = new PostfixTokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    assertEquals(3, p.size());
  }

  @Test
  public void testParseLongCatenation() throws IOException {
    final StringBuilder sb = new StringBuilder();
    sb.append("java.lang.Character(charValue() == 'a')/");
    sb.append("java.lang.Character(charValue() == 'b')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')/");
    sb.append("java.lang.Character(charValue() == 'c')");
    final String source = sb.toString();
    final PushbackReader reader = new PushbackReader(new StringReader(source));
    final PostfixTokenizer tokenizer = new PostfixTokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    assertEquals(source.split("/").length + 1, p.size());
  }

  @Test
  public void testParseSimpleAlternation() throws IOException {
    final PushbackReader reader = new PushbackReader(new StringReader("java.lang.Character(charValue() == 'a')|java.lang.Character(charValue() == 'b')"));
    final PostfixTokenizer tokenizer = new PostfixTokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    p.setName("Simple alternation");
    assertEquals(5, p.size());
    System.out.println(p);
  }

  @Test
  public void testParseLongAlternation() throws IOException {
    final StringBuilder sb = new StringBuilder();
    sb.append("java.lang.Character(charValue() == 'a')|");
    sb.append("java.lang.Character(charValue() == 'b')|");
    sb.append("java.lang.Character(charValue() == 'c')|");
    sb.append("java.lang.Character(charValue() == 'd')");
    final String source = sb.toString();
    final PushbackReader reader = new PushbackReader(new StringReader(source));
    final PostfixTokenizer tokenizer = new PostfixTokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    p.setName("Long Alternation");
    assertEquals(11, p.size());
    System.out.println(p);
  }

  @Test
  public void testCombinationCatenationAndAlternation() throws IOException {
    final StringBuilder sb = new StringBuilder();
    sb.append("java.lang.Character(charValue() == 'a')/"); // catenation
    sb.append("java.lang.Character(charValue() == 'b')|"); // altrnation
    sb.append("java.lang.Character(charValue() == 'c')");
    final String source = sb.toString();
    final PushbackReader reader = new PushbackReader(new StringReader(source));
    final PostfixTokenizer tokenizer = new PostfixTokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    p.setName("Combination Catenation and Alternation");
    assertEquals(6, p.size());
    System.out.println(p);
  }

  @Test
  public void testBeginInputFollowedByFilter() throws IOException {
    final StringBuilder sb = new StringBuilder();
    sb.append("^java.lang.Character(charValue() == 'a')");
    final String source = sb.toString();
    final PushbackReader reader = new PushbackReader(new StringReader(source));
    final PostfixTokenizer tokenizer = new PostfixTokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    p.setName("Combination Catenation and Alternation");
    System.out.println(p);
  }

  @Test
  public void testOneOrMoreFilter() throws IOException {
    final StringBuilder sb = new StringBuilder();
    sb.append("^java.lang.Character(charValue() == 'a')+");
    final String source = sb.toString();
    final PushbackReader reader = new PushbackReader(new StringReader(source));
    final PostfixTokenizer tokenizer = new PostfixTokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    p.setName("One or More Filter");
    System.out.println(p);
  }

  @Test
  public void testZeroOrMoreFilter() throws IOException {
    final StringBuilder sb = new StringBuilder();
    sb.append("^java.lang.Character(charValue() == 'a')*");
    final String source = sb.toString();
    final PushbackReader reader = new PushbackReader(new StringReader(source));
    final PostfixTokenizer tokenizer = new PostfixTokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    p.setName("Zero or More Filter");
    System.out.println(p);
  }

  // @Test
  public void testZeroOrMoreFilterConcatenated() throws IOException {
    final StringBuilder sb = new StringBuilder();
    sb.append("^java.lang.Character(charValue() == 'a')*/java.lang.Character");
    final String source = sb.toString();
    final PushbackReader reader = new PushbackReader(new StringReader(source));
    final PostfixTokenizer tokenizer = new PostfixTokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    p.setName("Zero or More Filter Concatenated");
    System.out.println(p);
  }

}
