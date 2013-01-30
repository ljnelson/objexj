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
import java.io.Reader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.edugility.objexj.Program;

public class PostfixTokenizer implements Iterator<Token> {

  private static final long serialVersionUID = 1L;

  private enum State {
    BEGIN_ATOM, END_ATOM, START, START_SAVING_OR_FILTER, INVALID, FILTER, MVEL, END_OF_FILTER, OPERATOR, LEFT_PAREN, RIGHT_PAREN, STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE, END
  }

  private final Map<Integer, Token> tokens;

  private transient Logger logger;

  private transient final PushbackReader reader;

  final transient LinkedList<Token> output;

  private transient Throwable error;

  private transient State state;

  private transient int position;

  private transient final Deque<Token> stack;

  public PostfixTokenizer(final PushbackReader reader) throws IOException {
    super();
    this.logger = Logger.getLogger(this.getClass().getName());
    assert this.logger != null;
    if (reader == null) {
      throw new IllegalArgumentException("reader", new NullPointerException("reader"));
    }
    this.tokens = new HashMap<Integer, Token>();
    this.setupTokens();
    this.output = new LinkedList<Token>();
    this.stack = new ArrayDeque<Token>();
    this.reader = reader;
    this.state = State.START;
    this.prime();
  }

  private static final String buildIllegalStateExceptionMessage(final Reader r, final int c, final int position) {
    return String.format("Unexpected character (%c) at position %d in Reader %s", c, position, r);
  }

