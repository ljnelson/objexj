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

import java.util.concurrent.atomic.AtomicInteger;

import java.util.List;
import java.util.Map;

public class InstructionContext<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final AtomicInteger idGenerator = new AtomicInteger(1);

  private final Thread<T> thread;

  InstructionContext(final Thread<T> thread) {
    super();
    if (thread == null) {
      throw new IllegalArgumentException("thread == null");
    }
    this.thread = thread;
  }

  public Map<Object, Object> getVariables() {
    return this.thread.getVariables();
  }

  public final int getProgramCounterIndex() {
    final ProgramCounter<T> pc = this.thread.getProgramCounter();
    assert pc != null;
    return pc.getIndex();
  }

  public final boolean scheduleNewThread(final int programCounterIndex, final boolean relative) {
    return this.thread.schedule(this.thread.newThread(idGenerator.getAndIncrement(), programCounterIndex, relative));
  }

  public final boolean jump(final int programCounter, final boolean relative) {
    return this.thread.jump(programCounter, relative);
  }

  public final void save(final Object key) {
    this.thread.save(key);
  }

  public final void stop(final Object key) {
    this.thread.stop(key);
  }

  public final boolean atStart() {
    return this.thread.atStart();
  }

  public final boolean atEnd() {
    return this.thread.atEnd();
  }

  public final boolean advanceProgramCounter() {
    return this.thread.advanceProgramCounter();
  }

  public final boolean advanceItemPointer() {
    return this.thread.advanceItemPointer();
  }

  public final boolean canRead() {
    return this.thread.canRead();
  }

  public final T read() {
    return this.thread.read();
  }

  public final void die() {
    this.thread.die();
  }

  public final boolean isDead() {
    return Thread.State.DEAD == this.thread.getState();
  }

  public final void match() {
    this.thread.match();
  }

  @Deprecated
  private final Thread<T> getThread() {
    return this.thread;
  }

}
