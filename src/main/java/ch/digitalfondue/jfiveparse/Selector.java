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
 * <code>Selector.select().element("span").isFirstChild().toMatcher()</code>
 * </td>
 * </tr>
 * <tr>
 * <td>span:last-child</td>
 * <td>
 * <code>Selector.select().element("span").isLastChild().toMatcher()</code>
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
     * Universal selector "*". It matches any node.
     *
     * @return
     */
    public Selector universal() {
        matchers.add(n -> true);
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
        matchers.add(node -> node.getNodeType() == Node.ELEMENT_NODE && name.equals(node.getNodeName())
                && Objects.equals(namespace, ((CommonNode.CommonElement) node).getNamespaceURI()));
        return this;
    }

    /**
     * Match an element with the class name. Case sensitive.
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
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                return false;
            }
            CommonNode.CommonElement elem = (CommonNode.CommonElement) node;
            return elem.containsAttribute(name) && attributeValueMatcher.test(elem.getAttributeValue(name), elem);
        };
    }

    /**
     * Match only the element with an attribute named "name". Case sensitive.
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
     * <p>
     * CSS equivalent: <code>:first-child</code>
     * </p>
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
     * @return
     */
    public Selector isFirstElementChild() {
        matchers.add(node -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getFirstElementChild()));
        return this;
    }

    /**
     * Match only the node which is a last child.
     * 
     * <p>
     * CSS equivalent: <code>:last-child</code>
     * </p>
     * 
     * @return
     */
    public Selector isLastChild() {
        matchers.add(node -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getLastChild()));
        return this;
    }

    /**
     * Match only the element which is a last child.
     * 
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

    /**
     * Build the selector.
     * 
     * @return
     */
    public NodeMatcher toMatcher() {
        return matchers.size() == 1 ? matchers.get(0) : andMatchers(matchers);
    }
}
