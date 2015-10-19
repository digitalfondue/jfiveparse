# jfiveparse: a java html5 parser

[![Build Status](https://travis-ci.org/digitalfondue/jfiveparse.png?branch=master)](https://travis-ci.org/digitalfondue/jfiveparse)
[![Coverage Status](https://coveralls.io/repos/digitalfondue/jfiveparse/badge.svg?branch=master)](https://coveralls.io/r/digitalfondue/jfiveparse?branch=master)


jfiveparse pass all the non scripted tests for the tokenizer and tree construction from the html5lib-tests suite.

It provide both fragment and full document parsing. It can parse from String or Reader.

## Why?

As far as I know, there is no pure java html5 parser that currently pass the html5-lib test suite (well, the more relevant tests :D).

Additionally, I wanted a library with a reduced footprint (and no dependencies). Currently the jar weight around ~135kb. The target is to keep it under 200kb.

Performance should be competitive with other java parsers.


## License

jfiveparse is licensed under the Apache License Version 2.0.

## Download

maven:

```xml
<dependency>
    <groupId>ch.digitalfondue.jfiveparse</groupId>
    <artifactId>jfiveparse</artifactId>
    <version>0.1.1</version>
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

```
<html><head></head><body>Hello world!</body></html>
<html><head></head><body>Hello world!</body></html>
<p><span>Hello world</span></p>
<p><span>Hello world</span></p>
```

## Notes:

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
 
Currently, jfiveparse can preserve the entities, the attribute quoting type and the case.
 
If you require to preserve as much as possible the document when serializing
back in a string, pass the following parameters:

 - pass the enum "Option.DONT_TRANSFORM_ENTITIES" to the Parser
 - when calling Node.get{Inner,Outer}HTML(), pass the enums:
   - Option.DONT_TRANSFORM_ENTITIES
   - Option.PRINT_ORIGINAL_ATTRIBUTE_QUOTE
   - Option.PRINT_ORIGINAL_ATTRIBUTES_CASE


## TODO:
- keep track of UpperCase tagname (if the overhead is not too much, obviously) (PRINT_ORIGINAL_TAG_CASE)
- add a typesafe matcher api
  - expand NodeMatchers: add "AND" rule 
  - HasAttribute support attribute value matching type (~=, |=, ^=, $=, *=)
- expand the Node api
  - https://developer.mozilla.org/en/docs/Web/API/Node
  - Node.contains(Node)
  - Node.normalize
  - Element.setInnerHTML(String)
- add some methods from jquery too
- cleanup code: but beware, no measurable slowdown is acceptable
  - treeConstructor.insertHtmlElementToken();
  - treeConstructor.popCurrentNode();
  - treeConstructor.ackSelfClosingTagIfSet()
  - this 3 calls can be made in a single one 
- keep track of lines, eventually chars too (this has some issues with multibyte char :D)
- profile
  - various optimizations...
  - TokenizerRCDataStates.handleRCDataState could be optimized 
      - (textarea related)
        
mvn clean test jacoco:report