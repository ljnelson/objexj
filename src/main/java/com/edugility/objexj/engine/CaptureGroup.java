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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A sub-{@link List} of items as captured by a {@link
 * com.edugility.objexj.engine.Thread}.
 *
 * @param <T> the type of {@link Object} which will govern {@link
 * List}s produced by this {@link CaptureGroup}
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class CaptureGroup<T> implements Cloneable, Serializable {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The inclusive zero-based index at which this {@link CaptureGroup}
   * starts.  This field will always be greater than or equal to
   * {@code 0} and less than the {@linkplain List#size() size} of the
   * {@linkplain #getItems() <code>List</code> of items}.
   *
   * @see #items
   */
  private final int startIndex;

  /**
   * The exclusive zero-based index at which this {@link CaptureGroup}
   * ends.  This field will always be greater than or equal to
   * {@linkplain #startIndex the start index} and less than or
   * equal to the {@linkplain List#size() size} of the {@linkplain
   * #getItems() <code>List</code> of items}.
   *
   * @see #items
   */
  private int endIndex;

  /**
   * The {@link List} of items from which this {@link CaptureGroup}
   * will capture a sub-{@link List}.  This field is never {@code
   * null}.
   */
  private List<? extends T> items;

  /**
   * Creates a new {@link CaptureGroup}.
   *
   * @param items the {@link List} of items from which to capture;
   * must not be {@code null} and must not be {@linkplain
   * List#isEmpty() empty}
   *
   * @param startIndex the zero-based index at which capturing will
   * start; must be greater than or equal to {@code 0} and less than
   * {@link List#size() items.size()}
   *
   * @exception IllegalArgumentException if {@code items} is {@code
   * null} or {@code items} {@linkplain List#isEmpty() is empty} or
   * {@code startIndex} is less than {@code 0} or {@code startIndex}
   * is greater than or equal to {@link List#size() items.size()}
   */ 
  public CaptureGroup(final List<? extends T> items, final int startIndex) {
    super();
    if (items == null) {
      throw new IllegalArgumentException("items", new NullPointerException("items"));
    }
    if (items.isEmpty()) {
      throw new IllegalArgumentException("items.isEmpty()");
    }
    if (startIndex < 0) {
      throw new IllegalArgumentException("startIndex < 0: " + startIndex);
    }
    if (startIndex >= items.size()) {
      throw new IllegalArgumentException("startIndex >= items.size(): " + startIndex + " >= " + items.size());
    }

    this.items = items;
    this.startIndex = startIndex;
    this.endIndex = -1;
  }

  /**
   * Returns a non-{@code null} {@linkplain
   * Collections#unmodifiableList(List) unmodifiable
   * <code>List</code>} of the items this {@link CaptureGroup}
   * captured.
   *
   * @return a non-{@code null} {@link List} of items captured by this
   * {@link CaptureGroup}.  The {@link List} returned is that returned
   * by the {@link List#subList(int, int)} method (or {@link
   * Collections#emptyList()})
   */
  public final List<? extends T> getItems() {
    if (this.items == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(this.items.subList(this.startIndex, this.endIndex < 0 ? this.items.size() : this.endIndex));
    }
  }

  /**
   * Sets the {@link List} of items this {@link CaptureGroup} will
   * capture from.
   *
   * @param items the {@link List} of items; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code items} is {@code
   * null}
   */
  private final void setItemList(final List<? extends T> items) {
    if (items == null) {
      throw new IllegalArgumentException("items", new NullPointerException("items == null"));
    }
    this.items = items;
  }

  /**
   * Sets the exclusive zero-based index that marks the end of the
   * items this {@link CaptureGroup} will capture.
   *
   * @param endIndex the exclusive zero-based end index; must be
   * greater than {@code 0}, less than the value of the {@code
   * startIndex} supplied at {@linkplain #CaptureGroup(List, int)
   * construction time} and less than the {@linkplain List#size()
   * size} of the {@link List} of items supplied at {@linkplain
   * #CaptureGroup(List, int) construction time}
   *
   * @exception IllegalArgumentException if {@code endIndex} is
   * invalid
   */
  public final void setEndIndex(final int endIndex) {
    if (endIndex < this.startIndex) {
      throw new IllegalArgumentException("endIndex < startIndex: " + endIndex + " < " + this.startIndex);
    }
    if (this.items == null) {
      throw new IllegalArgumentException("endIndex", new IllegalStateException("items == null; endIndex == " + endIndex));
    }
    this.endIndex = endIndex;
  }

  /**
   * Returns a non-{@code null} {@linkplain Object#clone() clone} of
   * this {@link CaptureGroup}.
   *
   * @return a non-{@code null} {@linkplain Object#clone() clone} of
   * this {@link CaptureGroup}
   */
  @Override
  public final CaptureGroup<T> clone() {
    CaptureGroup<T> clone = null;
    try {
      @SuppressWarnings("unchecked")
      final CaptureGroup<T> temp = (CaptureGroup<T>)super.clone();
      clone = temp;
    } catch (final CloneNotSupportedException severeError) {
      throw (InternalError)new InternalError().initCause(severeError);
    }
    assert clone != null;
    if (this.items != null) {
      clone.setItemList(new ArrayList<T>(this.items));
    }
    return clone;
  }

  /**
   * Returns a hashcode for this {@link CaptureGroup}.
   *
   * @return a hashcode for this {@link CaptureGroup}.
   */
  @Override
  public final int hashCode() {
    int result = 17;

    // startIndex
    result = 37 * result + startIndex;

    // endIndex
    result = 37 * result + endIndex;

    // items ignored on purpose

    return result;
  }

  /**
   * Returns {@code true} if the supplied {@link Object} is equal to
   * this {@link CaptureGroup}. Two {@link CaptureGroup}s are equal if
   * their start and end indices are the same.  Notably, the {@link
   * List} of items supplied {@linkplain #CaptureGroup(List, int) at
   * construction time} is deliberately excluded from consideration.
   *
   * @return {@code true} if the supplied {@link Object} is equal to
   * this {@link CaptureGroup}; {@code false} otherwise
   */
  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (this.getClass().equals(other.getClass())) {
      final CaptureGroup<?> him = (CaptureGroup<?>)other;
        
      // startIndex
      if (this.startIndex != him.startIndex) {
        return false;
      }

      // endIndex
      if (this.endIndex != him.endIndex) {
        return false;
      }

      // items ignored on purpose

      // All tests pass
      return true;

    } else {
      return false;
    }
  }

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link CaptureGroup}.
   *
   * @return a non-{@code null} representation of this {@link
   * CaptureGroup}
   */
  @Override
  public final String toString() {
    return String.format("(%d, %d)", this.startIndex, this.endIndex);
  }

}
