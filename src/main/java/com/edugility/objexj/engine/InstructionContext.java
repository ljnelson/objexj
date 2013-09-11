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

import java.util.concurrent.atomic.AtomicInteger;

import java.util.List;
import java.util.Map;

/**
 * A context inside of which an {@link Instruction} is {@linkplain
 * Instruction#execute(InstructionContext) executed}.
 *
 * <p>Structurally speaking, an {@link InstructionContext} is a
 * fa&ccedil;ade around a {@link Thread}, exposing only those methods
 * that are needed by {@link Instruction}s.</p>
 *
 * @param <T> the type of object returned by the {@link #read()}
 * method
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Thread
 */
public class InstructionContext<T> implements Serializable {

  /**
   * The version of this class for {@link Serializable serialization
   * purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * An {@link AtomicInteger} used to help generate identifiers for
   * new {@link Thread}s.
   *
   * <p>This field is never {@code null}.</p>
   */
  private static final AtomicInteger idGenerator = new AtomicInteger(1);

  /**
   * The {@link Thread} this {@link InstructionContext} wraps.
   *
   * <p>This field is never {@code null}.</p>
   */
  private final Thread<T> thread;

  /**
   * Creates a new {@link InstructionContext}.
   *
   * @param thread the {@link Thread} the new {@link
   * InstructionContext} will delegate to; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code thread} is {@code
   * null}
   */
  InstructionContext(final Thread<T> thread) {
    super();
    if (thread == null) {
      throw new IllegalArgumentException("thread == null");
    }
    this.thread = thread;
  }

  /**
   * Returns this {@link InstructionContext}'s associated non-{@code
   * null} {@link Map} of variables.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * <p>The {@link Map} returned by this method must be mutable.</p>
   *
   * <p>The {@link Map} this method returns is returned by reference,
   * so modifications made to it will be visible to consumers of the
   * return value of subsequent invocations of this method.
   * Subclasses must preserve these semantics if they override this
   * method.</p>
   *
   * @return a non-{@code null}, mutable {@link Map} of variables
   *
   * @see Thread#getVariables()
   */
  public Map<Object, Object> getVariables() {
    return this.thread.getVariables();
  }

  /**
   * Creates a new {@link Thread} and schedules it for execution
   * beginning at the supplied {@code programCounterIndex}.
   *
   * <p>The supplied {@code programCounterIndex} may be an absolute or
   * relative index&mdash;this is governed by the {@code relative}
   * parameter.</p>
   *
   * @param programCounterIndex where in the new {@link Thread}'s
   * {@link Program} execution should begin
   *
   * @param relative whether the {@code programCounterIndex} is an
   * absolute index or an index relative to this {@link
   * InstructionContext}'s current {@link Program}'s index
   *
   * @return {@code true} if a new {@link Thread} was actually
   * scheduled; {@code false} otherwise
   * 
   * @exception IllegalArgumentException if the {@code
   * programCounterIndex} was invalid
   *
   * @see Thread#newThread(Object, int, boolean)
   *
   * @see Thread#schedule(Thread)
   */
  public final boolean scheduleNewThread(final int programCounterIndex, final boolean relative) {
    return this.thread.schedule(this.thread.newThread(String.format("T%d", idGenerator.getAndIncrement()), programCounterIndex, relative));
  }

  /**
   * Advances this {@link InstructionContext} to the supplied {@code
   * programCounter} location.
   *
   * @param programCounter a representation of the new program counter
   *
   * @param relative whether {@code programCounter} is to be
   * interpreted relative to its current notional location or as an
   * absolute index
   *
   * @return {@code true} if the supplied {@code programCounter} and
   * {@code relative} parameters taken together represent a valid
   * location and the underlying {@link Thread} remains viable; {@code
   * false} otherwise
   *
   * @see Thread#jump(int, boolean)
   */
  public final boolean jump(final int programCounter, final boolean relative) {
    return this.thread.jump(programCounter, relative);
  }

  /**
   * Saves the current value of the {@linkplain #advanceItemPointer()
   * item pointer} so that ultimately a capture group may be recorded.
   *
   * @param key the key under which the group should be saved; must
   * not be {@code null}
   *
   * @exception IllegalArgumentException if {@code key} is {@code
   * null}
   *
   * @exception IllegalStateException if {@link #die()} has been
   * previously called (if the underlying {@link Thread} is
   * {@linkplain Thread#isViable() not viable})
   *
   * @see Thread#save(Object)
   *
   * @see Thread#getSubmatches()
   */
  public final void save(final Object key) {
    this.thread.save(key);
  }

