package com.edugility.objexj;

import java.util.List;

public class EndInput<T> extends Instruction<T> {

  private static final long serialVersionUID = 1L;

  public EndInput() {
    super();
  }

  @Override
  public final void execute(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    if (context.atEnd()) {
      context.advanceProgramCounter();
    } else {
      context.die();
    }
  }


}