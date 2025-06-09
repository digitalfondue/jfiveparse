package ch.digitalfondue.jfiveparse;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Files;
import java.nio.file.Paths;

// I know, completely wrong as a benchmark :D
public class TestParsePerf {

    String file;
    final Parser parser = new Parser();

    @BeforeEach
    void load() throws IOException {
        file = Files.readString(Paths.get("src/test/resources/test.html"));
    }

    final int round = 20_000_000;

    @Disabled
    @Test
    void check() {
        assertEquals(91, parser.parse(file).getAllNodesMatching(Selector.select().element("div").toMatcher()).size());
        assertEquals(91, Jsoup.parse(file).select("div").size());
        //some whitespace differences...
        assertEquals(parser.parse(file).getBody().getTextContent().trim(), Jsoup.parse(file).select("body").get(0).wholeText().trim());
    }

    @Disabled
    @Test
    public void parse() {
        long start = System.nanoTime();
        for (int i = 0; i < round; i++) {
            parser.parse(file);
        }
        long end = System.nanoTime();
        System.err.println("time " + ((end - start)/round));
    }

    @Disabled
    @Test
    public void parse2() {
        long start = System.nanoTime();
        for (int i = 0; i < round; i++) {
            Jsoup.parse(file);
        }
        long end = System.nanoTime();
        System.err.println("time " + ((end - start)/round));
    }
}
