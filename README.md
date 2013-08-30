<!-- -*- markdown -*- -->
# `objexj`

## Regular Expressions for Java Objects

### January 16, 2012

### [Laird Nelson][1]

`objexj` is a small project that provides the ability to match *input*
(a `List` of items) against an *expression*.

That of course is what regular expressions are all about.

The difference here is that you can use any Java `Object` you want,
not just characters in a string.

The syntax hews close to Perl-compatible regular expressions, with a
few modifications.

The underlying machinery is heavily inspired by [Russ Cox][2]'s
excellent paper entitled [Regular Expression Matching: the Virtual
Machine Approach][3].

### Sample Code


    @Test
    public void testLastException() throws IOException {
      final String sourceCode = "^java.lang.Exception*/(java.lang.Exception(message == \"third\"))$";
      final Pattern<Exception> pattern = Pattern.compile(sourceCode);
      assertNotNull(pattern);

      final List<Exception> input = new ArrayList<Exception>();
      input.add(new IllegalStateException("first"));
      input.add(new IllegalArgumentException("second"));
      final Exception third = new RuntimeException("third");
      input.add(third);

      final Matcher<Exception> matcher = pattern.matcher(input);
      assertNotNull(matcher);

      assertEquals(2, matcher.groupCount());

      final List<Exception> group1 = matcher.group(1);
      assertNotNull(group1);
      assertEquals(1, group1.size());

      final Exception group1Exception = group1.get(0);
      assertSame(third, group1Exception);
    }


[1]: http://about.me/lairdnelson
[2]: https://plus.google.com/116810148281701144465/posts
[3]: http://swtch.com/~rsc/regexp/regexp2.html
