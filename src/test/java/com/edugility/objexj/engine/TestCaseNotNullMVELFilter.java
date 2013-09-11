package com.edugility.objexj.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    final Map<Object, Object> variables = new HashMap<Object, Object>(3);
    new MVELFilter<Character>("arglebargle + 1").accept(Character.valueOf('a'), variables);
  }
  
  @Test
  public void testMVELFilter() {
    final MVELFilter<Character> filter = new MVELFilter<Character>("x = 'foo'; this != null && charValue() == 'a'");
    final Map<Object, Object> variables = new HashMap<Object, Object>(3);
    assertTrue(filter.accept(Character.valueOf('a'), variables));
    assertTrue(variables.containsKey("x"));
    variables.clear();    
    assertFalse(filter.accept(Character.valueOf('b'), variables));
    assertTrue(variables.isEmpty());
    assertFalse(filter.accept((Character)null, variables));
  }

}
