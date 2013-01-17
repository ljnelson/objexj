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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A trivial implementation of the {@link ThreadScheduler} interface.
 * This implementation is capable of creating new {@link Thread}s, but
 * does not ever actually schedule them.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 */
public class BasicThreadScheduler<T> implements ThreadScheduler<T> {

  /**
   * Creates a new {@link BasicThreadScheduler}.
   */
  public BasicThreadScheduler() {
    super();
  }

  /**
   * Creates a new {@link Thread}.
   *
   * @param id the identifier for the new {@link Thread}; may be
   * {@code null} and is used only for its {@link Object#toString()}
   * value
   *
   * @param programCounter the {@link ProgramCounter} to initialize
   * the new {@link Thread} with; must not be {@code null}
   *
   * @param items the {@link List} of items the new {@link Thread}
   * will {@linkplain Thread#read() read from}; may be {@code null} or
   * {@linkplain Collection#isEmpty() empty}
   *
   * @param itemPointer an {@code int} indicating which item from the
   * supplied {@code items} {@link List} to start reading from; may be
   * a positive {@code int} or equal to {@link
   * Thread#VALID_NO_INPUT_POINTER}
   *
   * @exception IllegalArgumentException if a parameter that cannot be
   * {@code null} is supplied with a {@code null} value
   */
  @Override
  public Thread<T> newThread(final Object id, final ProgramCounter<T> programCounter, final List<T> items, final int itemPointer, final Map<Object, CaptureGroup<T>> captureGroups) {
    return new Thread<T>(id, programCounter, items, itemPointer, captureGroups, this);
  }

  /**
   * Implements the {@link ThreadScheduler#schedule(Thread)} contract
   * by returning {@code true}, but otherwise taking no action.
   * Obviously this default implementation is suitable only for
   * inconsequential things like certain unit tests where scheduling
   * itself is not a concern.  Otherwise this method should be
   * overridden.
   *
   * @param thread the {@link Thread} to schedule; ignored
   *
   * @return {@code true} if invoked
   */
  @Override
  public boolean schedule(final Thread<T> thread) {
    return true;
  }
  
}
