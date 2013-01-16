package com.edugility.objexj;

import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jump<T> extends Instruction<T> {

  private static final long serialVersionUID = 1L;

  private static final Pattern OPERAND_PATTERN = Pattern.compile("^\\s*(\\d+)");

  public final int programLocation;
  
  public Jump(String operand) {
    super();
    if (operand == null) {
      throw new IllegalArgumentException("operand", new NullPointerException("operand"));
    }
    final Matcher m = OPERAND_PATTERN.matcher(operand);
    if (m.find()) {
      this.programLocation = Integer.parseInt(m.group(1));
    } else {
      throw new IllegalArgumentException("Bad operand: " + operand);
    }
    if (this.programLocation < 0) {
      throw new IllegalArgumentException("this.programLocation < 0: " + this.programLocation);
    }
  }

  public Jump(final int programLocation) {
    super();
    if (programLocation < 0) {
      throw new IllegalArgumentException("programLocation < 0: " + programLocation);
    }
    this.programLocation = programLocation;
  }

  @Override
  public void execute(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context");
    }
    context.jump(this.programLocation);
  }

  @Override
  public String toString() {
    return new StringBuilder(super.toString()).append(" ").append(this.programLocation).toString();
  }

}