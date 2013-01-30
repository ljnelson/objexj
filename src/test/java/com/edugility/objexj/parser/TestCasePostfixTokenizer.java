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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.edugility.objexj.parser.Token.Type.BEGIN_ATOM;
import static com.edugility.objexj.parser.Token.Type.CATENATION;
import static com.edugility.objexj.parser.Token.Type.END_ATOM;
import static com.edugility.objexj.parser.Token.Type.FILTER;
import static com.edugility.objexj.parser.Token.Type.ONE_OR_MORE;
import static com.edugility.objexj.parser.Token.Type.START_SAVING;
import static com.edugility.objexj.parser.Token.Type.STOP_SAVING;
import static com.edugility.objexj.parser.Token.Type.ZERO_OR_MORE;

public class TestCasePostfixTokenizer {

  private PostfixTokenizer pft;

  private PushbackReader pbr;

  public TestCasePostfixTokenizer() {
    super();
  }

  @After
  public void closeReader() throws IOException {
    if (this.pbr != null) {
      this.pbr.close();
    }
  }

  @Before
  public void nullOutTokenizer() {
    this.pft = null;
    this.pbr = null;
  }

  @Test
  public void testZeroOrMoreInRepresentativeInput() throws IOException {
    build("^java.lang.Character(charValue() == 'a')*/java.lang.Character");
    
    /*
     * Translated:
     *
     *   ^ / java.lang.Character(charValue() == 'a') * / java.lang.Character
     *
     * In tokens:
     *
     * BEGIN_ATOM FILTER ZERO_OR_MORE CATENATION FILTER
     */
    assertNextIs(BEGIN_ATOM);
    assertNextIs(FILTER);
    assertNextIs(ZERO_OR_MORE);
    assertNextIs(CATENATION);
    assertNextIs(FILTER);
    assertNextIs(CATENATION);

    assertNoMoreTokens();
  }

  @Test
  public void testSaving() throws IOException {
    build("abc/(def)");

    /*
     * Let's pretend '[' means start saving and '(' means start grouping.
     * 
     * Translated:
     * 
     *   abc / [ / def / ]
     * 
     * In tokens:
     * 
     *   FILTER START_SAVING CATENATION FILTER CATENATION STOP_SAVING CATENATION
     * 
     */
      
    assertNextIs(FILTER);
    assertNextIs(START_SAVING);
    assertNextIs(CATENATION);
    assertNextIs(FILTER);
    assertNextIs(CATENATION);
    assertNextIs(STOP_SAVING);
    assertNextIs(CATENATION);

    assertNoMoreTokens();
  }

  @Test
  public void testInitialCaret() throws IOException {
    build("^");

    assertNextIs(BEGIN_ATOM);
    assertNextIs(CATENATION);

    assertNoMoreTokens();
  }

  @Test(expected = IllegalStateException.class)
  public void testStupidDoubleCaret() throws IOException {
    build("^^");
    assertNextIs(BEGIN_ATOM);
    this.pft.hasNext(); // will throw IllegalStateException
  }

