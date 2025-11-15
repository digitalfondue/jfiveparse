package ch.digitalfondue.jfiveparse.example;

import ch.digitalfondue.jfiveparse.Element;
import ch.digitalfondue.jfiveparse.JFiveParse;
import ch.digitalfondue.jfiveparse.Selector;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class LoadHNTitle {

    public static void main(String[] args) throws IOException {
        try (Reader reader = new InputStreamReader(URI.create("https://news.ycombinator.com/").toURL().openStream(), StandardCharsets.UTF_8)) {
            var matcher = Selector.parseSelector("td.title > span.titleline > a");
            JFiveParse.parse(reader).getAllNodesMatchingAsStream(matcher)
                    .map(Element.class::cast)
                    .forEach(a -> System.out.printf("%s [%s]\n", a.getTextContent(), a.getAttribute("href")));
        }
    }
}
