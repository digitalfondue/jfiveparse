/**
 * Copyright © 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Selector is a type safe builder of node/element selectors. The API is similar
 * to CSS.
 * 
 * Some examples:
 * 
 * <table>
 * <tr>
 * <th>CSS</th>
 * <th>Selector</th>
 * </tr>
 * <tr>
 * <td>div</td>
 * <td><code>Selector.select().element("div").toMatcher()</code></td>
 * </tr>
 * <tr>
 * <td>.className</td>
 * <td><code>Selector.select().hasClass("className").toMatcher()</code></td>
 * </tr>
 * <tr>
 * <td>.className1 .className2</td>
 * <td><code>Selector.select().hasClass("className1").withDescendant().hasClass("className2").toMatcher()</code></td>
 * </tr>
 * <tr>
 * <td>div + p</td>
 * <td><code>Selector.select().element("div").nextSibling().element("p").toMatcher()</code></td>
 * </tr>
 * <tr>
 * <td>div ~ p</td>
 * <td><code>Selector.select().element("div").subsequentSibling().element("p").toMatcher()</code></td>
 * </tr>
 * <tr>
 * <td>div.className</td>
 * <td>
 * <code>Selector.select().element("div").hasClass("className").toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>div.className1.className2</td>
 * <td>
 * <code>Selector.select().element("div").hasClass("className1", "className2").toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>#myId</td>
 * <td><code>Selector.select().id("myId").toMatcher()</code></td>
 * </tr>
 * <tr>
 * <td>#myId &gt; span.className</td>
 * <td>
 * <code>Selector.select().id("myId").withChild().element("span").hasClass("className").toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>#myId span.className</td>
 * <td>
 * <code>Selector.select().id("myId").withDescendant().element("span").hasClass("className").toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>span[data-test]</td>
 * <td>
 * <code>Selector.select().element("span").attr("data-test").toMatcher()</code></td>
 * </tr>
 * <tr>
 * <td>span[data-test=bla]</td>
 * <td>
 * <code>Selector.select().element("span").attrValEq("data-test", "bla").toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>span[data-test*=bla]</td>
 * <td>
 * <code>Selector.select().element("span").attrValContains("data-test", "bla").toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>span[data-test$=bla]</td>
 * <td>
 * <code>Selector.select().element("span").attrValEndWith("data-test", "bla").toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>span[data-test~=bla]</td>
 * <td>
 * <code>Selector.select().element("span").attrValInList("data-test", "bla").toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>span[data-test^=bla]</td>
 * <td>
 * <code>Selector.select().element("span").attrValStartWith("data-test", "bla").toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>span:first-child</td>
 * <td>
 * <code>Selector.select().element("span").isFirstElementChild().toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>span:last-child</td>
 * <td>
 * <code>Selector.select().element("span").isLastElementChild().toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>td *</td>
 * <td>
 * <code>Selector.select().element("td").withDescendant().universal().toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>td > *</td>
 * <td>
 * <code>Selector.select().element("td").withChild().universal().toMatcher()</code>
 * </td>
 * </tr>
 * </table>
 */
public class Selector {

    private final List<NodeMatcher> matchers = new ArrayList<>();

    public static NodeMatcher parseSelector(String selector) {
        List<List<CSS.CssSelector>> cssSelectors = CSS.parseSelector(selector);
        List<NodeMatcher> res = new ArrayList<>();
        for (List<CSS.CssSelector> cssSelector : cssSelectors) {
            res.add(toNodeMatcher(cssSelector));
        }
        return res.size() == 1 ? res.get(0) : orMatchers(res);
    }

    private static NodeMatcher toNodeMatcher(List<CSS.CssSelector> selector) {
        var res = Selector.select();
        for (var part : selector) {
            if (part instanceof CSS.Combinator c) {
                res = switch (c.type()) {
                    case DESCENDANT -> res.withDescendant();
                    case CHILD -> res.withChild();
                    case ADJACENT -> res.nextSibling();
                    case SIBLING -> res.subsequentSibling();
                    case PARENT, COLUMN_COMBINATOR -> throw new IllegalStateException("to implement");
                };
            } else if (part instanceof CSS.TagSelector t) {
                res = t.namespace() == null ? res.element(t.name()) : res.element(t.name(), t.namespace());
            } else if (part instanceof CSS.AttributeSelector a) {
                var action = a.action();
                var name = a.name();
                var value = a.value();
                if (action == CSS.AttributeAction.EQUALS) {
                    res = res.attrValEq(name, value);
                } else if (action == CSS.AttributeAction.ELEMENT && "class".equals(name)) {
                    res = res.hasClass(value);
                } else if (action == CSS.AttributeAction.EXISTS) {
                    res = res.attr(name);
                } else if (action == CSS.AttributeAction.START) {
                    res = res.attrValStartWith(name, value);
                } else if (action == CSS.AttributeAction.ANY) {
                    res = res.attrValContains(name, value);
                } else if (action == CSS.AttributeAction.END) {
                    res = res.attrValEndWith(name, value);
                } else {
                    throw new IllegalStateException("to implement");
                }
            } else if (part instanceof CSS.PseudoElement pe) {
                throw new IllegalStateException("to implement");
            } else if (part instanceof CSS.PseudoSelector ps) {
                String name = ps.name();
                if ("contains".equals(name) && ps.data() instanceof CSS.DataString ds) {
                    res = res.contains(ds.value());
                } else if ("first-child".equals(name)) {
                    res = res.isFirstElementChild();
                } else if ("last-child".equals(name)) {
                    res = res.isLastElementChild();
                } else {
                    throw new IllegalStateException("to implement");
                }
            } else if (part instanceof CSS.UniversalSelector u) {
                res = res.universal();
            }
        }
        return res.toMatcher();
    }

