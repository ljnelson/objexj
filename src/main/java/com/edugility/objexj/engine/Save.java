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

import java.io.Serializable; // for javadoc only

import java.util.List;

/**
 * An {@link Instruction} that saves the current item pointer in an
 * {@link InstructionContext} so that later a capture group may be
 * identified.
 *
 * @param <T> the kind of {@link Object} that {@link
 * InstructionContext} instances submitted to the {@link
 * #execute(InstructionContext)} method work with
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see InstructionContext#save(Object)
 */
public class Save<T> extends Instruction<T> {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The key under which the item pointer will be saved.
   *
   * <p>This field may be {@code null}.</p>
   */
  private final Object key;

  /**
   * Creates a new {@link Save} instruction.
   *
   * @param key the key under which the item pointer will be saved;
   * may be {@code null}
   */
  public Save(final String key) {
    super();
    this.key = key;
  }

  /**
   * Creates a new {@link Save} instruction.
   *
   * @param key the key under which the item pointer will be saved;
   * may be {@code null}
   */
  public Save(final Object key) {
    super();
    this.key = key;
  }

  /**
   * Calls {@link InstructionContext#save(Object)} and then {@link
   * InstructionContext#advanceProgramCounter()}.
   *
   * @param context the {@link InstructionContext} this {@link Save}
   * instruction is running in; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code context} is {@code
   * null}
   */
  @Override
  public final void execute(final InstructionContext<? extends T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    context.save(this.key);
    context.advanceProgramCounter();
  }

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link Save} instruction.
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link Save} instruction.
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(super.toString()).append(" ");
    if (this.key == null) {
      sb.append("null");
    } else {
      sb.append(this.key);
    }
    return sb.toString();
  }

}
