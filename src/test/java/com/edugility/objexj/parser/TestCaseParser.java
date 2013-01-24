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
    final Tokenizer tokenizer = new Tokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    assertEquals(3, p.size());
  }

  @Test
  public void testParseSimpleAlternation() throws IOException {
    final PushbackReader reader = new PushbackReader(new StringReader("java.lang.Character(charValue() == 'a')|java.lang.Character(charValue() == 'b')"));
    final Tokenizer tokenizer = new Tokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    System.out.println(p);
  }

}
