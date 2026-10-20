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


    @Test
    void generalCheck() {
        var a = new Attributes();

        Assertions.assertTrue(a.isEmpty());

        a.put("key", "value");

        Assertions.assertEquals("value", a.getNamedItem("key"));
        Assertions.assertTrue(a.containsKey("key"));
        Assertions.assertFalse(a.containsKey("key1"));

        Assertions.assertFalse(a.isEmpty());

        a.put("key", "value1");
        Assertions.assertFalse(a.containsKey("value"));
        Assertions.assertEquals("value1", a.getNamedItem("key"));


        a.remove("key");
        Assertions.assertTrue(a.isEmpty());
        a.remove("key");
        Assertions.assertTrue(a.isEmpty());
    }
}