  @Test(expected = IllegalStateException.class)
  public void testInitialConcatenationIsImpossible() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("/"));
    try {
      final PostfixTokenizer postfixTokenizer = new PostfixTokenizer(pbr); // will throw IllegalStateException
    } finally {
      pbr.close();
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testInitialAlternationIsImpossible() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("|"));
    try {
      final PostfixTokenizer postfixTokenizer = new PostfixTokenizer(pbr); // will throw IllegalStateException
    } finally {
      pbr.close();
    }
  }

  @Test
  public void testValidSaveSyntax() throws IOException {
    build("(froob)");

    assertNextIs(START_SAVING);
    assertNextIsFilter("froob");
    assertNextIs(CATENATION);
    assertNextIs(STOP_SAVING);
    assertNextIs(CATENATION);
    
    assertNoMoreTokens();
  }

  @Test(expected = IllegalStateException.class)
  public void testEmptySaveBlock() throws IOException {
    build("()");

    assertNextIs(START_SAVING);

    assertNoMoreTokens();
  }

  @Test
  public void testLotsOfOpenParens() throws IOException {
    build("((((");

    assertNextIs(START_SAVING);
    assertNextIs(START_SAVING);
    assertNextIs(START_SAVING);
    assertNextIs(START_SAVING);

    try {
      assertNoMoreTokens(); // will throw IllegalStateException
      fail();
    } catch (final IllegalStateException expected) {
      
    }
  }

  @Test
  public void testAnchoredCatnenationOfNonQualifiedFilters() throws IOException {
    build("^java.lang.Character/java.lang.Integer");
      
    assertNextIs(BEGIN_ATOM);
    assertNextIs(FILTER);
    assertNextIs(CATENATION);
    assertNextIs(FILTER);
    assertNextIs(CATENATION);
      
    assertNoMoreTokens();
  }

  @Test(expected = IllegalStateException.class)
  public void testBadInitialDollar() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("$"));
    try {
      final PostfixTokenizer postfixTokenizer = new PostfixTokenizer(pbr);
    } finally {
      pbr.close();
    }
  }

  @Test
  public void testWhitespaceBeforeUnaryOperator() throws IOException {
    build("fred +");

    assertNextIs(FILTER);
    assertNextIs(ONE_OR_MORE);
      
    assertNoMoreTokens();
  }

  @Test
  public void testMvelFilterWithOperator() throws IOException {
    build("fred(xyz)+");
      
    assertNextIs(FILTER);
    assertNextIs(ONE_OR_MORE);
    
    assertNoMoreTokens();
  }

  @Test
  public void testMvelFilterWithOperatorAndWhitespace() throws IOException {
    build("fred (xyz) + ");
      
    assertNextIs(FILTER);
    assertNextIs(ONE_OR_MORE);
      
    assertNoMoreTokens();
  }

  @Test
  public void testSimpleFilterAnchoredAtEnd() throws IOException {
    build("x$");
      
    assertNextIs(FILTER);
    assertNextIs(END_ATOM);

    assertNoMoreTokens();
  }

  @Test
  public void testSimpleFilterAnchoredAtEndWithWhitespace() throws IOException {
    build(" x  $ ");
      
    assertNextIs(FILTER);
    assertNextIs(END_ATOM);
      
    assertNoMoreTokens();
  }

  @Test
  public void testAnchoredCatnenationOfNonQualifiedFiltersWithWhitespace() throws IOException {
    build("^  java.lang.Character /   java.lang.Integer  ");
      
    /*
     * Translated:
     *
     *   ^ / java.lang.Character / java.lang.Integer
     *
     * In tokens:
     *
     *   BEGIN_ATOM FILTER CATENATION FILTER CATENATION
     */
      
    assertNextIs(BEGIN_ATOM);
    assertNextIs(FILTER);
    assertNextIs(CATENATION);
    assertNextIs(FILTER);
    assertNextIs(CATENATION);
    
    assertNoMoreTokens();
  }

  @Test
  public void testPrime() throws IOException {
    build("(java.lang.Character(charValue() == 'f'))/java.lang.Integer");
      
    /*
     * Translated (where '[' means START_SAVING)
     *
     * [ / java.lang.Character(charValue() == 'f') / ] / java.lang.Integer
     *
     * In tokens:
     *
     * START_SAVING FILTER CATENATION STOP_SAVING CATENATION FILTER CATENATION
     * 
     */
    assertNextIs(START_SAVING);
    assertNextIsFilter(Character.class.getName(), "charValue() == 'f'");
    assertNextIs(CATENATION);
    assertNextIs(STOP_SAVING);
    assertNextIs(CATENATION);
    assertNextIsFilter(Integer.class.getName());
    assertNextIs(CATENATION);
    
    assertNoMoreTokens();
  }

  private final void build(final String input) throws IOException {
    assertNotNull(input);
    this.build(new PushbackReader(new StringReader(input)));
  }

  private final void build(final PushbackReader pbr) throws IOException {
    assertNotNull(pbr);
    this.pbr = pbr;
    this.pft = new PostfixTokenizer(pbr);
  }

  private final void assertNoMoreTokens() {
    assertFalse(this.pft.hasNext());
  }

  private final void assertNextIs(final Token.Type type) {
    assertNextIs(this.pft, type);
  }

  private final void assertNextIsFilter() {
    assertNextIsFilter(this.pft, null, null);
  }

  private final void assertNextIsFilter(final String filterType) {
    assertNextIsFilter(this.pft, filterType, null);
  }

  private static final void assertNextIs(final PostfixTokenizer pft, final Token.Type type) {
    assertNotNull(type);
    assertTrue(pft.hasNext());
    final Token token = pft.next();
    assertNotNull(token);
    assertSame(type, token.getType());
  }

  private final void assertNextIsFilter(final String filterType, final String mvel) {
    assertNextIsFilter(this.pft, filterType, mvel);
  }

  private static final void assertNextIsFilter(final PostfixTokenizer pft) {
    assertNextIsFilter(pft, null, null);
  }

  private static final void assertNextIsFilter(final PostfixTokenizer pft, final String filterType) {
    assertNextIsFilter(pft, filterType, null);
  }

  private static final void assertNextIsFilter(final PostfixTokenizer pft, final String filterType, final String mvel) {
    assertTrue(pft.hasNext());
    final Token token = pft.next();
    assertNotNull(token);
    assertSame(FILTER, token.getType());
    assertEquals(filterType, token.getFilterType());
    assertEquals(mvel, token.getValue());
  }

}
