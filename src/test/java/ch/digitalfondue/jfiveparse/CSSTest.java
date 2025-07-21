package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.*;

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

    @Test
    void checkQuotedAttributeWithInternalNewLine() {
        var r = CSS.parseSelector("[value=\"\nsome text\n\"]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector(CSS.SelectorType.ATTRIBUTE, "value", CSS.AttributeAction.EQUALS, "\nsome text\n", null, null))), r);
    }


    @Test
    void checkAttributeWithPreviouslyNormalizedCharacters() {
        var r = CSS.parseSelector("[name='foo ~ < > , bar' i]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector(CSS.SelectorType.ATTRIBUTE, "name", CSS.AttributeAction.EQUALS, "foo ~ < > , bar", "true", null))), r);
    }

    @Test
    void idStartingWithADot() {
        var r = CSS.parseSelector("#.identifier");
        assertEquals(List.of(List.of(new CSS.AttributeSelector(CSS.SelectorType.ATTRIBUTE, "id", CSS.AttributeAction.EQUALS, ".identifier", "quirks", null))), r);
    }

    //

    @Test
    void pseudoElement1() {
        var r = CSS.parseSelector("::foo");
        assertEquals(List.of(List.of(new CSS.PseudoElement(CSS.SelectorType.PSEUDO_ELEMENT, "foo", null))), r);
    }

    @Test
    void pseudoElement2() {
        var r = CSS.parseSelector("::foo()");
        assertEquals(List.of(List.of(new CSS.PseudoElement(CSS.SelectorType.PSEUDO_ELEMENT, "foo", ""))), r);
    }

    @Test
    void pseudoElement3() {
        var r = CSS.parseSelector("::foo(bar())");
        assertEquals(List.of(List.of(new CSS.PseudoElement(CSS.SelectorType.PSEUDO_ELEMENT, "foo", "bar()"))), r);
    }
    //

    //
    @Test
    void pseudoSelector1() {
        var r = CSS.parseSelector(":foo");
        assertEquals(List.of(List.of(new CSS.PseudoSelector(CSS.SelectorType.PSEUDO, "foo", null))), r);
    }

    @Test
    void pseudoSelector2() {
        var r = CSS.parseSelector(":bar(baz)");
        assertEquals(List.of(List.of(new CSS.PseudoSelector(CSS.SelectorType.PSEUDO, "bar", "baz"))), r);
    }

    @Test
    void pseudoSelector3() {
        var r = CSS.parseSelector(":contains(\"(foo)\")");
        assertEquals(List.of(List.of(new CSS.PseudoSelector(CSS.SelectorType.PSEUDO, "contains", "(foo)"))), r);
    }

    @Test
    void pseudoSelector4() {
        var r = CSS.parseSelector(":where(a)");
        assertEquals(List.of(List.of(new CSS.PseudoSelector(CSS.SelectorType.PSEUDO, "where", List.of(List.of(new CSS.TagSelector(CSS.SelectorType.TAG, "a", null)))))), r);
    }

    @Test
    void pseudoSelector5() {
        var r = CSS.parseSelector(":icontains('')");
        assertEquals(List.of(List.of(new CSS.PseudoSelector(CSS.SelectorType.PSEUDO, "icontains", ""))), r);
    }

    @Test
    void pseudoSelector6() {
        var r = CSS.parseSelector(":contains(\"(foo)\")");
        assertEquals(List.of(List.of(new CSS.PseudoSelector(CSS.SelectorType.PSEUDO, "contains", "(foo)"))), r);
    }
    //TODO: Multiple selectors

}
