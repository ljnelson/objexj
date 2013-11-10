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

import java.util.HashMap;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mvel2.CompileException; // for javadoc only
import org.mvel2.MVEL;

import org.mvel2.integration.VariableResolverFactory; // for javadoc only

import org.mvel2.integration.impl.MapVariableResolverFactory;

/**
 * A {@link Filter} that implements its {@link
 * #accept(InstructionContext)} method by running a user-supplied <a
 * href="http://mvel.codehaus.org/">MVEL</a> script against the {@link
 * InstructionContext}'s {@linkplain InstructionContext#read() current
 * <code>Object</code>}.
 *
 * @param <T> the type of {@link Object} that can be {@linkplain
 * #accept(Object, Map) accepted}
 *
 * @author <a href="http://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public class MVELFilter<T> extends Filter<T> {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The <a href="http://mvel.codehaus.org/">MVEL</a> source code used
   * to implement the behavior of the {@link #accept(Object, Map)}
   * method.
   *
   * <p>This field may be {@code null}.</p>
   *
   * @see #accept(Object, Map)
   */
  protected String mvelExpressionSource;

  /**
   * The {@linkplain MVEL#compileExpression(String) compiled} form of
   * the {@linkplain #mvelExpressionSource MVEL source code}.
   *
   * <p>This field may be {@code null}.</p>
   *
   * @see #mvelExpressionSource
   */
  protected Object mvelExpression;

  /**
   * Creates a new {@link MVELFilter}.  This constructor is intended
   * for subclasses only.  Subclasses should set the {@link
   * #mvelExpressionSource} and {@link #mvelExpression} fields
   * appropriately as soon as possible, and should treat these fields
   * as though they were declared to be {@code final}.
   */
  protected MVELFilter() {
    super();
  }

  /**
   * Creates a new {@link MVELFilter}.
   *
   * @param mvel the <a href="http://mvel.codehaus.org/">MVEL</a>
   * source code; may be {@code null}
   *
   * @exception CompileException if the source code could not be
   * compiled
   */
  public MVELFilter(final String mvel) {
    super();
    if (mvel == null) {
      this.mvelExpression = null;
      this.mvelExpressionSource = null;
    } else {
      this.mvelExpression = MVEL.compileExpression(mvel);
      this.mvelExpressionSource = mvel;
    }
  }

  /**
   * Accepts or rejects the supplied {@link InstructionContext} during
   * execution by making sure that it {@linkplain
   * InstructionContext#canRead() can be read from} and that the
   * {@link #accept(Object, Map)} method also returns {@code true}.
   *
   * @param context the {@link InstructionContext} in which this
   * {@link MVELFilter} is running; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code context} is {@code
   * null}, or if the return value of the {@link
   * InstructionContext#getVariables()} method is {@code null}
   *
   * @exception CompileException if the {@link #accept(Object, Map)}
   * method throws a {@link CompileException}
   *
   * @see #accept(Object, Map)
   */
  @Override
  public boolean accept(final InstructionContext<? extends T> context) {
    final String className = this.getClass().getName();
    final Logger logger = this.getLogger();
    final boolean finer = logger != null && logger.isLoggable(Level.FINER);
    if (finer) {
      logger.entering(className, "accept", context);
    }
    if (context == null) {
      throw new IllegalArgumentException("context", new NullPointerException("context == null"));
    }
    final boolean returnValue;
    if (this.mvelExpression == null) {
      returnValue = context.canRead(); // no MVEL expression means no additional constraints
    } else {
      final Map<Object, Object> variables = context.getVariables();
      if (variables == null) {
        throw new IllegalArgumentException("context", new IllegalStateException("context.getVariables()", new NullPointerException("context.getVariables()")));
      }
      returnValue = context.canRead() && this.accept(context.read(), variables);
    }
    if (finer) {
      logger.exiting(className, "accept", Boolean.valueOf(returnValue));
    }
    return returnValue;
  }

  /**
   * Returns {@code true} if the {@linkplain
   * MVEL#executeExpression(Object, Object, VariableResolverFactory)
   * execution} of the {@linkplain #mvelExpression MVEL expression}
   * associated with this {@link MVELFilter} returned an {@link
   * Object} {@linkplain Boolean#equals(Object) equal to} {@link
   * Boolean#TRUE} when evaluated against the supplied {@code item}.
   *
   * @param item the {@link Object} that will serve as the context for
   * the {@linkplain #mvelExpression MVEL expression} associated with
   * this {@link MVELFilter} during its evaluation; may be {@code
   * null}
   *
   * @param variables a {@link Map} of variables that may be affected
   * by the {@linkplain #mvelExpression MVEL expression} associated
   * with this {@link MVELFilter}; must not be {@code null}
   *
   * @return {@code true} if the {@linkplain
   * MVEL#executeExpression(Object, Object, VariableResolverFactory)
   * execution} of the {@linkplain #mvelExpression MVEL expression}
   * associated with this {@link MVELFilter} returned an {@link
   * Object} {@linkplain Boolean#equals(Object) equal to} {@link
   * Boolean#TRUE} when evaluated against the supplied {@code item};
   * {@code false} otherwise
   *
   * @exception CompileException if the {@link #mvelExpression}
   * field's value could be {@linkplain MVEL#compileExpression(String)
   * compiled}, but referenced an object or a property that could not
   * be resolved during {@linkplain MVEL#executeExpression(Object,
   * Object, VariableResolverFactory) execution}; thrown by the {@link
   * MVEL#executeExpression(Object, Object, VariableResolverFactory)}
   * method
   *
   * @exception IllegalArgumentException if {@code variables} is
   * {@code null}
   */
  public boolean accept(final T item, final Map<Object, Object> variables) {
    final String className = this.getClass().getName();
    final Logger logger = this.getLogger();
    final boolean finer = logger != null && logger.isLoggable(Level.FINER);
    if (finer) {
      logger.entering(className, "accept", new Object[] { item, variables });
    }
    if (variables == null) {
      throw new IllegalArgumentException("variables", new NullPointerException("variables"));
    }
    final boolean returnValue;
    if (this.mvelExpression == null) {
      returnValue = true;
    } else {
      final Map<Object, Object> oldVariables;
      if (!variables.isEmpty()) {
        oldVariables = new HashMap<Object, Object>(variables);
      } else {
        oldVariables = null;
      }
      final Object executionResult = MVEL.executeExpression(this.mvelExpression, item, new MapVariableResolverFactory(variables));
      if (finer) {
        logger.logp(Level.FINER, className, "accept", "Execution result: {0}; variables after execution: {1}", new Object[] { executionResult, variables });
      }
      if (executionResult instanceof Boolean) {
        returnValue = ((Boolean)executionResult).booleanValue();
      } else {
        returnValue = true;
      }
      if (!returnValue) {
        variables.clear();
        if (oldVariables != null && !oldVariables.isEmpty()) {
          variables.putAll(oldVariables);
        }
      }
    }
    if (finer) {
      logger.exiting(className, "accept", Boolean.valueOf(returnValue));
    }
    return returnValue;
  }

  /**
   * Returns a hashcode for this {@link MVELFilter}.
   *
   * @return a hashcode for this {@link MVELFilter}
   */
  @Override
  public int hashCode() {
    if (this.mvelExpressionSource == null) {
      return 0;
    }
    return this.mvelExpressionSource.hashCode();
  }

  /**
   * Returns {@code true} if the supplied {@link Object} is equal to
   * this {@link MVELFilter}.
   *
   * @param other the {@link Object} to test; may be {@code null} in
   * which case {@code false} will be returned
   */
  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (super.equals(other)) {
      final MVELFilter<?> him = (MVELFilter<?>)other;
      if (this.mvelExpressionSource == null) {
        return him.mvelExpressionSource == null;
      }
      return this.mvelExpressionSource.equals(him.mvelExpressionSource);
    } else {
      return false;
    }
  }

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link MVELFilter}.  If possible, the {@linkplain
   * #mvelExpressionSource source code} will be featured in the {@link
   * String} that is returned.
   *
   * @return a non-{@code null} {@link String} representation of this
   * {@link MVELFilter}.
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(super.toString());
    if (this.mvelExpressionSource != null) {
      sb.append(" ").append(this.mvelExpressionSource);
    }
    return sb.toString();
  }

}
