package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

class CSSSelectorWPTTest {

    // TODO: add
    //
    // https://github.com/web-platform-tests/wpt/blob/8f25d0cad39c05f4f169a3864b47300f504b292a/css/selectors/has-matches-to-uninserted-elements.html
    // https://github.com/web-platform-tests/wpt/blob/8f25d0cad39c05f4f169a3864b47300f504b292a/css/selectors/has-relative-argument.html
    // https://github.com/web-platform-tests/wpt/blob/8f25d0cad39c05f4f169a3864b47300f504b292a/css/selectors/query/query-is.html

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
        // FIXME
        checkWithIds(HAS_BASIC, ":has(> .target)", "b", "f", "h");
        checkWithIds(HAS_BASIC, ":has(> .parent, > .target)", "a", "b", "f", "h");

        // FIXME
        //checkWithIds(":has(+ #h)", "f");
        //checkWithIds(".parent:has(~ #h)", "b", "f");
        //

        checkWithIdFirst(HAS_BASIC, ".sibling:has(.descendant)", "c");
    }

    @Test
    void testIs() {
        checkWithIds(HAS_BASIC,":is(.target ~ .sibling .descendant)", "k");
        checkWithIds(HAS_BASIC,":has(:is(#k))", "a", "h", "j");
        checkWithIds(HAS_BASIC,":has(:is(.target ~ .sibling .descendant))", "a", "h", "j");
    }

    @Disabled
    @Test
    void testFailing() {
        checkWithIds(HAS_BASIC, ":has(+ #h)", "f");
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
        /* FIXME
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
*/


        // FIXME, this should work
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(.d .e)", "d48", "d49", "d50");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(.d .e) .f", "d54");


        checkWithIds(RELATIVE_ARGUMENT, ".x:has(> .d)", "d49", "d50");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(> .d) .f", "d54");
        // FIXME, this should work!
        /*
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(~ .d ~ .e)", "d48", "d55", "d56");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(~ .d ~ .e) ~ .f", "d60");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(+ .d ~ .e)", "d55", "d56");
        checkWithIds(RELATIVE_ARGUMENT, ".x:has(+ .d ~ .e) ~ .f", "d60");
*/
        checkWithIds(RELATIVE_ARGUMENT, ".y:has(> .g .h)", "d63", "d71");

        checkWithIds(RELATIVE_ARGUMENT, ".y:has(.g .h)", "d63", "d68", "d71");


        checkWithIds(RELATIVE_ARGUMENT, ".y:has(> .g .h) .i", "d67", "d75");

        checkWithIds(RELATIVE_ARGUMENT, ".y:has(.g .h) .i", "d67", "d75");

        checkWithIds(RELATIVE_ARGUMENT, ".d .x:has(.e)", "d51", "d52");

        // FIXME this should work
        // checkWithIds(RELATIVE_ARGUMENT, ".d ~ .x:has(~ .e)", "d57", "d58");
    }


    private static void checkWithIds(Document doc, String selector, String... ids) {
        var main = doc.getElementById("main");
        var byIds = Arrays.stream(ids).map(main::getElementById).toList();
        var bySelector = main.getAllNodesMatching(Selector.parseSelector(selector));
        var idsSel = bySelector.stream().map(Element.class::cast).map(Element::getId).toList();
        Assertions.assertEquals(byIds, bySelector);
    }

    private static void checkWithIdFirst(Document doc, String selector, String id) {
        var main = doc.getElementById("main");
        var byIds = Stream.of(id).map(main::getElementById).toList();
        var bySelector = main.getAllNodesMatching(Selector.parseSelector(selector), true);
        Assertions.assertEquals(byIds, bySelector);
    }
}
