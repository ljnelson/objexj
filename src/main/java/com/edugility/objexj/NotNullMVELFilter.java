package com.edugility.objexj;

/**
 * An {@link MVELFilter} that conveniently checks the {@linkplain
 * InstructionContext#read() current item} to see if it is {@code null} before
 * proceeding.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 */
public class NotNullMVELFilter<T> extends MVELFilter<T> {

  private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@link NotNullMVELFilter}.
   *
   * @param mvel the MVEL expression; may (trivially) be {@code null}
   */
  public NotNullMVELFilter(final String mvel) {
    super(mvel);
  }

  @Override
  public boolean accept(final InstructionContext<T> context) {
    if (context == null) {
      throw new IllegalArgumentException("context", new NullPointerException("context == null"));
    }
    return context.canRead() && context.read() != null && super.accept(context);
  }

}