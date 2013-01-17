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
package com.edugility.objexj;

public class InstanceOfFilter<T> extends Filter<T> {

  private static final long serialVersionUID = 1L;

  private final Class<?> cls;

  public InstanceOfFilter(final Class<?> cls) {
    super();
    if (cls == null) {
      throw new IllegalArgumentException("cls", new NullPointerException("cls == null"));
    }
    this.cls = cls;
  }

  public InstanceOfFilter(final String classname) throws ClassNotFoundException {
    super();
    Class<?> temp = null;
    try {
      temp = this.loadClass(classname);
    } finally {
      this.cls = temp;
    }
  }

  protected Class<?> loadClass(final String name) throws ClassNotFoundException {
    final ClassLoader ccl = java.lang.Thread.currentThread().getContextClassLoader();
    final Class<?> returnValue;
    if (ccl != null) {
      returnValue = ccl.loadClass(name);
    } else {
      returnValue = Class.forName(name);
    }
    return returnValue;
  }
  
  @Override
  public boolean accept(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context", new NullPointerException("context == null"));
    }
    boolean returnValue = false;
    if (super.accept(context) && context.canRead()) {
      final T item = context.read();
      assert item != null;
      if (this.cls != null) {
        returnValue = this.cls.isInstance(item);
      }
    }
    return returnValue;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(super.toString());
    if (this.cls != null) {
      sb.append(" ").append(this.cls.getName());
    }
    return sb.toString();
  }

}
