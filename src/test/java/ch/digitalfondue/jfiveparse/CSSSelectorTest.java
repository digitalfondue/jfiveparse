package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class CSSSelectorTest {

    private final Document sizzle = loadDocument("sizzle.html");


    // see https://github.com/fb55/css-select/blob/master/test/sizzle.ts#L15
    @Test
    void sizzleElementTest() {
        //
        Assertions.assertTrue(sizzle.getAllNodesMatching(Selector.parseSelector("")).isEmpty());
        //

        sizzleCheckMatcherIds("html", "html");
        sizzleCheckMatcherIds("body", "body");

        sizzleCheckMatcherIds("#qunit-fixture p", "firstp", "ap", "sndp", "en", "sap", "first");

        // Leading space
        sizzleCheckMatcherIds(" #qunit-fixture p", "firstp", "ap", "sndp", "en", "sap", "first");
        // Leading tab
        sizzleCheckMatcherIds("\t#qunit-fixture p", "firstp", "ap", "sndp", "en", "sap", "first");
        // Leading carriage return
        sizzleCheckMatcherIds("\r#qunit-fixture p", "firstp", "ap", "sndp", "en", "sap", "first");
        // Leading line feed
        sizzleCheckMatcherIds("\n#qunit-fixture p", "firstp", "ap", "sndp", "en", "sap", "first");
        // Leading form feed
        sizzleCheckMatcherIds("\f#qunit-fixture p", "firstp", "ap", "sndp", "en", "sap", "first");
        // Trailing space
        sizzleCheckMatcherIds("#qunit-fixture p ", "firstp", "ap", "sndp", "en", "sap", "first");
        // Trailing tab
        sizzleCheckMatcherIds("#qunit-fixture p\t", "firstp", "ap", "sndp", "en", "sap", "first");
        // Trailing carriage return
        sizzleCheckMatcherIds("#qunit-fixture p\r", "firstp", "ap", "sndp", "en", "sap", "first");
        // Trailing line feed
        sizzleCheckMatcherIds("#qunit-fixture p\n", "firstp", "ap", "sndp", "en", "sap", "first");
        // Trailing form feed
        sizzleCheckMatcherIds("#qunit-fixture p\f", "firstp", "ap", "sndp", "en", "sap", "first");


        // Parent Element
        sizzleCheckMatcherIds("dl ol", "empty", "listWithTabIndex");
        // Parent Element (non-space descendant combinator)
        sizzleCheckMatcherIds("dl\tol", "empty", "listWithTabIndex");



        // Checking sort order
        sizzleCheckMatcherIds("h2, h1", "qunit-header", "qunit-banner", "qunit-userAgent");

        // Checking sort order
        sizzleCheckMatcherIds("#qunit-fixture p, #qunit-fixture p a",
                "firstp",
                "simon1",
                "ap",
                "google",
                "groups",
                "anchor1",
                "mark",
                "sndp",
                "en",
                "yahoo",
                "sap",
                "anchor2",
                "simon",
                "first"
        );

        // TODO:
    }

    // see https://github.com/fb55/css-select/blob/master/test/sizzle.ts#L265
    @Test
    void sizzleId() {
        // ID Selector
        sizzleCheckMatcherIds("#body", "body");
        // ID Selector w/ Element
        sizzleCheckMatcherIds("body#body", "body");
        // ID Selector w/ Element
        sizzleCheckMatcherIds("ul#first");
        // ID selector with existing ID descendant
        sizzleCheckMatcherIds("#firstp #simon1", "simon1");
        // ID selector with non-existant descendant
        sizzleCheckMatcherIds("#firstp #foobar");
        // ID selector using UTF8
        sizzleCheckMatcherIds("#台北Táiběi", "台北Táiběi");
        // Multiple ID selectors using UTF8
        sizzleCheckMatcherIds("#台北Táiběi, #台北", "台北Táiběi", "台北");
        // Descendant ID selector using UTF8
        sizzleCheckMatcherIds("div #台北", "台北");
        // Child ID selector using UTF8
        sizzleCheckMatcherIds("form > #台北", "台北");

        // Escaped ID
        sizzleCheckMatcherIds("#foo\\:bar", "foo:bar");
        // Escaped ID with descendent FIXME
        // sizzleCheckMatcherIds("#foo\\:bar span:not(:input)", "foo_descendent");
        //
        // Escaped ID
        sizzleCheckMatcherIds("#test\\.foo\\[5\\]bar", "test.foo[5]bar");
        // Descendant escaped ID
        sizzleCheckMatcherIds("div #foo\\:bar", "foo:bar");
        // Descendant escaped ID
        sizzleCheckMatcherIds("div #test\\.foo\\[5\\]bar", "test.foo[5]bar");
        // Child escaped ID
        sizzleCheckMatcherIds("form > #foo\\:bar", "foo:bar");
        // Child escaped ID
        sizzleCheckMatcherIds("form > #test\\.foo\\[5\\]bar", "test.foo[5]bar");

        // ID Selector, child ID present
        sizzleCheckMatcherIds("#form > #radio1", "radio1");
        // ID Selector, not an ancestor ID
        sizzleCheckMatcherIds("#form #first");
        // ID Selector, not a child ID
        sizzleCheckMatcherIds("#form > #option1a");

        // All Children of ID
        sizzleCheckMatcherIds("#foo > *", "sndp", "en", "sap");
        // All Children of ID with no children
        sizzleCheckMatcherIds("#firstUL > *");

        // ID selector non-existing but name attribute on an A tag
        sizzleCheckMatcherIds("#tName2");
        // Leading ID selector non-existing but name attribute on an A tag
        sizzleCheckMatcherIds("#tName2 span");
        // Leading ID selector existing, retrieving the child
        sizzleCheckMatcherIds("#tName1 span", "tName1-span");

        // ID Selector contains backslash FIXME (add element)
        // sizzleCheckMatcherIds("#backslash\\\\foo", "backslash\\foo");

        // ID Selector on Form with an input that has a name of 'id'
        sizzleCheckMatcherIds("#lengthtest", "lengthtest");

        // ID selector with non-existant ancestor
        sizzleCheckMatcherIds("#asdfasdf #foobar");

        // ID selector within the context of another element
        //sizzleCheckMatcherIds("div#form", [], document.body);

        // Underscore ID
        sizzleCheckMatcherIds("#types_all", "types_all");
        // Dash ID
        sizzleCheckMatcherIds("#qunit-fixture", "qunit-fixture");

        // ID with weird characters in it
        sizzleCheckMatcherIds("#name\\+value", "name+value");
    }

    // see https://github.com/fb55/css-select/blob/master/test/sizzle.ts#L380
    @Test
    void sizzleClass() {
        // Class Selector
        sizzleCheckMatcherIds(".blog", "mark", "simon");
        // Class Selector
        sizzleCheckMatcherIds(".GROUPS", "groups");
        // Class Selector
        sizzleCheckMatcherIds(".blog.link", "simon");
        // Class Selector w/ Element
        sizzleCheckMatcherIds("a.blog", "mark", "simon");
        // Parent Class Selector
        sizzleCheckMatcherIds("p .blog", "mark", "simon");

        // Class selector using UTF8
        sizzleCheckMatcherIds(".台北Táiběi", "utf8class1");
        // Class selector using UTF8
        sizzleCheckMatcherIds(".台北", "utf8class1", "utf8class2");
        // Class selector using UTF8
        sizzleCheckMatcherIds(".台北Táiběi.台北", "utf8class1");
        // Class selector using UTF8
        sizzleCheckMatcherIds(".台北Táiběi, .台北", "utf8class1", "utf8class2");
        // Descendant class selector using UTF8
        sizzleCheckMatcherIds("div .台北Táiběi", "utf8class1");
        // Child class selector using UTF8
        sizzleCheckMatcherIds("form > .台北Táiběi", "utf8class1");

        // Escaped Class
        sizzleCheckMatcherIds(".foo\\:bar", "foo:bar");
        // Escaped Class
        sizzleCheckMatcherIds(".test\\.foo\\[5\\]bar", "test.foo[5]bar");
        // Descendant escaped Class
        sizzleCheckMatcherIds("div .foo\\:bar", "foo:bar");
        // Descendant escaped Class
        sizzleCheckMatcherIds("div .test\\.foo\\[5\\]bar", "test.foo[5]bar");
        // Child escaped Class
        sizzleCheckMatcherIds("form > .foo\\:bar", "foo:bar");
        // Child escaped Class
        sizzleCheckMatcherIds("form > .test\\.foo\\[5\\]bar", "test.foo[5]bar");
    }

    // see https://github.com/fb55/css-select/blob/master/test/sizzle.ts#L466
    @Test
    void sizzleName() {
        // Name selector
        sizzleCheckMatcherIds("input[name=action]", "text1");
        // Name selector with single quotes
        sizzleCheckMatcherIds("input[name='action']", "text1");
        // Name selector with double quotes
        sizzleCheckMatcherIds("input[name=\"action\"]", "text1");

        // Name selector non-input
        sizzleCheckMatcherIds("[name=example]", "name-is-example");
        // Name selector non-input
        sizzleCheckMatcherIds("[name=div]", "name-is-div");
        // Name selector non-input
        sizzleCheckMatcherIds("*[name=iframe]", "iframe");

        // Name selector for grouped input
        sizzleCheckMatcherIds("input[name='types[]']", "types_all", "types_anime", "types_movie");


        // Find elements that have similar IDs
        sizzleCheckMatcherIds("[name=tName1]", "tName1ID");
        // Find elements that have similar IDs
        sizzleCheckMatcherIds("[name=tName2]", "tName2ID");
        // Find elements that have similar IDs
        sizzleCheckMatcherIds("#tName2ID", "tName2ID");
    }

    @Test
    void sizzleMultiple() {
        // Comma Support
        sizzleCheckMatcherIds("h2, #qunit-fixture p",
                "qunit-banner",
                "qunit-userAgent",
                "firstp",
                "ap",
                "sndp",
                "en",
                "sap",
                "first"
        );
        // Comma Support
        sizzleCheckMatcherIds("h2 , #qunit-fixture p",
                "qunit-banner",
                "qunit-userAgent",
                "firstp",
                "ap",
                "sndp",
                "en",
                "sap",
                "first"
        );
        // Comma Support
        sizzleCheckMatcherIds("h2 , #qunit-fixture p",
                "qunit-banner",
                "qunit-userAgent",
                "firstp",
                "ap",
                "sndp",
                "en",
                "sap",
                "first"
        );
        // Comma Support
        sizzleCheckMatcherIds("h2,#qunit-fixture p",
                "qunit-banner",
                "qunit-userAgent",
                "firstp",
                "ap",
                "sndp",
                "en",
                "sap",
                "first"
        );
        // Comma Support
        sizzleCheckMatcherIds("h2,#qunit-fixture p ",
                "qunit-banner",
                "qunit-userAgent",
                "firstp",
                "ap",
                "sndp",
                "en",
                "sap",
                "first"
        );
        // Comma Support
        sizzleCheckMatcherIds("h2\t,\r#qunit-fixture p\n",
                "qunit-banner",
                "qunit-userAgent",
                "firstp",
                "ap",
                "sndp",
                "en",
                "sap",
                "first"
        );
    }

    @Test
    void sizzleChildAndAdjacent() {
        // Child
        sizzleCheckMatcherIds("p > a", "simon1", "google", "groups", "mark", "yahoo", "simon");
        // Child
        sizzleCheckMatcherIds("p> a", "simon1", "google", "groups", "mark", "yahoo", "simon");
        // Child
        sizzleCheckMatcherIds("p >a", "simon1", "google", "groups", "mark", "yahoo", "simon");
        // Child
        sizzleCheckMatcherIds("p>a", "simon1", "google", "groups", "mark", "yahoo", "simon");
        // Child w/ Class
        sizzleCheckMatcherIds("p > a.blog", "mark", "simon");
        // All Children
        sizzleCheckMatcherIds("code > *", "anchor1", "anchor2");
        // All Grandchildren
        sizzleCheckMatcherIds("p > * > *", "anchor1", "anchor2");
        // Adjacent
        sizzleCheckMatcherIds("#qunit-fixture a + a", "groups", "tName2ID");
        // Adjacent
        sizzleCheckMatcherIds("#qunit-fixture a +a", "groups", "tName2ID");
        // Adjacent
        sizzleCheckMatcherIds("#qunit-fixture a+ a", "groups", "tName2ID");
        // Adjacent
        sizzleCheckMatcherIds("#qunit-fixture a+a", "groups", "tName2ID");
        // Adjacent
        sizzleCheckMatcherIds("p + p", "ap", "en", "sap");
        // Adjacent
        sizzleCheckMatcherIds("p#firstp + p", "ap");
        // Adjacent
        sizzleCheckMatcherIds("p[lang=en] + p", "sap");
        // Adjacent
        sizzleCheckMatcherIds("a.GROUPS + code + a", "mark");
        // Comma, Child, and Adjacent
        sizzleCheckMatcherIds("#qunit-fixture a + a, code > a",
                "groups",
                "anchor1",
                "anchor2",
                "tName2ID"
        );
        // Element Preceded By
        sizzleCheckMatcherIds("#qunit-fixture p ~ div",
                "foo",
                "nothiddendiv",
                "moretests",
                "tabindex-tests",
                "liveHandlerOrder",
                "siblingTest"
        );
        // Element Preceded By
        sizzleCheckMatcherIds("#first ~ div",
                "moretests",
                "tabindex-tests",
                "liveHandlerOrder",
                "siblingTest"
        );
        // Element Preceded By
        sizzleCheckMatcherIds("#groups ~ a", "mark");
        // Element Preceded By
        sizzleCheckMatcherIds("#length ~ input", "idTest");
        // Element Preceded By
        sizzleCheckMatcherIds("#siblingfirst ~ em", "siblingnext", "siblingthird");
        // Element Preceded By (multiple)
        sizzleCheckMatcherIds("#siblingTest em ~ em ~ em ~ span", "siblingspan");
        // Element Preceded By, Containing
        sizzleCheckMatcherIds("#liveHandlerOrder ~ div em:contains('1')", "siblingfirst");

        // Multiple combinators selects all levels
        sizzleCheckMatcherIds("#siblingTest em *",
                "siblingchild",
                "siblinggrandchild",
                "siblinggreatgrandchild"
        );
        // Multiple combinators selects all levels
        sizzleCheckMatcherIds("#siblingTest > em *",
                "siblingchild",
                "siblinggrandchild",
                "siblinggreatgrandchild"
        );
        // Multiple sibling combinators doesn't miss general siblings
        sizzleCheckMatcherIds("#siblingTest > em:first-child + em ~ span", "siblingspan");
        // Combinators are not skipped when mixing general and specific
        sizzleCheckMatcherIds("#siblingTest > em:contains('x') + em ~ span");

        // Verify deep class selector
        sizzleCheckMatcherIds("div.blah > p > a");

        // No element deep selector
        sizzleCheckMatcherIds("div.foo > span > a");

        // Non-existant ancestors
        sizzleCheckMatcherIds(".fototab > .thumbnails > a");
    }


    private static Document loadDocument(String name) {
        try {
            return JFiveParse.parse(load(name));
        } catch (IOException e) {
            throw new IllegalStateException("not able to load " + name, e);
        }
    }

    private static String load(String name) throws IOException {
        return Files.readString(Path.of("src/test/resources/css/" + name));
    }


    // check if the given selector is able to find all the elements with the given id (with the specified order)
    private void sizzleCheckMatcherIds(String selector, String... ids) {
        var found = sizzle.getAllNodesMatching(Selector.parseSelector(selector));
        Assertions.assertEquals(ids.length, found.size());
        for (int i = 0; i < ids.length; i++) {
            Assertions.assertInstanceOf(Element.class, found.get(i));
            Element e = (Element) found.get(i);
            Assertions.assertEquals(ids[i], e.getAttribute("id"));
        }
    }
}