  private final void prime() throws IOException {
    if (this.state == State.INVALID) {
      throw new IllegalStateException();
    }

    final StringBuilder sb = new StringBuilder();
    int parenCount = 0;
    int c = -1;

    READ_LOOP:
    for (; (c = this.read()) != -1; this.position++) {
      switch (this.state) {

        /*
         * In this state switch block, "forwarding" states (states
         * that peek ahead to determine what real state to forward to)
         * are commented // LIKE THIS, and "real" states are commented
         * //
         * // LIKE THIS
         * //
         */


        // START
      case START:
        switch (c) {

        case '^':
          this.unread(c);
          this.state = State.BEGIN_ATOM;
          break;

        case '(':
          this.unread(c);
          this.state = State.LEFT_PAREN;
          break;

        default:
          if (Character.isWhitespace(c)) {
            continue READ_LOOP;
          } else if (c != '$' && Character.isJavaIdentifierStart(c)) {
            sb.append((char)c);
            this.state = State.FILTER;
          } else {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }
          break;
        }
        break;


        //
        // BEGIN_ATOM
        //
      case BEGIN_ATOM:
        assert c == '^';
        assert sb.length() == 0;
        this.output.add(this.tokenFor(c));
        // Now we have to do an implicit CATENATION operator.
        this.handleOperator(this.tokenFor('/'));
        assert this.state == State.START_SAVING_OR_FILTER;
        break READ_LOOP;


        //
        // END_ATOM
        //
      case END_ATOM:
        assert c == '$';
        this.output.add(this.tokenFor(c));
        sb.setLength(0);
        this.state = State.END;
        break READ_LOOP;


        //
        // LEFT_PAREN
        //
      case LEFT_PAREN:
        assert c == '(';
        /*
         * Shunting-yard algorithm: "If the token is a left
         * parenthesis, then push it onto the stack."
         *
         * In our case, a parenthesis is both a grouping operator and
         * an atom-- a signal to the parser that a START_SAVING
         * instruction should be fired.  So we also have to add it to
         * the output.
         *
         * TENTATIVE: since it's an atom, we also need to pretend we
         * found a concatenation operator right afterwards.
         */
        
        // First find a START_SAVING (atom) token and add it to the output.
        this.output.add(new Token(Token.Type.START_SAVING));

        // Then pretend we found a concatenation.
        this.handleOperator(this.tokenFor('/'));
        assert this.state == State.START_SAVING_OR_FILTER;

        // Then pretend we found a START_GROUP.
        final Token t = new Token(Token.Type.START_GROUP);
        assert t != null;
        this.push(t);
        this.state = State.START_SAVING_OR_FILTER;
        break READ_LOOP;


        //
        // RIGHT_PAREN
        //
      case RIGHT_PAREN:
        assert c == ')'; // OK
        /*
         * Shunting-yard algorithm:
         * "If the token is a right parenthesis:
         * " * Until the token at the top of the stack is a left parenthesis, pop operators off the stack onto the output queue.
         * " * Pop the left parenthesis from the stack, but not onto the output queue.
         * " * If the token at the top of the stack is a function token, pop it onto the output queue.
         * " * If the stack runs out without finding a left parenthesis, then there are mismatched parentheses."
         */

        final int originalOutputSize = this.output.size();
        while (!this.stack.isEmpty() && this.stack.peek().getType() != Token.Type.START_GROUP) {
          this.output.add(this.stack.pop());
        }
        if (this.stack.isEmpty()) {
          throw new IllegalStateException("Mismatched parentheses");
        }

        assert Token.Type.START_GROUP == this.stack.peek().getType();
        this.stack.pop();

        // The original shunting-yard algorithm discards the parens
        // because in that algorithm they are used only for grouping.
        // In regular expressions, they are also atoms.  So they
        // belong in the output queue. We already added the
        // START_SAVING token, so now we need to add the STOP_SAVING.
        
        // Concatenate...
        this.handleOperator(this.tokenFor('/'));

        // ...with stop saving
        this.output.add(new Token(Token.Type.STOP_SAVING));

        this.state = State.STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE;
        break READ_LOOP; // only because we added something to the output queue


        // START_SAVING_OR_FILTER
      case START_SAVING_OR_FILTER:
        if (Character.isWhitespace(c)) {
          continue READ_LOOP;
        } else if (c == '(') {
          // Forward to LEFT_PAREN state
          this.unread(c);
          this.state = State.LEFT_PAREN;
        } else if (c != '$' && Character.isJavaIdentifierStart(c)) {
          // Forward to FILTER state
          sb.append((char)c);
          this.state = State.FILTER;
        } else {
          throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
        }
        break;


        // FILTER (really more like START_OF_FILTER?)
      case FILTER:
        switch (c) {

        case '(':
          // Bumped into an MVEL guard.  Create the token, but don't
          // return it yet.  Fill in its filterType.  Transition to
          // the MVEL state and have it fill in the details.
          this.output.add(new Token(Token.Type.FILTER, sb.toString()));
          parenCount = 1;
          sb.setLength(0);
          this.state = State.MVEL;
          break;

        case ')': // un-MVELed atom; just ran into a RIGHT_PAREN; OK
          this.output.add(new Token(Token.Type.FILTER, sb.toString()));
          sb.setLength(0);
          this.unread(c);
          this.state = State.RIGHT_PAREN;
          break READ_LOOP;

        case '+':
        case '*':
        case '?':
        case '|':
        case '/':
        case ',':
          // Bumped into a genuine operator.  So create a FILTER token
          // without an accompanying MVEL guard, and forward to the
          // OPERATOR state.
          this.output.add(new Token(Token.Type.FILTER, sb.toString()));
          sb.setLength(0);
          this.unread(c);
          this.state = State.OPERATOR;
          break READ_LOOP;

        case '$':
          this.output.add(new Token(Token.Type.FILTER, sb.toString()));
          sb.setLength(0);
          this.unread(c);
          this.state = State.END_ATOM;
          break READ_LOOP;

        default:
          if (Character.isJavaIdentifierPart(c)) {
            sb.append((char)c);
          } else if (Character.isWhitespace(c)) {
            continue READ_LOOP;
          } else if (c == '.') {
            sb.append('.');
            c = this.read();
            if (c == -1) {
              this.unread(c);
            } else if (Character.isJavaIdentifierStart(c)) {
              sb.append((char)c);
            } else {
              throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
            }
          } else {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }
          break;
        }
        break;


        // MVEL
      case MVEL:
        assert parenCount > 0;
        switch (c) {

        case '(':
          parenCount++;
          sb.append((char)c);
          break;

        case ')': // Maybe a paren inside an MVEL block; maybe the paren that closes it; OK
          parenCount--;
          if (parenCount == 0) {
            final Token token = this.output.getLast();
            assert token != null;
            assert token.getFilterType() != null;
            assert token.getType() == Token.Type.FILTER;
            token.setValue(sb.toString());
            sb.setLength(0);
            this.state = State.END_OF_FILTER;
            break READ_LOOP;
          } else if (parenCount < 0) {
            throw new IllegalStateException("Mismatched parentheses");
          } else {
            sb.append((char)c);
          }
          break;

        default:
          sb.append((char)c);
          break;
        }
        break;


        // END_OF_FILTER
      case END_OF_FILTER:
        switch (c) {

        case '$':
          sb.setLength(0);
          this.unread(c);
          this.state = State.END_ATOM;
          break;

        case ')': // OK
          sb.setLength(0);
          this.unread(c);
          this.state = State.RIGHT_PAREN;
          break;

        case '+':
        case '*':
        case '?':
        case '|':
        case '/':
        case ',':
          // Forward to OPERATOR state
          sb.setLength(0);
          this.unread(c);
          this.state = State.OPERATOR;
          break;

        default:
          if (Character.isWhitespace(c)) {
            continue READ_LOOP;
          } else {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }

        }
        break;


        //
        // OPERATOR
        //
      case OPERATOR:
        sb.setLength(0);
        if (Character.isWhitespace(c)) {
          continue READ_LOOP;
        } else if (this.handleOperator(this.tokenFor(c))) {
          break READ_LOOP;
        }
        break;


        // STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE
        //
        // STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE permits
        // only those operators that come after a (potentially
        // augmented) filter token, or a group of such tokens, and
        // weeds out others before proceeding to the OPERATOR state.
        //
        // This handles closing of submatches (")") and end-of-input
        // ("$") as special (legal) cases.  Otherwise the operator has
        // to be catenation or alternation.
        //
        // In these cases we forward to the OPERATOR state.
        //
        // Any other character causes an IllegalStateException.
      case STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE:
        switch (c) {

        case ')': // stop saving
          this.unread(c);
          this.state = State.RIGHT_PAREN;
          break;

        case '$': // end of input
          this.unread(c);
          this.state = State.END_ATOM;
          break;

        case '/': // catenation
        case ',': // catenation
        case '|': // alternation
          this.reader.unread(c);
          this.state = State.OPERATOR;
          break;

        default:
          if (Character.isWhitespace(c)) {
            continue READ_LOOP;
          } else {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }
        }
        break;


        //
        // END
        //
      case END:
        if (!Character.isWhitespace(c)) {
          throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
        }
        break;

        //
        // DEFAULT
        //
      default:
        throw new IllegalStateException("Unexpected state: " + this.state);
      }
    } // READ_LOOP

    if (c == -1) {
      switch (this.state) {
      case FILTER:
        this.output.add(new Token(Token.Type.FILTER, sb.toString()));
        sb.setLength(0);
        break;
      case END:
        break;
      default:
        if (this.logger.isLoggable(Level.WARNING)) {
          this.logger.logp(Level.WARNING, this.getClass().getName(), "prime", "Unhandled cleanup in state {0} with stringbuffer {1}", new Object[] { state, sb });
        }
        break;
      }
      this.state = State.END;
      while (!this.stack.isEmpty()) {
        final Token top = this.stack.peek();
        assert top != null;
        final Token.Type type = top.getType();
        assert type != null;
        
        if (type == Token.Type.START_GROUP || type == Token.Type.STOP_GROUP) {
          throw new IllegalStateException("Mismatched parentheses");
        }
        this.output.add(this.stack.pop());
      }
    }

  }

