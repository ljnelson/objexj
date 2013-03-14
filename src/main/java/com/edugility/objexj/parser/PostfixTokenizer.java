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
import java.io.Serializable; // for javadoc only

import java.text.ParsePosition;
import java.text.ParseException;

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

/**
 * A {@link ParsePosition} and an {@link Iterator} of {@link Token}s
 * that converts a {@link PushbackReader} of a textual objexj pattern
 * into a series of {@link Token}s in postfix order.
 *
 * <p>A {@link PostfixTokenizer} is the lexer component of the
 * lex-then-parse series of events that go into compiling an objexj
 * pattern.</p>
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Parser
 *
 * @see <a
 * href="http://en.wikipedia.org/wiki/Shunting-yard_algorithm">Shunting-yard
 * algorithm. (2013, March 14). In Wikipedia, The Free
 * Encyclopedia. Retrieved 20:42, March 14, 2013, from
 * http://en.wikipedia.org/w/index.php?title=Shunting-yard_algorithm&oldid=544128320</a>
 */
public class PostfixTokenizer extends ParsePosition implements Iterator<Token> {

  /**
   * The version of this class for {@link Serializable serialization
   * purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * A state a {@link PostfixTokenizer} may be in.
   *
   * @author <a href="http://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   */
  private enum State {

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} is expecting a begin-input marker (usually
     * {@code ^}).
     */
    BEGIN_ATOM, 

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} is expecting an end-of-input marker (usually
     * {@code $})
     */
    END_ATOM, 

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} is beginning its lexing operation.
     */
    START, 

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} is expecting either a start-saving marker
     * (usually {@code (}) or a filter token.
     */
    START_SAVING_OR_FILTER, 

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} is in an invalid state and is essentially
     * useless.
     */
    INVALID, 

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} is expecting a filter token.
     */
    FILTER, 

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} is expecting an <a
     * href="http://mvel.codehaus.org/">MVEL</a> expression modifier
     * to a filter token.
     */
    MVEL, 

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} has just completed parsing a filter token.
     */
    END_OF_FILTER,

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} is expecting an operator token.
     */
    OPERATOR, 

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} is expecting a left parenthesis character (a
     * token that represents both a begin-saving operator and a
     * precedence adjuster).
     */
    LEFT_PAREN, 

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} is expecting a right parenthesis character (a
     * token that represents both a stop-saving operator and a
     * precedence adjuster).
     */
    RIGHT_PAREN, 

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} could forward to several other states.
     */
    STOP_SAVING_OR_END_OF_INPUT_OR_NEXT_IN_SEQUENCE, 

    /**
     * A {@link PostfixTokenizer.State} indicating the {@link
     * PostfixTokenizer} is at the end of its lexing.
     */
    END
  }

  /**
   * A {@link Map} of {@link Token}s indexed by their character
   * representations.  The representations are stored as {@link
   * Integer}s rather than {@link Character}s for convenience since
   * certain {@link Reader} operations return {@code int}s, not {@code
   * char}s.
   *
   * <p>This field is never {@code null}.</p>
   *
   * @see #setupTokens(Map)
   */
  private final Map<Integer, Token> tokens;

  /**
   * The {@link Logger} used by this {@link PostfixTokenizer} to log messages.
   *
   * <p>This field is never {@code null} in normal situations; it
   * might be {@code null} in some rare serialization edge cases.</p>
   */
  private transient Logger logger;

  /**
   * The {@link PushbackReader} responsible for reading characters
   * from a textual objexj pattern.
   *
   * <p>This field is never {@code null} in normal situations.  It
   * might be {@code null} in some rare serialization edge cases.</p>
   */
  private transient final PushbackReader reader;

  /**
   * A {@link LinkedList} of {@link Token}s that is iterated over by
   * this {@link PostfixTokenizer}.
   *
   * <p>This field is never {@code null} in normal situations.  It
   * might be {@code null} in some rare serialization edge cases.</p>
   */
  final transient LinkedList<Token> output;

  /**
   * A {@link Throwable} encountered during iteration.
   *
   * <p>This field may be {@code null}.</p>
   */
  private transient Throwable error;

  /**
   * The {@link PostfixTokenizer.State} this {@link PostfixTokenizer}
   * is currently in.
   *
   * <p>This field may be {@code null}.</p>
   */
  private transient State state;

  /**
   * The current position of this {@link PostfixTokenizer} within its
   * associated {@link #reader}.
   */
  private transient int position;

  /**
   * A {@link Deque} of {@link Token}s used during lexing.
   *
   * <p>This field is never {@code null} in normal situations.  It
   * might be {@code null} in some rare serialization edge cases.</p>
   */
  private transient final Deque<Token> stack;

  /**
   * Creates a new {@link PostfixTokenizer}.
   *
   * @param reader the {@link PushbackReader} that produces characters
   * to read; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code reader} is {@code
   * null}
   *
   * @exception IOException if the supplied {@link PushbackReader} has
   * trouble reading
   *
   * @exception ParseException if the supplied {@link PushbackReader}
   * yields an unparseable pattern
   */
  public PostfixTokenizer(final PushbackReader reader) throws IOException, ParseException {
    super(0);
    this.logger = Logger.getLogger(this.getClass().getName());
    assert this.logger != null;
    if (reader == null) {
      throw new IllegalArgumentException("reader", new NullPointerException("reader"));
    }
    this.output = new LinkedList<Token>();
    this.stack = new ArrayDeque<Token>();
    this.reader = reader;
    this.state = State.START;
    this.tokens = new HashMap<Integer, Token>();
    this.setupTokens(this.tokens);
    this.prime();
  }

  /**
   * Returns the position of this {@link PostfixTokenizer} within its
   * associated {@link PushbackReader}.
   *
   * @return the position of this {@link PostfixTokenizer} within its
   * associated {@link PushbackReader}
   */
  @Override
  public int getIndex() {
    return this.position;
  }

  /**
   * Sets the position of this {@link PostfixTokenizer} within its
   * associated {@link PushbackReader}.
   *
   * <p>This method does not actually advance the {@link
   * PushbackReader}; it is simply used to update an internal variable
   * that keeps track of where the {@link PushbackReader} is within
   * its stream.</p>
   *
   * @param index the new position
   */
  @Override
  public void setIndex(final int index) {
    this.position = index;
  }

  /**
   * Builds an appropriate exception message for an unexpected
   * character during lexing.
   *
   * @param c the unexpected character
   *
   * @return a non-{@code null} error message
   */
  private final String buildExceptionMessage(final int c) {
    return String.format("Unexpected character (%c) at position %d in Reader %s", c, this.getErrorIndex(), this.reader);
  }

  /**
   * Lexes as little as possible to instantiate a {@link Token} so
   * that {@linkplain #next() iteration} can then take place.
   *
   * @exception IOException if a reading error occurs
   *
   * @exception ParseException if a non-sensical textual pattern was
   * encountered
   */
  private final void prime() throws IOException, ParseException {
    if (this.state == State.INVALID) {
      throw new IllegalStateException();
    }

    final StringBuilder sb = new StringBuilder();
    int parenCount = 0;
    int c = -1;

    READ_LOOP:
    for (; (c = this.read()) != -1; this.setIndex(this.getIndex() + 1)) {
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
          this.insertBeginAtomZeroOrMoreAndCatenation();
          this.unread(c);
          this.state = State.LEFT_PAREN;
          break;

        default:
          if (Character.isWhitespace(c)) {
            continue READ_LOOP;
          } else if (c != '$' && (c == '=' || Character.isJavaIdentifierStart(c))) {
            this.insertBeginAtomZeroOrMoreAndCatenation();
            sb.append((char)c);
            this.state = State.FILTER;
          } else {
            this.setErrorIndex(this.getIndex());
            throw new ParseException(buildExceptionMessage(c), this.getIndex());
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
        this.insertCatenation();
        assert this.state == State.START_SAVING_OR_FILTER;
        break READ_LOOP;


        //
        // END_ATOM
        //
      case END_ATOM:
        assert c == '$';
        this.insertCatenation();
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

        // First find a START_SAVING (atom) token.
        final Token token = new Token(Token.Type.START_SAVING);
        
        // TODO: support named capture groups

        // Add it to the output (it's an atom):
        this.output.add(token);

        // Then pretend we found a concatenation.
        this.insertCatenation();

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
          this.setErrorIndex(this.getIndex());
          throw new ParseException("Mismatched parentheses", this.getIndex());
        }

        assert Token.Type.START_GROUP == this.stack.peek().getType();
        this.stack.pop();

        // The original shunting-yard algorithm discards the parens
        // because in that algorithm they are used only for grouping.
        // In regular expressions, they are also atoms.  So they
        // belong in the output queue. We already added the
        // START_SAVING token, so now we need to add the STOP_SAVING.

        // Concatenate...
        this.insertCatenation();

        // ...with stop saving:
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
        } else if (c != '$' && (c == '=' || Character.isJavaIdentifierStart(c))) {
          // Forward to FILTER state
          sb.append((char)c);
          this.state = State.FILTER;
        } else {
          this.setErrorIndex(this.getIndex());
          throw new ParseException(buildExceptionMessage(c), this.getIndex());
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
              this.setErrorIndex(this.getIndex());
              throw new ParseException(buildExceptionMessage(c), this.getIndex());
            }
          } else {
            this.setErrorIndex(this.getIndex());
            throw new ParseException(buildExceptionMessage(c), this.getIndex());
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
            final Token filter = this.output.getLast();
            assert filter != null;
            assert filter.getFilterType() != null;
            assert filter.getType() == Token.Type.FILTER;
            filter.setValue(sb.toString());
            sb.setLength(0);
            this.state = State.END_OF_FILTER;
            break READ_LOOP;
          } else if (parenCount < 0) {
            this.setErrorIndex(this.getIndex());
            throw new ParseException("Mismatched parentheses", this.getIndex());
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
            this.setErrorIndex(this.getIndex());
            throw new ParseException(buildExceptionMessage(c), this.getIndex());
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
        // Any other character causes an ParseException.
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
          this.unread(c);
          this.state = State.OPERATOR;
          break;

        default:
          if (Character.isWhitespace(c)) {
            continue READ_LOOP;
          } else {
            this.setErrorIndex(this.getIndex());
            throw new ParseException(buildExceptionMessage(c), this.getIndex());
          }
        }
        break;


        //
        // END
        //
      case END:
        if (!Character.isWhitespace(c)) {
          this.setErrorIndex(this.getIndex());
          throw new ParseException(buildExceptionMessage(c), this.getIndex());
        }
        break;

        //
        // DEFAULT
        //
      default:
        this.setErrorIndex(this.getIndex());
        throw new ParseException("Unexpected state: " + this.state, this.getIndex());
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
          this.setErrorIndex(this.getIndex());
          throw new ParseException("Mismatched parentheses", this.getIndex());
        }
        this.output.add(this.stack.pop());
      }
    }

  }

  /**
   * Inserts a set of {@link Token}s into the {@linkplain #output
   * output <code>List</code>} that logically belong at the beginning
   * of any unanchored pattern expression.
   */
  private final void insertBeginAtomZeroOrMoreAndCatenation() {
    this.output.add(this.tokenFor('^'));
    this.insertCatenation();
    this.insertZeroOrMoreAnything();
    this.insertCatenation();
  }

  /**
   * Insert a sequence of {@link Token]s into the {@linkplain #output
   * output <code>List</code>} representing zero or more occurrences
   * of any Java {@link Object}.
   */
  private final void insertZeroOrMoreAnything() {
    this.output.add(new Token(Token.Type.FILTER, "java.lang.Object"));
    this.handleOperator(this.tokenFor('*'));
  }

  /**
   * Handles the concatenation operation.
   */
  private final boolean insertCatenation() {
    return this.handleOperator(this.tokenFor('/'));
  }

  /**
   * Processes the supplied operator {@link Token} according to the <a
   * href="http://en.wikipedia.org/wiki/Shunting-yard_algorithm">shunting-yard
   * algorithm</a>.
   *
   * @param o1 the operator {@link Token}; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code o1} is {@code null}
   * or if it is {@linkplain Token#isOperator() not an operator}
   */
  private final boolean handleOperator(final Token o1) {
    if (o1 == null) {
      throw new IllegalArgumentException("o1", new NullPointerException("o1"));
    } else if (!o1.isOperator()) {
      throw new IllegalArgumentException("!o1.isOperator()");
    }
    Token o2;
    boolean added = false;
    while (!this.stack.isEmpty() && this.assertIsOperator(o2 = this.stack.peek())) {
      if (o1.compareTo(o2) <= 0) {
        added = this.output.add(this.stack.pop()) || added;
      } else {
        break;
      }
    }
    this.push(o1);
    this.state = this.stateAfterOperator(o1);
    return added;
  }

  /**
   * Pushes the supplied {@link Token} on the {@linkplain #stack
   * stack}, after checking for error conditions.
   *
   * @param token the {@link Token} to push; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code token} is {@code
   * null} or is inappropriate for pushing
   */
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

  /**
   * Returns the appropriate {@link PostfixTokenizer.State} this
   * {@link PostfixTokenizer} should transition to after processing
   * the supplied {@link Token}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>The {@code token} parameter must be non-{@code null} and must
   * {@linkplain Token#getType() have a <code>Type</code>} that is
   * equal to one of the following:</p>
   *
   * <ul>
   *
   * <li>{@link Token.Type#ONE_OR_MORE}</li>
   *
   * <li>{@link Token.Type#ZERO_OR_MORE}</li>
   *
   * <li>{@link Token.Type#ZERO_OR_ONE}</li>
   *
   * <li>{@link Token.Type#ALTERNATION}</li>
   *
   * <li>{@link Token.Type#CATENATION}</li>
   *
   * </ul>
   *
   * @param token the {@link Token} to process; must not be {@code null}
   *
   * @return a non-{@code null} {@link PostfixTokenizer.State}
   *
   * @exception IllegalArgumentException if {@code token} does not
   * meet the conditions described earlier
   */
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

  /**
   * Manipulates the supplied {@link Map} to contain {@link Token}s
   * indexed under the characters (expressed as {@link Integer}s) that
   * identify them.
   *
   * @param tokens the {@link Map} of {@link Token}s to manipulate;
   * must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code tokens} is {@code
   * null}
   */
  private final void setupTokens(final Map<Integer, Token> tokens) {
    if (tokens == null) {
      throw new IllegalArgumentException("tokens", new NullPointerException("tokens"));
    }
    tokens.clear();
    tokens.put(Integer.valueOf('^'), new Token(Token.Type.BEGIN_ATOM));
    tokens.put(Integer.valueOf('$'), new Token(Token.Type.END_ATOM));
    tokens.put(Integer.valueOf('+'), new Token(Token.Type.ONE_OR_MORE));
    tokens.put(Integer.valueOf('*'), new Token(Token.Type.ZERO_OR_MORE));
    tokens.put(Integer.valueOf('?'), new Token(Token.Type.ZERO_OR_ONE));
    tokens.put(Integer.valueOf('|'), new Token(Token.Type.ALTERNATION));
    final Token catenation = new Token(Token.Type.CATENATION);
    tokens.put(Integer.valueOf('/'), catenation);
    tokens.put(Integer.valueOf(','), catenation);
    tokens.put(Integer.valueOf('('), new Token(Token.Type.START_SAVING));
    tokens.put(Integer.valueOf(')'), new Token(Token.Type.STOP_SAVING));
  }

  /**
   * Returns a {@link Token} suitable for the supplied character.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @param c a character known to identify a {@link Token}
   *
   * @return a suitable {@link Token}; never {@code null}
   *
   * @exception IllegalArgumentException if {@code c} does not pick
   * out a {@link Token}
   */
  private final Token tokenFor(final int c) {
    final Token token = this.tokens.get(Integer.valueOf(c));
    if (token == null) {
      throw new IllegalArgumentException(String.format("c: %c", (char)c));
    }
    return token;
  }

  /**
   * Asserts that the supplied {@link Token} {@linkplain
   * Token#isOperator() is an operator}.  Returns {@code true} if this
   * is true; throws an {@link IllegalArgumentException} if this is
   * not.
   *
   * @return {@code true} when invoked
   *
   * @exception IllegalArgumentException if {@code token} is {@code
   * null} or if its {@link Token#isOperator() isOperator()} method
   * returns {@code false}
   */
  private final boolean assertIsOperator(final Token token) {
    if (token == null) {
      throw new IllegalArgumentException("token", new NullPointerException("token"));
    }
    if (!token.isOperator()) {
      throw new IllegalArgumentException("!token.isOperator(): " + token);
    }
    return true;
  }

  /**
   * Reads a character from this {@link PostfixTokenizer}'s
   * {@linkplain #reader affiliated <code>PushbackReader</code>} and
   * {@linkplain #setIndex(int) advances the position}.
   *
   * @return a character
   *
   * @exception IOException if there was a problem reading
   */
  private final int read() throws IOException {
    final int c = this.reader.read();
    this.setIndex(this.getIndex() + 1);
    return c;
  }

  /**
   * "Unreads" a character from this {@link PostfixTokenizer}'s
   * {@linkplain #reader affiliated <code>PushbackReader</code>} and
   * {@linkplain #setIndex(int) decrements the position}.
   *
   * @return the new position
   *
   * @exception IOException if there was a problem reading
   */
  private final int unread(final int c) throws IOException {
    this.reader.unread(c);
    final int newPosition = this.getIndex() - 1;
    this.setIndex(newPosition);
    return newPosition;
  }

  /**
   * Returns {@code true} if this {@link PostfixTokenizer} has more
   * {@link Token}s to iterate over.
   *
   * @return {@code true} if this {@link PostfixTokenizer} has more
   * {@link Token}s to iterate over
   *
   * @exception IllegalStateException if a prior invocation of {@link
   * #next()} caused an error; see {@linkplain Throwable#getCause()
   * its cause} for the root problem
   */
  @Override
  public boolean hasNext() {
    if (this.error != null) {
      throw new IllegalStateException(this.error);
    }
    return !this.output.isEmpty();
  }

  /**
   * Returns the next {@link Token} lexed from this {@link
   * PostfixTokenizer}'s underlying {@link Reader}.
   *
   * @return a non-{@code null} {@link Token}
   *
   * @exception NoSuchElementException if the next element could not
   * be produced
   */
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
      } catch (final Exception other) {
        this.error = other;
        this.state = State.INVALID;
        this.output.clear();
      }
    }
    return returnValue;
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public final void remove() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link PostfixTokenizer}.
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link PostfixTokenizer}
   */
  @Override
  public String toString() {
    return this.reader.toString();
  }

}
