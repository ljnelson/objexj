/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright (c) 2013 Edugility LLC.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * The original copy of this license is available at
 * http://www.opensource.org/license/mit-license.html.
 */
package com.edugility.objexj.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.edugility.objexj.engine.Instruction;
import com.edugility.objexj.engine.Program;

public class TestCaseParser {

  public TestCaseParser() {
    super();
  }

  public static boolean assertContentsAreEqual(final Program<?> program, final String resourceName) throws IOException {
    assertNotNull(program);
    assertNotNull(resourceName);
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    assertNotNull(cl);
    final InputStream stream = cl.getResourceAsStream(resourceName);
    assertNotNull(stream);
    
    final BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
    assertEquals(String.format("%s:", program.getName()), br.readLine());
    final Iterator<?> iterator = program.iterator();
    assertNotNull(iterator);
    String line;
    while ((line = br.readLine()) != null) {
      assertTrue(iterator.hasNext());
      final Object instruction = iterator.next();
      assertNotNull(instruction);
      assertEquals(instruction.toString(), line.substring(5));
    }
    assertFalse(iterator.hasNext());

    br.close();
    return true;
  }

  @Test
  public void testParseSimpleCatenation() throws IOException {
    final PushbackReader reader = new PushbackReader(new StringReader("java.lang.Character(charValue() == 'a')/java.lang.Character(charValue() == 'b')"));
    final PostfixTokenizer tokenizer = new PostfixTokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    p.setName("Simple catenation");
    assertContentsAreEqual(p, "SimpleCatenation.txt");
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
    p.setName("Long catenation");
    assertContentsAreEqual(p, "LongCatenation.txt");
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
    assertContentsAreEqual(p, "SimpleAlternation.txt");
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
    assertContentsAreEqual(p, "LongAlternation.txt");
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
    assertContentsAreEqual(p, "CombinationCatenationAndAlternation.txt");
  }

  @Test
  public void testEndInput() throws IOException {
    final StringBuilder sb = new StringBuilder();
    sb.append("^java.lang.Character(charValue() == 'a')$");
    final String source = sb.toString();
    final PushbackReader reader = new PushbackReader(new StringReader(source));
    final PostfixTokenizer tokenizer = new PostfixTokenizer(reader);
    final Parser parser = new Parser();
    final Program<Character> p = parser.parse(tokenizer);
    reader.close();
    assertNotNull(p);
    p.setName("End Input");
    assertContentsAreEqual(p, "EndInput.txt");
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
    p.setName("Begin Input Followed By Filter");
    assertContentsAreEqual(p, "BeginInputFollowedByFilter.txt");
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
    assertContentsAreEqual(p, "OneOrMoreFilter.txt");
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
    assertContentsAreEqual(p, "ZeroOrMoreFilter.txt");
  }

  @Test
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
    assertContentsAreEqual(p, "ZeroOrMoreFilterConcatenated.txt");
  }

}
