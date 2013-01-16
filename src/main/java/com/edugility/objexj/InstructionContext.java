package com.edugility.objexj;

import java.io.Serializable;

import java.util.List;

public class InstructionContext<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Thread<T> thread;

  public InstructionContext(final Thread<T> thread) {
    super();
    if (thread == null) {
      throw new IllegalArgumentException("thread == null");
    }
    this.thread = thread;
  }

  public final boolean scheduleNewThread(final int programCounterIndex) {
    return this.thread.schedule(this.thread.newThread(programCounterIndex));
  }

  public final boolean jump(final int programCounter) {
    return this.thread.jump(programCounter);
  }

  public final void save(final Object key) {
    this.thread.save(key);
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