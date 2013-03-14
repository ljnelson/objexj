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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader; // for javadoc only
import java.io.StringReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link LinkedList} of {@link Instruction}s that can be
 * {@linkplain Engine#run(Program, List) run} by an {@link Engine} via
 * a {@link com.edugility.objexj.engine.Thread}.
 *
 * @param <T> the type of {@link Object} that {@link Program}
 * instances are capable of matching.
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class Program<T> extends LinkedList<Instruction<T>> {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The platform-specific line separator.  This field is never {@code
   * null}.
   */
  private static final String LS = System.getProperty("line.separator", "\n");

  /**
   * The name of this {@link Program}.  This field may be {@code
   * null}.
   *
   * @see #getName()
   *
   * @see #setName(String)
   */
  private String name;

  /**
   * The source code for this {@link Program}.  This field may be
   * {@code null}.
   *
   * @see #getSource()
   *
   * @see #setSource(Object)
   */
  private Object source;

  /**
   * Creates a new {@link Program}.
   */
  public Program() {
    super();
  }

  /**
   * Creates a new {@link Program}.
   *
   * @param instruction an {@link Instruction}; may be {@code null} in
   * which case it is ignored
   */
  public Program(final Instruction<T> instruction) {
    super();
    if (instruction != null) {
      this.add(instruction);
    }
  }

  /**
   * Creates a new {@link Program}.
   *
   * @param instructions a {@link Collection} of {@link Instruction}s
   * that will be {@linkplain #addAll(Collection) added}
   */
  public Program(final Collection<? extends Instruction<T>> instructions) {
    super();
    if (instructions != null) {
      this.addAll(instructions);
    }
  }

  /**
   * Returns the name of this {@link Program}.  This method may return
   * {@code null}.
   *
   * @return the name of this {@link Program}, or {@code null}
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of this {@link Program}.
   *
   * @param name the new name; may be {@code null}
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Returns whatever {@link Object} it was from which this {@link
   * Program} was compiled.  The return value is really useful only
   * for its {@link Object#toString() toString()} method.  This method
   * may return {@code null}.
   *
   * @return the source {@link Object} from which this {@link Program}
   * was compiled, or {@code null}
   */
  public Object getSource() {
    return this.source;
  }

  /**
   * Sets the source from which this {@link Program} was compiled,
   * provided that this {@link Program} has not yet had its source
   * set.
   * 
   * @param source the new source; may be {@code null}
   */
  public void setSource(final Object source) {
    if (source == null) {
      throw new IllegalArgumentException("source", new NullPointerException("source"));
    }
    final Object old = this.getSource();
    if (old != null) {
      throw new IllegalStateException("getSource() != null: " + old);
    }
    this.source = source;
  }

  /**
   * Returns {@code true} if the supplied {@code programCounter} is
   * valid&mdash;that is, greater than or equal to {@code 0} and less
   * than {@linkplain #size() the return value of the
   * <code>size()</code> method}.
   * 
   * @return {@code true} if the supplied {@code programCounter} is
   * valid; {@code false} otherwise
   */
  public final boolean isValidProgramCounter(final int programCounter) {
    return programCounter >= 0 && programCounter < this.size();
  }

  /**
   * Returns the {@link Instruction} found at the supplied {@code
   * index}, or {@code null} if there is no such {@link Instruction}.
   *
   * @param index the zero-based index under which an {@link
   * Instruction} will hopefully be found
   *
   * @return an {@link Instruction} indexed under the supplied {@code
   * index}, or {@code null}
   * 
   * @exception InvalidProgramCounterException if {@code index} is
   * less than {@code 0} or greater than or equal to {@linkplain
   * #size() this <code>Program</code>'s size}
   */
  @Override
  public final Instruction<T> get(final int index) {
    if (!isValidProgramCounter(index)) {
      throw new InvalidProgramCounterException();
    }
    final Instruction<T> instruction = super.get(index);
    assert instruction != null;
    return instruction;
  }

  /**
   * Returns the index of the supplied {@link Object}, or {@code -1}
   * if the supplied {@link Object} is {@code null} or not contained
   * in this {@link Program}.
   *
   * @param o the {@link Object} whose index should be retrieved; may
   * be {@code null}
   *
   * @return the index of the supplied {@link Object}, or {@code -1}
   */
  @Override
  public final int indexOf(final Object o) {
    int returnValue = -1;
    if (o != null) {
      returnValue = super.indexOf(o);
    }
    return returnValue;
  }

  /**
   * Returns the last index of the supplied {@link Object}, or {@code
   * -1} if the supplied {@link Object} is {@code null} or not
   * contained in this {@link Program}.
   *
   * @param o the {@link Object} whose last index should be retrieved;
   * may be {@code null}
   *
   * @return the last index of the supplied {@link Object}, or {@code
   * -1}
   */
  @Override
  public final int lastIndexOf(final Object o) {
    int returnValue = -1;
    if (o != null) {
      returnValue = super.lastIndexOf(o);
    }
    return returnValue;
  }

  /**
   * Adds the supplied {@link Instruction} at the end of this {@link
   * Program}.  Returns {@code true} if this {@link Program} actually
   * added the {@link Instruction}.
   *
   * @param instruction the {@link Instruction} to add; may be {@code
   * null} in which case {@code false} is returned
   *
   * @return {@code true} if the supplied {@link Instruction} was
   * actually added; {@code false} otherwise
   */
  @Override
  public final boolean add(final Instruction<T> instruction) {
    boolean returnValue = false;
    if (instruction != null) {
      returnValue = super.add(instruction);
    }
    return returnValue;
  }
  
  /**
   * Adds the supplied {@link Instruction} immediately after the
   * specified zero-based index.  Returns {@code true} if this {@link
   * Program} actually added the {@link Instruction}.
   *
   * @param index the zero-based index after which to add the supplied
   * {@link Instruction}; must be greater than or equal to {@code 0}
   * and less than {@linkplain Program#size() this
   * <code>Program</code>'s size}
   *
   * @param instruction the {@link Instruction} to add; may be {@code
   * null} in which case {@code false} is returned
   *
   * @exception IndexOutOfBoundsException if the supplied {@code
   * index} is out of bounds
   */
  @Override
  public final void add(final int index, final Instruction<T> instruction) {
    if (instruction != null) {
      super.add(index, instruction);
    }
  }

  /**
   * Adds the supplied {@link Collection} of {@link Instruction}s to
   * the end of this {@link Program}.  Returns {@code true} if this
   * {@link Program} actually added the {@link Collection}; {@code
   * false} otherwise.
   *
   * @param instructions the instructions to add; may be {@code null}
   * in which case {@code false} will be returned
   *
   * @return {@code true} if this {@link Program} actually added the
   * supplied {@link Collection} of {@link Instruction}s; {@code
   * false} otherwise
   */
  @Override
  public final boolean addAll(final Collection<? extends Instruction<T>> instructions) {
    boolean returnValue = false;
    if (instructions != null && !instructions.isEmpty()) {
      returnValue = super.addAll(instructions);
    }
    return returnValue;
  }

  /**
   * Adds the supplied {@link Collection} of {@link Instruction}s
   * immediately after the supplied index in this {@link Program}.
   * Returns {@code true} if this {@link Program} actually added the
   * {@link Collection}; {@code false} otherwise.
   *
   * @param index the zero-based index after which to add the supplied
   * {@link Instruction}s; must be greater than or equal to {@code 0}
   * and less than {@linkplain Program#size() this
   * <code>Program</code>'s size}
   *
   * @param instructions the instructions to add; may be {@code null}
   * in which case {@code false} will be returned
   *
   * @return {@code true} if this {@link Program} actually added the
   * supplied {@link Collection} of {@link Instruction}s; {@code
   * false} otherwise
   */
  @Override
  public final boolean addAll(final int index, final Collection<? extends Instruction<T>> instructions) {
    boolean returnValue = false;
    if (instructions != null && !instructions.isEmpty()) {
      returnValue = super.addAll(index, instructions);
    }
    return returnValue;
  }

  /**
   * Returns a non-{@code null} textual representation of this {@link
   * Program}.
   *
   * @return a non-{@code null} {@link String}
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final String name = this.getName();
    if (name != null) {
      sb.append(String.format("%s:%n", name));
    }
    final Iterator<Instruction<T>> iterator = this.iterator();
    assert iterator != null;
    if (iterator.hasNext()) {
      for (int i = 0; iterator.hasNext(); i++) {
        sb.append(String.format("%3d: %s", i, iterator.next()));
        if (iterator.hasNext()) {
          sb.append(LS);
        }
      }      
    }
    return sb.toString();
  }
  

  /*
   * Unsupported operations.
   */

  
  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public final void clear() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public final boolean remove(final Object ignored) {
    throw new UnsupportedOperationException("remove");
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public final Instruction<T> remove(final int ignored) {
    throw new UnsupportedOperationException("remove");
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public final boolean removeAll(final Collection<?> ignored) {
    throw new UnsupportedOperationException("removeAll");
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public final boolean retainAll(final Collection<?> ignored) {
    throw new UnsupportedOperationException("retainAll");
  }

  /**
   * Throws an {@link UnsupportedOperationException} when invoked.
   *
   * @exception UnsupportedOperationException when invoked
   */
  @Override
  public final Instruction<T> set(final int ignored0, final Instruction<T> ignored1) {
    throw new UnsupportedOperationException("set");
  }


  /*
   * Static methods.
   */


  /**
   * Creates and returns a {@link Program} parsed from the supplied
   * {@link File}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @param <T> the type of {@link Object}s the returned {@link
   * Program} will be able to match
   *
   * @param file a {@link File} containing {@link Instruction}s in
   * {@linkplain Instruction#valueOf(String) source form}; must not be
   * {@code null}
   *
   * @return a new {@link Program}; never {@code null}
   *
   * @exception IllegalArgumentException if {@code file} is {@code
   * null}
   *
   * @exception ClassNotFoundException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception IllegalAccessException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception InstantiationException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception InvocationTargetException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception IOException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception NoSuchMethodException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   */
  public static final <T> Program<T> valueOf(final File file) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, NoSuchMethodException {
    if (file == null) {
      throw new IllegalArgumentException("file");
    }
    final BufferedReader reader = new BufferedReader(new FileReader(file));
    try {
      return valueOf(reader);
    } finally {
      try {
        reader.close();
      } catch (final IOException nothingToBeDone) {
        final Logger logger = Logger.getLogger(Program.class.getName());
        if (logger != null && logger.isLoggable(Level.SEVERE)) {
          logger.logp(Level.SEVERE, Program.class.getName(), "parse", "Exception encountered while attempting to close reader", nothingToBeDone);
        }
      }
    }
  }

  /**
   * Creates and returns a {@link Program} parsed from the supplied
   * {@link BufferedReader}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @param <T> the type of {@link Object}s the returned {@link
   * Program} will be able to match
   *
   * @param reader a {@link BufferedReader} that can read {@link
   * Instruction}s in {@linkplain Instruction#valueOf(String) source
   * form}; must not be {@code null} and must not be {@linkplain
   * Reader#close() closed}
   *
   * @return a new {@link Program}; never {@code null}
   *
   * @exception IllegalArgumentException if {@code reader} is {@code
   * null}
   *
   * @exception ClassNotFoundException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception IllegalAccessException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception InstantiationException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception InvocationTargetException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception IOException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception NoSuchMethodException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   */
  public static final <T> Program<T> valueOf(final BufferedReader reader) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, NoSuchMethodException {
    if (reader == null) {
      throw new IllegalArgumentException("reader == null");
    }
    Program<T> program = new Program<T>();
    String line = null;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (!line.isEmpty() && !line.startsWith("#")) {
        program.add(Instruction.<T>valueOf(line));
      }
    }
    return program;
  }
  
  /**
   * Creates and returns a {@link Program} parsed from the supplied
   * {@code text}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @param <T> the type of {@link Object}s the returned {@link
   * Program} will be able to match
   *
   * @param text a {@link String} containing {@link Instruction}s in
   * source form; must not be {@code null}
   *
   * @return a new {@link Program}; never {@code null}
   *
   * @exception IllegalArgumentException if {@code text} is {@code
   * null}
   *
   * @exception ClassNotFoundException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception IllegalAccessException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception InstantiationException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception InvocationTargetException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception IOException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception NoSuchMethodException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   */
  public static final <T> Program<T> valueOf(final String text) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, NoSuchMethodException {
    if (text == null) {
      throw new IllegalArgumentException("text == null");
    }
    final BufferedReader reader = new BufferedReader(new StringReader(text));
    try {
      return valueOf(reader);
    } finally {
      try {
        reader.close();
      } catch (final IOException nothingToBeDone) {
        final Logger logger = Logger.getLogger(Program.class.getName());
        if (logger != null && logger.isLoggable(Level.SEVERE)) {
          logger.logp(Level.SEVERE, Program.class.getName(), "parse", "Exception encountered while attempting to close reader", nothingToBeDone);
        }
      }
    }
  }

  /**
   * Creates and returns a {@link Program} parsed from the supplied
   * {@link Iterable} of {@link Instruction} lines in source form.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @param <T> the type of {@link Object}s the returned {@link
   * Program} will be able to match
   *
   * @param lines an {@link Iterable} of {@link String}s, each of
   * which represents an {@link Instruction} in source form; must not
   * be {@code null}
   *
   * @return a new {@link Program}; never {@code null}
   *
   * @exception IllegalArgumentException if {@code lines} is {@code
   * null}
   *
   * @exception ClassNotFoundException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception IllegalAccessException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception InstantiationException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception InvocationTargetException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception IOException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception NoSuchMethodException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   */
  public static final <T> Program<T> valueOf(final Iterable<String> lines) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
    return valueOf(lines == null ? (Iterator<String>)null : lines.iterator());
  }

  /**
   * Creates and returns a {@link Program} parsed from the supplied
   * {@link Iterator} of {@link Instruction} lines in source form.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @param <T> the type of {@link Object}s the returned {@link
   * Program} will be able to match
   *
   * @param lines an {@link Iterator} of {@link String}s, each of
   * which represents an {@link Instruction} in source form; must not
   * be {@code null}
   *
   * @return a new {@link Program}; never {@code null}
   *
   * @exception IllegalArgumentException if {@code lines} is {@code
   * null}
   *
   * @exception ClassNotFoundException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception IllegalAccessException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception InstantiationException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception InvocationTargetException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception IOException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   *
   * @exception NoSuchMethodException if there was an error
   * assembling an {@link Instruction} in the {@link File}
   */
  public static final <T> Program<T> valueOf(final Iterator<String> lines) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
    Program<T> program = new Program<T>();
    if (lines != null && lines.hasNext()) {
      while (lines.hasNext()) {
        final String line = lines.next().trim();
        if (!line.isEmpty() && !line.startsWith("#")) {
          program.add(Instruction.<T>valueOf(line));
        }
      }
    }
    return program;
  }

  /**
   * Creates a new {@link Program} that contains only the supplied
   * {@link Instruction}.
   *
   * @param <T> the type of {@link Object}s the returned {@link
   * Program} will be able to match
   *
   * @param instruction the {@link Instruction} to wrap with a new
   * {@link Program}; if {@code null} an {@linkplain
   * LinkedList#isEmpty() empty} {@link Program} is returned instead
   *
   * @return a non-{@code null} {@link Program}
   */
  public static final <T> Program<T> singleton(final Instruction<T> instruction) {
    return new Program<T>(instruction);
  }

}
