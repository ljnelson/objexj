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

  public boolean run(final String program, final List<T> items) throws IOException {
    if (program == null) {
      throw new IllegalArgumentException("program", new NullPointerException("program"));
    }
    return this.run(new Parser().<T>parse(program), items);
  }

  public boolean run(final Program<T> program, final List<T> items) {
    if (program == null) {
      throw new IllegalArgumentException("program", new NullPointerException("program"));
    }
    final AtomicInteger idGenerator = new AtomicInteger();
    final Set<Thread<? extends T>> threadSet = new LinkedHashSet<Thread<? extends T>>();
    final class Scheduler<X extends T> implements ThreadScheduler<X> {
      @Override
      public final Thread<X> newThread(Object id, final ProgramCounter<X> programCounter, final List<X> items, final int itemPointer, final Map<Object, CaptureGroup<X>> captureGroups, final Map<Object, Object> variables) {
        if (id == null) {
          id = String.format("T%d", idGenerator.getAndIncrement());
        }
        return new Thread<X>(id, programCounter, items, itemPointer, captureGroups, variables, this);
      }
      
      @Override
      public final boolean schedule(final Thread<X> t) {
        if (t == null) {
          throw new IllegalArgumentException("t", new NullPointerException("t"));
        }
        return threadSet.add(t);
      }
    };
    final Scheduler<T> scheduler = new Scheduler<T>();
    scheduler.schedule(scheduler.newThread(String.format("T%d", idGenerator.getAndIncrement()),
                                           new ProgramCounter<T>(program),
                                           items,
                                           0,
                                           null,
                                           null));
    final Iterator<Thread<? extends T>> threads = threadSet.iterator();
    assert threads != null;
    assert threads.hasNext();
    while (threads.hasNext()) {
      final Thread<? extends T> thread = threads.next();
      assert thread != null;
      threads.remove();
      thread.run();
      if (Thread.State.MATCH == thread.getState()) {
        return true;
      }
    }
    return false;
  }

}
