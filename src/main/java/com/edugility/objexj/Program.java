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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Program<T> {

  private final List<Instruction<T>> instructions;

  private String name;

  public Program() {
    super();
    this.instructions = new ArrayList<Instruction<T>>();
  }

  public Program(final Instruction<T> instruction) {
    this();
    if (instruction != null) {
      this.add(instruction);
    }
  }

  public Program(final Iterable<Instruction<T>> instructions) {
    this();
    if (instructions != null) {
      for (final Instruction<T> instruction : instructions) {
        if (instruction != null) {
          this.add(instruction);
        }
      }
    }
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public final boolean isEmpty() {
    return this.instructions.isEmpty();
  }

  public final int size() {
    return this.instructions.size();
  }

  public final boolean isValidProgramCounter(final int programCounter) {
    return programCounter >= 0 && programCounter < this.size();
  }

  /**
   * @exception InvvalidProgramCounterException if {@code index} is
   * less than {@code 0} or greater than or equal to {@linkplain
   * #size() this <tt>Program</tt>'s size}
   */
  public final Instruction<T> get(final int index) {
    if (!isValidProgramCounter(index)) {
      throw new InvalidProgramCounterException();
    }
    final Instruction<T> instruction = this.instructions.get(index);
    assert instruction != null;
    return instruction;
  }

  public final boolean add(final Instruction<T> instruction) {
    boolean returnValue = false;
    if (instruction != null) {
      returnValue = this.instructions.add(instruction);
    }
    return returnValue;
  }

  @Override
  public String toString() {
    final String name = this.getName();
    if (name == null) {
      return super.toString();
    } else {
      return name;
    }
  }

  /*
   * Static methods.
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

  public static final <T> Program<T> valueOf(final Iterable<String> lines) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
    return valueOf(lines == null ? (Iterator<String>)null : lines.iterator());
  }

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

  public static final <T> Program<T> singleton(final Instruction<T> instruction) {
    final Program<T> program = new Program<T>();
    if (instruction != null) {
      program.add(instruction);
    }
    return program;
  }


}
