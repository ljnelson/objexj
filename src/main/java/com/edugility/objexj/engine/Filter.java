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

/**
 * An {@link Instruction} that {@linkplain
 * InstructionContext#advanceProgramCounter() advances an
 * <code>InstructionContext</code>'s program counter} if the {@link
 * InstructionContext} {@linkplain InstructionContext#canRead() can be
 * read from} and {@linkplain InstructionContext#read() produces an
 * <code>Object</code>} that is non-{@code null}.
 *
 * @param <T> the type of {@link Object} that can be filtered
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public class Filter<T> extends Instruction<T> {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@link Filter}.
   */
  public Filter() {
    super();
  }

  /**
   * {@linkplain InstructionContext#advanceProgramCounter() Advances
   * an <code>InstructionContext</code>'s program counter} if and only
   * if the {@link #accept(InstructionContext)} method returns {@code
   * true}.
   *
   * <p>If the {@link #accept(InstructionContext)} method returns
   * {@code false}, then this method {@linkplain
   * InstructionContext#die() kills the
   * <code>InstructionContext</code>}.</p>
   *
   * @param context an {@link InstructionContext} to execute this
   * {@link Filter} in; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code context} is {@code
   * null}
   *
   * @see #accept(InstructionContext)
   */
  @Override
  public final void execute(final InstructionContext<? extends T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context", new NullPointerException("context == null"));
    }
    if (this.accept(context)) {
      context.advanceItemPointer();
      context.advanceProgramCounter();
    } else {
      context.die();
    }
  }

  /**
   * Returns {@code true} if this {@link Filter} accepts the supplied
   * {@link InstructionContext}.
   *
   * @return {@code true} if and only if the supplied {@link
   * InstructionContext} is not {@code null}, {@linkplain
   * InstructionContext#canRead() can be read from}, and {@linkplain
   * InstructionContext#read() produces an <code>Object</code>} that
   * is not {@code null}; {@code false} in all other cases
   */
  public boolean accept(final InstructionContext<? extends T> context) {
    return context != null && context.canRead() && context.read() != null;
  }

}
