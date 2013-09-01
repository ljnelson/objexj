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

/**
 * An {@link Instruction} that causes an {@link InstructionContext} to
 * {@linkplain InstructionContext#jump(int, boolean) jump} to a new
 * location in a {@link Program}.
 *
 * @param <T> the type of {@link Object} managed by {@link
 * InstructionContext}s passed to the {@link
 * #execute(InstructionContext)} method
 *
 * @author <a href="http://about.me/lairdnelson">
 * target="_parent">Laird Nelson</a>
 *
 * @see InstructionContext#jump(int, boolean)
 */
public class Jump<T> extends Instruction<T> {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * A {@link Pattern} for parsing a single {@link String} into
   * operands indicating a program location and a {@code boolean}
   * indicating whether that program location is relative to the
   * current location.
   *
   * <p>This field is never {@code null}.</p>
   */
  private static final Pattern OPERAND_PATTERN = Pattern.compile("^\\s*(\\+?)(\\d+)");

  /**
   * Whether or not the {@link #programLocation} field is to be
   * interpreted relative to the current program location.
   */
  public final boolean relative;

  /**
   * The new location within a {@link Program} that an {@link
   * InstructionContext} should {@linkplain
   * InstructionContext#jump(int, boolean) jump to}.
   */
  public final int programLocation;
  
  /**
   * Creates a new {@link Jump} instruction.
   *
   * @param operand a {@link String} containing the operands for this
   * {@link Jump} instruction; must not be {@code null}
   *
   * @exception IllegalArgumentException if the supplied {@code
   * operand} {@link String} could not be parsed
   */
  public Jump(final String operand) {
    super();
    if (operand == null) {
      throw new IllegalArgumentException("operand", new NullPointerException("operand"));
    }
    final Matcher m = OPERAND_PATTERN.matcher(operand);
    if (m.find()) {
      this.programLocation = Integer.parseInt(m.group(2));
      this.relative = "+".equals(m.group(1)) || this.programLocation < 0;
    } else {
      throw new IllegalArgumentException("Bad operand: " + operand);
    }
  }

  /**
   * Creates a new {@link Jump} instruction.
   *
   * @param programLocation the new location within a {@link Program}
   * to {@linkplain InstructionContext#jump(int, boolean) jump to}
   */
  public Jump(final int programLocation) {
    this(programLocation, false);
  }

  /**
   * Creates a new {@link Jump} instruction.
   *
   * @param programLocation the new location within a {@link Program}
   * to {@linkplain InstructionContext#jump(int, boolean) jump to}
   *
   * @param relative if {@code true}, then the supplied {@code
   * programLocation} will be interpreted as being relative to the
   * current program location in any {@link InstructionContext} passed
   * to the {@link #execute(InstructionContext)} method; if {@code
   * false}, then if the supplied {@code programLocation} is positive
   * or zero it will be treated as an absolute program location
   */
  public Jump(final int programLocation, final boolean relative) {
    super();
    this.relative = relative || programLocation < 0;
    this.programLocation = programLocation;
  }

  /**
   * Calls {@link InstructionContext#jump(int, boolean)} supplying it
   * with the values of the {@link #programLocation} field and the
   * {@link #relative} field respectively.
   *
   * @param context the {@link InstructionContext} in which this
   * {@link Jump} instruction is running; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code context} is {@code
   * null}
   */
  @Override
  public void execute(final InstructionContext<? extends T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    context.jump(this.programLocation, this.relative);
  }

  /**
   * Returns a hashcode for this {@link Jump} instruction.
   *
   * @return a hashcode for this {@link Jump} instruction
   */
  @Override
  public int hashCode() {
    return this.programLocation + (relative ? 1 : 0);
  }

  /**
   * Returns {@code true} if the supplied {@link Object} is equal to
   * this {@link Jump} instruction.
   *
   * @param other the {@link Object} to test; may be {@code null} in
   * which case {@code false} will be returned
   */
  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (super.equals(other)) {
      final Jump him = (Jump)other;
      return this.programLocation == him.programLocation && this.relative == him.relative;
    } else {
      return false;
    }
  }

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link Jump} instruction.
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link Jump} instruction
   */
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
