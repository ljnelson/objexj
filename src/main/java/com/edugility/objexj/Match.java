package com.edugility.objexj;

public class Match<T> extends Instruction<T> {

  private static final long serialVersionUID = 1L;

  public Match() {
    super();
  }

  @Override
  public final void execute(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context == null");
    }
    context.match();
  }


}