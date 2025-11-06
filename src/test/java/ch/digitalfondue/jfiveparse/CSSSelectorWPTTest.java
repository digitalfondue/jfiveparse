package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
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
        checkWithIds(":has(#a)");
        checkWithIds(":has(.ancestor)", "a");
        checkWithIds(":has(.target)", "a", "b", "f", "h");
        checkWithIds(":has(.descendant)", "a", "b", "c", "f", "h", "j");
        checkWithIds(".parent:has(.target)", "b", "f", "h");
        checkWithIds(":has(.sibling ~ .target)", "a", "b");
        checkWithIds(".parent:has(.sibling ~ .target)", "b");

        // FIXME: "j" is not matched if we set :has = :is in code
        // checkWithIds(":has(:is(.target ~ .sibling .descendant))", "a", "h", "j");

        checkWithIds(".parent:has(:is(.target ~ .sibling .descendant))", "h");
        checkWithIds(".sibling:has(.descendant) ~ .target", "e");

        checkWithIds(":has(> .parent)", "a");
        // FIXME
        // checkWithIds(":has(> .target)", "b", "f", "h");
        checkWithIds(":has(> .parent, > .target)", "a", "b", "f", "h");

        // FIXME
        //checkWithIds(":has(+ #h)", "f");
        //checkWithIds(".parent:has(~ #h)", "b", "f");
        //

        checkWithIdFirst(".sibling:has(.descendant)", "c");
    }


    private static void checkWithIds(String selector, String... ids) {
        var main = HAS_BASIC.getElementById("main");
        var byIds = Arrays.stream(ids).map(main::getElementById).toList();
        var bySelector = main.getAllNodesMatching(Selector.parseSelector(selector));
        Assertions.assertEquals(byIds, bySelector);
    }

    private static void checkWithIdFirst(String selector, String id) {
        var main = HAS_BASIC.getElementById("main");
        var byIds = Stream.of(id).map(main::getElementById).toList();
        var bySelector = main.getAllNodesMatching(Selector.parseSelector(selector), true);
        Assertions.assertEquals(byIds, bySelector);
    }
}
