<!-- -*- markdown -*- -->
# Syntax

This guide describes the syntax of `objexj` patterns.

`objexj` syntax is heavily inspired by ordinary regular expression
syntax, but jettisons constructs that are not needed.

## Patterns

A _pattern_ is either an _atom_, a _group_ of patterns, a
_concatenation_ of patterns, an _alternation_ of patterns, or a
_sequence_ of patterns.

### Atoms

An `objexj` _atom_ is like a single character in a textual regular
expression.  It matches (or does not match) a Java object.  For
example, the following atom matches any object that is an instance of
(`instanceof`) `java.lang.Character`:

    java.lang.Character
    
If the atom is preceded with an equals sign ('`=`'), then class
equality is required.  For example, the following filter matches any
object whose class is equal to `java.lang.RuntimeException`:

    =java.lang.RuntimeException
    
Notably this filter does not match, for example,
`java.lang.IllegalArgumentException` instances or
`java.lang.IllegalStateException` instances, because although such
instances are instances of `java.lang.RuntimeException` their classes
are not equal to `java.lang.RuntimeException`.

#### Expressions

Each atom can be augmented with an [MVEL][1] expression that forms the
_condition_. The condition is separated from the atom itself by
parentheses.

For example, the following atom and condition matches any object that
both is an instance of `java.lang.RuntimeException` and has a
`message` property value&#8212;as returned by its `getMessage()`
method&#8212;equal to "`fred`":

    java.lang.RuntimeException(message == "fred")

<h3 id="groups">Groups</h3>

A _group_ is one or more patterns surrounded by parentheses.  Groups
serve two purposes: they allow composition of patterns, and they also
serve as capture groups (see [Capture Groups][2]).

Here is a group consisting of a single atom that matches instances of
`java.lang.RuntimeException` whose `message` property is equal to
"`fred`".  The group is retrievable later under the index `1` (more on
this in the [Capture Groups][2] section):

    (java.lang.RuntimeException(message == "fred"))

### Concatenation

Two patterns may be concatenated into a single pattern by joining them
together with a solidus (slash) character ('`/`').  The result is a
pattern that is applied in order to sequences of objects returned by a
`List`'s `Iterator`.  For example, the following pattern matches any
`java.lang.RuntimeException` instance followed in a list's iteration
by a `java.lang.Exception` instance:

    java.lang.RuntimeException/java.lang.Exception
    
### Alternation

Two patterns may be alternated by joining them together into a single
pattern with a vertical bar ('`|`').  Patterns formed in this manner
match any object that meets the condition of the first atom or the
second atom.  For example, the following pattern matches any object
that is either a `java.lang.Integer` or a `java.lang.Double`:

    java.lang.Integer|java.lang.Double
    
### Sequences

A pattern becomes a sequence when any of the following operators is
appended to it:

 * **One or more (`+`):** Causes the modified pattern to match one or
     more occurrences of itself.  For example, the following pattern
     matches one or more consecutive occurrences of a
     `java.lang.RuntimeException` in a `List`:
     
         java.lang.RuntimeException+
 
 * **Zero or more (`*`):** Causes the modified pattern to match zero or
     more occurrences of itself.  For example, the following pattern
     matches nothing, and also matches any consecutive string of
     `java.lang.Number` instances:
     
         java.lang.Number*
 
 * **Zero or one (`?`):** Causes the modified pattern to match zero or
     exactly one occurrence of itself.  For example, the following
     pattern matches zero or one occurrences of a
     `java.lang.CharSequence` instance:
     
         java.lang.CharSequence?
         
<h2 id="capture_groups">Capture Groups</h2>

A group (see [Groups][3]) is automatically a _capture group_.  If a
match occurs, the group is "captured" under a numeric index.  Consider
the following pattern:

    (java.lang.RuntimeException/(java.lang.Exception))
    
Capture group `1` above consists of a `RuntimeException` followed by
an `Exception` (capture group `0` is always present, and always
captures the entire match).  If a match occurs, then asking the
`${project.artifactId}` engine to return group `1` will return a
`List` whose only two elements are these objects.  Asking the engine
to return group `2` will return a `List` consisting only of the object
that was matched by the `java.lang.Exception` pattern.

As in most regular expression implementations, group `0` returns the
whole match.

## Variables

An expression can set an arbitrary (global) variable that can be
retrieved by name later.  For example, the following atom has an
expression that sets a variable named `foo` to the value of the
particular `RuntimeException`'s `message` property:

    java.lang.RuntimeException(foo = message; return true;)
    
(The MVEL expression in this case returns `true` so that no further
conditions are placed on the match.)

Variables are almost like capture groups but are inherently scalar and
thus do not necessarily return `List`s, and when specified in
different parts of the overall pattern may overwrite previous
assignments.  They are good for simple, one-shot assignments that
capture a particular portion of a single object matched by an atom.

## Anchors

### Beginning-of-Input Anchor

A pattern can be _anchored_ to the beginning of the input by beginning
the pattern with '`^`'.  This anchor may be present only once in a
pattern string.  Here is an example:

    ^java.lang.RuntimeException
    
This pattern matches a `RuntimeException` instance if and only if it
appears at position `0` in the input `List` being matched.

### End-of-Input Anchor

A pattern can be anchored to the end of the input by ending the
pattern with '`$`'.  This anchor may be present only once in a pattern
string.  Here is an example:

    java.lang.RuntimeException$
    
This pattern matches a `RuntimeException` instance if and only if it
appears in the last position of the input `List` being matched.

[1]: http://mvel.codehaus.org/
[2]: #capture_groups
[3]: #groups
