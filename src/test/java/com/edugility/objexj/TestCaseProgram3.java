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
package com.edugility.objexj;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.edugility.objexj.Thread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestCaseProgram3 extends AbstractProgramTestCase<Character> {

  public TestCaseProgram3() {
    super();
  }

  @Test
  public void testSimpleVariableAssignment() {

    /*
     * NotNullMVELFilter foo = bar; charValue() == 'a'
     * Match
     *
     * This test runs the preceding VM program and asserts that foo is
     * set to bar afterwards.
     *
     * TODO: still not sure about whether a Thread's variables
     * internally should be cloned or not.  Not sure how to test that.
     */

    final Thread<Character> t = this.run(Arrays.asList('a'));
    assertMatch(t);
    final Map<Object, Object> variables = t.getVariables();
    assertNotNull(variables);
    assertTrue(variables.containsKey("foo"));
    assertEquals("bar", variables.get("foo"));
  }

}
