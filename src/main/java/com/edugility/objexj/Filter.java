package com.edugility.objexj;

public class Filter<T> extends Instruction<T> {

  private static final long serialVersionUID = 1L;

  public Filter() {
    super();
  }

  @Override
  public final void execute(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context", new NullPointerException("context == null"));
    }
    if (this.accept(context)) {
      if (context.advanceProgramCounter()) {
        context.advanceItemPointer();
      } else {
        assert context.isDead();
      }
      // no need to schedule it; it is already running!
    } else {
      context.die();
    }
  }

  public boolean accept(final InstructionContext<T> context) {
    return context != null && context.canRead() && context.read() != null;
  }

}