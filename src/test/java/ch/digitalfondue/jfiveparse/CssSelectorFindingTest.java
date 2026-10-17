package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CssSelectorFindingTest {

    @Test
    void checkSelectors() {
        var selector = Selector.parseSelector("span[data-test~=bla]");

        var doc = JFiveParse.parse("<div><span id=bla data-test='plop bla'></span><span id=bla2 data-test='blabla'></span></div>");

        var found = doc.getAllNodesMatching(selector);
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals("bla", ((Element) found.get(0)).getAttribute("id"));
    }
}
