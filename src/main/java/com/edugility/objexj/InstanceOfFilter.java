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