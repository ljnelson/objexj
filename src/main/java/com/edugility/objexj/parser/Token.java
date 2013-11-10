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

/**
 * A token produced by a {@link PostfixTokenizer} during the lexing
 * phase of processing a textual objexj pattern representation.
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see PostfixTokenizer
 */
public class Token implements Comparable<Token>, Serializable {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * A type that a {@link Token} may have.
   *
   * @author <a href="http://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   */
  public enum Type {


    /*
     * Atoms.
     */

    /**
     * A {@link Token.Type} identifying a {@link Token} as both an
     * atom and a start-of-input marker.
     */
    BEGIN_ATOM,

    /**
     * A {@link Token.Type} identifying a {@link Token} as both an
     * atom and an end-of-input marker.
     */
    END_ATOM,

    /**
     * A {@link Token.Type} identifying a {@link Token} as both an
     * atom and a filter capable of matching input.
     */
    FILTER(true),

    /**
     * A {@link Token.Type} identifying a {@link Token} as both an
     * atom and a directive to begin saving a capture group.
     */
    START_SAVING(true),

    /**
     * A {@link Token.Type} identifying a {@link Token} as both an
     * atom and a directive to stop saving a particular capture group.
     */
    STOP_SAVING(true),


    /*
     * Sequencing.
     */


    /**
     * A {@link Token.Type} identifying a {@link Token} as an
     * alternation operator.
     */
    ALTERNATION(0),

    /**
     * A {@link Token.Type} identifying a {@link Token} as a
     * catenation operator.
     */
    CATENATION(1),


    /*
     * Grouping.
     */

    
    /**
     * A {@link Token.Type} identifying the start of a capture group.
     * This differs from {@link Token.Type#START_SAVING} in that
     * {@link Token.Type#START_SAVING} is an atom, whereas {@link
     * Token.Type#START_GROUP} is merely an indicator to downstream
     * parsers that operator precedence is being explicitly directed.
     */
    START_GROUP(Integer.MIN_VALUE),

    /**
     * A {@link Token.Type} identifying the end of a capture group.
     * This differs from {@link Token.Type#STOP_SAVING} in that
     * {@link Token.Type#STOP_SAVING} is an atom, whereas {@link
     * Token.Type#STOP_GROUP} is merely an indicator to downstream
     * parsers that operator precedence is being explicitly directed.
     */
    STOP_GROUP(Integer.MIN_VALUE),


    /*
     * Repetition.
     */


    /**
     * A {@link Token.Type} identifying a {@link Token} as a
     * one-or-more operator (usually represented as {@code +}).
     */
    ONE_OR_MORE(2),

    /**
     * A {@link Token.Type} identifying a {@link Token} as a
     * zero-or-more operator (usually represented as {@code *}).
     */
    ZERO_OR_MORE(2),

    /**
     * A {@link Token.Type} identifying a {@link Token} as a
     * zero-or-one operator (usually represented as {@code ?}).
     */
    ZERO_OR_ONE(2);


    /**
     * The precedence of this {@link Token.Type} with respect to other
     * {@link Token.Type}s.
     */
    private final int precedence;

    /**
     * Whether or not this {@link Token.Type} is an operator.
     */
    private final boolean isOperator;

    /**
     * Whether or not {@link Token}s with this {@link Token.Type} take
     * operands or not.
     */
    private final boolean hasValue;

    /**
     * Creates a new {@link Token.Type} that is not an operator, with
     * maximum precedence and that does not take operands.
     */
    private Type() {
      this.isOperator = false;
      this.precedence = Integer.MAX_VALUE;
      this.hasValue = false;
    }

    /**
     * Creates a new {@link Token.Type} that is an operator with the
     * supplied precedence but that does not take operands.
     *
     * @param precedence the operator precedence of {@link Token}s
     * with this {@link Token.Type}
     */
    private Type(final int precedence) {
      this.isOperator = true;
      this.precedence = precedence;
      this.hasValue = false;
    }

