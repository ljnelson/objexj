package com.edugility.objexj.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    new Thread<Character>("T0", null, null, 0, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRejectsAllNullArgumentsExceptThreadScheduler() {
    new Thread<Character>("T0", null, null, 0, this);
  }

  @Test
  public void testEquals() {

    final Split<Character> split1 = new Split<Character>(0, 1, true);
    final Split<Character> split2 = new Split<Character>(0, 1, true);

    assertEquals(split1, split2);

    final Split<Character> split3 = new Split<Character>(0, 1, false);
    final Split<Character> split4 = new Split<Character>(0, 1, true);
    
    assertNotEquals(split3, split4);

    final Program<Character> program1 = new Program<Character>(split1);
    final Program<Character> program2 = new Program<Character>(split2);

    assertEquals(program1, program2);

    final ProgramCounter<Character> pc1 = new ProgramCounter<Character>(program1);
    final ProgramCounter<Character> pc2 = new ProgramCounter<Character>(program2);

    assertEquals(pc1, pc2);

    final ArrayList<Character> l1 = new ArrayList<Character>();
    l1.add('a');
    l1.add('b');

    @SuppressWarnings("unchecked")
    final ArrayList<Character> l2 = (ArrayList<Character>)l1.clone();
    assertNotNull(l2);
    assertNotSame(l1, l2);
    assertEquals(l1, l2);
    
    final Thread<Character> t1 = new Thread<Character>("t1", pc1, l1, 0, null, null, this);
    final Thread<Character> t2 = new Thread<Character>("t2", pc2, l2, 0, null, null, this);

    assertEquals(t1, t2);

    final CaptureGroup<Character> cg1 = new CaptureGroup<Character>(l1, 0);
    final CaptureGroup<Character> cg2 = cg1.clone();
    assertNotNull(cg2);
    assertNotSame(cg1, cg2);
    assertEquals(cg1, cg2);

    final Map<Object, CaptureGroup<Character>> map1 = new HashMap<Object, CaptureGroup<Character>>();
    map1.put("key", cg1);

    final Map<Object, CaptureGroup<Character>> map2 = new HashMap<Object, CaptureGroup<Character>>();
    map2.put("key", cg2);

    final Thread<Character> t3 = new Thread<Character>("t3", pc1, l1, 0, map1, null, this);
    final Thread<Character> t4 = new Thread<Character>("t4", pc2, l2, 0, map2, null, this);

    assertEquals(t3, t4);

    final Thread<Character> t5 = new Thread<Character>("t5", pc1, l1, 0, map1, null, this);

    final CaptureGroup<Character> cg3 = new CaptureGroup<Character>(l1, 1);

    final Map<Object, CaptureGroup<Character>> map3 = new HashMap<Object, CaptureGroup<Character>>();
    map2.put("key", cg3);

    final Thread<Character> t6 = new Thread<Character>("t6", pc1, l1, 0, map3, null, this);

    assertNotEquals(t5, t6);
    
  }


  /*
   * Tests for null input lists.
   */


  @Test
  public void testAcceptsNullItemsListWithValidNoInputPointer() {
    new Thread<Character>("T0", simpleProgramCounter, null, Thread.VALID_NO_INPUT_POINTER, this);
  }

  @Test
  public void testAcceptsNullItemsListWithZeroValuedItemPointer() {
    new Thread<Character>("T0", simpleProgramCounter, null, 0, this);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRejectsNullItemsListWithPositiveItemPointerGreaterThanItemsSize() {
    new Thread<Character>("T0", simpleProgramCounter, null, 2 /* random integer greater than Collections.emptyList().size() */, this);
  }


  /*
   * Tests for empty input lists.
   */


  @Test
  public void testAcceptsEmptyItemsListWithValidNoInputPointer() {
    new Thread<Character>("T0", simpleProgramCounter, Collections.<Character>emptyList(), Thread.VALID_NO_INPUT_POINTER, this);
  }

  @Test
  public void testAcceptsEmptyItemsListWithZeroValuedItemPointer() {
    new Thread<Character>("T0", simpleProgramCounter, Collections.<Character>emptyList(), 0, this);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRejectsEmptyItemsListWithPositiveItemPointerGreaterThanItemsSize() {
    new Thread<Character>("T0", simpleProgramCounter, Collections.<Character>emptyList(), 2 /* random integer greater than Collections.emptyList().size() */, this);
  }

  @Test
  public void testThreadStep() {
    final List<Character> items = Arrays.asList('a', 'b');

    final Thread<Character> thread = this.newThread("T0", simpleProgramCounter, items, 0, null, null);
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

    final Thread<Character> thread = this.newThread("T0", simpleProgramCounter, items, 0, null, null);
    assertNotNull(thread);
    thread.step();
    assertSame(Thread.State.DEAD, thread.getState());
    thread.step();
  }

}
