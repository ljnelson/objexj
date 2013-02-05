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
import java.io.StringReader;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.edugility.objexj.engine.BeginInput;
import com.edugility.objexj.engine.EndInput;
import com.edugility.objexj.engine.InstanceOfMVELFilter;
import com.edugility.objexj.engine.Instruction;
import com.edugility.objexj.engine.Jump;
import com.edugility.objexj.engine.Match;
import com.edugility.objexj.engine.Program;
import com.edugility.objexj.engine.Save;
import com.edugility.objexj.engine.Split;
import com.edugility.objexj.engine.Stop;

public class Parser {

  private static final class State<T> implements Iterator<Token> {

    private int groupIndex;
    
    private Token token;

    private int position;

    private final Deque<Program<T>> stack;

    private final PostfixTokenizer tokenizer;

    private State(final PostfixTokenizer tokenizer) {
      super();
      if (tokenizer == null) {
        throw new IllegalArgumentException("tokenizer", new NullPointerException("tokenizer"));
      }
      this.position = -1;
      this.tokenizer = tokenizer;
      this.stack = new ArrayDeque<Program<T>>();
      this.groupIndex = 1;
    }

    public final Token getToken() {
      return this.token;
    }

    @Override
    public final boolean hasNext() {
      return this.tokenizer.hasNext();
    }

    @Override
    public final Token next() {
      this.token = this.tokenizer.next();
      this.position++;
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

  public <T> Program<T> parse(final String input) throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("input", new NullPointerException("input"));
    }
    final StringReader sr = new StringReader(input);
    final PushbackReader reader = new PushbackReader(sr) {
        @Override
        public String toString() {
          return sr.toString();
        }
      };
    try {
      return this.parse(new State<T>(new PostfixTokenizer(reader)));
    } finally {
      try {
        reader.close();
      } catch (final IOException logMe) {
        final Logger logger = Logger.getLogger(this.getClass().getName());
        assert logger != null;
        if (logger.isLoggable(Level.SEVERE)) {
          logger.logp(Level.SEVERE, this.getClass().getName(), "parse", "Error closing reader", logMe);
        }
      }
    }
  }

  public <T> Program<T> parse(final PostfixTokenizer tokenizer) {
    if (tokenizer == null) {
      throw new IllegalArgumentException("tokenizer", new NullPointerException("tokenizer"));
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
          this.alternate(parsingState);
          break;

        case BEGIN_ATOM:
          this.beginInput(parsingState);
          break;

        case CATENATION:
          this.catenate(parsingState);
          break;

        case END_ATOM:
          this.endInput(parsingState);
          break;

        case FILTER:
          this.filter(parsingState);
          break;

        case ONE_OR_MORE:
          this.oneOrMore(parsingState);
          break;

        case START_SAVING:
          this.startSaving(parsingState);
          break;

        case ZERO_OR_MORE:
          this.zeroOrMore(parsingState);
          break;

        case ZERO_OR_ONE:
          this.zeroOrOne(parsingState);
          break;

        case STOP_SAVING:
          this.stopSaving(parsingState);
          break;

        default:
          throw new IllegalStateException("Unknown token type: " + tokenType);
        }
      }

    }
    final Program<T> program;
    final int stackSize = parsingState.stackSize();
    switch (stackSize) {
    case 0:
      throw new IllegalStateException();
    case 1:
      program = new Program<T>();
      program.add(new Save<T>(0));
      program.addAll(parsingState.pop());
      program.add(new Stop<T>(0));
      break;
    default:
      throw new IllegalStateException("Unexpected stack size: " + parsingState);
    }
    assert program != null;
    program.setSource(parsingState.tokenizer);
    program.add(new Match<T>());
    return program;
  }

  private final <T> void beginInput(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    parsingState.push(Program.singleton(new BeginInput<T>()));
  }

  private final <T> void endInput(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    parsingState.push(Program.singleton(new EndInput<T>()));
  }

  private final <T> void filter(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    final Token token = parsingState.getToken();
    assert token != null;
    assert Token.Type.FILTER == token.getType();

    parsingState.push(Program.singleton(new InstanceOfMVELFilter<T>(token.getFilterType(), token.getValue())));
  }

  private final <T> void catenate(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    assert parsingState.stackSize() >= 2;

    final Program<T> p2 = parsingState.pop();
    assert p2 != null;

    final Program<T> p1 = parsingState.peek();
    assert p1 != null;

    /*
     * Catenation program fragment:
     *
     * 34: ...
     * 35: (p1)
     * 36: (p2)
     * 37: ...
     */

    p1.addAll(p2);
  }

  private final <T> void alternate(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    assert parsingState.stackSize() >= 2;

    final Program<T> p2 = parsingState.pop();
    assert p2 != null;
    assert !p2.isEmpty();

    final Program<T> p1 = parsingState.pop();
    assert p1 != null;
    assert !p1.isEmpty();

    /*
     * Alternation program fragment:
     *
     *  46: ...
     *  47: split +1, +(p1.size() + 2)
     *  48: (p1)
     *  49: jump +(p2.size() + 1)
     *  50: (p2)
     *  51: ...
     */

    final Program<T> p0 = new Program<T>();
    p0.add(new Split<T>(1, p1.size() + 2, true));
    p0.addAll(p1);
    final int target = p2.size() + 1;
    p0.add(new Jump<T>(target, true)); // dangling jump target
    p0.addAll(p2);

    parsingState.push(p0);
  }

  private final <T> void zeroOrMore(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    assert parsingState.stackSize() >= 1;

    final Program<T> p1 = parsingState.pop();
    assert p1 != null;

    /*
     * Zero or more program fragment
     *
     * 24 ...
     * 25 split +1, +(p1.size() + 2)
     * 26 (p1)
     * 27 jump -(p1.size() + 1)
     * 28 ...
     */

    final Program<T> p0 = new Program<T>();
    final int target = p1.size() + 2;
    p0.add(0, new Split<T>(1, target, true)); // dangling split target
    p0.addAll(p1);
    p0.add(new Jump<T>(-(p1.size() + 1), true));
    
    parsingState.push(p0);
  }

  private final <T> void zeroOrOne(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    assert parsingState.stackSize() >= 1;

    final Program<T> p1 = parsingState.peek();
    assert p1 != null;
    assert !p1.isEmpty();

    /*
     * Zero or one program fragment:
     *
     * 17 ...
     * 18 split +1, +(p1.size() + 1)
     * 19 (p1)
     * 20 ...
     */

    final int target = p1.size() + 1;
    p1.add(0, new Split<T>(1, target, true)); // dangling split target
    
  }

  private final <T> void oneOrMore(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    assert parsingState.stackSize() >= 1;
    
    final Program<T> p1 = parsingState.peek();
    assert p1 != null;
    assert !p1.isEmpty();

    /*
     * One or more program fragment:
     *
     * 66: ...
     * 67: (p1)
     * 68: split -(p1.size()), +1
     * 69: ...
     */

    p1.add(new Split<T>(-p1.size(), 1, true));
  }

  private final <T> void startSaving(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    parsingState.push(Program.singleton(new Save<T>(Integer.valueOf(parsingState.groupIndex++))));
  }

  private final <T> void stopSaving(final State<T> parsingState) {
    if (parsingState == null) {
      throw new IllegalArgumentException("parsingState", new NullPointerException("parsingState"));
    }
    parsingState.push(Program.singleton(new Stop<T>(Integer.valueOf(--parsingState.groupIndex))));
  }

}