    /**
     * Creates a new {@link Token.Type} that is not an operator, with
     * maximum precedence, and which takes a value only if the
     * supplied {@code hasValue} parameter is {@code true}.
     *
     * @param hasValue whether this non-operator {@link Token.Type}
     * takes a value or not
     */
    private Type(final boolean hasValue) {
      this.isOperator = false;
      this.precedence = Integer.MAX_VALUE;
      this.hasValue = hasValue;
    }

    /**
     * Creates a new {@link Token.Type}.
     *
     * @param isOperator whether this {@link Token.Type} is an
     * operator type
     *
     * @param precedence the precedence to use; valid only when {@code
     * isOperator} is {@code true}
     *
     * @param hasValue whether this {@link Token.Type} takes a value
     */
    private Type(final boolean isOperator, final int precedence, final boolean hasValue) {
      this.isOperator = isOperator;
      this.precedence = isOperator ? precedence : Integer.MAX_VALUE;
      this.hasValue = hasValue;
    }

    /**
     * Returns {@code true} if this {@link Token.Type} has a value.
     *
     * @return {@code true} if this {@link Token.Type} has a value
     */
    public final boolean hasValue() {
      return this.hasValue;
    }

    /**
     * Returns {@code true} if this {@link Token.Type} is an operator.
     *
     * @return {@code true} if this {@link Token.Type} is an operator
     */
    public final boolean isOperator() {
      return this.isOperator;
    }

    /**
     * Returns the precedence of this {@link Token.Type}.  A {@link
     * Token.Type} that is {@linkplain #isOperator() not an operator}
     * will always have a precedence of {@link Integer#MAX_VALUE}.
     *
     * @return the precedence of this {@link Token.Type}
     */
    public final int getPrecedence() {
      return this.precedence;
    }

  }
 
  /**
   * The {@link Token.Type} this {@link Token} has.
   *
   * <p>This field is never {@code null}.</p>
   */
  private final Type type;

  /**
   * The kind of filter this {@link Token} represents.
   *
   * <p>This field may be {@code null}</p>.
   */
  private final String filterType;

  /**
   * The value for this {@link Token}.
   *
   * <p>This field may be {@code null}.</p>
   */
  private String value;

  /**
   * Creates a new {@link Token}.
   *
   * @param type the {@link Token.Type} this {@link Token} will have;
   * must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code type} is {@code null}
   */
  public Token(final Type type) {
    this(type, null, null);
  }

  /**
   * Creates a new {@link Token}.
   *
   * @param type the {@link Token.Type} this {@link Token} will have;
   * must not be {@code null}
   *
   * @param value either the type of filter this {@link Token} is (if
   * the supplied {@link Token.Type} is equal to {@link
   * Token.Type#FILTER}), or the operand (if the supplied {@link
   * Token.Type} {@linkplain Token.Type#hasValue() has a value}); may
   * be {@code null} only if the supplied {@link Token.Type} is equal
   * to {@link Token.Type#FILTER}
   *
   * @exception IllegalArgumentException if {@code type} is {@code
   * null}, or if {@link Token.Type#hasValue() type.hasValue()}
   * returns {@code false} and the supplied {@code value} is
   * non-{@code null}
   */
  public Token(final Type type, final String value) {
    super();
    if (type == null) {
      throw new IllegalArgumentException("type", new NullPointerException("type"));
    }
    this.type = type;
    if (Type.FILTER == type) {
      this.filterType = value;
    } else if (!type.hasValue() && value != null) {
      throw new IllegalArgumentException("value");
    } else {
      this.filterType = null;
      this.value = value;
    }
  }