  final boolean handleOperator(final Token o1) {
    if (o1 == null) {
      throw new IllegalArgumentException("o1", new NullPointerException("o1"));
    } else if (!o1.isOperator()) {
      throw new IllegalArgumentException("!o1.isOperator()");
    }
    Token o2;
    boolean added = false;
    while (!this.stack.isEmpty() && this.assertIsOperator(o2 = this.stack.peek())) {
      if (o1.compareTo(o2) <= 0) {
        added = added || this.output.add(this.stack.pop());
      } else {
        break;
      }
    }
    this.push(o1);
    this.state = this.stateAfterOperator(o1);
    return added;
  }

  private final void push(final Token token) {
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.entering(this.getClass().getName(), "push", token);
    }
    if (token == null) {
      throw new IllegalArgumentException("token", new NullPointerException("token"));
    } else if (Token.Type.STOP_GROUP == token.getType()) {
      throw new IllegalArgumentException("token.getType() == Token.Type.STOP_GROUP");
    }
    this.stack.push(token);
    if (this.logger.isLoggable(Level.FINER)) {
      this.logger.exiting(this.getClass().getName(), "push");
    }
  }

  private final State stateAfterOperator(final Token token) {
    if (token == null) {
      throw new IllegalArgumentException("token", new NullPointerException("token"));
    }
    final Token.Type type = token.getType();
    assert type != null;
    switch (type) {
    case ONE_OR_MORE:
    case ZERO_OR_MORE:
    case ZERO_OR_ONE:
      return State.STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE;
    case ALTERNATION:
    case CATENATION:
      return State.START_SAVING_OR_FILTER;
    default:
      throw new IllegalArgumentException("token: " + token);
    }
  }

  private final void setupTokens() {
    this.tokens.clear();
    this.tokens.put(Integer.valueOf('^'), new Token(Token.Type.BEGIN_ATOM));
    this.tokens.put(Integer.valueOf('$'), new Token(Token.Type.END_ATOM));
    this.tokens.put(Integer.valueOf('+'), new Token(Token.Type.ONE_OR_MORE));
    this.tokens.put(Integer.valueOf('*'), new Token(Token.Type.ZERO_OR_MORE));
    this.tokens.put(Integer.valueOf('?'), new Token(Token.Type.ZERO_OR_ONE));
    this.tokens.put(Integer.valueOf('|'), new Token(Token.Type.ALTERNATION));
    final Token catenation = new Token(Token.Type.CATENATION);
    this.tokens.put(Integer.valueOf('/'), catenation);
    this.tokens.put(Integer.valueOf(','), catenation);
    this.tokens.put(Integer.valueOf('('), new Token(Token.Type.START_SAVING));
    this.tokens.put(Integer.valueOf(')'), new Token(Token.Type.STOP_SAVING));
  }

  private final Token tokenFor(final int c) {
    final Token token = this.tokens.get(Integer.valueOf(c));
    if (token == null) {
      throw new IllegalArgumentException("c: " + (char)c);
    }
    return token;
  }

  private final boolean assertIsOperator(final Token token) {
    if (token == null) {
      throw new IllegalArgumentException("token", new NullPointerException("token"));
    }
    if (!token.isOperator()) {
      throw new IllegalArgumentException("!token.isOperator(): " + token);
    }
    return true;
  }

  private final int read() throws IOException {
    final int c = this.reader.read();
    ++this.position;
    return c;
  }

  private final int unread(final int c) throws IOException {
    this.reader.unread(c);
    return --this.position;
  }

  @Override
  public boolean hasNext() {
    if (this.error != null) {
      throw new IllegalStateException(this.error);
    }
    return !this.output.isEmpty();
  }

  @Override
  public Token next() {
    if (this.error != null) {
      throw (NoSuchElementException)new NoSuchElementException().initCause(this.error);
    } else if (this.output.isEmpty()) {
      throw new NoSuchElementException();
    }
    final Token returnValue = this.output.removeFirst();
    if (this.output.isEmpty()) {
      try {
        this.prime();
      } catch (final RuntimeException ise) {
        this.error = ise;
        this.state = State.INVALID;
        this.output.clear();
      } catch (final IOException io) {
        this.error = io;
        this.state = State.INVALID;
        this.output.clear();
      }
    }
    return returnValue;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
