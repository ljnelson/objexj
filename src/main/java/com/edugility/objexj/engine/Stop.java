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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An {@link Instruction} that stops saving a capture group by calling
 * {@link InstructionContext#stop(Object)} followed by {@link
 * InstructionContext#advanceProgramCounter()}.
 *
 * @param <T> the type of {@link Object}s that {@link
 * InstructionContext}s supplied to the {@link
 * #execute(InstructionContext)} method work with
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see InstructionContext#advanceProgramCounter()
 *
 * @see InstructionContext#stop(Object)
 */
public class Stop<T> extends Instruction<T> {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The key identifying the capture group to save.
   *
   * <p>This field may be {@code null}.</p>
   */
  private final Object key;

  /**
   * Creates a new {@link Stop} instruction.
   *
   * @param key the key identifying the capture group to save; may be
   * {@code null}
   */
  public Stop(final String key) {
    super();
    this.key = key;
  }

  /**
   * Creates a new {@link Stop} instruction.
   *
   * @param key the key identifying the capture group to save; may be
   * {@code null}
   */
  public Stop(final Object key) {
    super();
    this.key = key;
  }

  /**
   * Calls {@link InstructionContext#stop(Object)} followed by {@link
   * InstructionContext#advanceProgramCounter()}.
   *
   * @param context an {@link InstructionContext}; must not be {@code
   * null}
   *
   * @exception IllegalArgumentException if {@code context} is {@code
   * null}
   *
   * @see InstructionContext#advanceProgramCounter()
   *
   * @see InstructionContext#stop(Object)
   */
  @Override
  public final void execute(final InstructionContext<? extends T> context) {
    final String className = this.getClass().getName();
    final Logger logger = this.getLogger();
    final boolean finer = logger != null && logger.isLoggable(Level.FINER);
    if (finer) {
      logger.entering(className, "execute", context);
    }
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    context.stop(this.key);
    context.advanceProgramCounter();
    if (finer) {
      logger.exiting(className, "execute");
    }
  }

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link Stop} instruction.
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link Stop} instruction
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
