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
import java.util.Deque;
import java.util.Iterator;

import com.edugility.objexj.BeginInput;
import com.edugility.objexj.EndInput;
import com.edugility.objexj.Program;

public class Parser {

  public <T> Program<T> parse(final Iterator<Token> tokenizer) {
    if (tokenizer == null) {
      throw new IllegalStateException();
    }
    final Deque<T> stack = new ArrayDeque<T>();
    final Program<T> program = new Program<T>();
    for (int tokenCount = 0;tokenizer.hasNext(); tokenCount++) {
      final Token token = tokenizer.next();
      if (token != null) {
        final Token.Type tokenType = token.getType();
        assert tokenType != null;
        switch (tokenType) {
        case ALTERNATION:
          break;
        case BEGIN_INPUT:
          this.beginInput(program, tokenCount);
          break;
        case CATENATION:
          this.catenate(program);
          break;
        case END_INPUT:
          this.endInput(program, tokenCount);
          break;
        case FILTER:
          
          break;
        case ONE_OR_MORE:
          break;
        case START_SAVING:
          break;
        case STOP_SAVING:
          break;
        case ZERO_OR_MORE:
          break;
        case ZERO_OR_ONE:
          break;
        default:
          throw new IllegalStateException("Unknown token type: " + tokenType);
        }
      }
    }
    return program;
  }

  private <T> void beginInput(final Program<T> program, final int tokenCount) {
    if (tokenCount == 0) {
      program.add(new BeginInput<T>());
    } else {
      throw new IllegalStateException(); // TODO message
    }
  }

  private <T> void endInput(final Program<T> program, final int tokenCount) {
    if (tokenCount == 0) {
      throw new IllegalStateException(); // TODO message
    } else {
      program.add(new EndInput<T>());
    }
  }

  private <T> void catenate(final Program<T> program) {
    
  }

}
