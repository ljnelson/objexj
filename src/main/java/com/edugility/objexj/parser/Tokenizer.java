/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
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

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.edugility.objexj.Program;

public class Tokenizer implements Iterator<Token> {

  private static final long serialVersionUID = 1L;

  private enum State {
    START, START_SAVING_OR_FILTER, INVALID, UNDECIDED, FILTER, MVEL, OPERATOR, SEQUENCE, END
  }

  private transient final PushbackReader reader;

  transient Token token;

  private transient Throwable error;

  private transient State state;

  private transient int position;

  public Tokenizer(final PushbackReader reader) throws IOException {
    super();
    if (reader == null) {
      throw new IllegalArgumentException("reader", new NullPointerException("reader"));
    }
    this.reader = reader;
    this.state = State.START;
    this.prime();
  }

  private static final String buildIllegalStateExceptionMessage(final Reader r, final int c, final int position) {
    return String.format("Unexpected character (%c) at position %d in Reader %s", c, position, r);
  }

  void prime() throws IOException {
    if (this.state == State.INVALID) {
      throw new IllegalStateException();
    }
    final StringBuilder sb = new StringBuilder();
    int parenCount = 0;
    int c = -1;
    for (boolean go = true; go && (c = reader.read()) != -1; this.position++) {
      switch (this.state) {


        // START
      case START:
        switch (c) {

        case '^':
          this.token = new Token(Token.Type.BEGIN_INPUT);
          sb.setLength(0);
          go = false;
          this.state = State.START_SAVING_OR_FILTER;
          break;

        case '(':
          // just peekin'
          reader.unread(c);
          this.state = State.OPERATOR;
          break;

        default:
          if (Character.isWhitespace(c)) {
            // don't do anything
          } else if (Character.isJavaIdentifierStart(c)) {
            sb.append((char)c);
            this.state = State.FILTER;
          } else {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }
          break;
        }
        break;

      case START_SAVING_OR_FILTER:
        switch (c) {
        case '(':
          // just peekin'
          reader.unread(c);
          this.state = State.OPERATOR;
          break;
          
        default:
          if (Character.isWhitespace(c)) {
            // don't do anything
          } else if (Character.isJavaIdentifierStart(c)) {
            sb.append((char)c);
            this.state = State.FILTER;
          } else {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }
          break;
        }
        break;


        // UNDECIDED
      case UNDECIDED:
        switch (c) {
        case '(':
        case ')':
        case '+':
        case '*':
        case '?':
        case '|':
        case '/':
          this.reader.unread(c);
          this.state = State.OPERATOR;
          break;
        default:
          break;
        }
        break;


        // FILTER
      case FILTER:
        switch (c) {

        case '(':
          this.token = new Token(Token.Type.FILTER, sb.toString());
          parenCount = 1;
          sb.setLength(0);
          this.state = State.MVEL;
          break;

        case ')':
        case '+':
        case '*':
        case '?':
        case '|':
        case '/':
        case '$':
          token = new Token(Token.Type.FILTER, sb.toString());
          sb.setLength(0);
          reader.unread(c);
          go = false;
          this.state = State.OPERATOR;
          break;

        default:
          if (Character.isWhitespace(c)) {
            token = new Token(Token.Type.FILTER, sb.toString());
            sb.setLength(0);
            this.state = State.SEQUENCE;
            go = false;
          } else if (c == '.') {
            sb.append('.');
            c = reader.read();
            if (c == -1) {
              // never mind, just peeking
              reader.unread(c);
            } else if (Character.isJavaIdentifierStart(c)) {
              sb.append((char)c);
            } else {
              throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
            }
          } else if (Character.isJavaIdentifierPart(c)) {
            sb.append((char)c);
          } else {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }
          break;
        }
        break;


        // SEQUENCE
      case SEQUENCE:
        switch (c) {

        case '$':
        case '/':
        case '|':
        case ')':
          // just peekin'
          this.reader.unread(c);
          this.state = State.OPERATOR;
          break;

        default:
          if (Character.isWhitespace(c)) {
            // don't do anything
          } else {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }
        }
        break;


        // OPERATOR
      case OPERATOR:
        switch (c) {
        case '$':
          this.token = new Token(Token.Type.END_INPUT);
          sb.setLength(0);
          go = false;
          this.state = State.END; // XXX TODO FIXME: verify
          break;
        case '+':
          this.token = new Token(Token.Type.ONE_OR_MORE);
          sb.setLength(0);
          go = false;
          this.state = State.SEQUENCE;
          break;
        case '*':
          this.token = new Token(Token.Type.ZERO_OR_MORE);
          sb.setLength(0);
          go = false;
          this.state = State.SEQUENCE;
          break;
        case '?':
          this.token = new Token(Token.Type.ZERO_OR_ONE);
          sb.setLength(0);
          go = false;
          this.state = State.SEQUENCE;
          break;
        case '|':
          this.token = new Token(Token.Type.ALTERNATION);
          sb.setLength(0);
          go = false;
          this.state = State.START_SAVING_OR_FILTER;
          break;
        case '/':
          this.token = new Token(Token.Type.CATENATION);
          sb.setLength(0);
          go = false;
          this.state = State.START_SAVING_OR_FILTER;
          break;
        case '(':
          this.token = new Token(Token.Type.START_SAVING);
          sb.setLength(0);
          go = false;
          this.state = State.START_SAVING_OR_FILTER;
          break;
        case ')':
          this.token = new Token(Token.Type.STOP_SAVING);
          sb.setLength(0);
          go = false;
          this.state = State.SEQUENCE;
          break;
        default:
          if (!Character.isWhitespace(c)) {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }
          break;
        }
        break;


        // MVEL
      case MVEL:
        switch (c) {
        case '(':
          parenCount++;
          sb.append((char)c);
          break;
        case ')':
          parenCount--;
          if (parenCount == 0) {
            assert this.token != null;
            assert this.token.getFilterType() != null;
            assert this.token.getType() == Token.Type.FILTER;
            this.token.setValue(sb.toString());
            sb.setLength(0);
            go = false;
            this.state = State.SEQUENCE;
          } else {
            sb.append((char)c);
          }
          break;
        default:
          sb.append((char)c);
          break;
        }
        break;


        // END
      case END:
        if (!Character.isWhitespace(c)) {
          throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
        }
        break;


        // DEFAULT
      default:
        throw new IllegalStateException("Unexpected state: " + this.state);

      }
    }

    if (c == -1) {
      // Ran off the end; clean up
      switch (this.state) {
      case SEQUENCE:
        this.state = State.END;
        break;
      case START_SAVING_OR_FILTER:
        this.state = State.END;
        break;
      case FILTER:
        token = new Token(Token.Type.FILTER, sb.toString());
        sb.setLength(0);
        this.state = State.END;        
        break;
      case END:
        // Everything is OK
        break;
      default:
        throw new IllegalStateException("Unhandled cleanup; state is: " + this.state);
      }
    }
  }
  
  @Override
  public boolean hasNext() {
    if (this.error != null) {
      throw new IllegalStateException(this.error);
    } else {
      return this.token != null;
    }
  }

  @Override
  public Token next() {
    if (this.error != null) {
      throw (NoSuchElementException)new NoSuchElementException().initCause(this.error);
    }
    final Token returnValue;
    if (this.token != null) {
      returnValue = this.token;
      this.token = null;
      try {
        this.prime();
      } catch (final RuntimeException ise) {
        this.error = ise;
        this.state = State.INVALID;
        this.token = null;
      } catch (final IOException io) {
        this.error = io;
        this.state = State.INVALID;
        this.token = null;
      }
    } else {
      throw new NoSuchElementException();
    }
    return returnValue;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
