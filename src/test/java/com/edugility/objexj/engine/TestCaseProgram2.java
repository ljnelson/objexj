/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
 *
 * Copyright (c) 2013 Edugility LLC.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * The original copy of this license is available at
 * http://www.opensource.org/license/mit-license.html.
 */
package com.edugility.objexj.engine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.edugility.objexj.engine.Thread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestCaseProgram2 extends AbstractProgramTestCase<Character> {

  public TestCaseProgram2() {
    super();
  }

  @Test
  public void testMatchWithAAB() {
    final Thread<Character> t = this.run(Arrays.asList('a', 'a', 'b'));
    assertMatch(t);
    final Map<Object, List<? extends Character>> submatches = t.getSubmatches();
    assertNotNull(submatches);
    assertEquals(2, submatches.size());
    assertTrue(submatches.containsKey("FIRST"));
    assertTrue(submatches.containsKey("SECOND"));
    assertEquals(Arrays.asList('a', 'a'), submatches.get("FIRST"));
    assertEquals(Arrays.asList('b'), submatches.get("SECOND"));
  }

}
