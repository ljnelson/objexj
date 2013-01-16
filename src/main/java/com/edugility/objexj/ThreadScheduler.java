package com.edugility.objexj;

import java.util.List;

public interface ThreadScheduler<T> {

  /**
   * Creates a new {@link Thread} and returns it.  Implementations of
   * this method must not return {@code null}.
   *
   * @param programCounter a non-{@code null} {@link ProgramCounter}
   * to initialize the new {@link Thread} with
   *
   * @param items a possibly {@code null} {@link List} of items that
   * the new {@link Thread} may {@linkplain Thread#read() read from}
   *
   * @param itemPointer the starting index within the supplied {@link
   * List} of items from which the new {@link Thread} will {@linkplain
   * Thread#read() read}
   *
   * @return a new non-{@code null} {@link Thread}
   */
  public Thread<T> newThread(final ProgramCounter<T> programCounter, final List<T> items, final int itemPointer);

  /**
   * Schedules the supplied {@link Thread} for execution immediately
   * or later.
   *
   * @return {@code true} if the supplied {@link Thread} was
   * scheduled; {@code false} otherwise
   *
   * @exception IllegalArgumentException if {@code t} is {@code null}
   */
  public boolean schedule(final Thread<T> t);
  
}