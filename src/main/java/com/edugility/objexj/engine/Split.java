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

import java.io.Serializable;

import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link Jump} instruction that also {@link
 * InstructionContext#scheduleNewThread(int, boolean) schedules a new
 * <code>Thread</code>} for execution at a different program location.
 *
 * @param <T> the type of {@link Object} managed by {@link
 * InstructionContext}s passed to the {@link
 * #execute(InstructionContext)} method
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public class Split<T> extends Jump<T> {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The program location for the new {@link Thread} spawned by this
   * {@link Split} instruction.
   */
  public final int newThreadProgramLocation;

  /**
   * Whether or not the {@link Jump#programLocation} and {@link
   * #newThreadProgramLocation} fields are relative to an {@link
   * InstructionContext}'s current program location.
   */
  public final boolean relative;

  /**
   * A {@link Pattern} for parsing a single operand {@link String}
   * into arguments for new {@link Split} instances.
   *
   * <p>This field is never {@code null}.</p>
   */
  private static final Pattern OPERAND_PATTERN = Pattern.compile("^\\s*\\+?\\d+\\s*,\\s*(\\+?)(\\d+)");

  /**
   * Creates a new {@link Split} instruction.
   *
   * @param operands a {@link String} containing values for the {@link
   * Jump#programLocation}, {@link #newThreadProgramLocation} and
   * {@link #relative} fields; must not be {@code null}
   *
   * @exception IllegalArgumentException if after parsing the values of
   * the {@link Jump#programLocation} and {@link
   * #newThreadProgramLocation} fields are the same
   */
  public Split(String operands) {
    super(operands);
    assert operands != null;
    final Matcher m = OPERAND_PATTERN.matcher(operands);
    assert m != null;
    if (!m.find()) {
      throw new IllegalArgumentException("Bad operands: " + operands);
    } else {
      this.newThreadProgramLocation = Integer.parseInt(m.group(2));
      this.relative = "+".equals(m.group(1)) || this.newThreadProgramLocation < 0 || this.programLocation < 0;
    }
    if (this.programLocation == this.newThreadProgramLocation) {
      throw new IllegalArgumentException("this.programLocation == this.newThreadProgramLocation: " + this.newThreadProgramLocation);
    }
  }

  /**
   * Creates a new {@link Split} instruction.
   *
   * @param programLocation the location an {@link InstructionContext}
   * should {@linkplain InstructionContext#jump(int, boolean) jump to}
   *
   * @param newThreadProgramLocation the location a new {@link Thread}
   * should be positioned at
   *
   * @exception IllegalArgumentException if {@code programLocation}
   * and {@code newThreadProgramLocation} are equal
   */
  public Split(final int programLocation, final int newThreadProgramLocation) {
    this(programLocation, newThreadProgramLocation, false);
  }

  /**
   * Creates a new {@link Split} instruction.
   *
   * @param  programLocation the location an {@link InstructionContext}
   * should {@linkplain InstructionContext#jump(int, boolean) jump to}
   *
   * @param newThreadProgramLocation the location a new {@link Thread}
   * should be positioned at
   *
   * @param relative whether the {@code programLocation} and {@code
   * newThreadProgramLocation} parameters should be considered to be
   * relative locations
   *
   * @exception IllegalArgumentException if {@code programLocation}
   * and {@code newThreadProgramLocation} are equal
   */
  public Split(final int programLocation, final int newThreadProgramLocation, final boolean relative) {
    super(programLocation, relative);
    this.relative = super.relative || newThreadProgramLocation < 0 || programLocation < 0;
    if (programLocation == newThreadProgramLocation) {
      throw new IllegalArgumentException("programLocation == newThreadProgramLocation: " + programLocation);
    }
    this.newThreadProgramLocation = newThreadProgramLocation;
  }

  /**
   * Calls {@link InstructionContext#scheduleNewThread(int, boolean)},
   * supplying it with the values of the {@link
   * #newThreadProgramLocation} and {@link #relative} fields
   * respectively, and then calls {@link
   * Jump#execute(InstructionContext)}.
   *
   * @exception IllegalArgumentException if {@code context} is {@code
   * null}
   */
  @Override
  public final void execute(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    context.scheduleNewThread(this.newThreadProgramLocation, this.relative);
    super.execute(context);
  }

  /**
   * Returns a hashcode for this {@link Split} instruction.
   *
   * @return a hashcode for this {@link Split} instruction.
   */
  @Override
  public int hashCode() {
    return 37 * super.hashCode() + this.newThreadProgramLocation + (this.relative ? 1 : 0);
  }

  /**
   * Returns {@code true} if the supplied {@link Object} is equal to
   * this {@link Split} instruction.
   *
   * @param other the {@link Object} to test; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if the supplied {@link Object} is equal to
   * this {@link Split} instruction; {@code false} otherwise
   */
  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (super.equals(other)) {
      final Split him = (Split)other;
      return this.newThreadProgramLocation == him.newThreadProgramLocation && this.relative == him.relative;
    } else {
      return false;
    }
  }

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link Split} instruction.
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link Split} instruction
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(super.toString()).append(", ");
    if (this.relative && this.newThreadProgramLocation >= 0) {
      sb.append("+");
    }
    sb.append(this.newThreadProgramLocation);
    return sb.toString();
  }


}
