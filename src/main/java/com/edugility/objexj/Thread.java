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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A virtual thread for running {@link Instruction}s from a regular
 * expression {@link Program}.
 *
 * A {@link Thread} may be in several states:
 *
 * <ul>
 *
 * <li><strong>{@linkplain State#VIABLE Viable}</strong>.  A {@link
 * Thread} is <em>viable</em> when its {@linkplain #getState() state}
 * is {@link State#VIABLE VIABLE} and when it has a valid {@linkplain
 * ProgramCounter program counter}.</li>
 *
 * <li><strong>{@linkplain State#DEAD Dead}</strong>.  A {@link
 * Thread} is <em>{@linkplain State#DEAD dead}</em> when its
 * {@linkplain #getState() state} is {@link State#DEAD DEAD}.  Death
 * occurs when the {@link #die()} method is invoked.  Internally,
 * {@link #die()} is invoked in some cases by the {@link #step()}
 * method and the {@link #advanceProgramCounter()} method.  A dead
 * {@link Thread} will return {@code false} from the following
 * methods:
 *
 * <ul>
 *
 * <li>{@link #canRead()}</li>
 *
 * <li>{@link #isViable()}</li>
 *
 * </ul>
 *
 * Additionally, a dead {@link Thread} will throw an {@link
 * IllegalStateException} from any other method that is invoked.</li>
 *
 * <li><strong>{@linkplain State#MATCH Match}</strong>.  A {@link
 * Thread} is a <em>match</em> when its {@link #getState()} method
 * returns a {@link State} that is equal to {@link State#MATCH MATCH}.
 * A {@link Thread} is placed in the {@link State#MATCH MATCH} state
 * by invoking the {@link #match()} method.  Like a {@linkplain
 * State#DEAD dead} {@link Thread}, a {@link Thread} in the {@link
 * State#MATCH MATCH} state effectively cannot do anything.</li>
 *
 * </ul>
 */
public final class Thread<T> implements Runnable, ThreadScheduler<T> {

  public static final int VALID_NO_INPUT_POINTER = Integer.MAX_VALUE;

  private static final int INVALID_INPUT_POINTER = -1;

  public enum State {
    CONSTRUCTING, MATCH, DEAD, VIABLE
  }

  private final ThreadScheduler<T> threadScheduler;

  private final List<T> items;

  private int itemPointer;

  private final ProgramCounter<T> programCounter;

  private State state;

  private final Object id;

  private final Map<Object, CaptureGroup<T>> captureGroups;

  Thread(final Object id, final ProgramCounter<T> programCounter, final List<T> items, final int itemPointer, final ThreadScheduler<T> threadScheduler) {
    this(id, programCounter, items, itemPointer, null, threadScheduler);
  }

  Thread(final Object id, final ProgramCounter<T> programCounter, final List<T> items, final int itemPointer, final Map<Object, CaptureGroup<T>> captureGroups, final ThreadScheduler<T> threadScheduler) {
    super();
    this.state = State.CONSTRUCTING;
    this.id = id;
    if (captureGroups == null) {
      this.captureGroups = new HashMap<Object, CaptureGroup<T>>();
    } else {
      this.captureGroups = captureGroups;
    }
    this.items = items;

    if (programCounter == null) {
      throw new IllegalArgumentException("programCounter == null");
    }
    this.programCounter = programCounter;

    if (threadScheduler == null) {
      throw new IllegalArgumentException("threadScheduler == null");
    }
    this.threadScheduler = threadScheduler;

    /*
     * For checking the incoming itemPointer, we reject truly insane
     * values (like huge integers when a small items List is passed
     * in), but we accept an itemPointer that is "just off the end" of
     * the incoming List.
     *
     * It is certainly the case that if the caller decided to try to
     * read something with, say, an itemPointer of 0 from an empty
     * list, the read would fail.  But not all Threads run
     * Instructions that perform reads.
     */

    if (this.items == null || this.items.isEmpty()) {
      if (itemPointer != VALID_NO_INPUT_POINTER && itemPointer != 0) {
        throw new IllegalArgumentException("An empty or null input list requires an inputPointer of 0 or VALID_NO_INPUT_POINTER");
      }
    } else if (itemPointer < 0) {
      throw new IllegalArgumentException("itemPointer < 0: " + itemPointer);
    } else if (itemPointer == this.items.size()) {
      // WARN: the next read will fail
    } else if (itemPointer > this.items.size()) {
      throw new IllegalArgumentException("itemPointer > items.size(): " + itemPointer + " > " + this.items.size());
    }
    this.itemPointer = itemPointer;

    this.state = State.VIABLE;
  }

  public final Object getId() {
    return this.id;
  }

  public Map<Object, List<T>> getSubmatches() {
    if (State.MATCH != this.getState()) {
      throw new IllegalStateException();
    }
    Map<Object, List<T>> returnValue = null;
    final Set<Entry<Object, CaptureGroup<T>>> entries = this.captureGroups.entrySet();
    if (entries != null) {
      for (final Entry<Object, CaptureGroup<T>> entry : entries) {
        assert entry != null;
        final CaptureGroup<T> cg = entry.getValue();
        if (cg != null) {
          final Object key = entry.getKey();
          if (returnValue == null) {
            returnValue = new HashMap<Object, List<T>>();
          }
          returnValue.put(key, cg.getItems());
        }
      }
    }
    if (returnValue == null) {
      returnValue = Collections.emptyMap();
    } else {
      returnValue = Collections.unmodifiableMap(returnValue);
    }
    return returnValue;
  }

  public final void save(final Object key) {
    if (key == null) {
      throw new IllegalArgumentException("key", new NullPointerException("key"));
    }
    this.ensureViable();
    final CaptureGroup<T> cg = this.captureGroups.get(key);
    if (cg == null) {
      this.captureGroups.put(key, new CaptureGroup<T>(this.items, this.itemPointer));
    } else {
      cg.setEndIndex(this.itemPointer);
    }
  }

  public final void stop(final Object key) {
    if (key == null) {
      throw new IllegalArgumentException("key", new NullPointerException("key"));
    }
    this.ensureViable();
    final CaptureGroup<T> cg = this.captureGroups.get(key);
    if (cg != null) {
      cg.setEndIndex(this.itemPointer);
    }
  }

  /**
   * @exception IllegalStateException if this {@link Thread}
   * {@linkplain #isViable() is not viable}
   */
  public final Thread<T> newThread(final Object id, final int programCounterIndex) {
    this.ensureViable();
    ProgramCounter<T> programCounter = this.getProgramCounter();
    if (programCounter != null) {
      programCounter = programCounter.clone(programCounterIndex);
    }
    return this.newThread(id, programCounter, this.items, this.itemPointer, deepClone(this.captureGroups));
  }

  /**
   * {@inheritDoc}
   *
   * @exception IllegalStateException if this {@link Thread}
   * {@linkplain #isViable() is not viable}
   */
  @Override
  public final Thread<T> newThread(final Object id, final ProgramCounter<T> programCounter, final List<T> items, final int itemPointer, final Map<Object, CaptureGroup<T>> captureGroups) {
    this.ensureViable();
    return this.threadScheduler.newThread(id, programCounter, items, itemPointer, captureGroups);
  }

  /**
   * Schedules the supplied {@link Thread} for execution.
   *
   * <p>This method may be invoked only when {@link #isViable()}
   * returns {@code true}.</p>
   *
   * <p>This method performs no validity checking of any kind on the
   * supplied {@link Thread}, relying instead upon the underlying
   * {@link ThreadScheduler}'s {@link
   * ThreadScheduler#schedule(Thread)} method to perform any
   * checks.</p>
   *
   * @exception IllegalStateException if this {@link Thread}
   * {@linkplain #isViable() is not viable}
   */
  @Override
  public final boolean schedule(final Thread<T> t) {
    this.ensureViable();
    return this.threadScheduler.schedule(t);
  }

  public final boolean atStart() {
    if (!this.canRead()) {
      return false;
    }
    if (this.items == null || this.items.isEmpty()) {
      return this.itemPointer == VALID_NO_INPUT_POINTER;
    } else {
      return this.itemPointer == 0;
    }
  }

  public final boolean atEnd() {
    if (!this.isViable()) {
      return false;
    }
    if (this.items == null || this.items.isEmpty()) {
      return this.itemPointer == VALID_NO_INPUT_POINTER;
    } else {
      return this.itemPointer == this.items.size();
    }
  }

  /**
   * Returns {@code true} if this {@link Thread} {@linkplain
   * #isViable() is viable}, and has more items to read.
   *
   * <p>This method may be invoked in all states.</p>
   *
   * @return {@code true} if this {@link Thread} can read an item
   */
  public final boolean canRead() {
    return this.isViable() && this.isValidItemPointer(this.itemPointer);
  }

  /**
   * Reads (but does not "consume") the current item.
   *
   * <p>This method may be invoked only when {@link #isViable()}
   * returns {@code true}.</p>
   *
   * @return the current item which may be {@code null}
   *
   * @exception IllegalStateException if the {@link #canRead()} method
   * returns {@code false}
   *
   * @see #canRead()
   */
  public final T read() {
    if (!this.canRead()) {
      throw new IllegalStateException("Thread cannot read");
    }
    if (this.items == null || this.items.isEmpty()) {
      // This is an edge case.  If we are originally supplied with no
      // input, we need to be able to use an itemPointer that is both
      // valid, but doesn't point into the (non-existent) input list.
      assert this.itemPointer == VALID_NO_INPUT_POINTER;
      return null;
    }
    assert this.itemPointer >= 0 && this.itemPointer < this.items.size();
    return this.items.get(this.itemPointer);
  }

  private final boolean isValidItemPointer(final int itemPointer) {
    if (this.items == null || this.items.isEmpty()) {
      // We can have null or empty input.  In such a case, a value
      // equal to VALID_NO_INPUT_POINTER is OK; all others are not.
      return itemPointer == VALID_NO_INPUT_POINTER;
    }
    return itemPointer >= 0 && itemPointer < this.items.size();
  }

  /**
   * Advances this {@link Thread}'s item pointer, provided it was
   * valid to begin with.  Returns {@code true} if the resulting item
   * pointer is valid.
   *
   * <p>Please note that a return value of {@code false} does
   * <em>not</em> indicate that this {@link Thread} is dead.</p>
   *
   * @return {@code true} if this {@link Thread}'s item pointer was
   * successfully advanced; {@code false} otherwise
   *
   * @exception IllegalStateException if this {@link Thread}
   * {@linkplain #isViable() is not viable}
   */
  public final boolean advanceItemPointer() {
    this.ensureViable();
    final boolean returnValue;
    if (this.itemPointer == INVALID_INPUT_POINTER || this.itemPointer == VALID_NO_INPUT_POINTER) {
      returnValue = false;
    } else {
      returnValue = this.isValidItemPointer(++this.itemPointer);
    }
    return returnValue;
  }

  /**
   * Returns this {@link Thread}'s associated {@link ProgramCounter}.
   * This method never returns {@code null}.
   *
   * <p>This method may be invoked only when {@link #isViable()}
   * returns {@code true}.</p>
   *
   * @return this {@link Thread}'s associated {@link ProgramCounter};
   * never {@code null}
   */
  public final ProgramCounter<T> getProgramCounter() {
    this.ensureViable();
    return this.programCounter;
  }

  /**
   * Advances the program counter to point at the next {@link
   * Instruction} in the {@link Program}.  If this cannot
   * happen&mdash;as in the case where there are no more {@link
   * Instruction}s in the {@link Program}&mdash;then the {@link
   * #die()} method is invoked.
   *
   * <p>This method may be invoked only when {@link #isViable()}
   * returns {@code true}.</p>
   *
   * @return {@code true} if the program counter was successfully
   * advanced and therefore if there is another {@link Instruction}
   * ready to be executed; {@code false} if this {@link Thread} has
   * been killed via the {@link #die()} method since there are no more
   * {@link Instruction}s in the {@link Program} to be executed
   *
   * @exception IllegalStateException if this {@link Thread}
   * {@linkplain #isViable() is not viable}
   */
  public final boolean advanceProgramCounter() {
    this.ensureViable();
    boolean returnValue = false;
    final ProgramCounter<T> programCounter = this.getProgramCounter();
    if (programCounter != null) {
      returnValue = programCounter.advance();
    }
    if (!returnValue) {
      // The program counter could not be incremented, so there are no
      // further instructions to run, so we're dead.
      this.die();
    }
    return returnValue;
  }

  /**
   * Sets this {@link Thread}'s program counter to the supplied value,
   * provided it is valid.  If the supplied value is <em>not</em>
   * valid, then this {@link Thread} {@linkplain #die() dies}.
   *
   * <p>This method may be invoked only when {@link #isViable()}
   * returns {@code true}.</p>
   *
   * @param programCounter
   *
   * @return {@code true} if the program counter was actually set;
   * {@code false} if it was not (thus indicating that this {@link
   * Thread} is now dead)
   *
   * @exception IllegalArgumentException if the supplied {@code
   * programCounter} is not valid
   *
   * @exception IllegalStateException if this {@link Thread}
   * {@linkplain #isViable() is not viable}
   */
  public final boolean jump(final int programCounter) {
    this.ensureViable();
    final boolean returnValue;
    final ProgramCounter<T> pc = this.getProgramCounter();
    if (pc != null && pc.isValid(programCounter)) {
      pc.setIndex(programCounter);
      returnValue = true;
    } else {
      this.die();
      returnValue = false;
    }
    return returnValue;
  }

  /**
   * Returns the current {@link Instruction}.
   *
   * <p>This method may be invoked only when {@link #isViable()}
   * returns {@code true}.</p>
   *
   * @return the current {@link Instruction}, or {@code null} if the
   * {@link #isViable()} method returns {@code false}
   *
   * @exception IllegalStateException if this {@link Thread}
   * {@linkplain #isViable() is not viable}
   *
   * @see #isViable()
   */
  private final Instruction<T> getInstruction() {
    this.ensureViable();
    final Instruction<T> returnValue;
    final ProgramCounter<T> programCounter = this.getProgramCounter();
    if (programCounter != null && this.isViable()) {
      returnValue = programCounter.getInstruction();
    } else {
      returnValue = null;
    }
    return returnValue;
  }

  /**
   * Retrieves the current {@link Instruction} and {@linkplain
   * Instruction#execute(InstructionContext) executes} it.
   *
   * <p>If the {@link #isViable()} method returns {@code false},
   * then invoking this method will cause the {@link #die()} method to
   * be invoked and no further action will take place.</p>
   *
   * <p>This method may be invoked only when {@link #isViable()}
   * returns {@code true}.</p>
   *
   * @exception IllegalStateException if this {@link Thread}
   * {@linkplain #isViable() is not viable}
   */
  public final void step() {
    this.ensureViable();
    final Instruction<T> instruction = this.getInstruction();
    assert instruction != null;
    instruction.execute(new InstructionContext<T>(this));
  }

  /**
   * Repeatedly calls the {@link #step()} method until this {@link
   * Thread} has completed execution.
   *
   * <p>This method may be invoked only when {@link #isViable()}
   * returns {@code true}.</p>
   *
   * @exception IllegalStateException if this {@link Thread}
   * {@linkplain #isViable() is not viable}
   */
  @Override
  public final void run() {
    this.ensureViable();
    final Logger logger = Logger.getLogger(this.getClass().getName());
    assert logger != null;
    final boolean finer = logger.isLoggable(Level.FINER);
    final ProgramCounter<T> pc = this.getProgramCounter();
    assert pc != null;
    while (this.isViable()) {
      final Instruction<T> currentInstruction = pc.getInstruction();
      assert currentInstruction != null;
      if (finer) {
        final int index = pc.getIndex();
        logger.logp(Level.FINER, this.getClass().getName(), "run", "Before running {0} {1} ({2}) at input position {3}", new Object[] { this, currentInstruction, Integer.valueOf(index), Integer.valueOf(this.itemPointer) });
      }
      this.step();
      if (finer) {
        if (this.isViable()) {
          logger.logp(Level.FINER, this.getClass().getName(), "run", "After running {0} {1}; input position is now {2}", new Object[] { this, currentInstruction, Integer.valueOf(this.itemPointer) });
        } else {
          logger.logp(Level.FINER, this.getClass().getName(), "run", "Thread {0} is no longer viable", this);
        }
      }
    }
  }

  /**
   * Returns {@code true} if and only if this {@link Thread}'s
   * {@linkplain #getState() associated state} is {@link State#VIABLE
   * VIABLE} and if its associated {@link ProgramCounter}'s {@link
   * ProgramCounter#isValid()} method returns {@code true}.
   *
   * <p>This method may be invoked in all states.</p>
   *
   * @return {@code true} if and only if this {@link Thread} is viable
   */
  public final boolean isViable() {
    // Do not call getProgramCounter() here; just access the field
    // directly.  This avoids an infinite loop.
    return this.getState() == State.VIABLE && this.programCounter != null && this.programCounter.isValid();
  }

  /**
   * Causes this {@link Thread} to irrevocably enter the {@link
   * State#MATCH MATCH} state.
   *
   * <p>This method may be invoked only when {@link #isViable()}
   * returns {@code true}.</p>
   *
   * @exception IllegalStateException if this {@link Thread}
   * {@linkplain #isViable() is not viable}
   */
  public final void match() {
    this.ensureViable();
    this.state = State.MATCH;
  }

  /**
   * Causes this {@link Thread} to irrevocably enter the {@link
   * State#DEAD DEAD} state.
   *
   * <p>This method may be invoked in all states except {@link
   * State#MATCH MATCH}.</p>
   *
   * @exception IllegalStateException if this {@link Thread} is
   * currently in the {@link State#MATCH MATCH} state
   */
  public final void die() {
    if (State.MATCH == this.getState()) {
      throw new IllegalStateException();
    }
    this.state = State.DEAD;
    this.itemPointer = INVALID_INPUT_POINTER;
    if (this.captureGroups != null) {
      this.captureGroups.clear();
    }
  }

  /**
   * Returns the {@link State} of this {@link Thread}.  This method
   * never returns {@code null} and never returns {@link
   * State#CONSTRUCTING}.
   *
   * @return a non-{@code null} {@link State}
   */
  public final State getState() {
    assert this.state != null;
    return this.state;
  }

  /**
   * Does nothing if this {@link Thread} {@linkplain #isViable() is viable}.
   *
   * @exception IllegalStateException if this {@link Thread}
   * {@linkplain #isViable() is not viable}
   */
  private final void ensureViable() {
    if (!this.isViable()) {
      throw new IllegalStateException("Thread is not viable");
    }
  }

  @Override
  public String toString() {
    final Object id = this.getId();
    if (id == null) {
      return super.toString();
    } else {
      return id.toString();
    }
  }


  /*
   * Static methods.
   */


  private static final <T> Map<Object, CaptureGroup<T>> deepClone(final Map<Object, CaptureGroup<T>> suppliedCaptureGroups) {
    final Map<Object, CaptureGroup<T>> captureGroups;
    if (suppliedCaptureGroups != null) {
      captureGroups = new HashMap<Object, CaptureGroup<T>>(suppliedCaptureGroups.size());
      final Set<Entry<Object, CaptureGroup<T>>> entries = suppliedCaptureGroups.entrySet();
      if (entries != null && !entries.isEmpty()) {
        for (final Entry<Object, CaptureGroup<T>> entry : entries) {
          if (entry != null) {
            final CaptureGroup<T> cg = entry.getValue();
            if (cg == null) {
              captureGroups.put(entry.getKey(), null);
            } else {
              captureGroups.put(entry.getKey(), cg.clone());
            }
          }
        }
      }
    } else {
      captureGroups = null;
    }
    return captureGroups;
  }

}
