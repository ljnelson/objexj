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
import java.io.Reader;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import com.edugility.objexj.BeginInput;
import com.edugility.objexj.EndInput;
import com.edugility.objexj.InstanceOfMVELFilter;
import com.edugility.objexj.Instruction;
import com.edugility.objexj.Jump;
import com.edugility.objexj.Match;
import com.edugility.objexj.Program;
import com.edugility.objexj.Split;

public class Parser {

  private static final class State<T> implements Iterator<Token> {
    
    private Token operator;

    private Token token;

    private int position;

    private final Deque<Program<T>> stack;

    private final Tokenizer tokenizer;

    private State(final Tokenizer tokenizer) {
      super();
      if (tokenizer == null) {
        throw new IllegalArgumentException("tokenizer", new NullPointerException("tokenizer"));
      }
      this.position = -1;
      this.tokenizer = tokenizer;
      this.stack = new ArrayDeque<Program<T>>();
    }

    public final Token getToken() {
      return this.token;
    }

    public final Token getOperator() {
      return this.operator;
    }

    public final void setOperator(final Token operator) {
      if (operator != null) {
        final Token.Type type = operator.getType();
        switch (type) {
        case FILTER:
          throw new IllegalArgumentException("operator");
        default:
          break;
        }
      }
      this.operator = operator;
    }

    @Override
    public final boolean hasNext() {
      return this.tokenizer.hasNext();
    }

    @Override
    public final Token next() {
      this.token = this.tokenizer.next();
      this.position++;
      if (this.token.isOperator()) {
        this.operator = this.token;
      }
      return this.token;
    }

    @Override
    public final void remove() {
      this.tokenizer.remove();
    }

    public final int stackSize() {
      return this.stack.size();
    }

    public void push(final Program<T> programFragment) {
      if (programFragment == null) {
        throw new IllegalArgumentException("programFragment", new NullPointerException("programFragment"));
      }
      this.stack.push(programFragment);
    }

    public Program<T> peek() {
      return this.stack.peek();
    }

    public Program<T> pop() {
      return this.stack.pop();
    }

  }

  public Parser() {
    super();
  }

  public <T> Program<T> parse(final Tokenizer tokenizer) {
    if (tokenizer == null) {
      throw new IllegalStateException();
    }
    return this.parse(new State<T>(tokenizer));
  }

  private final <T> Program<T> parse(final Parser.State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalStateException("parsingState", new NullPointerException("parsingState"));
    }

    for (int tokenCount = 0; parsingState.hasNext(); tokenCount++) {

      final Token token = parsingState.next();
      if (token != null) {

        final Token.Type tokenType = token.getType();
        assert tokenType != null;

        switch (tokenType) {          

        case ALTERNATION:
          this.alternation(parsingState);
          break;

        case BEGIN_INPUT:
          this.beginInput(parsingState);
          break;

        case CATENATION:
          this.catenation(parsingState);
          break;

        case FILTER:
          this.filter(parsingState);
          break;

        case END_INPUT:
        case ONE_OR_MORE:
        case START_SAVING:
        case STOP_SAVING:
        case ZERO_OR_MORE:
        case ZERO_OR_ONE:
          throw new UnsupportedOperationException("Not yet handled: " + token);

        default:
          throw new IllegalStateException("Unknown token type: " + tokenType);
        }
      }

    }
    assert parsingState.stackSize() == 1;
    final Program<T> program = parsingState.pop();
    assert program != null;
    program.add(new Match<T>());
    return program;
  }

  private final <T> void alternation(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    final Token operator = parsingState.getOperator();
    assert operator != null;
    assert operator.getType() == Token.Type.ALTERNATION;
    final Token token = parsingState.getToken();
    assert token == operator;
  }

  private final <T> void beginInput(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    parsingState.push(Program.singleton(new BeginInput<T>()));
  }

  private final <T> void catenation(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    final Token operator = parsingState.getOperator();
    assert operator != null;
    assert operator.getType() == Token.Type.CATENATION;
    final Token token = parsingState.getToken();
    assert token == operator;
  }

  private final <T> void filter(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }

    final Token token = parsingState.getToken();
    assert token != null;
    assert Token.Type.FILTER == token.getType();

    parsingState.push(Program.singleton(new InstanceOfMVELFilter<T>(token.getFilterType(), token.getValue())));

    final Token operator = parsingState.getOperator();
    if (operator == null) {
      if (parsingState.stackSize() > 1) {
        this.catenate(parsingState);
      }
    } else {
      final Token.Type type = operator.getType();
      switch (type) {
      case ALTERNATION:
        this.alternate(parsingState);
        break;
      case CATENATION:
        this.catenate(parsingState);
        break;
      default:
        throw new IllegalStateException("Unexpected operator in filter(): " + operator);
      }
    }
    assert parsingState.stackSize() == 1;
  }

  private final <T> void catenate(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    assert parsingState.stackSize() == 2;
    final Program<T> p2 = parsingState.pop();
    assert p2 != null;
    final Program<T> p1 = parsingState.peek();
    assert p1 != null;
    p1.addAll(p2);
    assert parsingState.stackSize() == 1;
  }

  private final <T> void alternate(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    assert parsingState.stackSize() == 2;

    final Program<T> p2 = parsingState.pop();
    assert p2 != null;

    final Program<T> p1 = parsingState.pop();
    assert p1 != null;

    final Program<T> p0 = new Program<T>();
    p0.add(new Split<T>(1 /* next instruction (to be added) */, 1 + p1.size() /* we'll add a jump, then p1 */));
    p0.addAll(p1);
    p0.add(new Jump<T>(1 /* split instruction */ + p1.size() + 1 /* Jump instruction */ + p2.size()));
    p0.addAll(p2);

    parsingState.push(p0);
    assert parsingState.stackSize() == 1;
  }


}
