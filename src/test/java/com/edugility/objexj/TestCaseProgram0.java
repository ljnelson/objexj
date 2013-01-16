package com.edugility.objexj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseProgram0 extends AbstractProgramTestCase<Character> {

  public TestCaseProgram0() {
    super();
  }

  @Test
  public void testMatch() {
    final Thread<Character> t = this.run(Arrays.asList('a'));
    assertMatch(t);
  }

  @Test
  public void testNoMatchWithMoreThanOneCharacter() {
    final Thread<Character> t = this.run(Arrays.asList('a', 'a'));
    assertDead(t);
  }

  @Test
  public void testValidInputButNoMatch() {
    final Thread<Character> t = this.run(Arrays.asList('a', 'b'));
    assertDead(t);
  }

  @Test
  public void testNullInputThereforeNoMatch() {
    final Thread<Character> t = this.run(null);
    assertDead(t);
  }

  @Test
  public void testEmptyInputThereforeNoMatch() {
    final Thread<Character> t = this.run(Collections.<Character>emptyList());
    assertDead(t);
  }

}