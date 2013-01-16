package com.edugility.objexj;

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

  private static final Pattern OPERAND_PATTERN = Pattern.compile("^\\s*([^\\s]+)\\s+(.*)");

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
    return this.cls != null && context.canRead() && this.accept(context.read());
  }

  @Override
  public boolean accept(final T item, Map<Object, Object> variables) {
    return item != null && this.cls != null && this.cls.isInstance(item) && super.accept(item, variables);
  }

}