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

import com.edugility.objexj.engine.Engine;
import com.edugility.objexj.engine.MatchResult;
import com.edugility.objexj.engine.Program;

public class Matcher<T> {

  private final Engine<T> engine;
  
  private final Program<T> program;

  private List<T> items;

  private MatchResult<T> matchResult;

  public Matcher(final Engine<T> engine, final Program<T> program, final List<T> items) {
    super();
    if (engine == null) {
      throw new IllegalArgumentException("engine", new NullPointerException("engine"));
    }
    if (program == null) {
      throw new IllegalArgumentException("program", new NullPointerException("program"));
    }
    this.engine = engine;
    this.program = program;
    this.items = items;
  }

  public boolean matches() {
    final MatchResult<T> matchResult = this.getMatchResult();
    return matchResult != null && matchResult.matches();
  }

  public boolean lookingAt() {
    final MatchResult<T> matchResult = this.getMatchResult();
    return matchResult != null && matchResult.lookingAt();
  }

  public int groupCount() {
    final MatchResult<T> matchResult = this.getMatchResult();
    final int result;
    if (matchResult == null) {
      result = 0;
    } else {
      result = matchResult.groupCount();
    }
    return result;
  }

  public List<T> group(final int index) {
    final MatchResult<T> matchResult = this.getMatchResult();
    final List<T> result;
    if (matchResult == null) {
      result = null;
    } else {
      result = matchResult.group(index);
    }
    return result;
  }

  public Object get(final Object key) {
    final MatchResult<T> matchResult = this.getMatchResult();
    final Object result;
    if (matchResult == null) {
      result = null;
    } else {
      result = matchResult.get(key);
    }
    return result;
  }

  private final Engine<T> getEngine() {
    return this.engine;
  }

  private final Program<T> getProgram() {
    return this.program;
  }
  
  private final MatchResult<T> getMatchResult() {
    if (this.matchResult == null) {
      final Program<T> program = this.getProgram();
      assert program != null;
      final Engine<T> engine = this.getEngine();
      assert engine != null;
      this.matchResult = engine.run(program, this.items);
    }
    return this.matchResult;
  }

}
