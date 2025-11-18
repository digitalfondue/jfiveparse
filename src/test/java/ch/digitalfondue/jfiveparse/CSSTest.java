package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// see data based from https://github.com/fb55/css-what/blob/master/src/__fixtures__/tests.ts
class CSSTest {


    static CSS.CssSelector tag(String name) {
        return new CSS.TagSelector(name, null);
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
        assertEquals(List.of(List.of(new CSS.UniversalSelector(null))), r);
    }

    // Traversal
    @Test
    void checkDivDiv() {
        var r = CSS.parseSelector("div div");
        assertEquals(List.of(List.of(tag("div"), new CSS.Combinator(CSS.CT_DESCENDANT), tag("div"))), r);
    }


    @Test
    void checkDivSpaceDiv() {
        var r = CSS.parseSelector("div\t \n \tdiv");
        assertEquals(List.of(List.of(tag("div"), new CSS.Combinator(CSS.CT_DESCENDANT), tag("div"))), r);
    }

    @Test
    void checkDivPlusDiv() {
        var r = CSS.parseSelector("div + div");
        assertEquals(List.of(List.of(tag("div"), new CSS.Combinator(CSS.CT_ADJACENT), tag("div"))), r);
    }

    @Test
    void checkDivSiblingDiv() {
        var r = CSS.parseSelector("div ~ div");
        assertEquals(List.of(List.of(tag("div"), new CSS.Combinator(CSS.CT_SIBLING), tag("div"))), r);
    }

    @Test
    void checkParent() {
        var r = CSS.parseSelector("p < div");
        assertEquals(List.of(List.of(tag("p"), new CSS.Combinator(CSS.CT_PARENT), tag("div"))), r);
    }

    // Escaped whitespace & special characters

    @Test
    void checkSpecialCharacters() {
        var r = CSS.parseSelector(".m™²³");
        assertEquals(List.of(List.of(new CSS.AttributeSelector("class", CSS.ATTR_ACTION_ELEMENT, "m™²³", CSS.IGNORE_CASE_QUIRKS, null))), r);
    }

    //

    // Attributes
    @Test
    void checkAttributeStart() {
        var r = CSS.parseSelector("[name^=\"foo[\"]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector( "name", CSS.ATTR_ACTION_START, "foo[", -1, null))), r);
    }

    @Test
    void checkAttributeStart2() {
        var r = CSS.parseSelector("[name^=\"foo[bar]\"]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector( "name", CSS.ATTR_ACTION_START, "foo[bar]", -1, null))), r);
    }

    @Test
    void checkAttributeEnd() {
        var r = CSS.parseSelector("[name$=\"[bar]\"]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector("name", CSS.ATTR_ACTION_END, "[bar]", -1, null))), r);
    }

    @Test
    void checkAttributeAny() {
        var r = CSS.parseSelector("[href *= \"google\"]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector("href", CSS.ATTR_ACTION_ANY, "google", -1, null))), r);
    }

    @Test
    void checkQuotedAttributeWithInternalNewLine() {
        var r = CSS.parseSelector("[value=\"\nsome text\n\"]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector("value", CSS.ATTR_ACTION_EQUALS, "\nsome text\n", -1, null))), r);
    }


    @Test
    void checkAttributeWithPreviouslyNormalizedCharacters() {
        var r = CSS.parseSelector("[name='foo ~ < > , bar' i]");
        assertEquals(List.of(List.of(new CSS.AttributeSelector("name", CSS.ATTR_ACTION_EQUALS, "foo ~ < > , bar", CSS.IGNORE_CASE_TRUE, null))), r);
    }

    @Test
    void idStartingWithADot() {
        var r = CSS.parseSelector("#.identifier");
        assertEquals(List.of(List.of(new CSS.AttributeSelector("id", CSS.ATTR_ACTION_EQUALS, ".identifier", CSS.IGNORE_CASE_QUIRKS, null))), r);
    }

    //

    @Test
    void pseudoElement1() {
        var r = CSS.parseSelector("::foo");
        assertEquals(List.of(List.of(new CSS.PseudoElement("foo", null))), r);
    }

    @Test
    void pseudoElement2() {
        var r = CSS.parseSelector("::foo()");
        assertEquals(List.of(List.of(new CSS.PseudoElement("foo", ""))), r);
    }

    @Test
    void pseudoElement3() {
        var r = CSS.parseSelector("::foo(bar())");
        assertEquals(List.of(List.of(new CSS.PseudoElement("foo", "bar()"))), r);
    }
    //

    //
    @Test
    void pseudoSelector1() {
        var r = CSS.parseSelector(":foo");
        assertEquals(List.of(List.of(new CSS.PseudoSelector("foo", null))), r);
    }

    @Test
    void pseudoSelector2() {
        var r = CSS.parseSelector(":bar(baz)");
        assertEquals(List.of(List.of(new CSS.PseudoSelector("bar", new CSS.DataString("baz")))), r);
    }

    @Test
    void pseudoSelector3() {
        var r = CSS.parseSelector(":contains(\"(foo)\")");
        assertEquals(List.of(List.of(new CSS.PseudoSelector("contains", new CSS.DataString("(foo)")))), r);
    }

    @Test
    void pseudoSelector4() {
        var r = CSS.parseSelector(":where(a)");
        assertEquals(List.of(List.of(new CSS.PseudoSelector("where", new CSS.DataSelectors(List.of(List.of(new CSS.TagSelector("a", null))))))), r);
    }

    @Test
    void pseudoSelector5() {
        var r = CSS.parseSelector(":icontains('')");
        assertEquals(List.of(List.of(new CSS.PseudoSelector("icontains", new CSS.DataString("")))), r);
    }

    @Test
    void pseudoSelector6() {
        var r = CSS.parseSelector(":contains(\"(foo)\")");
        assertEquals(List.of(List.of(new CSS.PseudoSelector("contains", new CSS.DataString("(foo)")))), r);
    }


    @Test
    void multipleSelectors() {
        var r = CSS.parseSelector("a , b");
        assertEquals(List.of(List.of(new CSS.TagSelector("a", null)), List.of(new CSS.TagSelector("b", null))), r);
    }

    @Test
    void pseudoSelectorWithData() {
        var r = CSS.parseSelector(":host(h1, p)");
        assertEquals(List.of(List.of(
                new CSS.PseudoSelector(
                        "host",
                        new CSS.DataSelectors(List.of(List.of(new CSS.TagSelector( "h1", null)), List.of(new CSS.TagSelector("p", null))))
                ))), r);
    }

    @Test
    void checkIdSelectorWithEscapeSequence() {
        var r = CSS.parseSelector("#\\26 B");
        assertEquals(List.of(List.of(new CSS.AttributeSelector("id", CSS.ATTR_ACTION_EQUALS, "&B", CSS.IGNORE_CASE_QUIRKS, null))), r);
    }

    @Test
    void checkEscapedWhitespace() {
        var r = CSS.parseSelector("#\\  > a ");
        assertEquals(List.of(List.of(
                new CSS.AttributeSelector("id", CSS.ATTR_ACTION_EQUALS, " ", CSS.IGNORE_CASE_QUIRKS, null),
                new CSS.Combinator(CSS.CT_CHILD),
                new CSS.TagSelector("a", null)
        )), r);
    }

    // TODO: add all the missing String.raw tests
}
