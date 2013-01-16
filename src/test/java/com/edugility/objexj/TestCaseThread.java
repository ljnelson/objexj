package com.edugility.objexj;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseThread extends BasicThreadScheduler<Character> {

  private static final ProgramCounter<Character> simpleProgramCounter;
  
  static {
    simpleProgramCounter = new ProgramCounter<Character>(Program.singleton(new NotNullMVELFilter<Character>("this.charValue() == 'a'")));
  }

  public TestCaseThread() {
    super();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRejectsAllNullArguments() {
    new Thread<Character>(null, null, 0, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRejectsAllNullArgumentsExceptThreadScheduler() {
    new Thread<Character>(null, null, 0, this);
  }


  /*
   * Tests for null input lists.
   */


  @Test
  public void testAcceptsNullItemsListWithValidNoInputPointer() {
    new Thread<Character>(simpleProgramCounter, null, Thread.VALID_NO_INPUT_POINTER, this);
  }

  @Test
  public void testAcceptsNullItemsListWithZeroValuedItemPointer() {
    new Thread<Character>(simpleProgramCounter, null, 0, this);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRejectsNullItemsListWithPositiveItemPointerGreaterThanItemsSize() {
    new Thread<Character>(simpleProgramCounter, null, 2 /* random integer greater than Collections.emptyList().size() */, this);
  }


  /*
   * Tests for empty input lists.
   */


  @Test
  public void testAcceptsEmptyItemsListWithValidNoInputPointer() {
    new Thread<Character>(simpleProgramCounter, Collections.<Character>emptyList(), Thread.VALID_NO_INPUT_POINTER, this);
  }

  @Test
  public void testAcceptsEmptyItemsListWithZeroValuedItemPointer() {
    new Thread<Character>(simpleProgramCounter, Collections.<Character>emptyList(), 0, this);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRejectsEmptyItemsListWithPositiveItemPointerGreaterThanItemsSize() {
    new Thread<Character>(simpleProgramCounter, Collections.<Character>emptyList(), 2 /* random integer greater than Collections.emptyList().size() */, this);
  }

  @Test
  public void testThreadStep() {
    final List<Character> items = Arrays.asList('a', 'b');

    final Thread<Character> thread = this.newThread(simpleProgramCounter, items, 0);
    assertNotNull(thread);

    // This thread is running a program with a single instruction that
    // matches 'a'.
    thread.step();

    // So afterwards there are no further instructions to run, so this
    // thread is dead.
    assertSame(Thread.State.DEAD, thread.getState());
  }

  @Test(expected = IllegalStateException.class)
  public void testThreadStepPastDeath() {
    final List<Character> items = Arrays.asList('a', 'b');

    final Thread<Character> thread = this.newThread(simpleProgramCounter, items, 0);
    assertNotNull(thread);
    thread.step();
    assertSame(Thread.State.DEAD, thread.getState());
    thread.step();
  }

}