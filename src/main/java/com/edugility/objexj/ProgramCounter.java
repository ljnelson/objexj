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

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ProgramCounter<T> implements Cloneable, Serializable, Iterable<Instruction<T>>, Iterator<Instruction<T>> {

  private static final long serialVersionUID = 1L;

  private int index;

  private final Program<T> program;

  public ProgramCounter(final Program<T> program) {
    this(program, 0);
  }

  public ProgramCounter(final ProgramCounter<T> programCounter) {
    this(programCounter == null ? null : programCounter.getProgram(), programCounter == null ? 0 : programCounter.getIndex());
  }

  public ProgramCounter(final Program<T> program, final int index) {
    super();
    if (program == null) {
      throw new IllegalArgumentException("program", new NullPointerException("program == null"));
    }
    this.program = program;
    this.setIndex(index);
  }

  @Override
  public final Iterator<Instruction<T>> iterator() {
    return this.clone();
  }

  @Override
  public final boolean hasNext() {
    return this.canAdvance();
  }

  @Override
  public final void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public final Instruction<T> next() {
    if (this.advance()) {
      return this.getInstruction();
    }
    throw new NoSuchElementException();
  }

  public int getIndex() {
    return this.index;
  }

  public final void setIndex(final int index) {
    if (!this.isValid(index)) {
      throw new IllegalArgumentException("index: " + index);
    }
    this.index = index;
  }

  public final boolean canAdvance() {
    return this.isValid(this.getIndex() + 1);
  }
  
  public boolean advance() {
    final boolean returnValue;
    final int newIndex = this.getIndex() + 1;
    if (this.isValid(newIndex)) {
      this.setIndex(newIndex);
      returnValue = true;
    } else {
      returnValue = false;
    }
    return returnValue;
  }

  public final Instruction<T> getInstruction() {
    if (!this.isValid()) {
      throw new InvalidProgramCounterException();
    }
    return this.getProgram().get(this.getIndex());
  }

  public final boolean isValid() {
    return this.isValid(this.getIndex());
  }

  public final boolean isValid(final int index) {
    return isValid(this.getProgram(), index);
  }

  public static final boolean isValid(final Program<?> program, final int index) {
    return program != null && index >= 0 && index < program.size();
  }

  private final Program<T> getProgram() {
    return this.program;
  }

  @Override
  public ProgramCounter<T> clone() {
    Object clone = null;
    try {
      clone = super.clone();
    } catch (final CloneNotSupportedException neverHappens) {
      throw (InternalError)new InternalError().initCause(neverHappens);
    }
    assert clone instanceof ProgramCounter;
    @SuppressWarnings("unchecked")
    final ProgramCounter<T> returnValue = (ProgramCounter<T>)clone;
    return returnValue;
  }

  public ProgramCounter<T> clone(final int newIndex) {
    if (!this.isValid(newIndex)) {
      throw new IllegalArgumentException("newIndex: " + newIndex);
    }
    final ProgramCounter<T> clone = this.clone();
    assert clone != null;
    clone.setIndex(newIndex);
    return clone;
  }

  @Override
  public int hashCode() {
    int result = 23273 + this.getIndex();
    final Program<T> program = this.getProgram();
    if (program != null) {
      result += program.hashCode();
    }
    return result;
  }
  
  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass().equals(this.getClass())) {
      final ProgramCounter him = (ProgramCounter)other;
      final int myIndex = this.getIndex();
      if (myIndex != him.getIndex()) {
        return false;
      }
      final Program<T> myProgram = this.getProgram();
      if (myProgram == null) {
        if (him.getProgram() != null) {
          return false;
        }
      } else if (!myProgram.equals(him.getProgram())) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final Program<?> program = this.getProgram();
    if (program == null) {
      sb.append("null");
    } else {
      sb.append(program.toString());
    }
    sb.append(" ");
    final int index = this.getIndex();
    sb.append(index);
    
    final Object instruction = program.get(index);
    if (instruction == null) {
      sb.append(" (null)");
    } else {
      sb.append(" (").append(instruction).append(")");
    }

    return sb.toString();
  }

}
