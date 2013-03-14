/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
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

import java.io.Serializable; // for javadoc only

import java.util.List;

/**
 * An {@link Instruction} that {@linkplain
 * InstructionContext#advanceProgramCounter() advances the program
 * counter} if and only if an {@link InstructionContext} is
 * {@linkplain InstructionContext#atStart() at the beginning of the
 * input}.
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public class BeginInput<T> extends Instruction<T> {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@link BeginInput} instruction.
   */
  public BeginInput() {
    super();
  }

  /**
   * {@linkplain InstructionContext#advanceProgramCounter() Advances
   * the program counter} if and only if the supplied {@link
   * InstructionContext} is {@linkplain InstructionContext#atStart()
   * at the beginning of the input}.  {@linkplain
   * InstructionContext#die() Kills the
   * <code>InstructionContext</code} otherwise.
   *
   * @param context the {@link InstructionContext} to operate on; must
   * not be {@code null}
   *
   * @exception IllegalArgumentException if {@code context} is {@code
   * null}
   */
  @Override
  public final void execute(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    if (context.atStart()) {
      context.advanceProgramCounter();
    } else {
      context.die();
    }
  }

}
