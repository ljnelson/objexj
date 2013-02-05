/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.atomic.AtomicInteger;

import com.edugility.objexj.engine.CaptureGroup;
import com.edugility.objexj.engine.Engine;
import com.edugility.objexj.engine.Program;
import com.edugility.objexj.engine.Thread;
import com.edugility.objexj.engine.ThreadScheduler;
import com.edugility.objexj.engine.ProgramCounter;

import com.edugility.objexj.parser.Parser;

public class Pattern<T> {

  private final Engine<T> engine;

  private final Program<T> program;

  private Pattern(final Program<T> program) {
    this(null, program);
  }

  private Pattern(final Engine<T> engine, final Program<T> program) {
    super();
    if (program == null) {
      throw new IllegalArgumentException("program", new NullPointerException("program"));
    }
    this.program = program;
    if (engine == null) {
      this.engine = new Engine<T>();
    } else {
      this.engine = engine;
    }
  }

  public final Matcher<T> matcher(final List<T> items) {
    final Engine<T> engine = this.getEngine();
    assert engine != null;
    final Program<T> program = this.getProgram();
    assert program != null;
    return new Matcher<T>(engine, program, items);
  }

  private final Engine<T> getEngine() {
    assert this.engine != null;
    return this.engine;
  }

  final Program<T> getProgram() {
    assert this.program != null;
    return this.program;
  }


  /*
   * Static methods.
   */


  public static final <T> Pattern<T> compile(final String source) throws IOException {
    if (source == null) {
      throw new IllegalArgumentException("source", new NullPointerException("source"));
    }
    final Program<T> p = new Parser().parse(source);
    assert p != null;
    return new Pattern<T>(p);
  }

}
