package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// see data based from https://github.com/fb55/css-what/blob/master/src/__fixtures__/tests.ts
class CSSTest {


    static CSS.CssSelector tag(String name) {
        return new CSS.TagSelector(CSS.SelectorType.TAG, name, null);
    }

    // Tag names
    @Test
    void checkDiv() {
        var r = CSS.parseSelector("div");
        assertEquals(List.of(List.of(tag("div"))), r);
    }

    @Test
    void checkUniversal() {
        var r = CSS.parseSelector("*");
        assertEquals(List.of(List.of(new CSS.UniversalSelector(CSS.SelectorType.UNIVERSAL, null))), r);
    }

    // Traversal
    @Test
    void checkDivDiv() {
        var r = CSS.parseSelector("div div");
        assertEquals(List.of(List.of(tag("div"), new CSS.CssSelectorType(CSS.SelectorType.DESCENDANT), tag("div"))), r);
    }


    @Test
    void checkDivSpaceDiv() {
        var r = CSS.parseSelector("div\t \n \tdiv");
        assertEquals(List.of(List.of(tag("div"), new CSS.CssSelectorType(CSS.SelectorType.DESCENDANT), tag("div"))), r);
    }

    @Test
    void checkDivPlusDiv() {
        var r = CSS.parseSelector("div + div");
        assertEquals(List.of(List.of(tag("div"), new CSS.CssSelectorType(CSS.SelectorType.ADJACENT), tag("div"))), r);
    }

    @Test
    void checkDivSiblingDiv() {
        var r = CSS.parseSelector("div ~ div");
        assertEquals(List.of(List.of(tag("div"), new CSS.CssSelectorType(CSS.SelectorType.SIBLING), tag("div"))), r);
    }

    @Test
    void checkParent() {
        var r = CSS.parseSelector("p < div");
        assertEquals(List.of(List.of(tag("p"), new CSS.CssSelectorType(CSS.SelectorType.PARENT), tag("div"))), r);
    }

    // Escaped whitespace & special characters

    @Test
    void checkSpecialCharacters() {
        var r = CSS.parseSelector(".m™²³");
        assertEquals(List.of(List.of(new CSS.AttributeSelector(CSS.SelectorType.ATTRIBUTE, "class", CSS.AttributeAction.ELEMENT, "m™²³", "quirks", null))), r);
    }

    //

    // Attributes
    @Test
    void checkAttributeStart() {
        var r = CSS.parseSelector("[name^=\"foo[\"]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector(CSS.SelectorType.ATTRIBUTE, "name", CSS.AttributeAction.START, "foo[", null, null))), r);
    }

    @Test
    void checkAttributeStart2() {
        var r = CSS.parseSelector("[name^=\"foo[bar]\"]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector(CSS.SelectorType.ATTRIBUTE, "name", CSS.AttributeAction.START, "foo[bar]", null, null))), r);
    }

    @Test
    void checkAttributeEnd() {
        var r = CSS.parseSelector("[name$=\"[bar]\"]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector(CSS.SelectorType.ATTRIBUTE, "name", CSS.AttributeAction.END, "[bar]", null, null))), r);
    }

    @Test
    void checkAttributeAny() {
        var r = CSS.parseSelector("[href *= \"google\"]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector(CSS.SelectorType.ATTRIBUTE, "href", CSS.AttributeAction.ANY, "google", null, null))), r);
    }


}
