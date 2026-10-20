package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class AttributesTest {

    @Test
    void checkEquality() {
        var a = new Attributes(List.of(new AttributeNode("a", "b"), new AttributeNode("b", "c")));
        var b = new Attributes(List.of(new AttributeNode("b", "c"), new AttributeNode("a", "b")));

        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }
}
