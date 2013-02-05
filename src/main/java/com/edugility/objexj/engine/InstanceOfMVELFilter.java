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

import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mvel2.MVEL;

/**
 * An {@link MVELFilter} that conveniently checks the {@linkplain
 * InstructionContext#read() current item} to see if it is an instance
 * of a specified class before applying further MVEL-based filters to
 * it.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 */
public class InstanceOfMVELFilter<T> extends MVELFilter<T> {

  private static final long serialVersionUID = 1L;

  private static final Pattern OPERAND_PATTERN = Pattern.compile("^\\s*([^\\s]+)\\s*(.*)");

  private final Class<?> cls;

  /**
   * Creates a new {@link NotNullMVELFilter}.
   *
   * @param operands the operands to parse; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code operands} is {@code
   * null}
   */
  public InstanceOfMVELFilter(final String operands) {
    super();
    if (operands == null) {
      throw new IllegalArgumentException("operands", new NullPointerException("operands"));
    }
    final Matcher m = OPERAND_PATTERN.matcher(operands);
    assert m != null;
    if (!m.find()) {
      throw new IllegalArgumentException("Bad operands: " + operands);
    }
    final String className = m.group(1);
    assert className != null;

    Class<?> c = null;
    try {
      c = this.loadClass(className);
    } catch (final ClassNotFoundException cnfe) {
      throw new IllegalArgumentException("Bad operands: " + operands, cnfe);
    } finally {
      this.cls = c;
    }

    final String mvel = m.group(2);
    if (mvel == null) {
      this.mvelExpression = null;
      this.mvelExpressionSource = null;
    } else {
      this.mvelExpression = MVEL.compileExpression(mvel);
      this.mvelExpressionSource = mvel;
    }
  }

  public InstanceOfMVELFilter(final String className, final String mvel) {
    super();
    if (className == null) {
      throw new IllegalArgumentException("className", new NullPointerException("className"));
    }
    Class<?> c = null;
    try {
      c = this.loadClass(className);
    } catch (final ClassNotFoundException cnfe) {
      throw new IllegalArgumentException("className", cnfe);
    } finally {
      this.cls = c;
    }
    if (mvel == null) {
      this.mvelExpression = null;
      this.mvelExpressionSource = null;
    } else {
      this.mvelExpression = MVEL.compileExpression(mvel);
      this.mvelExpressionSource = mvel;
    }
  }

  public InstanceOfMVELFilter(final Class<?> c) {
    this(c, null);
  }

  public InstanceOfMVELFilter(final Class<?> c, final String mvel) {
    super();
    if (c == null) {
      throw new IllegalArgumentException("c", new NullPointerException("c"));
    }
    this.cls = c;
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
    return this.cls != null && context.canRead() && this.accept(context.read(), context.getVariables());
  }

  @Override
  public boolean accept(final T item, Map<Object, Object> variables) {
    return item != null && this.cls != null && this.cls.isInstance(item) && super.accept(item, variables);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(this.getClass().getName());
    sb.append(" ").append(this.cls.getName());
    if (this.mvelExpressionSource != null) {
      sb.append(" ").append(this.mvelExpressionSource);
    }
    return sb.toString();
  }

}
