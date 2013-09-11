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
package com.edugility.objexj.engine;

import java.io.Serializable;

import java.util.AbstractList; // for javadoc only
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A marriage of a {@link Program} and an index into that {@link
 * Program}, and hence also an {@link Iterator} and {@link Iterable}
 * of {@link Instruction}s.
 *
 * @param <T> the type of {@link Object} processed by the {@link
 * Program} affiliated with {@link ProgramCounter} instances
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Program
 *
 * @see Instruction
 */
public class ProgramCounter<T> implements Cloneable, Serializable, Iterable<Instruction<T>>, Iterator<Instruction<T>> {

  /**
   * The version of this class for {@link Serializable serialization
   * purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The index managed by this {@link ProgramCounter}.
   */
  private int index;

  /**
   * The {@link Program} managed by this {@link ProgramCounter}.
   *
   * <p>This field will never be {@code null}.</p>
   */
  private final Program<T> program;

  /**
   * Creates a new {@link ProgramCounter}.
   *
   * @param program the {@link Program} to be affiliated with this
   * {@link ProgramCounter}; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code program} is {@code
   * null}
   */
  public ProgramCounter(final Program<T> program) {
    this(program, 0);
  }

  /**
   * Creates a new {@link ProgramCounter}.
   *
   * @param programCounter a {@link ProgramCounter} whose {@linkplain
   * #getProgram() affiliated <code>Program</code>} will be used and
   * whose {@linkplain #getIndex() index} will also be used; must not
   * be {@code null}
   *
   * @exception IllegalArgumentException if {@code programCounter} is
   * {@code null} or {@linkplain #getProgram() somehow has a
   * <code>Program</code>} that is {@code null}
   */
  public ProgramCounter(final ProgramCounter<T> programCounter) {
    this(programCounter == null ? null : programCounter.getProgram(), programCounter == null ? 0 : programCounter.getIndex());
  }

  /**
   * Creates a new {@link ProgramCounter}.
   *
   * @param program the {@link Program} to be affiliated with this
   * {@link ProgramCounter}; must not be {@code null}
   *
   * @param index the index to be {@linkplain #setIndex(int)
   * affiliated with} this {@link ProgramCounter}
   *
   * @exception IllegalArgumentException if {@code program} is {@code
   * null}, or if the supplied {@code index} {@linkplain #isValid(int)
   * is not valid}
   *
   * @see #isValid(int)
   *
   * @see #setIndex(int)
   */
  public ProgramCounter(final Program<T> program, final int index) {
    super();
    if (program == null) {
      throw new IllegalArgumentException("program", new NullPointerException("program == null"));
    }
    this.program = program;
    this.setIndex(index);
  }


  /**
   * Returns a {@linkplain #clone() clone} of this {@link
   * ProgramCounter} which is itself an {@link Iterator} of {@link
   * Instruction}s.
   *
   * @return a non-{@code null} {@link Iterator} of {@link
   * Instruction}s not identical to this {@link ProgramCounter}
   */
  @Override
  public final Iterator<Instruction<T>> iterator() {
    return this.clone();
  }

