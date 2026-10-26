package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Objects;

final class CssTestAssert {

    private CssTestAssert() {
    }

    static void assertEquals(Object expected, Object actual) {
        if (!selectorEquals(expected, actual)) {
            Assertions.fail("expected: " + describe(expected) + " but was: " + describe(actual));
        }
    }

    private static boolean selectorEquals(Object expected, Object actual) {
        if (expected == actual) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }
        if (expected instanceof List<?> expectedList && actual instanceof List<?> actualList) {
            if (expectedList.size() != actualList.size()) {
                return false;
            }
            for (int i = 0; i < expectedList.size(); i++) {
                if (!selectorEquals(expectedList.get(i), actualList.get(i))) {
                    return false;
                }
            }
            return true;
        }
        if (expected instanceof CSS.Combinator e && actual instanceof CSS.Combinator a) {
            return e.type == a.type;
        }
        if (expected instanceof CSS.TagSelector e && actual instanceof CSS.TagSelector a) {
            return Objects.equals(e.name, a.name)
                    && Objects.equals(e.namespace, a.namespace);
        }
        if (expected instanceof CSS.AttributeSelector e && actual instanceof CSS.AttributeSelector a) {
            return Objects.equals(e.name, a.name)
                    && e.action == a.action
                    && Objects.equals(e.value, a.value)
                    && e.ignoreCase == a.ignoreCase
                    && Objects.equals(e.namespace, a.namespace);
        }
        if (expected instanceof CSS.PseudoElement e && actual instanceof CSS.PseudoElement a) {
            return Objects.equals(e.name, a.name)
                    && Objects.equals(e.data, a.data);
        }
        if (expected instanceof CSS.DataString e && actual instanceof CSS.DataString a) {
            return Objects.equals(e.value, a.value);
        }
        if (expected instanceof CSS.DataSelectors e && actual instanceof CSS.DataSelectors a) {
            return selectorEquals(e.value, a.value);
        }
        if (expected instanceof CSS.PseudoSelector e && actual instanceof CSS.PseudoSelector a) {
            return Objects.equals(e.name, a.name)
                    && selectorEquals(e.data, a.data);
        }
        if (expected instanceof CSS.InternalSelector e && actual instanceof CSS.InternalSelector a) {
            return Objects.equals(e.name, a.name);
        }
        if (expected instanceof CSS.UniversalSelector e && actual instanceof CSS.UniversalSelector a) {
            return Objects.equals(e.namespace, a.namespace);
        }
        return false;
    }

    private static String describe(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof List<?> list) {
            var sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(describe(list.get(i)));
            }
            return sb.append(']').toString();
        }
        if (value instanceof CSS.Combinator c) {
            return "Combinator[type=" + c.type + "]";
        }
        if (value instanceof CSS.TagSelector t) {
            return "TagSelector[name=" + t.name + ", namespace=" + t.namespace + "]";
        }
        if (value instanceof CSS.AttributeSelector a) {
            return "AttributeSelector[name=" + a.name + ", action=" + a.action
                    + ", value=" + a.value + ", ignoreCase=" + a.ignoreCase
                    + ", namespace=" + a.namespace + "]";
        }
        if (value instanceof CSS.PseudoElement p) {
            return "PseudoElement[name=" + p.name + ", data=" + p.data + "]";
        }
        if (value instanceof CSS.DataString d) {
            return "DataString[value=" + d.value + "]";
        }
        if (value instanceof CSS.DataSelectors d) {
            return "DataSelectors[value=" + describe(d.value) + "]";
        }
        if (value instanceof CSS.PseudoSelector p) {
            return "PseudoSelector[name=" + p.name + ", data=" + describe(p.data) + "]";
        }
        if (value instanceof CSS.InternalSelector i) {
            return "InternalSelector[name=" + i.name + "]";
        }
        if (value instanceof CSS.UniversalSelector u) {
            return "UniversalSelector[namespace=" + u.namespace + "]";
        }
        return String.valueOf(value);
    }
}
