package com.edugility.objexj.engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseProgramParsing {

  public TestCaseProgramParsing() {
    super();
  }

  @Test
  public void testValueOf() throws Exception {
    final URL program = java.lang.Thread.currentThread().getContextClassLoader().getResource("program0.txt");
    assert program != null;
    final BufferedReader reader = new BufferedReader(new InputStreamReader(program.openStream()));
    Program<Character> p;
    try {
      p = Program.valueOf(reader);
    } finally {
      reader.close();
    }
    assertNotNull(p);
    assertEquals(3, p.size());
    
  }

}
