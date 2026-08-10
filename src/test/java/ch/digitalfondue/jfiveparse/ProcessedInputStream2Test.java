package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

class ProcessedInputStream2Test {

    @Test
    void checkRead() {
        var is2 = new ProcessedInputStream2(new StringReader("hello world"), 4);
        int c;
        StringBuilder sb = new StringBuilder();
        while ((c = is2.read()) != Characters.EOF) {
            sb.append((char) c);
        }
        Assertions.assertEquals("hello world", sb.toString());
    }
}
