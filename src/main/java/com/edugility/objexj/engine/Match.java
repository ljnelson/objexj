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
 * An {@link Instruction} that causes an {@link InstructionContext} to
 * be {@linkplain InstructionContext#match() placed into the match
 * state}.
 *
 * @param <T> the type of {@link Object} managed by {@link
 * InstructionContext}s passed to the {@link
 * #execute(InstructionContext)} method
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see InstructionContext
 *
 * @see InstructionContext#match()
 */
public class Match<T> extends Instruction<T> {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@link Match} instruction.
   */
  public Match() {
    super();
  }

  /**
   * Calls {@link InstructionContext#match()} when invoked.
   *
   * @param context an {@link InstructionContext}; must not be {@code
   * null}
   *
   * @exception IllegalArgumentException if {@code context} is {@code null}
   */
  @Override
  public final void execute(final InstructionContext<? extends T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context == null");
    }
    context.match();
  }

}
