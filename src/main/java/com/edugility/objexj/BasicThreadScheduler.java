package com.edugility.objexj;

import java.util.Collection;
import java.util.List;

/**
 * A trivial implementation of the {@link ThreadScheduler} interface.
 * This implementation is capable of creating new {@link Thread}s, but
 * does not ever actually schedule them.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 */
public class BasicThreadScheduler<T> implements ThreadScheduler<T> {

  /**
   * Creates a new {@link BasicThreadScheduler}.
   */
  public BasicThreadScheduler() {
    super();
  }

  /**
   * Creates a new {@link Thread}.
   *
   * @param programCounter the {@link ProgramCounter} to initialize
   * the new {@link Thread} with; must not be {@code null}
   *
   * @param items the {@link List} of items the new {@link Thread}
   * will {@linkplain Thread#read() read from}; may be {@code null} or
   * {@linkplain Collection#isEmpty() empty}
   *
   * @param itemPointer an {@code int} indicating which item from the
   * supplied {@code items} {@link List} to start reading from; may be
   * a positive {@code int} or equal to {@link
   * Thread#VALID_NO_INPUT_POINTER}
   *
   * @exception IllegalArgumentException if a parameter that cannot be
   * {@code null} is supplied with a {@code null} value
   */
  @Override
  public Thread<T> newThread(final ProgramCounter<T> programCounter, final List<T> items, final int itemPointer) {
    return new Thread<T>(programCounter, items, itemPointer, this);
  }

  /**
   * Implements the {@link ThreadScheduler#schedule(Thread)} contract
   * by returning {@code true}, but otherwise taking no action.
   * Obviously this default implementation is suitable only for
   * inconsequential things like certain unit tests where scheduling
   * itself is not a concern.  Otherwise this method should be
   * overridden.
   *
   * @param thread the {@link Thread} to schedule; ignored
   *
   * @return {@code true} if invoked
   */
  @Override
  public boolean schedule(final Thread<T> thread) {
    return true;
  }
  
}