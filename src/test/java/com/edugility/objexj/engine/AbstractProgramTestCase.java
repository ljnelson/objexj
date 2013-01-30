package com.edugility.objexj.engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Map;

import java.util.concurrent.atomic.AtomicInteger;

import java.lang.reflect.InvocationTargetException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class AbstractProgramTestCase<T> implements ThreadScheduler<T> {

  private static final AtomicInteger ids = new AtomicInteger();

  protected Program<T> program;

  protected Collection<Thread<T>> threads;

  protected AbstractProgramTestCase() {
    super();
  }

  @Before
  public void before() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, NoSuchMethodException {
    this.threads = new ArrayList<Thread<T>>();
    this.program = this.getProgram();
  }

  public Program<T> getProgram() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, NoSuchMethodException {
    return this.getProgram(String.format("%s.rgx", this.getClass().getSimpleName()));
  }

  public Program<T> getProgram(final String resourceName) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, NoSuchMethodException {
    assertNotNull(resourceName);
    final URL program = java.lang.Thread.currentThread().getContextClassLoader().getResource(resourceName);
    assertNotNull(program);
    final BufferedReader reader = new BufferedReader(new InputStreamReader(program.openStream()));
    Program<T> p;
    try {
      p = Program.valueOf(reader);
    } finally {
      try {
        reader.close();
      } catch (final IOException nothingToBeDone) {
        final Logger logger = Logger.getLogger(this.getClass().getName());
        if (logger != null && logger.isLoggable(Level.SEVERE)) {
          logger.logp(Level.SEVERE, this.getClass().getName(), "getProgram", "Exception encountered while trying to close reader", nothingToBeDone);
        }
      }
    }
    assertNotNull(p);
    p.setName(resourceName);
    return p;
  }

  @Override
  public Thread<T> newThread(final Object id, final ProgramCounter<T> programCounter, final List<T> items, final int itemPointer, final Map<Object, CaptureGroup<T>> captureGroups, final Map<Object, Object> variables) {
    return new Thread<T>(id, programCounter, items, itemPointer, captureGroups, variables, this);
  }

  @Override
  public boolean schedule(final Thread<T> thread) {
    assertNotNull(thread);
    assertNotNull(this.threads);
    return this.threads.add(thread);
  }

  public static final void assertDead(final Thread<?> t) {
    assertNotNull(t);
    assertSame(Thread.State.DEAD, t.getState());
  }

  public static final void assertMatch(final Thread<?> t) {
    assertNotNull(t);
    assertSame(Thread.State.MATCH, t.getState());
  }

  public Thread<T> getThread(final List<T> input) {
    assertNotNull(this.program);    
    final int inputPointer;
    if (input == null || input.isEmpty()) {
      inputPointer = Thread.VALID_NO_INPUT_POINTER;
    } else {
      inputPointer = 0;
    }
    final Thread<T> t = this.newThread(String.format("T%d", this.ids.getAndIncrement()), new ProgramCounter<T>(this.program), input, inputPointer, null, null);
    assertNotNull(t);
    return t;
  }

  public Thread<T> run(final List<T> input) {
    assertNotNull(this.threads);
    assertTrue(this.threads.isEmpty());
    Thread<T> thread = this.getThread(input);
    assertNotNull(thread);
    this.threads.add(thread);
    while (!this.threads.isEmpty()) {
      final Iterator<Thread<T>> iterator = this.threads.iterator();
      assertNotNull(iterator);
      assertTrue(iterator.hasNext());
      thread = iterator.next();
      assertNotNull(thread);
      iterator.remove();
      thread.run();
      if (Thread.State.MATCH == thread.getState()) {
        // we have a match
        break;
      }
    }
    return thread;
  }

}
