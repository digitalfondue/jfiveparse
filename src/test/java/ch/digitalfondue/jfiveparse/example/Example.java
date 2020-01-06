package ch.digitalfondue.jfiveparse.example;

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
