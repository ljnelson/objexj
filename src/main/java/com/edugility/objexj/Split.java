package com.edugility.objexj;

import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Split<T> extends Jump<T> {

  private static final long serialVersionUID = 1L;

  public final int newThreadProgramLocation;

  private static final Pattern OPERAND_PATTERN = Pattern.compile("^\\s*\\d+\\s*,\\s*(\\d+)");

  public Split(String operands) {
    super(operands);
    assert operands != null;
    final Matcher m = OPERAND_PATTERN.matcher(operands);
    assert m != null;
    if (!m.find()) {
      throw new IllegalArgumentException("Bad operands: " + operands);
    } else {
      this.newThreadProgramLocation = Integer.parseInt(m.group(1));
    }
    if (this.newThreadProgramLocation < 0) {
      throw new IllegalArgumentException("this.newThreadProgramLocation < 0: " + this.newThreadProgramLocation);
    }
    if (this.programLocation == this.newThreadProgramLocation) {
      throw new IllegalArgumentException("this.programLocation == this.newThreadProgramLocation: " + this.newThreadProgramLocation);
    }
  }

  public Split(final int programLocation, final int newThreadProgramLocation) {
    super(programLocation);
    assert programLocation >= 0;
    if (newThreadProgramLocation < 0) {
      throw new IllegalArgumentException("newThreadProgramLocation < 0: " + newThreadProgramLocation);
    }
    if (programLocation == newThreadProgramLocation) {
      throw new IllegalArgumentException("programLocation == newThreadProgramLocation: " + programLocation);
    }
    this.newThreadProgramLocation = newThreadProgramLocation;
  }

  @Override
  public final void execute(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    super.execute(context);
    context.scheduleNewThread(this.newThreadProgramLocation);
  }

  @Override
  public String toString() {
    return new StringBuilder(super.toString()).append(", ").append(this.newThreadProgramLocation).toString();
  }


}