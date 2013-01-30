package com.edugility.objexj.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseProgram1 extends AbstractProgramTestCase<Character> {

  public TestCaseProgram1() {
    super();
  }

  @Test
  public void testNoMatchWithSingleCharacter() {
    final Thread<Character> t = this.run(Arrays.asList('a'));
    assertDead(t);
  }

  @Test
  public void testMatchWithAAB() {
    final Thread<Character> t = this.run(Arrays.asList('a', 'a', 'b'));
    assertMatch(t);
  }

}
