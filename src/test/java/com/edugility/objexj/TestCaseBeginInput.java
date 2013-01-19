package com.edugility.objexj;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseBeginInput extends BasicThreadScheduler<Character> {

  public TestCaseBeginInput() {
    super();
  }

  @Test
  public void testAtStartWithNullInput() {

    /*
     * Set up a (bogus, virtually nonsensical) program that runs two
     * BeginInput instructions, which in normal circumstances would be
     * stupid.  The second one should fail, obviously, as the program
     * counter will no longer be at the beginning.
     *
     * This program will have no input (which is legal).
     */

    final Program<Character> p = new Program<Character>();
    p.add(new BeginInput<Character>());
    p.add(new BeginInput<Character>());
    assertNotNull(p);

    final Thread<Character> t = this.newThread("T0", new ProgramCounter<Character>(p), null /* no input */, Thread.VALID_NO_INPUT_POINTER, null, null);
    assertNotNull(t);

    t.step();
    assertEquals(Thread.State.VIABLE, t.getState());

    t.step();
    assertEquals(Thread.State.DEAD, t.getState());
  }  

  @Test
  public void testAtStartWithEmptyInput() {

    /*
     * Set up a (bogus, virtually nonsensical) program that runs two
     * BeginInput instructions, which in normal circumstances would be
     * stupid.  The second one should fail, obviously, as the program
     * counter will no longer be at the beginning.
     *
     * This program will have empty input (which is legal).
     */

    final Program<Character> p = new Program<Character>();
    p.add(new BeginInput<Character>());
    p.add(new BeginInput<Character>());
    assertNotNull(p);

    final Thread<Character> t = this.newThread("T0", new ProgramCounter<Character>(p), Collections.<Character>emptyList() /* empty input */, Thread.VALID_NO_INPUT_POINTER, null, null);
    assertNotNull(t);
    t.step();
    assertEquals(Thread.State.VIABLE, t.getState());

    t.step();
    assertEquals(Thread.State.DEAD, t.getState());
  }  

  @Test
  public void testAtStartWithFullInput() {
    final Program<Character> p = new Program<Character>();
    p.add(new BeginInput<Character>());
    p.add(new BeginInput<Character>());
    assertNotNull(p);

    final List<Character> input = Arrays.asList('a', 'b', 'c', 'd');

    final Thread<Character> t = this.newThread("T0", new ProgramCounter<Character>(p), input, 0, null, null);
    assertNotNull(t);

    t.step();
    assertEquals(Thread.State.VIABLE, t.getState());

    t.step();
    assertEquals(Thread.State.DEAD, t.getState());
  }  

}