  /**
   * Returns the result of invoking the {@link #canAdvance()} method.
   *
   * @return the result of invoking the {@link #canAdvance()} method
   */
  @Override
  public final boolean hasNext() {
    return this.canAdvance();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   */
  @Override
  public final void remove() {
    throw new UnsupportedOperationException();
  }

  /**
   * Calls the {@link #advance()} method, and if it returns {@code
   * true}, returns the result of calling the {@link
   * #getInstruction()} method.  Otherwise throws a {@link
   * NoSuchElementException}.
   *
   * @return an {@link Instruction}, or {@code null}
   *
   * @exception NoSuchElementException if there are no further {@link
   * Instruction}s
   *
   * @see #advance()
   *
   * @see #getInstruction()
   *
   * @see #hasNext()
   */
  @Override
  public final Instruction<T> next() {
    if (this.advance()) {
      return this.getInstruction();
    }
    throw new NoSuchElementException();
  }

  /**
   * Returns the index managed by this {@link ProgramCounter}.
   *
   * <p>The index returned by this method is guaranteed to be
   * {@linkplain #isValid(int) valid}.  Subclasses who choose to
   * override this method must preserve these semantics.</p>
   *
   * @return the (valid) index managed by this {@link ProgramCounter}
   *
   * @see #setIndex(int)
   */
  public int getIndex() {
    return this.index;
  }

  /**
   * Sets the index managed by this {@link ProgramCounter} to the
   * supplied {@code index}, provided that it is {@linkplain
   * #isValid(int) valid}.
   *
   * @param index the new index; must be {@linkplain #isValid(int)
   * valid}
   *
   * @exception IllegalArgumentException if {@code index} {@linkplain
   * #isValid(int) not valid}
   */
  public final void setIndex(final int index) {
    if (!this.isValid(index)) {
      throw new IllegalArgumentException("index: " + index);
    }
    this.index = index;
  }

  /**
   * Returns {@code true} if this {@link ProgramCounter} can advance
   * its internal index by one.
   *
   * @return {@code true} if the {@linkplain #getIndex() current
   * index} plus one {@linkplain #isValid(int) is valid}; {@code
   * false} otherwise
   */
  public final boolean canAdvance() {
    return this.isValid(this.getIndex() + 1);
  }
  
  /**
   * Attempts to advance the index and returns {@code true} if the
   * attempt was successful.
   *
   * @return {@code true} if this {@link ProgramCounter} was in a
   * state where it {@linkplain #canAdvance() could advance} and did;
   * {@code false} otherwise
   */
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

  /**
   * Returns the {@link Instruction} located at the {@linkplain
   * #getIndex() current index}, or {@code null} if no such {@link
   * Instruction} was found.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the {@link Instruction} at the {@linkplain #getIndex()
   * current index}, or {@code null}
   *
   * @exception InvalidProgramCounterException if this {@link
   * ProgramCounter} {@linkplain #isValid() is not valid}
   */
  public final Instruction<T> getInstruction() {
    if (!this.isValid()) {
      throw new InvalidProgramCounterException();
    }
    return this.getProgram().get(this.getIndex());
  }

  /**
   * Returns {@code true} if this {@link ProgramCounter} is valid.
   *
   * @return {@code true} if the {@link #isValid(int)} method returns
   * {@code true} when supplied with the {@link #getIndex() current
   * index}; {@code false} otherwise
   */
  public final boolean isValid() {
    return this.isValid(this.getIndex());
  }

  /**
   * Returns {@code true} if the {@link #isValid(Program, int)} method
   * returns {@code true} when supplied with the {@linkplain
   * #getProgram() current <code>Program</code>} and the supplied
   * {@code index}.
   *
   * @return {@code true} if the {@link #isValid(Program, int)} method
   * returns {@code true} when supplied with the {@linkplain
   * #getProgram() current <code>Program</code>} and the supplied
   * {@code index}
   */
  public final boolean isValid(final int index) {
    return isValid(this.getProgram(), index);
  }

  /**
   * Returns {@code true} if the supplied {@link Program} and index
   * represent a valid combination.
   *
   * <p>Effectively this means the supplied {@link Program}:</p>
   *
   * <ul>
   *
   * <li>Must not be {@code null}.</li>
   *
   * </ul>
   *
   * <p>The supplied {@code index}:</p>
   *
   * <ul>
   *
   * <li>Must be greater than or equal to {@code 0}</li>
   *
   * <li>Must be less than the {@linkplain AbstractList#size() size of
   * the supplied <code>Program</code>}</li>
   *
   * </ul>
   *
   * @param program the {@link Program} to test; may be {@code null}
   * in which case {@code false} will be returned
   *
   * @param index the index to test; may be less than {@code 0} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if the supplied {@link Program} and {@code
   * index} represent a valid combination; {@code false} otherwise
   */
  public static final boolean isValid(final Program<?> program, final int index) {
    return program != null && index >= 0 && index < program.size();
  }
   
  /**
   * Returns the {@link Program} affiliated with this {@link
   * ProgramCounter}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a non-{@code null} {@link Program}
   */
  private final Program<T> getProgram() {
    return this.program;
  }

  /**
   * {@linkplain Object#clone() Clones} this {@link ProgramCounter}
   * and returns the clone.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a non-{@code null} {@link ProgramCounter} not identical
   * to this {@link ProgramCounter} but {@linkplain #equals(Object)
   * equal to it}
   *
   * @see Object#clone()
   */
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

  /**
   * {@linkplain #clone() Clones} this {@link ProgramCounter} and then
   * {@linkplain #setIndex(int) sets a new index on it}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a non-{@code null} {@link ProgramCounter} not identical
   * to this {@link ProgramCounter} but {@linkplain #equals(Object)
   * equal to it} except for its associated {@linkplain #getIndex()
   * index}
   */
  public ProgramCounter<T> clone(final int newIndex) {
    if (!this.isValid(newIndex)) {
      throw new IllegalArgumentException("newIndex: " + newIndex);
    }
    final ProgramCounter<T> clone = this.clone();
    assert clone != null;
    clone.setIndex(newIndex);
    return clone;
  }

  /**
   * Returns a hashcode for this {@link ProgramCounter}.
   *
   * @return a hashcode for this {@link ProgramCounter}
   */
  @Override
  public int hashCode() {
    int result = 23273 + this.getIndex();
    final Program<T> program = this.getProgram();
    if (program != null) {
      result += program.hashCode();
    }
    return result;
  }

  /**
   * Returns {@code true} if the supplied {@link Object} is equal to
   * this {@link ProgramCounter}; {@code false} otherwise
   *
   * @param other the {@link Object} to test; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if the supplied {@link Object} is equal to
   * this {@link ProgramCounter}; {@code false} otherwise
   */
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

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link ProgramCounter}.
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link ProgramCounter}
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final Program<?> program = this.getProgram();
    if (program == null) {
      sb.append("null");
    } else {
      sb.append(program.toString(this.getIndex()));
    }
    /*
    sb.append(" ");
    final int index = this.getIndex();
    sb.append(index);
    
    final Object instruction = program.get(index);
    if (instruction == null) {
      sb.append(" (null)");
    } else {
      sb.append(" (").append(instruction).append(")");
      }
    */

    return sb.toString();
  }

}
