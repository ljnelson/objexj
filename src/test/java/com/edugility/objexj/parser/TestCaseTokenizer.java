package com.edugility.objexj.parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseTokenizer {

  public TestCaseTokenizer() {
    super();
  }

  @Test
  public void testInitialCaret() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("^"));
    final Tokenizer tokenizer = new Tokenizer(pbr);
    assertTrue(tokenizer.hasNext());
    final Token token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.BEGIN_INPUT, token.getType());
    assertFalse(tokenizer.hasNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testInitialConcatenationIsImpossible() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("/"));
    final Tokenizer tokenizer = new Tokenizer(pbr);
  }

  @Test
  public void testValidSaveSyntax() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("(froob)"));
    final Tokenizer tokenizer = new Tokenizer(pbr);
    assertTrue(tokenizer.hasNext());
    assertSame(Token.Type.START_SAVING, tokenizer.next().getType());

    assertTrue(tokenizer.hasNext());
    Token token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.FILTER, token.getType());
    assertEquals("froob", token.getFilterType());
    assertNull(token.getValue());

    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.STOP_SAVING, token.getType());

    assertFalse(tokenizer.hasNext());
  }

  @Test
  public void testEmptySaveBlock() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("()"));
    final Tokenizer tokenizer = new Tokenizer(pbr);
    assertTrue(tokenizer.hasNext());
    assertSame(Token.Type.START_SAVING, tokenizer.next().getType());
    try {
      tokenizer.hasNext(); // will throw an error because () is a syntax error
    } catch (final IllegalStateException expected) {
      final Throwable cause = expected.getCause();
      assertTrue(cause instanceof IllegalStateException);
      final IllegalStateException syntaxError = (IllegalStateException)cause;
      final StackTraceElement[] stack = syntaxError.getStackTrace();
      assertNotNull(stack);
      assertTrue(stack.length >= 1);
      final StackTraceElement firstFrame = stack[0];
      assertNotNull(firstFrame);
      assertEquals("prime", firstFrame.getMethodName());
    }
  }

  @Test
  public void testLotsOfOpenParens() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("((((")); // Four open parens
    final Tokenizer tokenizer = new Tokenizer(pbr);

    // Iteration 0
    assertTrue(tokenizer.hasNext());
    assertSame(Token.Type.START_SAVING, tokenizer.next().getType());

    // Iteration 1
    assertTrue(tokenizer.hasNext());
    assertSame(Token.Type.START_SAVING, tokenizer.next().getType());

    // Iteration 2
    assertTrue(tokenizer.hasNext());
    assertSame(Token.Type.START_SAVING, tokenizer.next().getType());

    // Iteration 3
    assertTrue(tokenizer.hasNext());
    assertSame(Token.Type.START_SAVING, tokenizer.next().getType());

    // No more parens
    assertFalse(tokenizer.hasNext());
    try {
      tokenizer.next();
      fail();
    } catch (final NoSuchElementException expected) {
      // fine
    }
  }

  @Test
  public void testAnchoredCatnenationOfNonQualifiedFilters() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("^java.lang.Character/java.lang.Integer"));
    final Tokenizer tokenizer = new Tokenizer(pbr);
    assertTrue(tokenizer.hasNext());
    Token token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.BEGIN_INPUT, token.getType());

    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.FILTER, token.getType());

    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.CATENATION, token.getType());

    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.FILTER, token.getType());

    assertFalse(tokenizer.hasNext());    
  }

  @Test
  public void testSimpleFilterAnchoredAtEnd() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("x$"));
    final Tokenizer tokenizer = new Tokenizer(pbr);
    assertTrue(tokenizer.hasNext());
    Token token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.FILTER, token.getType());
    assertEquals("x", token.getFilterType());
    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.END_INPUT, token.getType());
    assertFalse(tokenizer.hasNext());
  }

  @Test
  public void testSimpleFilterAnchoredAtEndWithWhitespace() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader(" x  $ "));
    final Tokenizer tokenizer = new Tokenizer(pbr);
    assertTrue(tokenizer.hasNext());
    Token token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.FILTER, token.getType());
    assertEquals("x", token.getFilterType());
    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.END_INPUT, token.getType());
    assertFalse(tokenizer.hasNext());
  }

  @Test
  public void testAnchoredCatnenationOfNonQualifiedFiltersWithWhitespace() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("^  java.lang.Character /   java.lang.Integer  "));
    final Tokenizer tokenizer = new Tokenizer(pbr);
    assertTrue(tokenizer.hasNext());
    Token token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.BEGIN_INPUT, token.getType());

    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.FILTER, token.getType());

    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.CATENATION, token.getType());

    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.FILTER, token.getType());

    assertFalse(tokenizer.hasNext());    
  }

  @Test
  public void testPrime() throws IOException {
    final PushbackReader pbr = new PushbackReader(new StringReader("(java.lang.Character(charValue() == 'f'))/java.lang.Integer"));
    final Tokenizer tokenizer = new Tokenizer(pbr);
    assertTrue(tokenizer.hasNext());

    Token token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.START_SAVING, token.getType());

    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.FILTER, token.getType());
    Object c = token.getFilterType();
    assertNotNull(c);
    assertEquals(Character.class.getName(), c);
    String mvel = token.getValue();
    assertEquals("charValue() == 'f'", mvel);

    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.STOP_SAVING, token.getType());

    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.CATENATION, token.getType());

    assertTrue(tokenizer.hasNext());
    token = tokenizer.next();
    assertNotNull(token);
    assertSame(Token.Type.FILTER, token.getType());
    c = token.getFilterType();
    assertNotNull(c);
    assertEquals(Integer.class.getName(), c);
    mvel = token.getValue();
    assertNull(mvel);

    assertFalse(tokenizer.hasNext());
    pbr.close();
  }

}