    /**
     * Pseudo selector: div:contains('text').
     * See <a href="https://api.jquery.com/contains-selector/#contains1">https://api.jquery.com/contains-selector/#contains1</a>.
     *
     * @param value
     * @return
     */
    public Selector contains(String value) {
        matchers.add((node) -> {
            if (node instanceof CommonNode.CommonElement e) {
                var textContent = e.getTextContent();
                return textContent != null && textContent.contains(value);
            }
            return false;
        });
        return this;
    }


    /**
     * Static factory method. Build a selector builder.
     * 
     * @return
     */
    public static Selector select() {
        return new Selector();
    }

    /**
     * Match an element with the given name.
     * <p>
     * CSS equivalent: <code>name</code>
     * </p>
     * 
     * @param name
     * @return
     */
    public Selector element(String name) {
        matchers.add(node -> node.getNodeType() == Node.ELEMENT_NODE && name.equals(node.getNodeName()));
        return this;
    }

    /**
     * Universal selector "*". It matches any _element_ node.
     *
     * @return
     */
    public Selector universal() {
        matchers.add(n -> n.getNodeType() == Node.ELEMENT_NODE);
        return this;
    }

    /**
     * Match an element with the given name and namespace. Useful for matching
     * SVG or MathML elements.
     * 
     * @param name
     * @param namespace
     * @return
     */
    public Selector element(String name, String namespace) {
        matchers.add(node -> node instanceof CommonNode.CommonElement ce && name.equals(ce.getNodeName())
                && Objects.equals(namespace, ce.getNamespaceURI()));
        return this;
    }

    /**
     * Match an element with the class name. Case-sensitive.
     * <p>
     * CSS equivalent: <code>.value</code>
     * </p>
     * 
     * @param value
     * @return
     */
    public Selector hasClass(String value) {
        return attrValInList("class", value);
    }

    /**
     * Match an element with all the classes.
     * <p>
     * CSS equivalent: <code>.value.other1.other2</code>
     * </p>
     * 
     * @param value
     * @param others
     * @return
     */
    public Selector hasClass(String value, String... others) {
        hasClass(value);
        if (others != null) {
            for (String n : others) {
                hasClass(n);
            }
        }
        return this;
    }

    /**
     * Match an element with the given id value
     * <p>
     * CSS equivalent: <code>#value</code>
     * </p>
     * 
     * @param value
     * @return
     */
    public Selector id(String value) {
        return attrValEq("id", value);
    }

    private static NodeMatcher matchAttr(String name, BiPredicate<String, CommonNode.CommonElement> attributeValueMatcher) {
        return (node) -> {
            if (node instanceof CommonNode.CommonElement elem) {
                var isHtml = Node.NAMESPACE_HTML.equals(elem.getNamespaceURI());
                var toCompareAttr = isHtml ? Common.convertToAsciiLowerCase(name) : name;
                return elem.containsAttribute(toCompareAttr) && attributeValueMatcher.test(elem.getAttributeValue(toCompareAttr), elem);
            }
            return false;
        };
    }

    /**
     * Match only the element with an attribute named "name". Case insensitive for html element.
     * <p>
     * CSS equivalent: <code>[name]</code>
     * </p>
     * 
     * @param name
     * @return
     */
    public Selector attr(String name) {
        matchers.add(matchAttr(name, (attrValue, elem) -> true));
        return this;
    }

