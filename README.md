# jfiveparse: a java html5 parser


jfiveparse pass all the non scripted tests for the tokenizer and tree construction from the html5lib-tests suite.

It provide both fragment and full document parsing. It can parse from String or Reader.

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

### Entities
The &ntities; are by default (and by specs) parsed and interpreted. 
This behavior can be disabled by:

  - passing the enum "Option.DONT_TRANSFORM_ENTITIES" to the Parser
  - when calling Node.get{Inner,Outer}HTML(), pass the enum 
    "Option.DONT_TRANSFORM_ENTITIES" for disabling the escaping.
    It's possible that something will be wrong in the generated document.

## TODO:
- keep track of ' " o empty for attribute (TRACK_ATTRIBUTE_VALUE_QUOTING_TYPE)
- keep track of UpperCase tagname/attribute names with a BitSet (if the overhead is not too much, obviously) (TRACK_CASE_FOR_ATTRIBUTES_AND_TAG)
- expand the Node api
  - https://developer.mozilla.org/en/docs/Web/API/Node
  - $0.firstElementChild, $0.lastElementChild
  - $0.previousElementSibling, $0.nextElementSibling
  - textContent
- add some methods from jquery too
- cleanup code: but beware, no measurable slowdown is acceptable 
- check if we can use directly a hashmap for the entities
- add a typesafe matcher support lib
- parameters for parser
- keep track of lines, eventually chars too (this has some issues with multibyte char :D)
- profile
  - various optimizations...
  - TokenizerRCDataStates.handleRCDataState could be optimized 
      - (textarea related)
        
mvn clean test jacoco:report