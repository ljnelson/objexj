package com.edugility.objexj;

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
      returnValue = true; // no MVEL expression means no additional constraints
    } else {
      final Map<Object, Object> variables = new HashMap<Object, Object>(); // XXX TODO FIXME get from context
      returnValue = context.canRead() && this.accept(context.read(), variables);
    }
    return returnValue;
  }

  public final boolean accept(final T item) {
    return this.accept(item, null);
  }
  
  public boolean accept(final T item, Map<Object, Object> variables) {
    if (variables == null) {
      variables = new HashMap<Object, Object>();
    }
    final Object executionResult = MVEL.executeExpression(this.mvelExpression, item, new MapVariableResolverFactory(variables));
    return executionResult instanceof Boolean && ((Boolean)executionResult).booleanValue();
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