    /**
     * Match only the element with an attribute named "name" with value "value".
     * Case sensitive.
     * <p>
     * CSS equivalent: <code>[name=value]</code>
     * </p>
     * 
     * @param name
     * @param value
     * @return
     */
    public Selector attrValEq(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> Objects.equals(attrValue, value)));
        return this;
    }

    /**
     * Match only the element with an attribute named "name" with "value"
     * present in the list of white space delimited values. Case sensitive.
     * <p>
     * CSS equivalent: <code>[name~=value]</code>
     * </p>
     * 
     * @param name
     * @param value
     * @return
     */
    public Selector attrValInList(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> DOMTokenList.extractValues(elem, name).contains(value)));
        return this;
    }

    /**
     * Match only the element with an attribute named "name" with an attribute
     * that start with "value".
     * <p>
     * CSS equivalent: <code>[name^=value]</code>
     * </p>
     * 
     * @param name
     * @param value
     * @return
     */
    public Selector attrValStartWith(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.startsWith(value)));
        return this;
    }

    /**
     * Match only the element with an attribute named "name" with an attribute
     * that end with "value".
     * <p>
     * CSS equivalent: <code>[name$=value]</code>
     * </p>
     * 
     * @param name
     * @param value
     * @return
     */
    public Selector attrValEndWith(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.endsWith(value)));
        return this;
    }

    /**
     * Match only the element with an attribute named "name" with an attribute
     * that that contains the string "value".
     * <p>
     * CSS equivalent: <code>[name*=value]</code>
     * </p>
     * 
     * @param name
     * @param value
     * @return
     */
    public Selector attrValContains(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.contains(value)));
        return this;
    }

    /**
     * Match only the node which is a first child.
     * 
     * @return
     */
    public Selector isFirstChild() {
        matchers.add(node -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getFirstChild()));
        return this;
    }

    /**
     * Match only the element which is a first child.
     *
     * <p>
     * CSS equivalent: <code>:first-child</code>
     * </p>
     * 
     * @return
     */
    public Selector isFirstElementChild() {
        matchers.add(node -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getFirstElementChild()));
        return this;
    }

    /**
     * Match only the node which is a last child.
     * 
     * @return
     */
    public Selector isLastChild() {
        matchers.add(node -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getLastChild()));
        return this;
    }

    /**
     * Match only the element which is a last child.
     * <p>
     * CSS equivalent: <code>:last-child</code>
     * </p>
     * @return
     */
    public Selector isLastElementChild() {
        matchers.add(node -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getLastElementChild()));
        return this;
    }

    private List<NodeMatcher> copyAndClear() {
        var copyMatchers = new ArrayList<>(matchers);
        matchers.clear();
        return copyMatchers;
    }

    /**
     * Match a child node with another set of matchers.
     * 
     * <p>
     * CSS equivalent: <code>&gt;</code>
     * </p>
     * 
     * @return
     */
    public Selector withChild() {
        var rules = andMatchers(copyAndClear());
        NodeMatcher hasParentMatching = (node) -> node.getParentNode() != null && rules.match(node.getParentNode());
        matchers.add(hasParentMatching);
        return this;
    }

    /**
     * Next sibling combinator (+).
     *
     * <p>
     * CSS equivalent: <code>img + p</code>
     * </p>
     *
     * @return
     */
    public Selector nextSibling() {
        var rules = andMatchers(copyAndClear());
        NodeMatcher nextSibling = (node) -> {
            var previousElementSibling = node.getPreviousElementSibling();
            return previousElementSibling != null && rules.match(previousElementSibling);
        };
        matchers.add(nextSibling);
        return this;
    }

    /**
     * Subsequent sibling combinator (~).
     *
     * <p>
     * CSS equivalent: <code>img ~ p</code>
     * </p>
     *
     * @return
     */
    public Selector subsequentSibling() {
        var rules = andMatchers(copyAndClear());
        NodeMatcher subsequentSibling = (node) -> {
            var previousElementSibling = node.getPreviousElementSibling();
            while(previousElementSibling != null) {
                if (rules.match(previousElementSibling)) {
                    return true;
                }
                previousElementSibling = previousElementSibling.getPreviousElementSibling();
            };
            return false;
        };
        matchers.add(subsequentSibling);
        return this;
    }

    /**
     * Match a descendant node with another set of matchers.
     * 
     * <p>
     * CSS equivalent: the space between div and span in <code>div span</code>
     * </p>
     * 
     * @return
     */
    public Selector withDescendant() {
        var ancestorMatcher = andMatchers(copyAndClear());
        NodeMatcher hasAncestorMatching = (node) -> {
            while (node.getParentNode() != null) {
                node = node.getParentNode();
                if (ancestorMatcher.match(node)) {
                    return true;
                }
            }
            return false;
        };
        matchers.add(hasAncestorMatching);
        return this;
    }

    private static NodeMatcher andMatchers(List<NodeMatcher> nodeMatchers) {
        return (node) -> {
            for (NodeMatcher m : nodeMatchers) {
                if (!m.match(node)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static NodeMatcher orMatchers(List<NodeMatcher> nodeMatchers) {
        return (node) -> {
            for (NodeMatcher m : nodeMatchers) {
                if (m.match(node)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Build the selector.
     * 
     * @return
     */
    public NodeMatcher toMatcher() {
        return matchers.size() == 1 ? matchers.get(0) : andMatchers(matchers);
    }
}
