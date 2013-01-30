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
package com.edugility.objexj.parser;

import java.io.Serializable;

public class Token implements Comparable<Token>, Serializable {

  private static final long serialVersionUID = 1L;

  public enum Type {
    // Deprecated types
    @Deprecated
    END_INPUT(true, 1, false), // XXX actually an atom
    @Deprecated
    BEGIN_INPUT(true, 1, false), // XXX actually an atom

    // Atoms
    BEGIN_ATOM,
    END_ATOM,
    FILTER(true),
    START_SAVING(true),
    STOP_SAVING(true),

    // Sequencing
    ALTERNATION(0),
    CATENATION(1),

    // Grouping
    START_GROUP(Integer.MIN_VALUE),
    STOP_GROUP(Integer.MIN_VALUE),

    // Repetition
    ONE_OR_MORE(2),
    ZERO_OR_MORE(2),
    ZERO_OR_ONE(2);

    private final int precedence;

    private final boolean isOperator;

    private final boolean hasValue;

    private Type() {
      this.isOperator = false;
      this.precedence = Integer.MAX_VALUE;
      this.hasValue = false;
    }

    private Type(final int precedence) {
      this.isOperator = true;
      this.precedence = precedence;
      this.hasValue = false;
    }

    private Type(final boolean hasValue) {
      this.isOperator = false;
      this.precedence = Integer.MAX_VALUE;
      this.hasValue = hasValue;
    }

    private Type(final boolean isOperator, final int precedence, final boolean hasValue) {
      this.isOperator = isOperator;
      this.precedence = isOperator ? precedence : Integer.MAX_VALUE;
      this.hasValue = hasValue;
    }

    public final boolean hasValue() {
      return this.hasValue;
    }

    public final boolean isOperator() {
      return this.isOperator;
    }

    public final int getPrecedence() {
      return this.precedence;
    }

    public static final int getPrecedence(final int character) {
      return -1; // TODO implement
    }

  }
 
  private final Type type;

  private final String filterType;

  private String value;

  public Token(final Type type) {
    this(type, null, null);
  }

  public Token(final Type type, final String x) {
    super();
    if (type == null) {
      throw new IllegalArgumentException("type", new NullPointerException("type"));
    }
    this.type = type;
    if (type.FILTER == type) {
      this.filterType = x;
    } else if (!type.hasValue() && x != null) {
      throw new IllegalArgumentException("x");
    } else {
      this.filterType = null;
      this.value = x;
    }
  }

  public Token(final Type type, final String filterType, final String value) {
    super();
    if (type == null) {
      throw new IllegalArgumentException("type", new NullPointerException("type"));
    }
    if (!type.hasValue() && value != null) {
      throw new IllegalArgumentException("value");
    }
    if (Type.FILTER != type) {
      if (filterType != null) {
        throw new IllegalArgumentException("filterType");
      }
    }
    this.type = type;
    this.filterType = filterType;
    this.value = value;
  }

  public final boolean isOperator() {
    final Type type = this.getType();
    return type != null && type.isOperator();
  }

  public final Type getType() {
    assert this.type != null;
    return this.type;
  }

  public final int getPrecedence() {
    if (!this.isOperator()) {
      throw new IllegalStateException("!isOperator()");
    }
    final Type type = this.getType();
    assert type != null;
    return type.getPrecedence();
  }

  public final String getFilterType() {
    if (Type.FILTER != this.getType()) {
      throw new IllegalStateException();
    }
    return this.filterType;
  }

  public String getValue() {
    final Type type = this.getType();
    assert type != null;
    if (!type.hasValue()) {
      throw new IllegalStateException();
    }
    return this.value;
  }

  public void setValue(final String value) {
    final Type type = this.getType();
    assert type != null;
    if (!type.hasValue()) {
      throw new IllegalStateException();
    }
    this.value = value;
  }

  @Override
  public int compareTo(final Token other) {
    if (other == null) {
      throw new IllegalArgumentException("other", new NullPointerException("other"));
    } else if (!other.isOperator()) {
      throw new IllegalArgumentException("!other.isOperator()");
    } else if (!this.isOperator()) {
      throw new IllegalStateException("!this.isOperator()");
    }
    final int myPrecedence = this.getPrecedence();
    final int hisPrecedence = other.getPrecedence();
    if (myPrecedence > hisPrecedence) {
      return 1;
    } else if (myPrecedence == hisPrecedence) {
      return 0;
    } else {
      assert myPrecedence < hisPrecedence;
      return -1;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final Type type = this.getType();
    assert type != null;
    sb.append(type.name());
    if (type == Type.FILTER) {
      sb.append(" ").append(this.getFilterType());
    }
    if (type.hasValue()) {
      sb.append(" ").append(this.getValue());
    }
    return sb.toString();
  }

}
