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
import java.util.Map;

public interface ThreadScheduler<T> {

  /**
   * Creates a new {@link Thread} and returns it.  Implementations of
   * this method must not return {@code null}.
   *
   * @param id the identifier for the new {@link Thread}; may be
   * {@code null} and used only for its {@link Object#toString()}
   * method
   *
   * @param programCounter a non-{@code null} {@link ProgramCounter}
   * to initialize the new {@link Thread} with
   *
   * @param items a possibly {@code null} {@link List} of items that
   * the new {@link Thread} may {@linkplain Thread#read() read from}
   *
   * @param itemPointer the starting index within the supplied {@link
   * List} of items from which the new {@link Thread} will {@linkplain
   * Thread#read() read}
   *
   * @param captureGroups any {@link CaptureGroup}s that the new
   * {@link Thread} should start out with; may be {@code null} or
   * {@linkplain Map#isEmpty() empty}
   *
   * @return a new non-{@code null} {@link Thread}
   */
  public Thread<T> newThread(final Object id, final ProgramCounter<T> programCounter, final List<T> items, final int itemPointer, final Map<Object, CaptureGroup<T>> captureGroups, final Map<Object, Object> variables);

  /**
   * Schedules the supplied {@link Thread} for execution immediately
   * or later.
   *
   * @return {@code true} if the supplied {@link Thread} was
   * scheduled; {@code false} otherwise
   *
   * @exception IllegalArgumentException if {@code t} is {@code null}
   */
  public boolean schedule(final Thread<T> t);
  
}
