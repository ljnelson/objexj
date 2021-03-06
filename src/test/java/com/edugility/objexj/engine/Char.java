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
package com.edugility.objexj.engine;

public class Char extends Filter<Character> {
  
  private static final long serialVersionUID = 1L;

  private final char c;

  public Char(final String operands) {
    super();
    if (operands == null) {
      throw new IllegalArgumentException("operands", new NullPointerException("operands"));
    } else if (operands.isEmpty()) {
      throw new IllegalArgumentException("operands.isEmpty()");
    }
    this.c = operands.charAt(0);
  }

  public Char(final char x) {
    super();
    this.c = x;
  }

  @Override
  public boolean accept(final InstructionContext<? extends Character> context) {
    boolean returnValue = false;
    if (context != null && context.canRead()) {
      final Character x = context.read();
      returnValue = x != null && x.charValue() == this.c;
    }
    return returnValue;
  }

  @Override
  public String toString() {
    return new StringBuilder(super.toString()).append(" ").append(this.c).toString();
  }

}
