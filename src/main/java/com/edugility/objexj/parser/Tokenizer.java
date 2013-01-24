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

/**
 * An {@link Iterator} of {@link Token}s that produces the iteration
 * by reading a {@link PushbackReader}, character by character.
 *
 * <h2>Design Notes</h2>
 *
 * <ul>
 *
 * <li>The {@code $} character is a {@linkplain
 * Character#isJavaIdentifierPart(char) legal Java identifier part},
 * but we treat it as meaning end-of-input.  This will prevent some
 * class names from being tokenized properly.</li>
 *
 * </ul>
 *
 * <h3>Tasks</h3>
 *
 * <ul>
 *
 * <li>Implement escaping</li>
 *
 * </ul>
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @since 1.0-SNAPSHOT
 */
public class Tokenizer implements Iterator<Token> {

  private static final long serialVersionUID = 1L;

  private enum State {
    START, START_SAVING_OR_FILTER, INVALID, FILTER, MVEL, END_OF_FILTER, OPERATOR, STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE, END
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

  private final void prime() throws IOException {
    if (this.state == State.INVALID) {
      throw new IllegalStateException();
    }

    final StringBuilder sb = new StringBuilder();
    int parenCount = 0;
    int c = -1;

    READ_LOOP:
    for (; (c = reader.read()) != -1; this.position++) {

      switch (this.state) {


        // START
      case START:
        switch (c) {

        case '^':
          this.token = new Token(Token.Type.BEGIN_INPUT);
          sb.setLength(0);
          this.state = State.START_SAVING_OR_FILTER;
          break READ_LOOP;

        case '(':
          reader.unread(c);
          this.state = State.OPERATOR;
          break;

        case '$':
          // $ is a valid Java identifier start so we have to special
          // case it here as end-of-input
          throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));

        default:
          if (Character.isWhitespace(c)) {
            continue READ_LOOP;
          } else if (Character.isJavaIdentifierStart(c)) {
            assert c != '$';
            sb.append((char)c);
            this.state = State.FILTER;
          } else {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }
          break;
        }
        break;


        // START_SAVING_OR_FILTER
      case START_SAVING_OR_FILTER:
        switch (c) {

        case '(':
          reader.unread(c);
          this.state = State.OPERATOR;
          break;

        case '$':
          // $ is a valid Java identifier start so we have to special
          // case it here
          throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          
        default:
          if (Character.isWhitespace(c)) {
            continue READ_LOOP;
          } else if (Character.isJavaIdentifierStart(c)) {
            assert c != '$';
            sb.append((char)c);
            this.state = State.FILTER;
          } else {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }
          break;
        }
        break;


        // FILTER
      case FILTER:
        switch (c) {

        case '(':
          // Create the token, but don't return it yet.  Transition to
          // the MVEL state and have it fill in the details.
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
        case ',':
        case '$':
          this.token = new Token(Token.Type.FILTER, sb.toString());
          sb.setLength(0);
          reader.unread(c);
          this.state = State.OPERATOR;
          break READ_LOOP;

        default:
          if (Character.isJavaIdentifierPart(c)) {
            sb.append((char)c);
          } else if (Character.isWhitespace(c)) {
            continue READ_LOOP;
          } else if (c == '.') {
            sb.append('.');

            this.position++;
            c = reader.read();

            if (c == -1) {
              reader.unread(c);
              this.position--;
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


        // END_OF_FILTER
      case END_OF_FILTER:
        switch (c) {

        case ')':
        case '+':
        case '*':
        case '?':
        case '|':
        case '/':
        case ',':
        case '$':
          sb.setLength(0);
          reader.unread(c);
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
        case '$': // end of input
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


        // OPERATOR
      case OPERATOR:
        sb.setLength(0);

        switch (c) {

        case '$':
          this.token = new Token(Token.Type.END_INPUT);
          this.state = State.END; // XXX TODO FIXME: verify
          break READ_LOOP;

        case '+':
          this.token = new Token(Token.Type.ONE_OR_MORE);
          this.state = State.STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE;
          break READ_LOOP;

        case '*':
          this.token = new Token(Token.Type.ZERO_OR_MORE);
          this.state = State.STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE;
          break READ_LOOP;

        case '?':
          this.token = new Token(Token.Type.ZERO_OR_ONE);
          this.state = State.STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE;
          break READ_LOOP;

        case '|':
          this.token = new Token(Token.Type.ALTERNATION);
          this.state = State.START_SAVING_OR_FILTER;
          break READ_LOOP;

        case '/':
        case ',':
          this.token = new Token(Token.Type.CATENATION);
          this.state = State.START_SAVING_OR_FILTER;
          break READ_LOOP;

        case '(':
          this.token = new Token(Token.Type.START_SAVING);
          this.state = State.START_SAVING_OR_FILTER;
          break READ_LOOP;

        case ')':
          this.token = new Token(Token.Type.STOP_SAVING);
          this.state = State.STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE;
          break READ_LOOP;

        default:
          if (Character.isWhitespace(c)) {
            continue READ_LOOP;
          } else {
            throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
          }

        }



        // MVEL
      case MVEL:
        assert parenCount > 0;
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


        // END
      case END:        
        if (Character.isWhitespace(c)) {
          continue READ_LOOP;
        } else {
          throw new IllegalStateException(buildIllegalStateExceptionMessage(this.reader, c, this.position));
        }


        // DEFAULT
      default:
        throw new IllegalStateException("Unexpected state: " + this.state);

      }

    } // READ_LOOP

    if (c == -1) {
      // Ran off the end; clean up
      switch (this.state) {
      case STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE:
        this.state = State.END;
        break;
      case START_SAVING_OR_FILTER:
        this.state = State.END;
        break;
      case END_OF_FILTER:
        this.state = State.END;
        break;
      case FILTER:
        token = new Token(Token.Type.FILTER, sb.toString());
        sb.setLength(0);
        this.state = State.END;        
        break;
      case END:
        // Everything is OK
        if ("$".equals(sb.toString())) {
          token = new Token(Token.Type.END_INPUT);
          sb.setLength(0);
        }
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
