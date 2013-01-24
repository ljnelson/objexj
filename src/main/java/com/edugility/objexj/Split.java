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
package com.edugility.objexj;

import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Split<T> extends Jump<T> {

  private static final long serialVersionUID = 1L;

  public final int newThreadProgramLocation;

  private final boolean relative;

  private static final Pattern OPERAND_PATTERN = Pattern.compile("^\\s*\\+?\\d+\\s*,\\s*(\\+?)(\\d+)");

  public Split(String operands) {
    super(operands);
    assert operands != null;
    final Matcher m = OPERAND_PATTERN.matcher(operands);
    assert m != null;
    if (!m.find()) {
      throw new IllegalArgumentException("Bad operands: " + operands);
    } else {
      this.relative = "+".equals(m.group(1));
      this.newThreadProgramLocation = Integer.parseInt(m.group(2));
    }
    if (this.newThreadProgramLocation < 0) {
      throw new IllegalArgumentException("this.newThreadProgramLocation < 0: " + this.newThreadProgramLocation);
    }
    if (this.programLocation == this.newThreadProgramLocation) {
      throw new IllegalArgumentException("this.programLocation == this.newThreadProgramLocation: " + this.newThreadProgramLocation);
    }
  }

  public Split(final int programLocation, final int newThreadProgramLocation) {
    this(programLocation, newThreadProgramLocation, false);
  }

  public Split(final int programLocation, final int newThreadProgramLocation, final boolean relative) {
    super(programLocation, relative);
    assert programLocation >= 0;
    if (newThreadProgramLocation < 0) {
      throw new IllegalArgumentException("newThreadProgramLocation < 0: " + newThreadProgramLocation);
    }
    if (programLocation == newThreadProgramLocation) {
      throw new IllegalArgumentException("programLocation == newThreadProgramLocation: " + programLocation);
    }
    this.relative = relative;
    this.newThreadProgramLocation = newThreadProgramLocation;
  }

  @Override
  public final void execute(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    super.execute(context);
    context.scheduleNewThread(this.newThreadProgramLocation, this.relative);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(super.toString()).append(", ");
    if (this.relative) {
      sb.append("+");
    }
    sb.append(this.newThreadProgramLocation);
    return sb.toString();
  }


}
