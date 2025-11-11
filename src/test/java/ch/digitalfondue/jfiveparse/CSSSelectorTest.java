package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class CSSSelectorTest {

    private Document sizzle;

    @BeforeEach
    void reloadDocs() {
        sizzle = loadDocument("sizzle.html");
    }



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

    // https://github.com/fb55/css-select/blob/master/test/sizzle.ts#L508
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

    // https://github.com/fb55/css-select/blob/master/test/sizzle.ts#L579
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

    // https://github.com/fb55/css-select/blob/master/test/sizzle.ts#L703
    @Test
    void sizzleAttributes() {
        // Attribute Exists
        sizzleCheckMatcherIds("#qunit-fixture a[title]", "google");
        // Attribute Exists (case-insensitive)
        sizzleCheckMatcherIds("#qunit-fixture a[TITLE]", "google");
        // Attribute Exists
        sizzleCheckMatcherIds("#qunit-fixture *[title]", "google");
        // Attribute Exists
        sizzleCheckMatcherIds("#qunit-fixture [title]", "google");
        // Attribute Exists
        sizzleCheckMatcherIds("#qunit-fixture a[ title ]", "google");

        // Boolean attribute exists
        sizzleCheckMatcherIds("#select2 option[selected]", "option2d");
        // Boolean attribute equals
        sizzleCheckMatcherIds("#select2 option[selected='selected']", "option2d");

        // Attribute Equals
        sizzleCheckMatcherIds("#qunit-fixture a[rel='bookmark']", "simon1");
        // Attribute Equals
        sizzleCheckMatcherIds("#qunit-fixture a[rel='bookmark']", "simon1");
        // Attribute Equals
        sizzleCheckMatcherIds("#qunit-fixture a[rel=bookmark]", "simon1");
        // Attribute Equals
        sizzleCheckMatcherIds("#qunit-fixture a[href='http://www.google.com/']", "google");
        // Attribute Equals
        sizzleCheckMatcherIds("#qunit-fixture a[ rel = 'bookmark' ]", "simon1");
        // Attribute Equals Number
        sizzleCheckMatcherIds("#qunit-fixture option[value=1]",
                "option1b",
                "option2b",
                "option3b",
                "option4b",
                "option5c"
        );
        // Attribute Equals Number
        sizzleCheckMatcherIds("#qunit-fixture li[tabIndex=-1]", "foodWithNegativeTabIndex");

        sizzle.getElementById("anchor2").setAttribute("href", "#2");
        // `href` Attribute
        sizzleCheckMatcherIds("p a[href^=#]", "anchor2");
        sizzleCheckMatcherIds("p a[href*=#]", "simon1", "anchor2");

        // `for` Attribute
        sizzleCheckMatcherIds("form label[for]", "label-for");
        // `for` Attribute in form
        sizzleCheckMatcherIds("#form [for=action]", "label-for");

        // Attribute containing []
        sizzleCheckMatcherIds("input[name^='foo[']", "hidden2");
        // Attribute containing []
        sizzleCheckMatcherIds("input[name^='foo[bar]']", "hidden2");
        // Attribute containing []
        sizzleCheckMatcherIds("input[name*='[bar]']", "hidden2");
        // Attribute containing []
        sizzleCheckMatcherIds("input[name$='bar]']", "hidden2");
        // Attribute containing []
        sizzleCheckMatcherIds("input[name$='[bar]']", "hidden2");
        // Attribute containing []
        sizzleCheckMatcherIds("input[name$='foo[bar]']", "hidden2");
        // Attribute containing []
        sizzleCheckMatcherIds("input[name*='foo[bar]']", "hidden2");
        // Without context, double-quoted attribute containing ','
        sizzleCheckMatcherIds("input[data-comma=\"0,1\"]", "el12087");

        // Multiple Attribute Equals
        sizzleCheckMatcherIds("#form input[type='radio'], #form input[type='hidden']",
                "radio1",
                "radio2",
                "hidden1"
        );
        // Multiple Attribute Equals
        sizzleCheckMatcherIds("#form input[type='radio'], #form input[type=\"hidden\"]",
                "radio1",
                "radio2",
                "hidden1"
        );
        // Multiple Attribute Equals
        sizzleCheckMatcherIds("#form input[type='radio'], #form input[type=hidden]",
                "radio1",
                "radio2",
                "hidden1"
        );

        // Attribute selector using UTF8
        sizzleCheckMatcherIds("span[lang=中文]", "台北");

        // Attribute Begins With
        sizzleCheckMatcherIds("a[href ^= 'http://www']", "google", "yahoo");
        // Attribute Ends With
        sizzleCheckMatcherIds("a[href $= 'org/']", "mark");
        // Attribute Contains
        sizzleCheckMatcherIds("a[href *= 'google']", "google", "groups");

        // Empty values
        sizzleCheckMatcherIds("#select1 option[value='']", "option1a");
        // Empty values

        // Select options via :selected
        // sizzleCheckMatcherIds("#select1 option:selected", "option1a");
        // Select options via :selected
        // sizzleCheckMatcherIds("#select2 option:selected", "option2d");
        // Select options via :selected
        // sizzleCheckMatcherIds("#select3 option:selected", "option3b", "option3c");
        // Select options via :selected
        // sizzleCheckMatcherIds("select[name='select2'] option:selected", "option2d");

        // Grouped Form Elements
        sizzleCheckMatcherIds("input[name='foo[bar]']", "hidden2");

        // Underscores don't need escaping
        sizzleCheckMatcherIds("input[id=types_all]", "types_all");

        // FIXME added in document
        // Escaped space
        //sizzleCheckMatcherIds("input[name=foo\\ bar]", "attrbad_space");
        // Escaped dot
        //sizzleCheckMatcherIds("input[name=foo\\.baz]", "attrbad_dot");
        // Escaped brackets
        //sizzleCheckMatcherIds("input[name=foo\\[baz\\]]", "attrbad_brackets");

        // Escaped quote + right bracket
        //sizzleCheckMatcherIds("input[data-attr='foo_baz\\']']", "attrbad_injection");

        // Quoted quote
        //sizzleCheckMatcherIds("input[data-attr='\\'']", "attrbad_quote");
        // Quoted backslash
        //sizzleCheckMatcherIds("input[data-attr='\\\\']", "attrbad_backslash");
        // Quoted backslash quote
        //sizzleCheckMatcherIds("input[data-attr='\\\\\\'']", "attrbad_backslash_quote");
        // Quoted backslash backslash
        //sizzleCheckMatcherIds("input[data-attr='\\\\\\\\']", "attrbad_backslash_backslash");

        // Quoted backslash backslash (numeric escape)
        //sizzleCheckMatcherIds("input[data-attr='\\5C\\\\']", "attrbad_backslash_backslash");
        // Quoted backslash backslash (numeric escape with trailing space)
        //sizzleCheckMatcherIds("input[data-attr='\\5C \\\\']", "attrbad_backslash_backslash");
        // Quoted backslash backslash (numeric escape with trailing tab)
        //sizzleCheckMatcherIds("input[data-attr='\\5C\t\\\\']", "attrbad_backslash_backslash");
        // Long numeric escape (BMP)
        //sizzleCheckMatcherIds("input[data-attr='\\04e00']", "attrbad_unicode");

        // `input[type=text]`
        sizzleCheckMatcherIds("#form input[type=text]", "text1", "text2", "hidden2", "name");
        // `input[type=search]`
        sizzleCheckMatcherIds("#form input[type=search]", "search");
        // `script[src]` (jQuery #13777)
        sizzleCheckMatcherIds("#moretests script[src]", "script-src");

        var foo = sizzle.getElementById("foo");
        // Object.prototype property "constructor" (negative)',
        sizzleCheckMatcherIds("[constructor]");
        // Gecko Object.prototype property "watch" (negative)',
        sizzleCheckMatcherIds("[watch]");

        foo.setAttribute("constructor", "foo");
        foo.setAttribute("watch", "bar");
        // Object.prototype property "constructor"',
        sizzleCheckMatcherIds("[constructor='foo']", "foo");
        // Gecko Object.prototype property "watch"',
        sizzleCheckMatcherIds("[watch='bar']", "foo");

        // Value attribute is retrieved correctly
        sizzleCheckMatcherIds("input[value=Test]", "text1", "text2");
    }


    // https://github.com/fb55/css-select/blob/master/test/sizzle.ts#L967
    @Test
    void sizzlePseudoEmpty() {
        sizzleCheckMatcherIds("ul:empty", "firstUL");
        // Empty with comment node
        sizzleCheckMatcherIds("ol:empty", "empty");
    }

    // https://github.com/fb55/css-select/blob/master/test/sizzle.ts#L984
    @Test
    void sizzlePseudoFirstLastOnlyChildOfType() {
        // First Child
        sizzleCheckMatcherIds("p:first-child", "firstp", "sndp");
        // First Child (leading id)
        sizzleCheckMatcherIds("#qunit-fixture p:first-child", "firstp", "sndp");
        // First Child (leading class)
        sizzleCheckMatcherIds(".nothiddendiv div:first-child", "nothiddendivchild");
        // First Child (case-insensitive)
        sizzleCheckMatcherIds("#qunit-fixture p:FIRST-CHILD", "firstp", "sndp");

        // Last Child
        sizzleCheckMatcherIds("p:last-child", "sap");
        // Last Child (leading id)

        sizzleCheckMatcherIds("#qunit-fixture a:last-child",
                "simon1",
                "anchor1",
                "mark",
                "yahoo",
                "anchor2",
                "simon",
                "liveLink1",
                "liveLink2"
        );


        // Only Child
        sizzleCheckMatcherIds("#qunit-fixture a:only-child",
                "simon1",
                "anchor1",
                "yahoo",
                "anchor2",
                "liveLink1",
                "liveLink2"
        );


        // First-of-type
        sizzleCheckMatcherIds("#qunit-fixture > p:first-of-type", "firstp");


        // Last-of-type
        sizzleCheckMatcherIds("#qunit-fixture > p:last-of-type", "first");

        // FIXME
        // Only-of-type
        /*sizzleCheckMatcherIds("#qunit-fixture > :only-of-type",
                "name+value",
                "firstUL",
                "empty",
                "floatTest",
                "iframe",
                "table"
        );*/
    }

    // https://github.com/fb55/css-select/blob/master/test/sizzle.ts#L1336
    @Test
    void sizzleHas() {
        sizzleCheckMatcherIds("p:has(a)", "firstp", "ap", "en", "sap");
        // Basic test (irrelevant whitespace)
        sizzleCheckMatcherIds("p:has( a )", "firstp", "ap", "en", "sap");

        // Nested with overlapping candidates
        sizzleCheckMatcherIds("#qunit-fixture div:has(div:has(div:not([id])))", "moretests", "t2037");
    }

    // https://github.com/fb55/css-select/blob/master/test/sizzle.ts#L1738
    @Test
    void sizzleRoot() {
        sizzleCheckMatcherIds(":root", "html");
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
