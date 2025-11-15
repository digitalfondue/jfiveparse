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

import java.util.function.Function;

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
public final class Selector extends BaseSelector<Node, Selector> {

    private Selector() {
        super((node) -> node);
    }

    public static Selector select() {
        return new Selector();
    }

    public static NodeMatcher parseSelector(String selector) {
        return select().parseSelectorInstance(selector)::test;
    }

    @Override
    Selector inst() {
        return this;
    }

    @Override
    Selector newInst() {
        return select();
    }


    public NodeMatcher toMatcher() {
        return super.internalToMatcher()::test;
    }
}
