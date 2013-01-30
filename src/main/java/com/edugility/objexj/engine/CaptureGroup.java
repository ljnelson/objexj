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

public final class CaptureGroup<T> implements Cloneable, Serializable {

  private static final long serialVersionUID = 1L;

  private final int startIndex;

  private int endIndex;

  private List<T> items;

  public CaptureGroup(final List<T> items, final int startIndex) {
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

  public final List<T> getItems() {
    if (this.items == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(items.subList(this.startIndex, this.endIndex < 0 ? items.size() : this.endIndex));
    }
  }

  private final void setItems(final List<T> items) {
    if (items == null) {
      throw new IllegalArgumentException("items", new NullPointerException("items == null"));
    }
    this.items = items;
  }

  public final void setEndIndex(final int endIndex) {
    if (endIndex < this.startIndex) {
      throw new IllegalArgumentException("endIndex < startIndex: " + endIndex + " < " + this.startIndex);
    }
    if (this.items == null) {
      throw new IllegalArgumentException("endIndex", new IllegalStateException("items == null; endIndex == " + endIndex));
    }
    this.endIndex = endIndex;
  }

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
      clone.setItems(new ArrayList<T>(this.items));
    }
    return clone;
  }

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

  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (this.getClass().equals(other.getClass())) {
      final CaptureGroup him = (CaptureGroup)other;
        
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

  @Override
  public final String toString() {
    return String.format("(%d, %d)", this.startIndex, this.endIndex);
  }

}