  /**
   * Creates a new {@link Token}.
   *
   * @param type the {@link Token.Type} this {@link Token} will have;
   * must not be {@code null}
   *
   * @param filterType the type of filter this {@link Token} is; must
   * not be {@code null} if the supplied {@code type} is equal to
   * {@link Token.Type#FILTER}
   *
   * @param value the value for this {@link Token}; may be {@code
   * null}; <em>must</em> be {@code null} if the supplied {@link
   * Token.Type} {@linkplain Token.Type#hasValue() indicates} that
   * this {@link Token} does not take a value
   *
   * @exception IllegalArgumentException if {@code type} is {@code
   * null}, or if {@link Token.Type#hasValue() type.hasValue()}
   * returns {@code false} and the supplied {@code value} is
   * non-{@code null}, or if the supplied {@link Token.Type} is not
   * equal to {@link Token.Type#FILTER} and {@code filterType} is
   * non-{@code null}
   */
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

  /**
   * Returns {@code true} if this {@link Token} is an operator.
   *
   * @return {@code true} if this {@link Token} is an operator; {@code
   * false} otherwise
   *
   * @see Token.Type#isOperator()
   */
  public final boolean isOperator() {
    final Type type = this.getType();
    return type != null && type.isOperator();
  }

  /**
   * Returns the {@link Token.Type} that defines this {@link Token}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link Token.Type} that defines this {@link Token};
   * never {@code null}
   */
  public final Type getType() {
    assert this.type != null;
    return this.type;
  }

  /**
   * Returns the precedence of this {@link Token}.
   *
   * @return the precedence of this {@link Token}
   *
   * @exception IllegalStateException if this {@link Token}
   * {@linkplain #isOperator() is not an operator}
   */
  public final int getPrecedence() {
    if (!this.isOperator()) {
      throw new IllegalStateException("!isOperator()");
    }
    final Type type = this.getType();
    assert type != null;
    return type.getPrecedence();
  }

  /**
   * Returns the type of filter this {@link Token} represents.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the type of filter this {@link Token} represents, or
   * {@code null}
   *
   * @see IllegalStateException if this {@link Token} {@linkplain
   * #getType() is not} a {@linkplain Token.Type#FILTER filter}
   */
  public final String getFilterType() {
    if (Type.FILTER != this.getType()) {
      throw new IllegalStateException();
    }
    return this.filterType;
  }

  /**
   * Returns the value or operand for this {@link Token}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the value or operand for this {@link Token}, or {@code
   * null}
   *
   * @exception IllegalStateException if this {@link Token}
   * {@linkplain Token.Type#hasValue() does not have a value}
   */
  public String getValue() {
    final Type type = this.getType();
    assert type != null;
    if (!type.hasValue()) {
      throw new IllegalStateException();
    }
    return this.value;
  }

  /**
   * Sets the value for this {@link Token}.
   *
   * @param value the new value; may be {@code null}
   *
   * @exception IllegalStateException if this {@link Token} does not
   * {@linkplain Token.Type#hasValue() cannot take a value}
   */
  public void setValue(final String value) {
    final Type type = this.getType();
    assert type != null;
    if (!type.hasValue()) {
      throw new IllegalStateException();
    }
    this.value = value;
  }

  /**
   * Compares two tokens based on precedence.  The results of this
   * method are inconsistent with {@linkplain Object#equals(Object)
   * equals}.
   *
   * @param other the other {@link Token}; must not be {@code null}
   *
   * @return {@code 0} if this {@link Token} and the supplied {@link
   * Token} have equal {@linkplain #getPrecedence() precedences},
   * {@code -1} if this {@link Token}'s {@linkplain #getPrecedence()
   * precedence} is less than the supplied {@link Token}'s {@linkplain
   * #getPrecedence() precedence}, or {@code 1} if this {@link
   * Token}'s {@linkplain #getPrecedence() precedence} is greater than
   * the supplied {@link Token}'s {@linkplain #getPrecedence()
   * precedence}
   *
   * @exception IllegalArgumentException if {@code other} is {@code null}
   *
   * @exception IllegalStateException if {@link #isOperator()
   * this.isOperator()} returns {@code false}
   *
   * @see Comparable
   */
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

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link Token}.
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link Token}
   */
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