  /**
   * Completes capturing a portion of the input starting with the
   * {@linkplain #save(Object) previously saved item pointer location}
   * and ending with the exclusive current {@linkplain
   * #advanceItemPointer() item pointer} location.
   *
   * @param key the key identifying the starting item pointer
   * location; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code key} is {@code
   * null}
   *
   * @exception IllegalStateException if {@link #die()} has been
   * previously called (if the underlying {@link Thread} is
   * {@linkplain Thread#isViable() not viable})
   *
   * @see Thread#stop(Object)
   *
   * @see Thread#getSubmatches()
   */
  public final void stop(final Object key) {
    this.thread.stop(key);
  }

  /**
   * Returns {@code true} if the next call to {@link #read()} will
   * read the first item in the input.
   *
   * <p>As an edge case, if the input is {@code null} or empty, this
   * method will return {@code true}.</p>
   *
   * @return {@code true} if the next call to {@link #read()} will
   * read from the beginning; {@code false} otherwise
   *
   * @exception IllegalStateException if the underlying {@link Thread}
   * {@linkplain Thread#getState() has a state} that is {@link
   * Thread.State#DEAD}
   *
   * @see Thread#atStart()
   */
  public final boolean atStart() {
    return this.thread.atStart();
  }

  /**
   * Returns {@code true} if the last call to {@link #read()} read the
   * last item in the input.
   *
   * @return {@code true} if the last call to {@link #read()} read the
   * last item in the input; {@code false} otherwise
   *
   * @exception IllegalStateException if the underlying {@link Thread}
   * {@linkplain Thread#getState() has a state} that is {@link
   * Thread.State#DEAD}
   *
   * @see Thread#atEnd()
   */
  public final boolean atEnd() {
    return this.thread.atEnd();
  }

  /**
   * Advances the associated program counter by one instruction.
   * Returns {@code true} if advancement was possible, or {@code
   * false} if it was not and if the underlying {@link Thread} is no
   * longer {@linkplain Thread#isViable() viable}.
   *
   * @return {@code true} if advancement succeeded; {@code false}
   * otherwise
   *
   * @exception IllegalStateException if the underlying {@link Thread}
   * {@linkplain Thread#getState() has a state} that is {@link
   * Thread.State#DEAD}
   *
   * @see Thread#advanceProgramCounter()
   */
  public final boolean advanceProgramCounter() {
    return this.thread.advanceProgramCounter();
  }

  /**
   * Advances the associated item pointer forward by one item.
   *
   * @return {@code true} if advancement succeeded and the resulting
   * item pointer {@link Thread#isValidItemPointer(int) is valid};
   * {@code false} otherwise
   *
   * @exception IllegalStateException if the underlying {@link Thread}
   * {@linkplain Thread#getState() has a state} that is {@link
   * Thread.State#DEAD}
   *
   * @see Thread#advanceItemPointer()
   */
  public final boolean advanceItemPointer() {
    return this.thread.advanceItemPointer();
  }

  /**
   * Returns {@code true} if this {@link InstructionContext} can
   * {@linkplain #read() read an item in the input}.
   *
   * @return {@code true} if this {@link InstructionContext} can
   * {@linkplain #read() read}; {@code false} otherwise
   *
   * @see Thread#canRead()
   */
  public final boolean canRead() {
    return this.thread.canRead();
  }

  /**
   * Reads the current item in the input and returns it.  Subsequent
   * invocations of this method will return the same {@link Object}.
   *
   * <p>This method may return {@code null}.</code>
   *
   * @return the current item in the input; may be {@code null}
   *
   * @exception IllegalStateException if this {@link
   * InstructionContext} {@linkplain #canRead() cannot read}
   *
   * @see Thread#read()
   */
  public final T read() {
    return this.thread.read();
  }

  /**
   * Causes this {@link InstructionContext} to become invalid.
   *
   * @exception IllegalStateException if {@link #match()} was
   * previously called
   *
   * @see Thread#die()
   */
  public final void die() {
    this.thread.die();
  }

  /**
   * Records the fact that a match has occurred.
   *
   * @exception IllegalStateException if {@link #die()} was previously
   * called
   *
   * @see Thread#match()
   */
  public final void match() {
    this.thread.match();
  }

}
