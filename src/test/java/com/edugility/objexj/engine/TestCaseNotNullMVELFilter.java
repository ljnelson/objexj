package com.edugility.objexj.engine;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseNotNullMVELFilter extends BasicThreadScheduler<Character> {

  public TestCaseNotNullMVELFilter() {
    super();
  }

  @Test
  public void testMVELFilter() {
    final MVELFilter<Character> filter = new MVELFilter<Character>("this != null && charValue() == 'a'");
    assertTrue(filter.accept(Character.valueOf('a')));
    assertFalse(filter.accept(Character.valueOf('b')));
    assertFalse(filter.accept((Character)null));
  }

}
