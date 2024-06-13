package ch.digitalfondue.jfiveparse;

import org.junit.Assert;
import org.junit.Test;


public class Resizable {

    @Test
    public void checkTest() {
        var v = new ResizableCharBuilder();
        for (int i = 0; i < 100; i++) {
            v.append('a');
        }
        Assert.assertEquals("a".repeat(100), v.toString());
    }
}
