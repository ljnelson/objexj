package com.edugility.objexj.parser;

import java.io.Serializable;

public class Token implements Serializable {

  private static final long serialVersionUID = 1L;

  public enum Type {
    ALTERNATION,
    BEGIN_INPUT,
    CATENATION,
    END_INPUT,
    FILTER(true),
    ONE_OR_MORE,
    START_SAVING,
    STOP_SAVING,
    ZERO_OR_MORE,
    ZERO_OR_ONE;

    private final boolean hasValue;

    private Type() {
      this.hasValue = false;
    }

    private Type(final boolean hasValue) {
      this.hasValue = hasValue;
    }

    public final boolean hasValue() {
      return this.hasValue;
    }
  }
 
  private final Type type;

  private final String filterType;

  private String value;

  public Token(final Type type) {
    this(type, null, null);
  }

  public Token(final Type type, final String filterType) {
    this(type, filterType, null);
  }

  public Token(final Type type, final String filterType, final String value) {
    super();
    if (type == null) {
      this.type = Type.FILTER;
    } else {
      this.type = type;
    }
    if (!type.hasValue() && value != null) {
      throw new IllegalArgumentException("value");
    }
    if (Type.FILTER != type) {
      if (filterType != null) {
        throw new IllegalArgumentException("filterType");
      }
    }
    this.filterType = filterType;
    this.value = value;
  }

  public final Type getType() {
    assert this.type != null;
    return this.type;
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
