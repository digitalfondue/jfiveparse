package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class Resizable {

    @Test
    void checkTest() {
        var v = new ResizableCharBuilder();
        for (int i = 0; i < 100; i++) {
            v.append('a');
        }
        assertEquals("a".repeat(100), v.toString());
    }
}
