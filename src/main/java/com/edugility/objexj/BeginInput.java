package com.edugility.objexj;

import java.util.List;

public class BeginInput<T> extends Instruction<T> {

  private static final long serialVersionUID = 1L;

  public BeginInput() {
    super();
  }

  @Override
  public final void execute(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    if (context.atStart()) {
      context.advanceProgramCounter();
    } else {
      context.die();
    }
  }


}