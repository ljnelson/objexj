package com.edugility.objexj.engine;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.mvel2.CompileException;

import static org.junit.Assert.*;

public class TestCaseNotNullMVELFilter extends BasicThreadScheduler<Character> {

  public TestCaseNotNullMVELFilter() {
    super();
  }

  @Test(expected = CompileException.class)
  public void testBadMVEL() throws Throwable {
    new MVELFilter<Character>("arglebargle + 1").accept(Character.valueOf('a'), null);
  }
  
  @Test
  public void testMVELFilter() {
    final MVELFilter<Character> filter = new MVELFilter<Character>("this != null && charValue() == 'a'");
    assertTrue(filter.accept(Character.valueOf('a'), null));
    assertFalse(filter.accept(Character.valueOf('b'), null));
    assertFalse(filter.accept((Character)null, null));
  }

}
