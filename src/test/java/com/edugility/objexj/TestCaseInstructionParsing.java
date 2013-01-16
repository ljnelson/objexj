package com.edugility.objexj;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseInstructionParsing {

  public TestCaseInstructionParsing() {
    super();
  }

  @Test
  public void testParseSplit() throws Exception {
    final Instruction<Character> i = Instruction.valueOf("Split 4, 8");
    assertNotNull(i);
    assertTrue(i instanceof Split);
    final Split<Character> s = (Split<Character>)i;
    assertEquals(4, s.programLocation);
    assertEquals(8, s.newThreadProgramLocation);
  }

}