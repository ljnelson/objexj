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

import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jump<T> extends Instruction<T> {

  private static final long serialVersionUID = 1L;

  private static final Pattern OPERAND_PATTERN = Pattern.compile("^\\s*(\\+?)(\\d+)");

  private final boolean relative;

  public final int programLocation;
  
  public Jump(String operand) {
    super();
    if (operand == null) {
      throw new IllegalArgumentException("operand", new NullPointerException("operand"));
    }
    final Matcher m = OPERAND_PATTERN.matcher(operand);
    if (m.find()) {
      this.relative = "+".equals(m.group(1));
      this.programLocation = Integer.parseInt(m.group(2));
    } else {
      throw new IllegalArgumentException("Bad operand: " + operand);
    }
    if (!this.relative && this.programLocation < 0) {
      throw new IllegalArgumentException("this.programLocation < 0: " + this.programLocation);
    }
  }

  public Jump(final int programLocation) {
    this(programLocation, false);
  }

  public Jump(final int programLocation, final boolean relative) {
    super();
    this.relative = relative;
    if (!relative && programLocation < 0) {
      throw new IllegalArgumentException("programLocation < 0: " + programLocation);
    }
    this.programLocation = programLocation;
  }

  @Override
  public void execute(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    context.jump(this.programLocation, this.relative);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(super.toString()).append(" ");
    if (this.relative && this.programLocation >= 0) {
      sb.append("+");
    }
    sb.append(this.programLocation);
    return sb.toString();
  }

}
