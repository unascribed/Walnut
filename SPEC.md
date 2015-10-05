# Walnut Specification (version 0.0.1)

## Definitions
 * a **key** is a *non*-quoted string containing any characters *except* the 
colon and equals symbols (`:` and `=`)
 * a **value** can be any of the following:
  * a decimal number, such as `501`, `-149` or `281.2`
  * a hex number, such as `0xFE` or `#EED3`
  * a scientific number, such as `2.1e23` or `1.2e-47`
  * a special numeric value, such as `Infinity` or `NaN`
  * a boolean, as in `true` or `false`, `on` or `off`, and `enabled` or 
`disabled`
  * a null value, as in `null`, `nil`, or `undefined`
  * an array, consisting of any number of comma-seperated values, such as `[1, 
2, 3]`, `[1, 2, "potato"]` or `[1, ["nested", 5], 8]`
  * a quoted string, such as `"hello"` or `"aäαā ćç¢"`
  * a spanning string, such as `(\nsome\nstring\n)` (where \n is a newline)
  * a section, being a set of pairs surrounded by curly braces (`{` and `}`), 
such as `{\nkey: "value"\n}` (where \n is a newline)
  * Note that numbers prefixed with a zero are valid, but are not octal; `0100`
is `100`, not `64`
 * a **pair** is a key, followed by a `:` or `=` surrounded by any amount of 
whitespace, followed by a value
 * **file** is used here to refer to any given byte stream, and not 
neccessarily a file on a filesystem

## Syntax
Files must be UTF-8.

### Comments
Anything between // and the next newline or between `/*` and `*/` is considered 
a comment and ignored, unless it is inside a quoted string.

#### Documentation Comments (optional)
A comment beginning with /** is a *documentation* comment, and should be 
loaded rather than ignored.
A documentation comment is associated with the first pair after it. If the file 
is later saved again, a documentation comment should be placed before it's 
associated pair in the resulting file. Normal comments should not be kept.

### Whitespace

Whitespace is:

 * any Unicode space separator (Zs category), line separator (Zl category), or 
paragraph separator (Zp category).
 * tab (`\t` 0x09), line feed (`\n` 0x0A), vertical tab (`\v` 0x0B), form feed 
(`\f` 0x000C), carriage return (`\r` 0x0D), file separator (0x1C), group 
separator (0x1D), record separator (0x1E), unit separator (0x1F).

This is identical to `Character::isWhitespace` in Java.

While all of these should be treated as whitespace, in this spec 'newline' 
refers only to the ASCII line feed, `\n` (0x0A)

## Keys
A key is everything before the colon or equals sign in a pair.
A key can have any characters, except for the colon, equals, open curly brace,
open parentheses, and open square bracket.
For example:

 * `This is a key: 5` - valid
 * `my=key: 5` - *invalid*
 * `such:key = 12` - *invalid*
 * `kartläggning: 500` - valid
 * `マッピング: 432` - valid
 * `b̨̪̫̤̖̤̝̩̣̔́̓̃̅̂é̡̯̏̒hȉ̡̗̮̪̦̠́̔̊n̜̮̮̩̙̞̙̑̑̃̅̍̅̊ḑ̡̠̜̪̙̞̋̓̓̃̈̒̉̏ t̝̞̃̕ḩ̮̙̃̉̊̐ė̠̔̋̒ w̢̝̫̬̜̒̑́̋a̢̛̪̩̖̯̍̅̒̈̅̔̋l̥̐̄́̕l̢̨̧̧̥̤̮̜̘̭̖̅̎̊̅̅̂ = "he waits"` - valid

In the case of a multi-line value, such as a section, array, or spanned string, 
the colon or equals sign may be omitted.

### Duplicate keys
A duplicate key is a syntax error. Pairs must not be merged or replaced.

### Strings
A string is an arbitrary sequence of UTF-8 characters, surrounded in `"`
characters. Strings support escape sequences, which are one of the following,
prefixed by a backslash (`\`):

 * `xXX` - arbitrary UTF-8 character, where 'X' are hex digits
 * `uXXXX` - arbitrary UTF-16 character, where 'X' are hex digits
 * `UXXXXXXXX` - arbitrary UTF-32 character, where 'X' are hex digits
 * `"` - a literal quote character (will not close a string)
 * `)` - a literal close parentheses (will not close a spanned string)
 * `\` - a literal backslash

## Spanning (or multi-line) strings
A spanning string is something similar to the following:
```
(
	this is
	a
	spanning string
	!
)
```
It is basically a string that spans multiple lines. Newlines are kept in the 
resulting string, but all leading whitespace  on a line should be stripped. The 
above example would result in `this is\na\nspanning string\n!`, where \n is a 
newline.

## Type Conversion
Generally, a Walnut implementation should not perform type conversion at all. 
If a value is specified as a string and a number is requested, an exception 
should be thrown, or an error value returned (depending on the language). This 
includes attempting to use a special number value such as Infinity or NaN as an 
integer, or a partial number (e.g. 5.2) as an integer. Using an integer as a 
floating point or arbitrary precision number should work, however.

The exception is converting anything to a string, where an appropriate string 
that is valid Walnut that will result in an equivalent value if later used as 
the correct value again should be returned.

For example, the number `0x2D` could become `"45"`, the boolean `on` could 
become `"true"`, etc.

When possible, the same definition as in the file should be used. As such, `on` 
becoming `"on"` and `0x2D` becoming `"0x2D"` is much preferred to the above 
example.

## Conventions
Config keys should be `hyphen-separated-lowercase`, and not `camelCase`, 
`TitleCase`, or `Sentence case with spaces`. All of these are valid, however.

When using colons for pairs, they should be given as `key: value`. For equals 
signs, they should be given as `key = value`. These are, again, not absolute 
requirements. `key:value` is perfectly valid, as are `key :value`, `key=value`, 
and any other amounts of whitespace on either side.

### MIME Type
`application/walnut-config` should be used if a MIME type is required.

### File Extension
A Walnut file should have the extension .wlnt or .walnut. If you think these 
are too long, realize we moved away from 8.3 filenames a *long* time ago.

## Tests
Test files can be found in the Walnut repository, in the 'src/test/resources' 
directory.