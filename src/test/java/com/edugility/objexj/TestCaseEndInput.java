package com.edugility.objexj;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseEndInput extends BasicThreadScheduler<Character> {

  public TestCaseEndInput() {
    super();
  }

  @Test
  public void testAtEnd() {

    /*
     * Set up a (bogus, virtually nonsensical) program that runs two
     * EndInput instructions, which in normal circumstances would be
     * stupid.  The second one should fail, obviously, as the program
     * counter will no longer be at the end.
     *
     * This program will have no input (which is legal).
     */

    final Program<Character> p = new Program<Character>();
    p.add(new EndInput<Character>());
    p.add(new EndInput<Character>());
    assertNotNull(p);

    final Thread<Character> t = this.newThread(0, new ProgramCounter<Character>(p), null /* no input */, Thread.VALID_NO_INPUT_POINTER, null, null);
    assertNotNull(t);

    t.step();
    assertEquals(Thread.State.VIABLE, t.getState());

    t.step();
    assertEquals(Thread.State.DEAD, t.getState());
  }  

}
