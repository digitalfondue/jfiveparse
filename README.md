# jfiveparse: a java html5 parser

[![Maven Central](https://img.shields.io/maven-central/v/ch.digitalfondue.jfiveparse/jfiveparse.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jfiveparse%22)
[![Build Status](https://travis-ci.org/digitalfondue/jfiveparse.png?branch=master)](https://travis-ci.org/digitalfondue/jfiveparse)
[![Coverage Status](https://coveralls.io/repos/digitalfondue/jfiveparse/badge.svg?branch=master)](https://coveralls.io/r/digitalfondue/jfiveparse?branch=master)


jfiveparse pass all the non scripted tests for the tokenizer and tree construction from the [html5lib-tests suite](https://github.com/html5lib/html5lib-tests).

It provide both fragment and full document parsing. It can parse directly from a String or by streaming through a Reader 
(note: the encoding must be known, currently the parser does not implement an autodetect feature).

Requires java 8.

[Javadoc@javadoc.io](https://www.javadoc.io/doc/ch.digitalfondue.jfiveparse/jfiveparse/).

## Why?

As far as I know, there is no pure java html5 parser that currently pass the [html5lib-tests suite](https://github.com/html5lib/html5lib-tests) (well, the more relevant tests :D).

Additionally, I wanted a library with a reduced footprint (and no dependencies). Currently the jar weight around ~140kb. The target is to keep it under 200kb.

Performance should be competitive with other java parsers.


## License

jfiveparse is licensed under the Apache License Version 2.0.

## Download

maven:

```xml
<dependency>
    <groupId>ch.digitalfondue.jfiveparse</groupId>
    <artifactId>jfiveparse</artifactId>
    <version>0.5.3</version>
</dependency>
```

## Use:

```java
import ch.digitalfondue.jfiveparse.Document;
import ch.digitalfondue.jfiveparse.Element;
import ch.digitalfondue.jfiveparse.Node;
import ch.digitalfondue.jfiveparse.Parser;

public class MyTest {

    public static void main(String[] args) {
        // directly from String
        Parser p = new Parser();
        Document doc = p.parse("<html><body>Hello world!</body></html>");
        System.out.println(doc.getOuterHTML());

        // from reader
        Document doc2 = p.parse(new StringReader("<html><body>Hello world!</body></html>"));
        System.out.println(doc2.getOuterHTML());

        // parse fragment
        List<Node> fragment = p.parseFragment(new Element("div"), "<p><span>Hello world</span></p>");
        System.out.println(fragment.get(0).getOuterHTML());

        // parse fragment from reader
        List<Node> fragment2 = p.parseFragment(new Element("div"), new StringReader("<p><span>Hello world</span></p>"));
        System.out.println(fragment2.get(0).getOuterHTML());
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

### Fetch all titles+links on the front page of HN

```java
import ch.digitalfondue.jfiveparse.Element;
import ch.digitalfondue.jfiveparse.NodeMatcher;
import ch.digitalfondue.jfiveparse.Parser;
import ch.digitalfondue.jfiveparse.Selector;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoadHNTitle {

    public static void main(String[] args) throws IOException {
        Parser p = new Parser();
        Reader reader = new InputStreamReader(new URL("https://news.ycombinator.com/").openStream(), StandardCharsets.UTF_8);
        // select td.title > a
        NodeMatcher matcher = Selector.select().element("td").hasClass("title").withChild().element("a").toMatcher();
        p.parse(reader).getAllNodesMatching(matcher).stream()
                .map(Element.class::cast)
                .filter(a -> !"nofollow".equals(a.getAttribute("rel"))) //remove some extraneous a elements
                .forEach(a -> System.out.println(a.getTextContent() + " [" + a.getAttribute("href") + "]"));
    }
}
```

## Notes:

### Specs/Doc:

 - html by whatwg: https://html.spec.whatwg.org/multipage/syntax.html (of interest: tokenization, tree-construction)
 - entities: https://html.spec.whatwg.org/entities.json

### template element handling

The template element is a "normal" element, so the child nodes are _not_
placed inside a documentFragment. This will be fixed.

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
 - all the attribute values will be double quoted
 - tag and attribute names will be lower case
 - the "/" character used in self closing tag will be ignored
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
- expand the Node api
  - https://developer.mozilla.org/en/docs/Web/API/Node
  - Node.normalize
- add some methods from jquery too 
- cleanup code: but beware, no measurable slowdown is acceptable 
- keep track of lines, eventually chars too
- profile
  - various optimizations...
  - TokenizerRCDataStates.handleRCDataState could be optimized 
      - (textarea related)


mvn clean test jacoco:report
