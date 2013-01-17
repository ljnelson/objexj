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

  public abstract void execute(final InstructionContext<T> context);

  protected Class<?> loadClass(final String className) throws ClassNotFoundException {
    if (className == null) {
      throw new IllegalArgumentException("className", new NullPointerException("className"));
    }
    return java.lang.Thread.currentThread().getContextClassLoader().loadClass(className);
  }

  @Override
  public String toString() {
    final String simpleName = this.getClass().getSimpleName();
    return String.format("%s%s", simpleName.substring(0, 1).toLowerCase(), simpleName.substring(1));
  }

  
  /*
   * Static methods.
   */


  public static final <T> Instruction<T> valueOf(final String line) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
    if (line == null) {
      throw new IllegalArgumentException("line", new NullPointerException("line"));
    }
    final Matcher m = LINE_PATTERN.matcher(line);
    assert m != null;
    if (!m.matches()) {
      throw new IllegalArgumentException("Bad instruction line: " + line);
    }

    final String rawCommand = m.group(1);
    assert rawCommand != null;
    assert rawCommand.length() >= 2;

    final String command = String.format("com.edugility.objexj.%s%s", rawCommand.substring(0, 1).toUpperCase(), rawCommand.substring(1));
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

}
