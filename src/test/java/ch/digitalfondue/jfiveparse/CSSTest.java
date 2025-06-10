package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Test;

// see data based from https://github.com/fb55/css-what/blob/master/src/__fixtures__/tests.ts
class CSSTest {


    // Tag names

    @Test
    void checkDiv() {
        CSS.parseSelector("div");
    }

    @Test
    void checkUniversal() {
        CSS.parseSelector("*");
    }

    // Traversal
    @Test
    void checkDivDiv() {
        CSS.parseSelector("div div");
    }


    @Test
    void checkDivSpaceDiv() {
        CSS.parseSelector("div\\t \\n \\tdiv");
    }

    @Test
    void checkDivPlusDiv() {
        CSS.parseSelector("div + div");
    }

    @Test
    void checkDivSiblingDiv() {
        CSS.parseSelector("div ~ div");
    }

    @Test
    void checkParent() {
        CSS.parseSelector("p < div");
    }

    // Escaped whitespace

    //

    // Attributes
    

}
