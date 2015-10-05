# Walnut
![xkcd #927: Standards](http://imgs.xkcd.com/comics/standards.png)

*Walnut is living proof of the above comic.*

**In a nutshell**: Keep It Simple, Stupid.

## Overall

### Why another config format? What's wrong with HOCON, YAML, JSON, et al?
I'll go through these in order.

HOCON, while it is a good idea on paper, is extremely complex, ends up being 
more difficult to write than JSON in a lot of cases, and is ridiculously 
complicated to parse. As such, there are almost no implementations for it other 
than the reference implementation, Typesafe Config.

YAML, due to it's reliance on semantic whitespace, can be confusing or 
inconvenient to write if you accidentally mix indentation. This is especially 
easy when you have something such as a default maintainer-provided config, and 
they use a different indentation policy than you. Worse, these problems can be 
invisible in a lot of editors if groups of 4/8 spaces and tabs are mixed, which 
is also the most common error.

JSON, due to it's goal of being a data exchange format, is difficult to write 
due to it's strict syntax. Missing commas in a JSON file and having the entire 
thing fail to parse is extremely common, and rather annoying.


### How does Walnut solve these problems?
It very well may not, depending on your use case.

Walnut *tries* to be better than other config formats by being *extremely 
simple*; we don't go for any extra goals like the ability to parse JSON, or 
being a JSON superset, being under 100 calories, or any other things. Walnut is 
a config format, nothing more.

Walnut also has comparatively strict syntax; You must quote values, and keys 
must be unquoted. You cannot mix value types (e.g. in HOCON, '10foo' is the 
number '10' and the unquoted string 'foo', but in Walnut it is a syntax error 
unless quoted), only double-quoted strings are supported, sections are defined 
using {} syntax only, duplicate keys are a syntax error and are not attempted 
to be merged, there's no include syntax, there's no replacement syntax... I 
could go on, but that is probably sufficient.

### What does it look like, anyway?
```java
section {
    key: "This is a string value."
    another-key: 482 // a number
    nested-section {
        key: true // a boolean
        // This is a comment without an associated key
        /*
         * This is a block comment
         */
        // All the above comments would be lost following a deserialize/serialize
        /**
         * But this one won't, it is associated with 'documented-key', as it is
         * a documentation comment
         */
        documented-key: "I have documentation!"
    }
}
empty-section {}
root-key: "I'm not in a section!"
```
The above colons (`:`) could be replaced with equals (`=`) for an equivalent 
file.

### What does it *not* look like?
```java
key: This is not a string // Syntax error - strings must be quoted
```
```java
# C-style comment // Syntax error - only // and /**/ style comments are supported
```
```java
"quoted-key": 5 // Syntax error - keys are not quoted
```

### Eh, I don't like it.
That's fine, we don't try to cater to everyone, as that's a recipe for disaster.

Some config formats we mentioned earlier, and a few we haven't mentioned, may 
be better for you. Here's a few:

 * [HOCON][] (what inspired Walnut)
 * [YAML][]
 * [HJSON][]
 * [java.util.Properties][juP]
 * [JSON][]
 * [Jodd Props][] (basically an improved java.util.Properties)
 * ...and a lot more that we don't know of.

[HOCON]: https://github.com/typesafehub/config
[YAML]: http://yaml.org
[HJSON]: http://hjson.org/
[juP]: https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html
[JSON]: http://json.org/
[Jodd Props]: http://jodd.org/doc/props.html



You may also want to consider just forking Walnut, if there's only a couple 
things you dislike about it. If your change is minor enough and fits with the 
keep-it-simple philosophy, we might even merge it.

## What about languages other than Java?
Currently, there are no Walnut implementations for other languages.

You're more than welcome to write one yourself based on the spec, and if you 
do, please make sure to post an issue to this repository so we can add it to 
the readme!



## Java Reference Implementation Information

### What dependencies does it have?
Walnut has no dependencies, other than the standard Java runtime.

### What version of Java do I need?
At least Java 6. Some features may be more convenient to use in Java 8.

### How do I get it?
Walnut is in Maven Central. For your convenience, here's it's coordinates in a 
few different formats:

Gradle/Grails: `com.unascribed:walnut:0.0.1`

Maven:
```xml
<dependency>
    <groupId>com.unascribed</groupId>
    <artifactId>walnut</artifactId>
    <version>0.0.1</version>
</dependency>
```

Buildr:
`'com.unascribed:walnut:jar:0.0.1'`

Ivy: `<dependency org="com.unascribed" name="walnut" rev="0.0.1"/>`

Grape:
```groovy
@Grapes( 
    @Grab(group='com.unascribed', module='walnut', version='0.0.1') 
)
```

Scala Build Tools: `libraryDependencies += "com.unascribed" % "walnut" % "0.0.1"`

Leiningen: `[com.unascribed/walnut "0.0.1"]`

### How do I compile it?
To compile Walnut, you will need JDK 8 or later. You can then build it by
`cd`ing to the directory and running:

`./gradlew build` on Linux and Mac

or `gradlew build` on Windows

All tests will be ran and the jar will be placed into `./build/libs/Walnut-x.y.z.jar`.