package com.edugility.objexj;

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
    if (items == null) {
      throw new IllegalStateException();
    }
    return Collections.unmodifiableList(items.subList(this.startIndex, this.endIndex < 0 ? items.size() : this.endIndex));
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
      throw new IllegalStateException();
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
    clone.setItems(new ArrayList<T>(this.items));
    return clone;
  }

  @Override
  public final int hashCode() {
    int result = 17;
    result = 37 * result + startIndex;
    result = 37 * result + endIndex;
    // this.items ignored on purpose
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

      // this.items ignored on purpose

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