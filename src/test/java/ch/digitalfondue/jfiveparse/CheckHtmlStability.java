package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

class CheckHtmlStability {

    @Test
    void checkWikipediaPage() throws IOException {
        var res = JFiveParse.parse(Files.newBufferedReader(Paths.get("src/test/resources/wikipedia.html"), StandardCharsets.UTF_8));
        var content = JFiveParse.serialize(res);
        Assertions.assertEquals(Files.readString(Paths.get("src/test/resources/wikipedia-serialized.html")), content);
    }

    @Test
    void checkTestPage() throws IOException {
        var res = JFiveParse.parse(Files.newBufferedReader(Paths.get("src/test/resources/test.html"), StandardCharsets.UTF_8));
        var content = JFiveParse.serialize(res);
        Assertions.assertEquals(Files.readString(Paths.get("src/test/resources/test-serialized.html")), content);
    }

    @Test
    @Disabled
    void checkFailure() {
        var res = JFiveParse.parse("<body><?something>");
        var content = JFiveParse.serialize(res);
        Assertions.assertEquals("<html><head></head><body><?something></body></html>", content);
    }
}
