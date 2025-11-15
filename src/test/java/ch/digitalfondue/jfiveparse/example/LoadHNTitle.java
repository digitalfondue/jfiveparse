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
            NodeMatcher matcher = Selector.parseSelector("td.title > span.titleline > a");
            JFiveParse.parse(reader).getAllNodesMatchingAsStream(matcher)
                    .map(Element.class::cast)
                    .forEach(a -> System.out.printf("%s [%s]\n", a.getTextContent(), a.getAttribute("href")));
        }
    }
}
