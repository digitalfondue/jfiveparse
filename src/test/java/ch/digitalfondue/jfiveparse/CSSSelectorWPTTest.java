package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CSSSelectorWPTTest {

    // TODO: add
    //
    // https://github.com/web-platform-tests/wpt/blob/8f25d0cad39c05f4f169a3864b47300f504b292a/css/selectors/has-matches-to-uninserted-elements.html

    private static final Document HAS_BASIC = JFiveParse.parse("""
            <main id=main>
              <div id=a class="ancestor">
                <div id=b class="parent ancestor">
                  <div id=c class="sibling descendant">
                    <div id=d class="descendant"></div>
                  </div>
                  <div id=e class="target descendant"></div>
                </div>
                <div id=f class="parent ancestor">
                  <div id=g class="target descendant"></div>
                </div>
                <div id=h class="parent ancestor">
                  <div id=i class="target descendant"></div>
                  <div id=j class="sibling descendant">
                    <div id=k class="descendant"></div>
                  </div>
                </div>
              </div>
            </main>
            
            """);


    // see https://github.com/web-platform-tests/wpt/blob/8f25d0cad39c05f4f169a3864b47300f504b292a/css/selectors/has-basic.html#L64
    @Test
    void checkHasBasic() {
        checkWithIds(HAS_BASIC, ":has(#a)");
        checkWithIds(HAS_BASIC, ":has(.ancestor)", "a");
        checkWithIds(HAS_BASIC, ":has(.target)", "a", "b", "f", "h");
        checkWithIds(HAS_BASIC, ":has(.descendant)", "a", "b", "c", "f", "h", "j");
        checkWithIds(HAS_BASIC, ".parent:has(.target)", "b", "f", "h");
        checkWithIds(HAS_BASIC, ":has(.sibling ~ .target)", "a", "b");
        checkWithIds(HAS_BASIC, ".parent:has(.sibling ~ .target)", "b");


        checkWithIds(HAS_BASIC,":has(:is(.target ~ .sibling .descendant))", "a", "h", "j");

        checkWithIds(HAS_BASIC, ".parent:has(:is(.target ~ .sibling .descendant))", "h");
        checkWithIds(HAS_BASIC, ".sibling:has(.descendant) ~ .target", "e");

         checkWithIds(HAS_BASIC, ":has(> .parent)", "a");

        checkWithIds(HAS_BASIC, ":has(> .target)", "b", "f", "h");
        checkWithIds(HAS_BASIC, ":has(> .parent, > .target)", "a", "b", "f", "h");


        checkWithIds(HAS_BASIC,":has(+ #h)", "f");
        checkWithIds(HAS_BASIC,".parent:has(~ #h)", "b", "f");
        //

        checkWithIdFirst(HAS_BASIC, ".sibling:has(.descendant)", "c");
    }

    @Test
    void testIs() {
        checkWithIds(HAS_BASIC,":is(.target ~ .sibling .descendant)", "k");
        checkWithIds(HAS_BASIC,":has(:is(#k))", "a", "h", "j");
        checkWithIds(HAS_BASIC,":has(:is(.target ~ .sibling .descendant))", "a", "h", "j");
    }

    private static final Document RELATIVE_ARGUMENT = JFiveParse.parse("""
            <main id=main>
             <div id=d01>
              <div id=d02 class="x">
                <div id=d03 class="a"></div>
                <div id=d04></div>
                <div id=d05 class="b"></div>
              </div>
              <div id=d06 class="x">
                <div id=d07 class="x">
                  <div id=d08 class="a"></div>
                </div>
              </div>
              <div id=d09 class="x">
                <div id=d10 class="a">
                  <div id=d11 class="b"></div>
                </div>
              </div>
              <div id=d12 class="x">
                <div id=d13 class="a">
                  <div id=d14>
                    <div id=d15 class="b"></div>
                  </div>
                </div>
                <div id=d16 class="b"></div>
              </div>
             </div>
             <div id=d17>
              <div id=d18 class="x"></div>
              <div id=d19 class="x"></div>
              <div id=d20 class="a"></div>
              <div id=d21 class="x"></div>
              <div id=d22 class="a">
               <div id=d23 class="b"></div>
              </div>
              <div id=d24 class="x"></div>
              <div id=d25 class="a">
               <div id=d26>
                <div id=d27 class="b"></div>
               </div>
              </div>
              <div id=d28 class="x"></div>
              <div id=d29 class="a"></div>
              <div id=d30 class="b">
               <div id=d31 class="c"></div>
              </div>
              <div id=d32 class="x"></div>
              <div id=d33 class="a"></div>
              <div id=d34 class="b">
               <div id=d35>
                <div id=d36 class="c"></div>
               </div>
              </div>
              <div id=d37 class="x"></div>
              <div id=d38 class="a"></div>
              <div id=d39 class="b"></div>
              <div id=d40 class="x"></div>
              <div id=d41 class="a"></div>
              <div id=d42></div>
              <div id=d43 class="b">
               <div id=d44 class="x">
                <div id=d45 class="c"></div>
               </div>
              </div>
              <div id=d46 class="x"></div>
              <div id=d47 class="a">
              </div>
             </div>
             <div>
              <div id=d48 class="x">
               <div id=d49 class="x">
                <div id=d50 class="x d">
                 <div id=d51 class="x d">
                  <div id=d52 class="x">
                   <div id=d53 class="x e">
                    <div id=d54 class="f"></div>
                   </div>
                  </div>
                 </div>
                </div>
               </div>
              </div>
              <div id=d55 class="x"></div>
              <div id=d56 class="x d"></div>
              <div id=d57 class="x d"></div>
              <div id=d58 class="x"></div>
              <div id=d59 class="x e"></div>
              <div id=d60 class="f"></div>
             </div>
             <div>
              <div id=d61 class="x"></div>
              <div id=d62 class="x y"></div>
              <div id=d63 class="x y">
               <div id=d64 class="y g">
                <div id=d65 class="y">
                 <div id=d66 class="y h">
                  <div id=d67 class="i"></div>
                 </div>
                </div>
               </div>
              </div>
              <div id=d68 class="x y">
               <div id=d69 class="x"></div>
               <div id=d70 class="x"></div>
               <div id=d71 class="x y">
                <div id=d72 class="y g">
                 <div id=d73 class="y">
                  <div id=d74 class="y h">
                   <div id=d75 class="i"></div>
                  </div>
                 </div>
                </div>
               </div>
               <div id=d76 class="x"></div>
               <div id=d77 class="j"><div id=d78><div id=d79></div></div></div>
              </div>
              <div id=d80 class="j"></div>
             </div>
            </main>
            """);

    // see https://github.com/web-platform-tests/wpt/blob/8f25d0cad39c05f4f169a3864b47300f504b292a/css/selectors/has-relative-argument.html
    @Test
    void checkRelativeArgument() {
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(.a)", "d02", "d06", "d07", "d09", "d12");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(.a > .b)", "d09");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(.a .b)", "d09", "d12");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(.a + .b)", "d12");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(.a ~ .b)", "d02", "d12");


        checkWithIds(RELATIVE_ARGUMENT, ".x:has(> .a)", "d02", "d07", "d09", "d12");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(> .a > .b)", "d09");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(> .a .b)", "d09", "d12");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(> .a + .b)", "d12");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(> .a ~ .b)", "d02", "d12");

        checkWithIds(RELATIVE_ARGUMENT, ".x:has(+ .a)", "d19", "d21", "d24", "d28", "d32", "d37", "d40", "d46");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(+ .a > .b)", "d21");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(+ .a .b)", "d21", "d24");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(+ .a + .b)", "d28", "d32", "d37");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(+ .a ~ .b)", "d19", "d21", "d24", "d28", "d32", "d37", "d40");

        checkWithIds(RELATIVE_ARGUMENT, ".x:has(~ .a)", "d18", "d19", "d21", "d24", "d28", "d32", "d37", "d40", "d46");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(~ .a > .b)", "d18", "d19", "d21");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(~ .a .b)", "d18", "d19", "d21", "d24");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(~ .a + .b)", "d18", "d19", "d21", "d24", "d28", "d32", "d37");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(~ .a + .b > .c)", "d18", "d19", "d21", "d24", "d28");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(~ .a + .b .c)", "d18", "d19", "d21", "d24", "d28", "d32");


        checkWithIds(RELATIVE_ARGUMENT, ".x:has(.d .e)", "d48", "d49", "d50");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(.d .e) .f", "d54");


        checkWithIds(RELATIVE_ARGUMENT, ".x:has(> .d)", "d49", "d50");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(> .d) .f", "d54");

        checkWithIds(RELATIVE_ARGUMENT, ".x:has(~ .d ~ .e)", "d48", "d55", "d56");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(~ .d ~ .e) ~ .f", "d60");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(+ .d ~ .e)", "d55", "d56");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(+ .d ~ .e) ~ .f", "d60");

        checkWithIds(RELATIVE_ARGUMENT, ".y:has(> .g .h)", "d63", "d71");

        checkWithIds(RELATIVE_ARGUMENT, ".y:has(.g .h)", "d63", "d68", "d71");


        checkWithIds(RELATIVE_ARGUMENT, ".y:has(> .g .h) .i", "d67", "d75");

        checkWithIds(RELATIVE_ARGUMENT, ".y:has(.g .h) .i", "d67", "d75");

        checkWithIds(RELATIVE_ARGUMENT, ".d .x:has(.e)", "d51", "d52");

        checkWithIds(RELATIVE_ARGUMENT, ".d ~ .x:has(~ .e)", "d57", "d58");
    }


    // https://github.com/web-platform-tests/wpt/blob/master/css/selectors/not-complex.html
    private static final Document NOT_COMPLEX = JFiveParse.parse("""
            <main id=main>
              <div id=a><div id=d></div></div>
              <div id=b><div id=e></div></div>
              <div id=c><div id=f></div></div>
            </main>
            """);

    @Test
    void checkNotComplex() {
        checkWithIds(NOT_COMPLEX, ":not(#a)", "b", "c", "d", "e", "f");
        checkWithIds(NOT_COMPLEX, ":not(#a #d)", "a", "b", "c", "e", "f");
        checkWithIds(NOT_COMPLEX, ":not(#b div)", "a", "b", "c", "d", "f");
        checkWithIds(NOT_COMPLEX, ":not(div div)", "a", "b", "c");
        checkWithIds(NOT_COMPLEX, ":not(div + div)", "a", "d", "e", "f");
        checkWithIds(NOT_COMPLEX, ":not(main > div)", "d", "e", "f");
        checkWithIds(NOT_COMPLEX, ":not(#a, #b)", "c", "d", "e", "f");
        checkWithIds(NOT_COMPLEX, ":not(#f, main > div)", "d", "e");
        checkWithIds(NOT_COMPLEX, ":not(div + div + div, div + div > div)", "a", "b", "d");


        checkWithIds(NOT_COMPLEX, ":not(div:nth-child(1))", "b", "c");
        checkWithIds(NOT_COMPLEX, ":not(:not(div))", "a", "b", "c", "d", "e", "f");
        checkWithIds(NOT_COMPLEX, ":not(:not(:not(div)))");
        checkWithIds(NOT_COMPLEX, ":not(div, span)");
        checkWithIds(NOT_COMPLEX, ":not(span, p)", "a", "b", "c", "d", "e", "f");
        checkWithIds(NOT_COMPLEX, ":not(#unknown, .unknown)", "a", "b", "c", "d", "e", "f");
        checkWithIds(NOT_COMPLEX, ":not(#unknown > div, span)", "a", "b", "c", "d", "e", "f");
        checkWithIds(NOT_COMPLEX, ":not(#unknown ~ div, span)", "a", "b", "c", "d", "e", "f");

        // :hover not  supported
        // checkWithIds(NOT_COMPLEX, ":not(:hover div)", "a", "b", "c", "d", "e", "f");
        // checkWithIds(NOT_COMPLEX, ":not(:link div)", "a", "b", "c", "d", "e", "f");
        // checkWithIds(NOT_COMPLEX, ":not(:visited div)", "a", "b", "c", "d", "e", "f");
    }


    // https://github.com/web-platform-tests/wpt/blob/8f25d0cad39c05f4f169a3864b47300f504b292a/css/selectors/is-where-basic.html
    private static final Document IS_WHERE_BASIC = JFiveParse.parse("""
            <main id=main>
             <div id=a><div id=d></div></div>
             <div id=b><div id=e></div></div>
             <div id=c><div id=f></div></div>
           </main>
           """);

    @Test
    void checkIsWhereBasic() {
        // FIXME check we fail here
        // checkWithIds(IS_WHERE_BASIC, ":is()");
        checkWithIds(IS_WHERE_BASIC, ":is(#a)", "a");
        checkWithIds(IS_WHERE_BASIC, ":is(#a, #f)", "a", "f");
        checkWithIds(IS_WHERE_BASIC, ":is(#a, #c) :where(#a #d, #c #f)", "d", "f");
        checkWithIds(IS_WHERE_BASIC, "#c > :is(#c > #f)", "f");
        checkWithIds(IS_WHERE_BASIC, "#c > :is(#b > #f)");
        checkWithIds(IS_WHERE_BASIC, "#a div:is(#d)", "d");
        checkWithIds(IS_WHERE_BASIC, ":is(div) > div", "d", "e", "f");
        checkWithIds(IS_WHERE_BASIC, ":is(*) > div", "a", "b", "c", "d", "e", "f");
        checkWithIds(IS_WHERE_BASIC, ":is(*) div", "a", "b", "c", "d", "e", "f");
        checkWithIds(IS_WHERE_BASIC, "div > :where(#e, #f)", "e", "f");
        checkWithIds(IS_WHERE_BASIC, "div > :where(*)", "d", "e", "f");
        checkWithIds(IS_WHERE_BASIC, ":is(*) > :where(*)", "a", "b", "c", "d", "e", "f");
        checkWithIds(IS_WHERE_BASIC, ":is(#a + #b) + :is(#c)", "c");
        checkWithIds(IS_WHERE_BASIC, ":is(#a, #b) + div", "b", "c");
    }


    private static final Document LAST_CHILD = JFiveParse.parse("""
            <main id="main">
            <div>
              <div id="target1">Whitespace nodes should be ignored.</div>
            </div>
            
            <div>
              <blockquote></blockquote>
              <div id="target2">There is a prior child element.</div>
            </div>
            
            <div>
              <div id="target3">A comment node should be ignored.</div>
              <!-- -->
            </div>
            
            <div>
              <div id="target4">Non-whitespace text node should be ignored.</div>
              .
            </div>
            
            <div>
              <div id="target5" data-expected="false">The first child should not be matched.</div>
              <blockquote></blockquote>
            </div>
            </main>
            """);

    // https://github.com/web-platform-tests/wpt/blob/8f25d0cad39c05f4f169a3864b47300f504b292a/css/selectors/last-child.html
    @Test
    void checkLastChild() {
        checkWithIds(LAST_CHILD, "#target1:last-child", "target1");
        checkWithIds(LAST_CHILD, "#target2:last-child", "target2");
        checkWithIds(LAST_CHILD, "#target3:last-child", "target3");
        checkWithIds(LAST_CHILD, "#target4:last-child", "target4");
        checkWithIds(LAST_CHILD, "#target5:last-child");
    }


    private static final Document ONLY_CHILD = JFiveParse.parse("""
            <main id=main>
            <div>
              <div id="target1">Whitespace nodes should be ignored.</div>
            </div>
            
            <div>
              <div id="target2">A comment node should be ignored.</div>
              <!-- -->
            </div>
            
            <div>
              <div id="target3">Non-whitespace text node should be ignored.</div>
              .
            </div>
            
            <div>
              <blockquote></blockquote>
              <div id="target4" data-expected="false">There is another child element.</div>
            </div>
            
            <div>
              <div id="target5"></div>
            </div>
            </main>
            """);

    // https://github.com/web-platform-tests/wpt/blob/8f25d0cad39c05f4f169a3864b47300f504b292a/css/selectors/only-child.html
    @Test
    void checkOnlyChild() {
        checkWithIds(ONLY_CHILD, "#target1:only-child", "target1");
        checkWithIds(ONLY_CHILD, "#target2:only-child", "target2");
        checkWithIds(ONLY_CHILD, "#target3:only-child", "target3");
        checkWithIds(ONLY_CHILD, "#target4:only-child");
        checkWithIds(ONLY_CHILD, "#target5:only-child", "target5");
    }


    private static final Document QUERY_WHERE = JFiveParse.parse("""
            <main id="main">
            <div id="a1" class="a">
                <div class="b" id="b1"></div>
                <div class="c" id="c1"></div>
                <div class="c" id="d"></div>
                <div class="e" id="e1"></div>
                <div class="f" id="f1"></div>
                <div class="g">
                  <div class="b" id="b2">
                    <div class="b" id="b3"></div>
                  </div>
                </div>
                <div class="h" id="h1"></div>
              </div>
              <div class="c" id="c2">
                <div id="a2" class="a"></div>
                <div class="e" id="e2"></div>
              </div>
            </main>
            """);
    //  https://github.com/web-platform-tests/wpt/blob/master/css/selectors/query/query-where.html
    @Test
    void checkQueryWhere() {
        checkWithIds(QUERY_WHERE, ".a :where(.b, .c)","b1", "c1", "d", "b2", "b3");

        // Compound selector arguments are supported by :where
        checkWithIds(QUERY_WHERE, ".a :where(.c#d, .e)", "d", "e1");

        // Complex selector arguments are supported by :where
        checkWithIds(QUERY_WHERE, ".a :where(.e+.f, .g>.b, .h)", "f1", "b2", "h1");

        // Nested selector arguments are supported by :where
        checkWithIds(QUERY_WHERE, ".a+:where(.b+.f, :where(.c>.e, .g))", "e2");

        // Nested :is selector arguments are supported by :where
        checkWithIds(QUERY_WHERE, ".a :where(:is(:is(.b ~ .c)))", "c1", "d");

        // Nested :not selector arguments are supported by :where
        checkWithIds(QUERY_WHERE, ".b + :where(.c + .c + .c, .b + .c:not(span), .b + .c + .e) ~ .h", "h1");
    }


    private static final Document IS_WHERE_NOT = JFiveParse.parse("""
            <main id=main>
              <div id=a><div id=d></div></div>
              <div id=b><div id=e></div></div>
              <div id=c><div id=f></div></div>
            </main>
            """);

    // https://github.com/web-platform-tests/wpt/blob/8f25d0cad39c05f4f169a3864b47300f504b292a/css/selectors/is-where-not.html
    @Test
    void checkIsWhereNot() {
        checkWithIds(IS_WHERE_NOT, ":not(:is(#a))", "b", "c", "d", "e", "f");
        checkWithIds(IS_WHERE_NOT, ":not(:where(#b))", "a", "c", "d", "e", "f");
        checkWithIds(IS_WHERE_NOT, ":not(:where(:root #c))", "a", "b", "d", "e", "f");
        checkWithIds(IS_WHERE_NOT, ":not(:is(#a, #b))", "c", "d", "e", "f");
        checkWithIds(IS_WHERE_NOT, ":not(:is(#b div))", "a", "b", "c", "d", "f");
        checkWithIds(IS_WHERE_NOT, ":not(:is(#a div, div + div))", "a", "e", "f");
        checkWithIds(IS_WHERE_NOT, ":not(:is(span))", "a", "b", "c", "d", "e", "f");
        checkWithIds(IS_WHERE_NOT, ":not(:is(div))");
        checkWithIds(IS_WHERE_NOT, ":not(:is(*|div))");
        checkWithIds(IS_WHERE_NOT, ":not(:is(*|*))");
        checkWithIds(IS_WHERE_NOT, ":not(:is(*))");
        checkWithIds(IS_WHERE_NOT, ":not(:is(svg|div))", "a", "b", "c", "d", "e", "f");
        checkWithIds(IS_WHERE_NOT, ":not(:is(:not(div)))", "a", "b", "c", "d", "e", "f");
        checkWithIds(IS_WHERE_NOT, ":not(:is(span, b, i))", "a", "b", "c", "d", "e", "f");
        checkWithIds(IS_WHERE_NOT, ":not(:is(span, b, i, div))");
        checkWithIds(IS_WHERE_NOT, ":not(:is(#b ~ div div, * + #c))", "a", "b", "d", "e");
        checkWithIds(IS_WHERE_NOT, ":not(:is(div > :not(#e)))", "a", "b", "c", "e");
        checkWithIds(IS_WHERE_NOT, ":not(:is(div > :not(:where(#e, #f))))", "a", "b", "c", "e", "f");
    }

    // https://github.com/web-platform-tests/wpt/blob/master/css/selectors/query/query-is.html

    private static final Document QUERY_IS=  JFiveParse.parse("""
            <main id=main>
            <div id="a1" class="a">
                <div class="b" id="b1"></div>
                <div class="c" id="c1"></div>
                <div class="c" id="d"></div>
                <div class="e" id="e1"></div>
                <div class="f" id="f1"></div>
                <div class="g">
                  <div class="b" id="b2">
                    <div class="b" id="b3"></div>
                  </div>
                </div>
                <div class="h" id="h1"></div>
              </div>
              <div class="c" id="c2">
                <div id="a2" class="a"></div>
                <div class="e" id="e2"></div>
              </div>
            </main>
            """);

    @Test
    void checkQueryIs() {
        checkWithIds(QUERY_IS, ".a :is(.b, .c)", "b1", "c1", "d", "b2", "b3");

        // Compound selector arguments are supported by :is
        checkWithIds(QUERY_IS, ".a :is(.c#d, .e)", "d", "e1");

        // Complex selector arguments are supported by :is
        checkWithIds(QUERY_IS, ".a :is(.e+.f, .g>.b, .h)", "f1", "b2", "h1");

        // Nested selector arguments are supported by :is
        checkWithIds(QUERY_IS, ".a+:is(.b+.f, :is(.c>.e, .g))", "e2");

        // Nested :where selector arguments are supported by :is
        checkWithIds(QUERY_IS, ".a :is(:where(:where(.b ~ .c)))", "c1", "d");

        // Nested :not selector arguments are supported by :is
        checkWithIds(QUERY_IS, ".b + :is(.c + .c + .c, .b + .c:not(span), .b + .c + .e) ~ .h", "h1");
    }


    private static final Document ONLY_OF_TYPE = JFiveParse.parse("""
            <main id=main>
            <div>
              <div id="target1">Whitespace nodes should be ignored.</div>
            </div>
            
            <div>
              <div id="target2">A comment node should be ignored.</div>
              <!-- -->
            </div>
            
            <div>
              <div id="target3">Non-whitespace text node should be ignored.</div>
              .
            </div>
            
            <div>
              <blockquote></blockquote>
              <div id="target4" data-expected="false">There is another child element of a different type.</div>
            </div>
            
            <div>
              <div id="target5"></div>
            </div>
            </main>
            """);

    // https://github.com/web-platform-tests/wpt/blob/master/css/selectors/only-of-type.html
    @Test
    void checkOnlyOfType() {
        var target1 = ONLY_OF_TYPE.getElementById("target1");
        var target2 = ONLY_OF_TYPE.getElementById("target2");
        var target3 = ONLY_OF_TYPE.getElementById("target3");
        var target4 = ONLY_OF_TYPE.getElementById("target4");
        var target5 = ONLY_OF_TYPE.getElementById("target5");

        Assertions.assertTrue(target1.matches(":only-of-type"));
        Assertions.assertTrue(target2.matches(":only-of-type"));
        Assertions.assertTrue(target3.matches(":only-of-type"));
        Assertions.assertTrue(target4.matches(":only-of-type"));
        Assertions.assertTrue(target5.matches(":only-of-type"));


        var ofDifferentType = new Element("span", Node.NAMESPACE_HTML);
        ofDifferentType.setId("target6");
        ONLY_OF_TYPE.getElementById("target5").getParentNode().appendChild(ofDifferentType);

        var target6 = ONLY_OF_TYPE.getElementById("target6");

        Assertions.assertTrue(target5.matches(":only-of-type"));
        Assertions.assertTrue(target6.matches(":only-of-type"));

        var anotherOfType = new Element("div", Node.NAMESPACE_HTML);
        anotherOfType.setId("target7");
        ONLY_OF_TYPE.getElementById("target5").getParentNode().appendChild(anotherOfType);

        var target7 = ONLY_OF_TYPE.getElementById("target7");

        Assertions.assertFalse(target5.matches(":only-of-type"));
        Assertions.assertFalse(target7.matches(":only-of-type"));

        ONLY_OF_TYPE.getElementById("target5").getParentNode().removeChild(anotherOfType);
        Assertions.assertTrue(target5.matches(":only-of-type"));
    }


    private static void checkWithIds(Node doc, String selector, String... ids) {
        var main = doc.getElementById("main");
        var byIds = Arrays.stream(ids).map(main::getElementById).collect(Collectors.toSet()); // not in order
        var bySelector = new HashSet<>(main.getAllNodesMatching(Selector.parseSelector(selector))); // not in order
        // var selectedIds = bySelector.stream().map(n -> ((Element) n).getAttribute("id")).toList();
        Assertions.assertEquals(byIds, bySelector);
    }

    private static void checkWithIdFirst(Node doc, String selector, String id) {
        var main = doc.getElementById("main");
        var byIds = Stream.of(id).map(main::getElementById).collect(Collectors.toSet());
        var bySelector = new HashSet<>(main.getAllNodesMatching(Selector.parseSelector(selector), true));
        Assertions.assertEquals(byIds, bySelector);
    }
}
