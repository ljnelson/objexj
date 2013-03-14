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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import java.util.concurrent.atomic.AtomicInteger;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.edugility.objexj.parser.Parser;

/**
 * A stateless regular-expression-processing virtual machine made to
 * run {@link Program}s against {@link List}s of input using {@link
 * com.edugility.objexj.engine.Thread}s.
 *
 * @param <T> the type of {@link Object}s that {@link List}s supplied
 * to the {@link #run(Program, List)} method will consist of
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #run(Program, List)
 */
public class Engine<T> {

  /**
   * Creates a new {@link Engine}.
   */
  public Engine() {
    super();
  }

  /**
   * Runs the supplied {@link Program} against the supplied {@link
   * List} and returns a (possibly {@code null}) {@link MatchResult}
   * describing the run result.
   *
   * @param program the {@link Program} to run; must not be {@code
   * null}
   *
   * @param items the input {@link List}; may be {@code null}
   *
   * @return a {@link MatchResult}, or {@code null} if no match
   * occurred
   *
   * @exception IllegalArgumentException if {@code program} is {@code
   * null}
   */
  public MatchResult<T> run(final Program<T> program, final List<T> items) {
    if (program == null) {
      throw new IllegalArgumentException("program", new NullPointerException("program"));
    }
    final AtomicInteger idGenerator = new AtomicInteger();
    final Queue<Thread<T>> threads = new LinkedList<Thread<T>>();
    final Scheduler<T> scheduler = new Scheduler<T>(threads, idGenerator);

    scheduler.schedule(scheduler.newThread(String.format("T%d", idGenerator.getAndIncrement()),
                                           new ProgramCounter<T>(program),
                                           items,
                                           0,
                                           null,
                                           null));
    MatchResult<T> result = null;
    while (!threads.isEmpty()) {
      final Thread<T> thread = threads.remove();
      assert thread != null;
      thread.run();
      if (Thread.State.MATCH == thread.getState()) {
        result = new MatchResult<T>(thread);
        break;
      }
    }
    return result;
  }

  /**
   * A {@link ThreadScheduler} that schedules {@link Thread}s in a
   * simplistic, sequential manner.
   *
   * @author <a href="http://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   */
  private static final class Scheduler<T> implements ThreadScheduler<T> {

    /**
     * A {@link Queue} of scheduled {@link Thread}s.  This field may
     * be {@code null}.
     */
    private final Queue<Thread<T>> threads;

    /**
     * An {@link AtomicInteger} used to generate {@link Thread}
     * identifiers.  This field is never {@code null}.
     */
    private final AtomicInteger idGenerator;

    /**
     * A {@link Logger} to log messages.  This field is never {@code
     * null}.
     */
    private final Logger logger;

    /**
     * Creates a new {@link Scheduler}.
     *
     * @param threads a {@lik Queue} of {@link Thread}s; may be {@code
     * null}
     */
    private Scheduler(final Queue<Thread<T>> threads) {
      this(threads, new AtomicInteger());
    }

    /**
     * Creates a new {@link Scheduler}.
     *
     * @param threads a {@link Queue} of {@link Thread}s; may be
     * {@code null}
     *
     * @param idGenerator an {@link AtomicInteger} to help with {@link
     * Thread} identifier generation; may be {@code null}
     */
    private Scheduler(final Queue<Thread<T>> threads, final AtomicInteger idGenerator) {
      super();
      this.logger = Logger.getLogger(this.getClass().getName());
      this.threads = threads;
      if (idGenerator == null) {
        this.idGenerator = new AtomicInteger();
      } else {
        this.idGenerator = idGenerator;
      }
    }

    /**
     * Creates a new {@link Thread}.
     *
     * @param id the identifier for the new {@link Thread}; may be
     * {@code null} in which case a default identifier will be used
     * instead
     *
     * @param programCounter a {@link ProgramCounter}; must not be {@code null}
     *
     * @param items the input {@link List} to read from; may be {@code
     * null} in which case the supplied {@code itemPointer} must be
     * equal to {@code 0} or {@link Thread#VALID_NO_INPUT_POINTER}
     *
     * @param itemPointer the index within the supplied {@code items}
     * {@link List} from which to begin {@linkplain Thread#read()
     * reading}; must be zero or a positive integer less than the
     * supplied {@code items} {@linkplain Collection#size() size}, or
     * must be equal to {@link Thrad#VALID_NO_INPUT_POINTER} provided
     * that the {@code items} {@link List} is {@code null} or
     * {@linkplain Collection#isEmpty() empty}
     *
     * @param captureGroups a {@link Map} of {@link CaptureGroup}s
     * indexed by key that this new {@link Thread} will use to store
     * {@linkplain Thread#getSubmatches() submatches}; may be {@code
     * null}
     *
     * @param variables a {@link Map} of variables the new {@link
     * Thread} will be initialized with; may be {@code null}
     *
     * @exception IllegalArgumentException if any of the preconditions
     * outlined as part of the parameter descriptions is not fulfilled
     */
    @Override
    public final Thread<T> newThread(Object id, final ProgramCounter<T> programCounter, final List<T> items, final int itemPointer, final Map<Object, CaptureGroup<T>> captureGroups, final Map<Object, Object> variables) {
      if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
        this.logger.entering(this.getClass().getName(), "newThread", new Object[] { id, programCounter, items, itemPointer, captureGroups, variables });
      }      
      if (id == null) {
        id = String.format("T%d", this.idGenerator.getAndIncrement());
      }
      final Thread<T> returnValue = new Thread<T>(id, programCounter, items, itemPointer, captureGroups, variables, this);
      if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
        this.logger.exiting(this.getClass().getName(), "newThread", returnValue);
      }      
      return returnValue;
    }
      
    /**
     * Schedules the supplied {@link Thread} for execution.  This
     * implementation of the {@link ThreadScheduler#schedule(Thread)}
     * method adds the supplied {@link Thread} to the end of a
     * first-in-last-out {@link Queue} of scheduled {@link Thread}s.
     *
     * @param t the {@link Thread} to schedule; must not be {@code
     * null}
     *
     * @return {@code true} if the supplied {@link Thread} was
     * actually scheduled; {@code false} if this {@link Scheduler}
     * concluded that&mdash;for whatever reason&mdash;that the
     * supplied {@link Thread} could not be scheduled
     *
     * @exception IllegalArgumentException if {@code t} is {@code
     * null}
     */
    @Override
    public final boolean schedule(final Thread<T> t) {
      if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
        this.logger.entering(this.getClass().getName(), "schedule", t);
      }
      if (t == null) {
        throw new IllegalArgumentException("t", new NullPointerException("t"));
      }
      if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
        this.logger.logp(Level.FINER, this.getClass().getName(), "schedule", "Scheduling thread {0}", t);
      }
      final boolean returnValue = this.threads != null && this.threads.add(t);
      if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
        this.logger.exiting(this.getClass().getName(), "schedule", Boolean.valueOf(returnValue));
      }
      return returnValue;
    }
  }

}
