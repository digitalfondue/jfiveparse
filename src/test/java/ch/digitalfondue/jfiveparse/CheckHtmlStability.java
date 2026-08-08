package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

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
    void checkProcessingInstructionSerialization() {
        var res = JFiveParse.parse("<html><body><template><?something data></template>");
        var content = JFiveParse.serialize(res);
        Assertions.assertEquals("<html><head></head><body><template><?something data?></template></body></html>", content);
    }

    @Test
    void checkNewElementForFragment() {
        // see Element constructor, check when we can truly lower case the element
        var p = new Parser();
        var v = (Element) p.parseFragment(new Element("foreignObject", Node.NAMESPACE_SVG, null), "<figure></figure>").get(0);
        Assertions.assertEquals(Node.NAMESPACE_HTML, v.getNamespaceURI());
    }
}
