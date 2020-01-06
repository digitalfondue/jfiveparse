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
            // select td.title > a.storylink
            NodeMatcher matcher = Selector.select().element("td").hasClass("title").withChild().element("a").hasClass("storylink").toMatcher();
            JFiveParse.parse(reader).getAllNodesMatching(matcher).stream()
                    .map(Element.class::cast)
                    .forEach(a -> System.out.println(a.getTextContent() + " [" + a.getAttribute("href") + "]"));
        }
    }
}
