package ch.digitalfondue.jfiveparse;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

// I know, completely wrong as a benchmark :D
public class TestParsePerf {

    String file;
    Parser parser = new Parser();

    @BeforeEach
    void load() throws IOException {
        file = new String(Files.readAllBytes(Paths.get("src/test/resources/test.html")), StandardCharsets.UTF_8);
    }

    int round = 20_000;

    @Test
    void check() {
        assertEquals(91, parser.parse(file).getAllNodesMatching(Selector.select().element("div").toMatcher()).size());
        assertEquals(91, Jsoup.parse(file).select("div").size());
        //some whitespace differences...
        assertEquals(parser.parse(file).getBody().getTextContent().trim(), Jsoup.parse(file).select("body").get(0).wholeText().trim());
    }

    //@Test
    public void parse() {

        long start = System.nanoTime();
        for (int i = 0; i < round; i++) {
            parser.parse(file);
        }
        long end = System.nanoTime();
        System.err.println("time " + ((end - start)/round));
    }

    //@Test
    public void parse2() {
        long start = System.nanoTime();
        for (int i = 0; i < round; i++) {
            Jsoup.parse(file);
        }
        long end = System.nanoTime();
        System.err.println("time " + ((end - start)/round));
    }
}
