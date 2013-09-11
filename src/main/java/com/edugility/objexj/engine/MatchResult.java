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
package com.edugility.objexj.engine;

import java.io.Serializable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.edugility.objexj.engine.Thread;

/**
 * The result of a {@link Thread} that has successfully {@linkplain
 * Thread#match() matched} its input.
 *
 * @param <T> the type of {@link Object} the matching {@link Thread}
 * works with
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Thread#match()
 */
public class MatchResult<T> implements Serializable {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The {@link Thread} that caused the match to happen.
   *
   * <p>This field may be {@code null}.</p>
   */
  private final Thread<? extends T> thread;

  /**
   * Creates a new {@link MatchResult}.
   */
  MatchResult() {
    super();
    this.thread = null;
  }

  /**
   * Creates a new {@link MatchResult}.
   *
   * @param thread the {@link Thread} that {@linkplain Thread#match()
   * matched}; may be {@code null}
   */
  MatchResult(final Thread<? extends T> thread) {
    super();
    if (thread != null && Thread.State.MATCH != thread.getState()) {
      throw new IllegalArgumentException(String.format("Thread.State.MATCH != thread.getState(): %s", thread.getState()));
    }
    this.thread = thread;
  }

  /**
   * Returns a non-{@code null} {@link Logger} for logging messages
   * from this class.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a non-{@code null} {@link Logger}
   */
  protected Logger getLogger() {
    return Logger.getLogger(this.getClass().getName());
  }

  /**
   * Returns {@code true} if the {@link #lookingAt()} method returns
   * {@code true} and if the underlying {@link Thread} was {@linkplain
   * Thread#atEnd() at the end of its input} when the match occurred.
   *
   * @return {@code true} if the {@link #lookingAt()} method returns
   * {@code true} and if the underlying {@link Thread} was {@linkplain
   * Thread#atEnd() at the end of its input} when the match occurred.
   */
  public boolean matches() {
    return this.lookingAt() && this.thread.atEnd();
  }

  /**
   * Returns {@code true} if the underlying {@link Thread} {@linkplain
   * #MatchResult(Thread) supplied at construction time} has a
   * {@linkplain Thread#getState() state} of {@link
   * Thread.State#MATCH}.
   *
   * @return {@code true} if the underlying {@link Thread} {@linkplain
   * #MatchResult(Thread) supplied at construction time} has a
   * {@linkplain Thread#getState() state} of {@link
   * Thread.State#MATCH}.
   */
  public boolean lookingAt() {
    return this.thread != null && this.thread.getState() == Thread.State.MATCH;
  }

  /**
   * Returns a {@link List} of items captured as a <em>capture
   * group</em> under the supplied {@code key}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param key the key of the capture group {@link
   * List} to return; may be {@code null}
   *
   * @return a {@link List} of items captured, or {@code null} if
   * there is no such {@link List}
   *
   * @see Thread#getGroup(Object)
   */
  public List<? extends T> getGroup(final Object key) {
    final String className = this.getClass().getName();
    final Logger logger = this.getLogger();
    final boolean finer = logger != null && logger.isLoggable(Level.FINER);
    if (finer) {
      logger.entering(className, "getGroup", key);
    }      
    List<? extends T> result = null;
    if (this.thread != null) {
      result = thread.getGroup(key);
    }
    if (finer) {
      logger.exiting(className, "getGroup", result);
    }      
    return result;
  }

  /**
   * Returns the total number of <em>capture groups</em> present in
   * this {@link MatchResult}.
   *
   * @return the total number of capture groups present in this {@link
   * MatchResult}; always an {@code int} greater than or equal to
   * {@code 0}
   *
   * @see #getGroup(Object)
   *
   * @see Thread#getGroupCount()
   */
  public int getGroupCount() {
    final String className = this.getClass().getName();
    final Logger logger = this.getLogger();
    final boolean finer = logger != null && logger.isLoggable(Level.FINER);
    if (finer) {
      logger.entering(className, "getGroupCount");
    }
    final int result;
    if (this.thread == null) {
      result = 0;
    } else {
      result = this.thread.getGroupCount();
    }
    if (finer) {
      logger.exiting(className, "getGroupCount", Integer.valueOf(result));
    }
    return result;
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableSet(Set)
   * unmodifiable} {@link Set} of {@link Object}s representing valid
   * keys that can be used to {@linkplain #getGroup(Object) capture
   * groups}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a non-{@code null} {@link Set} of keys, each element of
   * which can be used as an argument to the {@link #getGroup(Object)}
   * method
   */
  public Set<?> getGroupKeySet() {
    final String className = this.getClass().getName();
    final Logger logger = this.getLogger();
    final boolean finer = logger != null && logger.isLoggable(Level.FINER);
    if (finer) {
      logger.entering(className, "getGroupKeySet");
    }
    final Set<?> returnValue;
    if (this.thread == null) {
      returnValue = Collections.emptySet();
    } else {
      returnValue = this.thread.getGroupKeySet();
    }
    if (finer) {
      logger.exiting(className, "getGroupKeySet", returnValue);
    }
    return returnValue;
  }

  /**
   * Returns the value of a variable indexed under the supplied {@code
   * key}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param key the name of the variable; may be {@code null}
   *
   * @return the value of the variable, or {@code null} if there is no
   * such variable
   *
   * @see #getVariables()
   */
  public Object getVariable(final Object key) {
    final String className = this.getClass().getName();
    final Logger logger = this.getLogger();
    final boolean finer = logger != null && logger.isLoggable(Level.FINER);
    if (finer) {
      logger.entering(className, "getVariable", key);
    }
    Object result = null;
    if (this.thread != null) {
      final Map<?, ?> variables = this.getVariables();
      if (variables != null) {
        result = variables.get(key);
      }
    }
    if (finer) {
      logger.exiting(className, "getVariable", result);
    }
    return result;
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableMap(Map)
   * unmodifiable <code>Map</code>} of the variables set on the {@link
   * Thread} that this {@link MatchResult} wraps.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return a {@link Map} of variables, or {@code null}
   */
  public Map<?, ?> getVariables() {
    final String className = this.getClass().getName();
    final Logger logger = this.getLogger();
    final boolean finer = logger != null && logger.isLoggable(Level.FINER);
    if (finer) {
      logger.entering(className, "getVariables");
    }
    Map<?, ?> result = null;
    if (this.thread != null) {
      result = this.thread.getVariables();
    }
    if (finer) {
      logger.exiting(className, "getVariables", result);
    }
    return result;
  }

}
