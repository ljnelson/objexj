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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.edugility.objexj.engine.Engine;
import com.edugility.objexj.engine.MatchResult;
import com.edugility.objexj.engine.Program;

/**
 * An object that matches a {@link Pattern} against a {@link List} of
 * items (the <em>input</em>).
 *
 * @param <T> the kind of {@link Object} any input consists of
 *
 * @author <a href="http://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 *
 * @see #matches()
 *
 * @see #lookingAt()
 */
public class Matcher<T> {

  /**
   * The {@link Pattern} that this {@link Matcher} is going to apply.
   * This field is never {@code null}.
   *
   * @see #getPattern()
   *
   * @see #Matcher(Pattern, List)
   */
  private final Pattern<T> pattern;

  /**
   * The {@link List} of items against which a match will be
   * attempted.  This field may be {@code null}.
   *
   * @see #getInput()
   */
  private List<T> input;

  /**
   * A {@link MatchResult} that contains the state of the last match
   * attempt.  This field may be {@code null}.
   */
  private transient MatchResult<T> matchResult;

  /**
   * Creates a {@link Matcher} with the supplied {@link Pattern} and
   * input.
   *
   * @param pattern the {@link Pattern} to apply; must not be {@code
   * null}
   *
   * @param input a possibly {@code null} {@link List} of items to
   * match the supplied {@link Pattern} against
   * 
   * @exception IllegalArgumentException if {@code pattern} is {@code
   * null}
   */
  Matcher(final Pattern<T> pattern, final List<T> input) {
    super();
    if (pattern == null) {
      throw new IllegalArgumentException("pattern", new NullPointerException("pattern"));
    }
    this.pattern = pattern;
    this.input = input;
  }

  /**
   * Returns {@code true} if this {@link Matcher} matches the entire
   * input against its {@linkplain #getPattern() affiliated
   * <tt>Pattern</tt>}.
   *
   * @return {@code true} if this {@link Matcher} matches the entire
   * input against its {@linkplain #getPattern() affiliated
   * <tt>Pattern</tt>}; {@code false} otherwise
   */
  public final boolean matches() {
    final MatchResult<T> matchResult = this.getMatchResult();
    return matchResult != null && matchResult.matches();
  }

  /**
   * Returns {@code true} if a <em>prefix</em> of this {@link
   * Matcher}'s {@linkplain #getInput() input} matches this {@link
   * Matcher}'s {@linkplain #getPattern() affiliated
   * <tt>Pattern</tt>}.
   *
   * @return {@code true} if a <em>prefix</em> of this {@link
   * Matcher}'s {@linkplain #getInput() input} matches this {@link
   * Matcher}'s {@linkplain #getPattern() affiliated
   * <tt>Pattern</tt>}; {@code false} otherwise
   */
  public final boolean lookingAt() {
    final MatchResult<T> matchResult = this.getMatchResult();
    return matchResult != null && matchResult.lookingAt();
  }

  /**
   * Returns the total number of <em>capture groups</em> matched by
   * this {@link Matcher}.  Any successful match will cause this
   * method to return an {@code int} greater than or equal to {@code
   * 1}.  Group indices are numbered starting with {@code 0}.
   *
   * @return the total number of capture groups matched by this {@link
   * Matcher}; never less than {@code 0}
   */
  public final int groupCount() {
    final MatchResult<T> matchResult = this.getMatchResult();
    final int result;
    if (matchResult == null) {
      result = 0;
    } else {
      result = matchResult.groupCount();
    }
    return result;
  }

  /**
   * Returns the <em>capture group</em> matched by the last match
   * indexed under the supplied zero-based index, or {@code null} if
   * no such capture group was matched.
   *
   * @param index a zero-based number identifying a capture group; may
   * be any number
   *
   * @return a {@link List} of items (a subset of the {@linkplain
   * #getInput() input}), or {@code null} if no such capture group was
   * ever identified
   */
  public final List<T> group(final int index) {
    final MatchResult<T> matchResult = this.getMatchResult();
    final List<T> result;
    if (matchResult == null) {
      result = null;
    } else {
      result = matchResult.group(index);
    }
    return result;
  }

  /**
   * Returns a {@link Map} representing the variables set by this
   * {@link Matcher} during its execution.
   *
   * @return a non-{@code null} {@link Map} of variables
   */
  public final Map<?, ?> getVariables() {
    Map<?, ?> result = null;
    final MatchResult<T> matchResult = this.getMatchResult();
    if (matchResult == null) {
      result = Collections.emptyMap();
    } else {
      result = matchResult.getVariables();
    }
    if (result == null || result.isEmpty()) {
      result = Collections.emptyMap();
    } else {
      result = Collections.unmodifiableMap(result);
    }
    return result;
  }

  /**
   * Returns the value of the <em>variable</em> indexed under the
   * supplied key, or {@code null} if no such value was ever
   * established.  Variables may be set from within a {@link
   * Pattern}'s parsed expression.
   *
   * @param key the name of the variable; may be {@code null} but if
   * so then {@code null} will be returned
   *
   * @return the value of the variable, or {@code null}
   */
  public final Object get(final Object key) {
    final Map<?, ?> variables = this.getVariables();
    final Object result;
    if (variables == null) {
      result = null;
    } else {
      result = variables.get(key);
    }
    return result;
  }

  /**
   * Returns the {@link Pattern} with which this {@link Matcher} is
   * currently affiliated.  This method never returns {@code null}.
   *
   * @return a non-{@code null} {@link Pattern}
   */
  public final Pattern<T> getPattern() {
    return this.pattern;
  }

  /**
   * Returns the input with which this {@link Matcher} is currently
   * affiliated.  This method may return {@code null}.
   *
   * @return the input, or {@code null}
   */
  public final List<T> getInput() {
    return this.input;
  }

  private final Engine<T> getEngine() {
    final Pattern<T> pattern = this.getPattern();
    assert pattern != null;
    return pattern.getEngine();
  }

  private final Program<T> getProgram() {
    final Pattern<T> pattern = this.getPattern();
    assert pattern != null;
    return pattern.getProgram();
  }
  
  private final MatchResult<T> getMatchResult() {
    if (this.matchResult == null) {
      final Program<T> program = this.getProgram();
      assert program != null;
      final Engine<T> engine = this.getEngine();
      assert engine != null;
      this.matchResult = engine.run(program, this.input);
    }
    return this.matchResult;
  }

}
