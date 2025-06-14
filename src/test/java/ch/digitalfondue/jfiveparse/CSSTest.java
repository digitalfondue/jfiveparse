package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// see data based from https://github.com/fb55/css-what/blob/master/src/__fixtures__/tests.ts
class CSSTest {


    static CSS.CssSelector tag(String name) {
        return new CSS.TagSelector(CSS.SelectorType.Tag, name, null);
    }

    // Tag names
    @Test
    void checkDiv() {
        assertEquals(List.of(List.of(tag("div"))), CSS.parseSelector("div"));
    }

    @Test
    void checkUniversal() {
        assertEquals(List.of(List.of(new CSS.UniversalSelector(CSS.SelectorType.Universal, null))), CSS.parseSelector("*"));
    }

    // Traversal
    @Test
    void checkDivDiv() {
        assertEquals(List.of(List.of(tag("div"), new CSS.CssSelectorType(CSS.SelectorType.Descendant), tag("div"))), CSS.parseSelector("div div"));
    }


    @Test
    void checkDivSpaceDiv() {
        assertEquals(List.of(List.of(tag("div"), new CSS.CssSelectorType(CSS.SelectorType.Descendant), tag("div"))), CSS.parseSelector("div\t \n \tdiv"));
    }

    @Test
    void checkDivPlusDiv() {
        assertEquals(List.of(List.of(tag("div"), new CSS.CssSelectorType(CSS.SelectorType.Adjacent), tag("div"))), CSS.parseSelector("div + div"));
    }

    @Test
    void checkDivSiblingDiv() {
        assertEquals(List.of(List.of(tag("div"), new CSS.CssSelectorType(CSS.SelectorType.Sibling), tag("div"))), CSS.parseSelector("div ~ div"));
    }

    @Test
    void checkParent() {
        assertEquals(List.of(List.of(tag("p"), new CSS.CssSelectorType(CSS.SelectorType.Parent), tag("div"))), CSS.parseSelector("p < div"));
    }

    // Escaped whitespace

    //

    // Attributes
    @Test
    void checkAttributeStart() {
        assertEquals(List.of(List.of(new CSS.AttributeSelector(CSS.SelectorType.Attribute, "name", CSS.AttributeAction.Start, "foo[", null, null))), CSS.parseSelector("[name^=\"foo[\"]"));
    }

    @Test
    void checkAttributeStart2() {
        assertEquals(List.of(List.of(new CSS.AttributeSelector(CSS.SelectorType.Attribute, "name", CSS.AttributeAction.Start, "foo[bar]", null, null))), CSS.parseSelector("[name^=\"foo[bar]\"]"));
    }


}
