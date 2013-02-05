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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.atomic.AtomicInteger;

import com.edugility.objexj.parser.Parser;

public class Engine<T> {

  public Engine() {
    super();
  }

  public MatchResult<T> run(final Program<T> program, final List<T> items) {
    if (program == null) {
      throw new IllegalArgumentException("program", new NullPointerException("program"));
    }
    final AtomicInteger idGenerator = new AtomicInteger();
    final Set<Thread<T>> threadSet = new LinkedHashSet<Thread<T>>();
    final Scheduler<T> scheduler = new Scheduler<T>(threadSet, idGenerator);

    scheduler.schedule(scheduler.newThread(String.format("T%d", idGenerator.getAndIncrement()),
                                           new ProgramCounter<T>(program),
                                           items,
                                           0,
                                           null,
                                           null));
    MatchResult<T> result = null;
    final Iterator<Thread<T>> threads = threadSet.iterator();
    assert threads != null;
    assert threads.hasNext();
    while (threads.hasNext()) {
      final Thread<T> thread = threads.next();
      assert thread != null;
      threads.remove();
      thread.run();
      if (Thread.State.MATCH == thread.getState()) {
        result = new MatchResult<T>(thread);
        break;
      }
    }
    return result;
  }

  private static final class Scheduler<T> implements ThreadScheduler<T> {

    private final Set<Thread<T>> threadSet;

    private final AtomicInteger idGenerator;

    private Scheduler(final Set<Thread<T>> threadSet) {
      this(threadSet, new AtomicInteger());
    }

    private Scheduler(final Set<Thread<T>> threadSet, final AtomicInteger idGenerator) {
      super();
      this.threadSet = threadSet;
      if (idGenerator == null) {
        this.idGenerator = new AtomicInteger();
      } else {
        this.idGenerator = idGenerator;
      }
    }

    @Override
    public final Thread<T> newThread(Object id, final ProgramCounter<T> programCounter, final List<T> items, final int itemPointer, final Map<Object, CaptureGroup<T>> captureGroups, final Map<Object, Object> variables) {
      if (id == null) {
        id = String.format("T%d", this.idGenerator.getAndIncrement());
      }
      return new Thread<T>(id, programCounter, items, itemPointer, captureGroups, variables, this);
    }
      
    @Override
    public final boolean schedule(final Thread<T> t) {
      if (t == null) {
        throw new IllegalArgumentException("t", new NullPointerException("t"));
      }
      return this.threadSet != null && this.threadSet.add(t);
    }
  }

}
