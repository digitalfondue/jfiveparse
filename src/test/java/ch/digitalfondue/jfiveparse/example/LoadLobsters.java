package ch.digitalfondue.jfiveparse.example;

import ch.digitalfondue.jfiveparse.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class LoadLobsters {

    public static void main(String[] args) throws IOException {
        try (Reader reader = new InputStreamReader(URI.create("https://lobste.rs/").toURL().openStream(), StandardCharsets.UTF_8)) {
            NodeMatcher<Node> matcher = Selector.parseSelector("span[role=heading] a.u-url");
            JFiveParse.parse(reader).getAllNodesMatchingAsStream(matcher)
                    .map(Element.class::cast)
                    .forEach(a -> System.out.printf("%s [%s]\n", a.getTextContent(), a.getAttribute("href")));
        }
    }
}
