package com.edugility.objexj;

import java.io.Serializable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Instruction<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final Pattern LINE_PATTERN = Pattern.compile("^([^\\s]+)(?:\\s+?(.*))?$");

  protected Instruction() {
    super();
  }

  protected Class<?> loadClass(final String className) throws ClassNotFoundException {
    if (className == null) {
      throw new IllegalArgumentException("className", new NullPointerException("className"));
    }
    return java.lang.Thread.currentThread().getContextClassLoader().loadClass(className);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName();
  }

  public static final <T> Instruction<T> valueOf(final String line) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
    if (line == null) {
      throw new IllegalArgumentException("line", new NullPointerException("line"));
    }
    final Matcher m = LINE_PATTERN.matcher(line);
    assert m != null;
    if (!m.matches()) {
      throw new IllegalArgumentException("Bad instruction line: " + line);
    }
    final String command = String.format("com.edugility.objexj.%s", m.group(1));
    assert command != null;
    
    final Class<?> instructionClass = java.lang.Thread.currentThread().getContextClassLoader().loadClass(command);
    assert instructionClass != null;
    if (!Instruction.class.isAssignableFrom(instructionClass)) {
      throw new IllegalArgumentException("bad instruction: " + command);
    }
    
    String operands = m.group(2);
    if (operands == null) {
      operands = "";
    } else {
      operands = operands.trim();
    }

    Object instruction = null;
    final Constructor<?> c;
    if (operands.isEmpty()) {
      c = null;
      instruction = instructionClass.newInstance();
    } else {
      c = instructionClass.getConstructor(String.class);
      assert c != null;
      instruction = c.newInstance(operands);
    }
    @SuppressWarnings("unchecked")
    final Instruction<T> temp = (Instruction<T>)instruction;
    return temp;
  }

  public abstract void execute(final InstructionContext<T> context);

}