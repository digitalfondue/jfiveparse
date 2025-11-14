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

    private static final BaseSelector<Node> NODE_BASE_SELECTOR = new BaseSelector<>();

    private final BaseSelector.BaseSelectorState<Node> state = new BaseSelector.BaseSelectorState<>(NODE_BASE_SELECTOR);

    public static Selector select() {
        return new Selector();
    }

    public static NodeMatcher parseSelector(String selector) {
        var res = CSS.parseSelector(selector).stream().map(NODE_BASE_SELECTOR::toBaseNodeMatcher).toList();
        return (node, base) -> NODE_BASE_SELECTOR.andMatchers(List.of(NODE_BASE_SELECTOR.IS_ELEMENT, res.size() == 1 ? res.get(0) : NODE_BASE_SELECTOR.orMatchers(res))).match(node, base);
    }

    public Selector element(String name) {
        state.element(name);
        return this;
    }


    public NodeMatcher toMatcher() {
        return (node, base) -> state.toMatcher().match(node, base);
    }

    public Selector element(String name, String namespace) {
        state.element(name, namespace);
        return this;
    }

    public Selector id(String id) {
        state.id(id);
        return this;
    }

    public Selector withChild() {
        state.withChild();
        return this;
    }

    public Selector withDescendant() {
        state.withDescendant();
        return this;
    }

    public Selector universal() {
        state.universal();
        return this;
    }

    public Selector attrValEq(String attr, String val) {
        state.attrValEq(attr, val);
        return this;
    }

    public Selector attr(String attr) {
        state.attr(attr);
        return this;
    }

    public Selector hasClass(String value) {
        state.hasClass(value);
        return this;
    }

    public Selector isFirstChild() {
        state.isFirstChild();
        return this;
    }

    public Selector hasClass(String value, String... others) {
        state.hasClass(value, others);
        return this;
    }

    public Selector isFirstElementChild() {
        state.isFirstElementChild();
        return this;
    }

    public Selector isLastElementChild() {
        state.isLastElementChild();
        return this;
    }

    public Selector isLastChild() {
        state.isLastChild();
        return this;
    }

    public Selector attrValInList(String name, String value) {
        state.attrValInList(name, value);
        return this;
    }

    public Selector attrValStartWith(String name, String value) {
        state.attrValStartWith(name, value);
        return this;
    }

    public Selector attrValEndWith(String name, String value) {
        state.attrValEndWith(name, value);
        return this;
    }

    public Selector attrValContains(String name, String value) {
        state.attrValContains(name, value);
        return this;
    }
}
