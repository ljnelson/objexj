package com.edugility.objexj.engine;

import java.io.Serializable;

import java.util.List;
import java.util.Map;

import com.edugility.objexj.engine.Thread;

public class MatchResult<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Thread<T> thread;

  MatchResult() {
    super();
    this.thread = null;
  }

  MatchResult(final Thread<T> thread) {
    super();
    if (thread != null && Thread.State.MATCH != thread.getState()) {
      throw new IllegalArgumentException(String.format("Thread.State.MATCH != thread.getState(): %s", thread.getState()));
    }
    this.thread = thread;
  }

  public boolean matches() {
    return this.lookingAt() && this.thread.atEnd();
  }

  public boolean lookingAt() {
    return this.thread != null && this.thread.getState() == Thread.State.MATCH;
  }

  public List<T> group(final int index) {
    List<T> result = null;
    if (this.thread != null) {
      result = thread.getGroup(Integer.valueOf(index));
    }
    return result;
  }

  public int groupCount() {
    final int result;
    if (this.thread == null) {
      result = 0;
    } else {
      result = this.thread.getGroupCount();
    }
    return result;
  }

  public Object get(final Object key) {
    Object result = null;
    if (this.thread != null) {
      final Map<?, ?> variables = this.getVariables();
      if (variables != null) {
        result = variables.get(key);
      }
    }
    return result;
  }

  public Map<?, ?> getVariables() {
    Map<?, ?> result = null;
    if (this.thread != null) {
      result = this.thread.getVariables();
    }
    return result;
  }

}