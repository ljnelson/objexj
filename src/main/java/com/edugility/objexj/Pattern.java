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

import java.util.List;

import com.edugility.objexj.engine.Engine;
import com.edugility.objexj.engine.Program;

import com.edugility.objexj.parser.Parser;

/**
 * A regular expression pattern for arbitrary {@link Object}s.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 */
public class Pattern<T> {

  /**
   * The {@link Engine} to use to {@linkplain Engine#run(Program,
   * List) run <tt>Program</tt>s}.  This field is never {@code null}.
   */
  private final Engine<T> engine;

  /**
   * The {@link Program} this {@link Pattern} will {@linkplain
   * Engine#run(Program, List) run}.  This field is never {@code
   * null}.
   */
  private final Program<T> program;

  /**
   * Creates a new {@link Pattern} with the supplied {@link Program}.
   * A new {@link Engine} will be used to {@linkplain
   * Engine#run(Program, List) run} the supplied {@link Program}.
   *
   * @param program the {@link Program} to {@linkplain
   * Engine#run(Program, List) run}; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code program} is {@code
   * null}
   *
   * @see #Pattern(Engine, Program)
   *
   * @see Pattern#compile(String)
   */
  private Pattern(final Program<T> program) {
    this(null, program);
  }

  /**
   * Creates a new {@link Pattern} with the supplied {@link Engine}
   * and {@link Program}.
   *
   * @param engine the {@link Engine} that will be used to {@linkplain
   * Engine#run(Program, List) run} the supplied {@link Program}; if
   * {@code null} a new {@link Engine} will be used instead
   *
   * @param program the {@link Program} to {@linkplain
   * Engine#run(Program, List) run}; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code program} is {@code
   * null}
   *
   * @see Pattern#compile(String)
   */
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

  /**
   * Returns a {@link Matcher} initialized to match the supplied
   * {@link List} of items.  This method never returns {@code null}.
   *
   * @param items the input; may be {@code null}
   *
   * @return a new {@link Matcher}; never {@code null}
   */
  public final Matcher<T> matcher(final List<T> items) {
    return new Matcher<T>(this, items);
  }

  /**
   * Returns the {@link Engine} that will be used to {@linkplain
   * Engine#run(Program, List) run} this {@link Pattern}'s {@linkplain
   * #getProgram() affiliated <tt>Program</tt>}.  This method never
   * returns {@code null}.
   *
   * @return a non-{@code null} {@link Engine}
   */
  final Engine<T> getEngine() {
    assert this.engine != null;
    return this.engine;
  }

  /**
   * Returns the {@link Program} that this {@link Pattern} will cause
   * to be {@linkplain Engine#run(Program, List) run} by {@link
   * Matcher}s {@linkplain #matcher(List) supplied by its
   * <tt>matcher(List)</tt> method}.  This method never returns {@code
   * null}.
   *
   * @return a non-{@code null} {@link Program}
   */
  final Program<T> getProgram() {
    assert this.program != null;
    return this.program;
  }

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link Pattern}.
   *
   * <p>This implementation attempts to return the original source
   * code used to produce this {@link Pattern}.  If that fails for
   * some reason, then the normal {@link Object#toString()} method
   * return value is returned instead.</p>
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link Pattern}
   */
  @Override
  public String toString() {
    final Program<T> program = this.getProgram();
    assert program != null;
    final String returnValue;
    final Object source = program.getSource();
    if (source == null) {
      returnValue = super.toString();
    } else {
      returnValue = source.toString();
    }
    return returnValue;
  }


  /*
   * Static methods.
   */


  /**
   * Compiles a new {@link Pattern} from the supplied source code.
   *
   * @param source the source code for the {@link Pattern}; must not
   * be {@code null}
   *
   * @return a new, non-{@code null} {@link Pattern}
   * 
   * @exception IllegalArgumentException if {@code source} is {@code
   * null}
   *
   * @exception IOException if the source code could not be compiled
   *
   * @see <a href="../../../../syntax.html">Syntax Guide</a>
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
