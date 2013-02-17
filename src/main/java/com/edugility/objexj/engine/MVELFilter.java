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

import org.mvel2.MVEL;

import org.mvel2.integration.impl.MapVariableResolverFactory;

public class MVELFilter<T> extends Filter<T> {

  private static final long serialVersionUID = 1L;

  protected String mvelExpressionSource;

  protected Object mvelExpression;

  protected MVELFilter() {
    super();
  }
  
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

  @Override
  public boolean accept(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context", new NullPointerException("context == null"));
    }
    final boolean returnValue;
    if (this.mvelExpression == null) {
      returnValue = context.canRead(); // no MVEL expression means no additional constraints
    } else {
      returnValue = context.canRead() && this.accept(context.read(), context.getVariables());
    }
    return returnValue;
  }

  public boolean accept(final T item, Map<Object, Object> variables) {
    if (variables == null) {
      variables = new HashMap<Object, Object>();
    }
    final boolean returnValue;
    if (this.mvelExpression == null) {
      returnValue = true;
    } else {
      final Object executionResult = MVEL.executeExpression(this.mvelExpression, item, new MapVariableResolverFactory(variables));
      returnValue = executionResult instanceof Boolean && ((Boolean)executionResult).booleanValue();
    }
    return returnValue;
  }

  @Override
  public int hashCode() {
    if (this.mvelExpressionSource == null) {
      return 0;
    }
    return this.mvelExpressionSource.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (super.equals(other)) {
      final MVELFilter him = (MVELFilter)other;
      if (this.mvelExpressionSource == null) {
        return him.mvelExpressionSource == null;
      }
      return this.mvelExpressionSource.equals(him.mvelExpressionSource);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(super.toString());
    if (this.mvelExpressionSource != null) {
      sb.append(" ").append(this.mvelExpressionSource);
    }
    return sb.toString();
  }

}
