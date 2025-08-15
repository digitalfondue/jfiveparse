# jfiveparse: a java html5 parser

[![Maven Central](https://img.shields.io/maven-central/v/ch.digitalfondue.jfiveparse/jfiveparse.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jfiveparse%22)
[![Build Status](https://img.shields.io/github/actions/workflow/status/digitalfondue/jfiveparse/.github/workflows/maven.yml)](https://github.com/digitalfondue/jfiveparse/actions?query=workflow%3A%22Java+CI+with+Maven%22)



jfiveparse pass all the non-scripted tests for the tokenizer and tree construction from the [html5lib-tests suite](https://github.com/html5lib/html5lib-tests).

It provides both fragment and full document parsing. It can parse directly from a String or by streaming through a Reader 
(note: the encoding must be known, currently the parser does not implement an autodetect feature).

Requires java 17 (previous version 1.1.3 requires at least java 11).

[Javadoc@javadoc.io](https://www.javadoc.io/doc/ch.digitalfondue.jfiveparse/jfiveparse/).

## Why?

As far as I know, there is no pure java html5 parser that currently pass the [html5lib-tests suite](https://github.com/html5lib/html5lib-tests) (well, the more relevant tests :D, note: this project was published in october 2015).

Additionally, I wanted a library with a reduced footprint (and no dependencies). Currently, the jar weight around ~150kb. The target is to keep it under 200kb.

Performance should be competitive with other java parsers.


## License

jfiveparse is licensed under the Apache License Version 2.0.

## Download

maven:

```xml
<dependency>
    <groupId>ch.digitalfondue.jfiveparse</groupId>
    <artifactId>jfiveparse</artifactId>
    <version>1.1.3</version>
</dependency>
```

gradle:

```
compile 'ch.digitalfondue.jfiveparse:jfiveparse:1.1.3'
```

## Use:

If you use it as a module, remember to add `requires ch.digitalfondue.jfiveparse;` in your module-info.
If you are using the W3CDom class (and the various inner classes), you may also need to require the `java.xml` module, as it's an optional dependency.

```java
import ch.digitalfondue.jfiveparse.Document;
import ch.digitalfondue.jfiveparse.JFiveParse;
import ch.digitalfondue.jfiveparse.Node;

import java.io.StringReader;
import java.util.List;

public class Example {

    public static void main(String[] args) {
        // directly from String
        Document doc = JFiveParse.parse("<html><body>Hello world!</body></html>");
        System.out.println(JFiveParse.serialize(doc));

        // from reader
        Document doc2 = JFiveParse.parse(new StringReader("<html><body>Hello world!</body></html>"));
        System.out.println(JFiveParse.serialize(doc2));

        // parse fragment
        List<Node> fragment = JFiveParse.parseFragment("<p><span>Hello world</span></p>");
        System.out.println(JFiveParse.serialize(fragment.get(0)));

        // parse fragment from reader
        List<Node> fragment2 = JFiveParse.parseFragment(new StringReader("<p><span>Hello world</span></p>"));
        System.out.println(JFiveParse.serialize(fragment2.get(0)));
    }
}
```

It will print:

```html
<html><head></head><body>Hello world!</body></html>
<html><head></head><body>Hello world!</body></html>
<p><span>Hello world</span></p>
<p><span>Hello world</span></p>
```

## Examples:

See directory: https://github.com/digitalfondue/jfiveparse/tree/master/src/test/java/ch/digitalfondue/jfiveparse/example

### Fetch all titles+links on the front page of HN

```java
package ch.digitalfondue.jfiveparse.example;

import ch.digitalfondue.jfiveparse.Element;
import ch.digitalfondue.jfiveparse.JFiveParse;
import ch.digitalfondue.jfiveparse.NodeMatcher;
import ch.digitalfondue.jfiveparse.Selector;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoadHNTitle {

    public static void main(String[] args) throws IOException {
        try (Reader reader = new InputStreamReader(new URL("https://news.ycombinator.com/").openStream(), StandardCharsets.UTF_8)) {
            // select td.title > span.titleline > a
            NodeMatcher matcher = Selector.select().
                    element("td").hasClass("title")
                    .withChild().element("span").hasClass("titleline")
                    .withChild().element("a").toMatcher();
            JFiveParse.parse(reader).getAllNodesMatching(matcher).stream()
                    .map(Element.class::cast)
                    .forEach(a -> System.out.printf("%s [%s]\n", a.getTextContent(), a.getAttribute("href")));
        }
    }
}
```

### Convert to the java DOM representation

If you need to generate a `org.w3c.dom.Document` from the `ch.digitalfondue.jfiveparse.Document` representation, there is a
static method in the helper class: `W3CDom.toW3CDocument`.

## Notes:

### Specs/Doc:

 - html by whatwg: https://html.spec.whatwg.org/multipage/syntax.html (of interest: tokenization, tree-construction)
 - entities: https://html.spec.whatwg.org/entities.json

### Template element handling

The template element is a "normal" element, so the child nodes are _not_
placed inside a documentFragment. This will be fixed.

### Special parsing options

The parser can be customized to allow some non-standard behaviour, you can see the following tests: https://github.com/digitalfondue/jfiveparse/blob/master/src/test/java/ch/digitalfondue/jfiveparse/OptionParseTest.java

 - DISABLE_IGNORE_TOKEN_IN_BODY_START_TAG : allow to have for example "tr" tag without the containing table/tbody.
 - INTERPRET_SELF_CLOSING_ANYTHING_ELSE :  When encountering unknown self-closing tag, they will be interpreted 
   as it is and not as open tag only, thus creating a non-intuitive DOM.    

### Entities
The &ntities; are by default (and by specification) parsed and interpreted. 
This behavior can be disabled by:

  - passing the enum "Option.DONT_TRANSFORM_ENTITIES" to the Parser
  - when calling Node.get{Inner,Outer}HTML(), pass the enum 
    "Option.DONT_TRANSFORM_ENTITIES" for disabling the escaping.
    It's possible that something will be wrong in the generated document.
    
### Preserving as much as possible the original document when serializing
By default, when parsing/serializing, the following transformations will 
be applied:
 
 - entities will be interpreted and converted
 - all the attribute values will be double-quoted
 - tag and attribute names will be lower-case
 - the "/" character used in self-closing tag will be ignored
 - some whitespace will be ignored
 
Currently, jfiveparse can preserve the entities, the attribute quoting type and 
the case and the tag name case.
 
If you require to preserve as much as possible the document when serializing
back in a string, pass the following parameters:

 - pass the enum "Option.DONT_TRANSFORM_ENTITIES" to the Parser
 - when calling Node.get{Inner,Outer}HTML(), pass the enums:
   - Option.DONT_TRANSFORM_ENTITIES
   - Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE
   - Option.PRINT_ORIGINAL_ATTRIBUTES_CASE
   - Option.PRINT_ORIGINAL_TAG_CASE
   
### Uppercase handling in the tokenizer

Note: this is a deviation from the specification in term of _implementation_ of
the tokenizer, but globally, the end result is correct, as the attributes and
tag names are then converted to lower case.

In the tokenizer, instead of applying the toLowerCase function on each 
character, the transformation is done in a single call in the TreeConstructor
(see setTagName). This is used for saving the original case of the attributes 
and tag names. 


## TODO:
- additional doc
- expand the typesafe matcher api
- keep track of lines, eventually chars too
- profile
  - various optimizations...
  - TokenizerRCDataStates.handleRCDataState could be optimized 
      - (textarea related)


mvn clean test jacoco:report
