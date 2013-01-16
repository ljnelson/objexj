package com.edugility.objexj;

import java.util.List;

public class Save<T> extends Instruction<T> {

  private static final long serialVersionUID = 1L;

  private final Object key;

  public Save(final Object key) {
    super();
    this.key = key;
  }

  @Override
  public final void execute(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    context.save(this.key);
    // TODO: advance()? advance just the program counter?
  }

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