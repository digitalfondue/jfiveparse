/**
 * Copyright (C) 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
 * <td>#myId > span.className</td>
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
 * </table>
 *
 */
public class Selector {

    private List<NodeMatcher> matchers = new ArrayList<>();

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
        matchers.add(new NodeMatchers.ElementHasTagName(name));
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
        matchers.add(new NodeMatchers.ElementHasTagName(name, namespace));
        return this;
    }

    /**
     * Match an element with the class name. Case sensitive.
     * <p>
     * CSS equivalent: <code>.value</code>
     * </p>
     * 
     * @param name
     * @return
     */
    public Selector hasClass(String value) {
        matchers.add(new NodeMatchers.HasAttribute("class", value, NodeMatchers.ATTRIBUTE_MATCH_VALUE_IN_LIST));
        return this;
    }

    /**
     * Match an element with all the classes.
     * <p>
     * CSS equivalent: <code>.value.other1.other2</code>
     * </p>
     * 
     * @param name
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
     * @param name
     * @param others
     * @return
     */
    public Selector id(String value) {
        matchers.add(new NodeMatchers.HasAttribute("id", value, NodeMatchers.ATTRIBUTE_MATCH_VALUE_EQ));
        return this;

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
        matchers.add(new NodeMatchers.HasAttribute(name));
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
        matchers.add(new NodeMatchers.HasAttribute(name, value, NodeMatchers.ATTRIBUTE_MATCH_VALUE_EQ));
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
        matchers.add(new NodeMatchers.HasAttribute(name, value, NodeMatchers.ATTRIBUTE_MATCH_VALUE_IN_LIST));
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
        matchers.add(new NodeMatchers.HasAttribute(name, value, NodeMatchers.ATTRIBUTE_MATCH_VALUE_START_WITH));
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
        matchers.add(new NodeMatchers.HasAttribute(name, value, NodeMatchers.ATTRIBUTE_MATCH_VALUE_END_WITH));
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
        matchers.add(new NodeMatchers.HasAttribute(name, value, NodeMatchers.ATTRIBUTE_MATCH_VALUE_CONTAINS));
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
        matchers.add(new NodeMatchers.IsFirstChild());
        return this;
    }

    /**
     * Match only the element which is a first child.
     * 
     * @return
     */
    public Selector isFirstElementChild() {
        matchers.add(new NodeMatchers.IsFirstElementChild());
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
        matchers.add(new NodeMatchers.IsLastChild());
        return this;
    }

    /**
     * Match only the element which is a last child.
     * 
     * @return
     */
    public Selector isLastElementChild() {
        matchers.add(new NodeMatchers.IsLastElementChild());
        return this;
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
        NodeMatcher hasParentMatching = new NodeMatchers.HasParentMatching(new NodeMatchers.AndMatcher(new ArrayList<>(matchers)));
        matchers.clear();
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
        NodeMatcher hasAncestorMatching = new NodeMatchers.HasAncestorMatching(new NodeMatchers.AndMatcher(new ArrayList<>(matchers)));
        matchers.clear();
        matchers.add(hasAncestorMatching);
        return this;
    }

    /**
     * Build the selector.
     * 
     * @return
     */
    public NodeMatcher toMatcher() {
        return new NodeMatchers.AndMatcher(matchers);
    }
}
