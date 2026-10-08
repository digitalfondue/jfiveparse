package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessedInputStreamTest {

    @Test
    void testEquivalenceStringAndReader() throws IOException {
        String content = Files.readString(Paths.get("src/test/resources/test.html"));
        Parser parser = new Parser();
        Document doc1 = parser.parse(content);
        Document doc2 = parser.parse(new StringReader(content));

        assertEquals(JFiveParse.serialize(doc1), JFiveParse.serialize(doc2));
    }

    @Test
    void testEquivalenceFragment() {
        Parser parser = new Parser();
        Element context = new Element("div");

        String fragment = "<p class='test'>Hello <span>world</span></p>";
        var res1 = parser.parseFragment(context, fragment);
        var res2 = parser.parseFragment(context, new StringReader(fragment));

        assertEquals(res1.size(), res2.size());
        for (int i = 0; i < res1.size(); i++) {
            assertEquals(((Element) res1.get(i)).getOuterHTML(), ((Element) res2.get(i)).getOuterHTML());
        }
    }

    @Test
    void testNewlineNormalization() {
        Parser parser = new Parser();

        String htmlCrlf = "<div>Line 1\r\nLine 2\rLine 3\nLine 4\r</div>";
        Document doc1 = parser.parse(htmlCrlf);
        Document doc2 = parser.parse(new StringReader(htmlCrlf));

        assertEquals(JFiveParse.serialize(doc1), JFiveParse.serialize(doc2));
        assertEquals("Line 1\nLine 2\nLine 3\nLine 4\n", doc1.getElementsByTagName("div").get(0).getTextContent());
    }

    @Test
    void testBoundaryAcross8192Buffer() {
        // Create an input larger than buffer size (8192) with CRLF right around boundary
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        while (sb.length() < 8190) {
            sb.append("a");
        }
        sb.append("\r\n"); // spans boundary around 8190-8192
        sb.append("rest of content</div>");

        String input = sb.toString();
        Parser parser = new Parser();
        Document doc1 = parser.parse(input);
        Document doc2 = parser.parse(new StringReader(input));

        assertEquals(JFiveParse.serialize(doc1), JFiveParse.serialize(doc2));
    }

    @Test
    void testSingleCharChunkedReader() {
        String html = "<!DOCTYPE html><html><head><title>Test</title></head><body><div class=\"foo\" id='bar' data-test=unquoted><!-- a comment -->Text with &amp; entity</div></body></html>";

        // A Reader that returns at most 1 character per read() call
        Reader slowReader = new Reader() {
            private int pos = 0;

            @Override
            public int read(char[] cbuf, int off, int len) {
                if (pos >= html.length()) {
                    return -1;
                }
                cbuf[off] = html.charAt(pos++);
                return 1;
            }

            @Override
            public void close() {}
        };

        Parser parser = new Parser();
        Document doc1 = parser.parse(html);
        Document doc2 = parser.parse(slowReader);

        assertEquals(JFiveParse.serialize(doc1), JFiveParse.serialize(doc2));
    }

    @Test
    void testLargeAttributesAndCommentsAcrossBuffer() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><body>");
        sb.append("<div title=\"");
        sb.append("x".repeat(10000));
        sb.append("\" data-attr='");
        sb.append("y".repeat(10000));
        sb.append("'>");
        sb.append("<!-- ");
        sb.append("c".repeat(10000));
        sb.append(" -->");
        sb.append("z".repeat(10000));
        sb.append("</div></body></html>");

        String input = sb.toString();
        Parser parser = new Parser();
        Document doc1 = parser.parse(input);
        Document doc2 = parser.parse(new StringReader(input));

        assertEquals(JFiveParse.serialize(doc1), JFiveParse.serialize(doc2));
    }
}
