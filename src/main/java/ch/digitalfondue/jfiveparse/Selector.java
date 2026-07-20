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
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

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
public final class Selector<T> {

    private List<BiPredicate<SelectableNode<T>, SelectableNode<T>>> matchers = new ArrayList<>();
    private final Function<T, SelectableNode<T>> wrapper;
    private final Function<SelectableNode<T>, T> unwrapper;
    private final Map<String, String> nameSpaceAlias;

    Selector(Function<T, SelectableNode<T>> wrapper, Function<SelectableNode<T>, T> unwrapper, Map<String, String> nameSpaceAlias) {
        this.wrapper = wrapper;
        this.unwrapper = unwrapper;
        this.nameSpaceAlias = nameSpaceAlias;
    }

    public static Selector<Node> select() {
        return new Selector<Node>(node -> node, node -> (Node) node, Map.of());
    }

    public static NodeMatcher<Node> parseSelector(String selector) {
        return parseSelector(selector, Map.of());
    }

    public static NodeMatcher<Node> parseSelector(String selector, Map<String, String> namespaceAlias) {
        return new NodeMatcher<>(new Selector<Node>(node -> node, node -> (Node) node, namespaceAlias).parseSelectorInstance(selector));
    }

    private Selector<T> newInst() {
        return new Selector<>(wrapper, unwrapper, nameSpaceAlias);
    }

    private void contains(String value) {
        matchers.add((node, base) -> {
            if (node instanceof SelectableNode.SelectableElement<?> e) {
                var textContent = e.getTextContent();
                return textContent != null && textContent.contains(value);
            }
            return false;
        });
    }

    public Selector<T> element(String name) {
        matchers.add((node, base) -> isElement(node) && name.equals(node.getNodeName()));
        return this;
    }

    public Selector<T> element(String name, String namespace) {
        String namespaceToMatch = nameSpaceAlias.getOrDefault(namespace, namespace);
        matchers.add((node, base) -> node instanceof SelectableNode.SelectableElement<?> ce && name.equals(ce.getNodeName())
                && Objects.equals(namespaceToMatch, ce.getNamespaceURI()));
        return this;
    }

    public Selector<T> hasClass(String value) {
        attrValInList("class", value);
        return this;
    }

    public Selector<T> hasClass(String value, String... others) {
        hasClass(value);
        if (others != null) {
            for (String n : others) {
                hasClass(n);
            }
        }
        return this;
    }

    public Selector<T> id(String value) {
        attrValEq("id", value);
        return this;
    }

    public Selector<T> attr(String name) {
        matchers.add(matchAttr(name, (attrValue, elem) -> true));
        return this;
    }

    public Selector<T> attrValEq(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> Objects.equals(attrValue, value)));
        return this;
    }

    public Selector<T> attrValInList(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> DOMTokenList.extractValues(elem, name).contains(value)));
        return this;
    }

    public Selector<T> attrValStartWith(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.startsWith(value)));
        return this;
    }

    public Selector<T> attrValEndWith(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.endsWith(value)));
        return this;
    }

    public Selector<T> attrValContains(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.contains(value)));
        return this;
    }

    public Selector<T> withDescendant() {
        var ancestorMatcher = collectMatchers();
        matchers.add(withDescendant(ancestorMatcher));
        return this;
    }

    public Selector<T> withChild() {
        var rules = collectMatchers();
        matchers.add((node, base) -> node.getParentNode() != null && rules.test(wrapper.apply(node.getParentNode()), base));
        return this;
    }

    public Selector<T> nextSibling() {
        var rules = collectMatchers();
        matchers.add((node, base) -> {
            var previousElementSibling = wrapper.apply(node.getPreviousElementSibling());
            return previousElementSibling != null && rules.test(previousElementSibling, base);
        });
        return this;
    }

    private BiPredicate<SelectableNode<T>, SelectableNode<T>> collectMatchers() {
        var matcherToHandle = matchers;
        matchers = new ArrayList<>();
        return andMatchers(matcherToHandle);
    }

    BiPredicate<SelectableNode<T>, SelectableNode<T>> parseSelectorInstance(String selector) {
        var res = CSS.parseSelector(selector).stream().map(l -> toBaseNodeMatcher(l, this::newInst)).toList();
        return andMatchers(List.of(Selector::isElement, res.size() == 1 ? res.get(0) : orMatchers(res)));
    }

    private BiPredicate<SelectableNode<T>, SelectableNode<T>> internalToMatcher() {
        return matchers.size() == 1 ? matchers.get(0) : andMatchers(matchers);
    }

    public NodeMatcher<T> toMatcher() {
        return new NodeMatcher<>(internalToMatcher());
    }

    public Selector<T> universal() {
        matchers.add(Selector::isElement);
        return this;
    }

    public Selector<T> isFirstChild() {
        matchers.add(this::isFirstChild);
        return this;
    }

    public Selector<T> isFirstElementChild() {
        matchers.add(this::isFirstElementChild);
        return this;
    }

    public Selector<T> isLastElementChild() {
        matchers.add(this::isLastElementChild);
        return this;
    }

    public Selector<T> isLastChild() {
        matchers.add(this::isLastChild);
        return this;
    }

    public Selector<T> subsequentSibling() {
        var rules = collectMatchers();
        matchers.add((node, base) -> {
            var previousElementSibling = wrapper.apply(node.getPreviousElementSibling());
            while (previousElementSibling != null) {
                if (rules.test(previousElementSibling, base)) {
                    return true;
                }
                previousElementSibling = wrapper.apply(previousElementSibling.getPreviousElementSibling());
            }
            return false;
        });
        return this;
    }

    BiPredicate<SelectableNode<T>, SelectableNode<T>> toBaseNodeMatcher(List<CSS.CssSelector> selector, Supplier<Selector<T>> stateSupplier) {
        var res = stateSupplier.get();
        for (var part : selector) {
            if (part instanceof CSS.Combinator c) {
                switch (c.type()) {
                    case CSS.CT_CHILD -> res.withChild();
                    case CSS.CT_SIBLING -> res.subsequentSibling();
                    case CSS.CT_ADJACENT -> res.nextSibling();
                    case CSS.CT_DESCENDANT -> res.withDescendant();
                    default -> throw new ParserException("Combinator " + c + " is not supported");
                }
            } else if (part instanceof CSS.TagSelector t) {
                if (t.namespace() == null || "*".equals(t.namespace())) {
                    res.element(t.name());
                } else {
                    res.element(t.name(), t.namespace());
                }
            } else if (part instanceof CSS.AttributeSelector a) {
                var action = a.action();
                var name = a.name();
                var value = a.value();
                if (action == CSS.ATTR_ACTION_EQUALS) {
                    res.attrValEq(name, value);
                } else if (action == CSS.ATTR_ACTION_ELEMENT && "class".equals(name)) {
                    res.hasClass(value);
                } else if (action == CSS.ATTR_ACTION_EXISTS) {
                    res.attr(name);
                } else if (action == CSS.ATTR_ACTION_START) {
                    res.attrValStartWith(name, value);
                } else if (action == CSS.ATTR_ACTION_ANY) {
                    res.attrValContains(name, value);
                } else if (action == CSS.ATTR_ACTION_END) {
                    res.attrValEndWith(name, value);
                } else {
                    throw new ParserException("AttributeSelector " + a + " is not supported");
                }
            } else if (part instanceof CSS.InternalSelector is && "base".equals(is.name())) {
                res.matchers.add((node, base) -> base.isSameNode(unwrapper.apply(node)));
            } else if (part instanceof CSS.PseudoSelector ps) {
                String name = ps.name();
                if ("contains".equals(name) && ps.data() instanceof CSS.DataString ds) {
                    res.contains(ds.value());
                } else if ("first-child".equals(name)) {
                    res.matchers.add(this::isFirstElementChild);
                } else if ("last-child".equals(name)) {
                    res.matchers.add(this::isLastElementChild);
                } else if ("empty".equals(name)) {
                    res.matchers.add(this::isEmpty);
                } else if ("only-child".equals(name)) {
                    res.matchers.add(andMatchers(List.of(this::isFirstElementChild, this::isLastElementChild)));
                } else if ("first-of-type".equals(name)) {
                    res.matchers.add(this::isFirstOfType);
                } else if ("last-of-type".equals(name)) {
                    res.matchers.add(this::isLastOfType);
                } else if ("only-of-type".equals(name)) {
                    res.matchers.add(andMatchers(List.of(this::isFirstOfType, this::isLastOfType)));
                } else if ("root".equals(name)) {
                    res.matchers.add(this::isRoot);
                } else if (("is".equals(name) || "where".equals(name) || "not".equals(name)) && ps.data() instanceof CSS.DataSelectors ds) {
                    var isMatchers = orMatchers(ds.value().stream().map((l) -> toBaseNodeMatcher(l, stateSupplier)).toList());
                    var baseRule = res.collectMatchers();
                    var mustMatch = !"not".equals(name);
                    res.matchers.add((node, base) -> baseRule.test(node, base) && isMatchers.test(node, base) == mustMatch);
                } else if ("has".equals(name) && ps.data() instanceof CSS.DataSelectors ds) {
                    // see https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_selectors/Selector_structure#relative_selector
                    var hasMatchers = orMatchers(ds.value().stream().map(s -> {
                        var r = new ArrayList<CSS.CssSelector>(s.size() + 2);
                        r.add(new CSS.InternalSelector("base"));
                        int comb = !s.isEmpty() && s.get(0) instanceof CSS.Combinator combinator ? combinator.type() : -1;
                        if (comb == -1) {
                            comb = CSS.CT_DESCENDANT;
                            r.add(new CSS.Combinator(comb));
                        }
                        r.addAll(s);
                        var nm = toBaseNodeMatcher(r, stateSupplier);
                        return switch (comb) {
                            case CSS.CT_CHILD, CSS.CT_DESCENDANT -> (BiPredicate<SelectableNode<T>, SelectableNode<T>>) (node, base) -> node.getAllNodesMatchingAsStream(new NodeMatcher<>(nm), true, unwrapper.apply(base)).count() == 1;
                            case CSS.CT_SIBLING, CSS.CT_ADJACENT -> (BiPredicate<SelectableNode<T>, SelectableNode<T>>) (node, base) -> wrapper.apply(node.getParentNode()).getAllNodesMatchingAsStream(new NodeMatcher<>(nm), true, unwrapper.apply(base)).count() == 1;
                            default -> throw new ParserException("Combinator " + comb + " is not supported in :has");
                        };
                    }).toList());
                    var baseRule = res.collectMatchers();
                    res.matchers.add((node, base) -> baseRule.test(node, base) && hasMatchers.test(node, node));
                } else if (("nth-child".equals(name) || "nth-last-child".equals(name)) && ps.data() instanceof CSS.DataString s) {
                    var idxPredicate = CSS.parseNth(s.value());
                    var reversedOrder = "nth-last-child".equals(name);
                    res.matchers.add((node, base) -> isAt(node, idxPredicate, reversedOrder));
                } else if (("nth-of-type".equals(name) || "nth-last-of-type".equals(name)) && ps.data() instanceof CSS.DataString s) {
                    var idxPredicate = CSS.parseNth(s.value());
                    var reversedOrder = "nth-last-of-type".equals(name);
                    res.matchers.add((node, base) -> isAtNthOfType(node, idxPredicate, reversedOrder));
                } else {
                    throw new ParserException("PseudoSelector '" + name + "' is not supported");
                }
            } else if (part instanceof CSS.UniversalSelector) {
                res.matchers.add(Selector::isElement);
            } else {
                throw new ParserException(part + " is not supported");
            }
        }
        return andMatchers(List.of(Selector::isElement, res.internalToMatcher()));
    }

    private boolean isFirstChild(SelectableNode<T> node, SelectableNode<T> base) {
        return node.getParentNode() != null && node.isSameNode(wrapper.apply(node.getParentNode()).getFirstChild());
    }

    private boolean isFirstElementChild(SelectableNode<T> node, SelectableNode<T> base) {
        return node.getParentNode() != null && node.isSameNode(wrapper.apply(node.getParentNode()).getFirstElementChild());
    }

    private boolean isLastChild(SelectableNode<T> node, SelectableNode<T> base) {
        return node.getParentNode() != null && node.isSameNode(wrapper.apply(node.getParentNode()).getLastChild());
    }

    private boolean isLastElementChild(SelectableNode<T> node, SelectableNode<T> base) {
        return node.getParentNode() != null && node.isSameNode(wrapper.apply(node.getParentNode()).getLastElementChild());
    }

    private static <T> boolean isElement(SelectableNode<T> node) {
        return node.getNodeType() == Node.ELEMENT_NODE;
    }

    private static <T> boolean isElement(SelectableNode<T> node, SelectableNode<T> base) {
        return isElement(node);
    }

    private boolean isRoot(SelectableNode<T> n, SelectableNode<T> base) {
        return (n.getParentNode() == null || wrapper.apply(n.getParentNode()).getNodeType() == Node.DOCUMENT_NODE) && isElement(n);
    }

    private boolean isAt(SelectableNode<T> n, IntPredicate idxPredicate, boolean reversedOrder) {
        if (n.getParentNode() != null) {
            List<T> c = wrapper.apply(n.getParentNode()).getChildNodes();
            var childNodes = c;
            if (reversedOrder) {
                int size = c.size();
                childNodes = new Common.NodeList<>(size, (idx) -> c.get(size - idx - 1));
            }
            var elemIdx = 0;
            for (T child : childNodes) {
                if (isElement(wrapper.apply(child))) {
                    elemIdx++;
                    if (n.isSameNode(child) && idxPredicate.test(elemIdx)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isAtNthOfType(SelectableNode<T> node, IntPredicate idxPredicate, boolean reversedOrder) {
        if (node.getParentNode() != null) {
            var nodeName = node.getNodeName();
            var c = wrapper.apply(node.getParentNode()).getChildNodes();
            var childNodes = c;
            if (reversedOrder) {
                int size = c.size();
                childNodes = new Common.NodeList<>(size, (idx) -> c.get(size - idx - 1));
            }
            int elemIdx = 0;
            for (T child : childNodes) {
                if (node.isSameNode(child)) {
                    elemIdx++;
                    break;
                }
                var wrappedChild = wrapper.apply(child);
                if (isElement(wrappedChild) && nodeName.equals(wrappedChild.getNodeName())) {
                    elemIdx++;
                }
            }
            return idxPredicate.test(elemIdx);
        }
        return false;
    }

    private boolean isLastOfType(SelectableNode<T> node, SelectableNode<T> base) {
        if (node.getParentNode() != null) {
            var nodeName = node.getNodeName();
            var childNodes = wrapper.apply(node.getParentNode()).getChildNodes();
            for (int i = childNodes.size() - 1; i >= 0; i--) {
                var e = childNodes.get(i);
                var we = wrapper.apply(e);
                if (isElement(we) && we.getNodeName().equals(nodeName)) {
                    return node.isSameNode(e);
                }
            }
        }
        return false;
    }

    private boolean isFirstOfType(SelectableNode<T> node, SelectableNode<T> base) {
        if (node.getParentNode() != null) {
            var nodeName = node.getNodeName();
            var childNodes = wrapper.apply(node.getParentNode()).getChildNodes();
            for (var e : childNodes) {
                var we = wrapper.apply(e);
                if (isElement(we) && we.getNodeName().equals(nodeName)) {
                    return node.isSameNode(e);
                }
            }
        }
        return false;
    }

    private boolean isEmpty(SelectableNode<T> node, SelectableNode<T> base) {
        return node.getChildNodes().stream().noneMatch(s -> {
            var ws = wrapper.apply(s);
            return isElement(ws) || ws.getNodeType() == Node.TEXT_NODE;
        });
    }

    private BiPredicate<SelectableNode<T>, SelectableNode<T>> withDescendant(BiPredicate<SelectableNode<T>, SelectableNode<T>> ancestorMatcher) {
        return (node, base) -> {
            while (node.getParentNode() != null) {
                node = wrapper.apply(node.getParentNode());
                if (ancestorMatcher.test(node, base)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static <T> BiPredicate<SelectableNode<T>, SelectableNode<T>> orMatchers(List<BiPredicate<SelectableNode<T>, SelectableNode<T>>> BaseNodeMatchers) {
        if (BaseNodeMatchers.size() == 1) {
            return BaseNodeMatchers.get(0);
        }
        if (BaseNodeMatchers.size() == 2) {
            var v1 = BaseNodeMatchers.get(0);
            var v2 = BaseNodeMatchers.get(1);
            return (node, base) -> v1.test(node, base) || v2.test(node, base);
        }

        return (node, base) -> {
            for (var m : BaseNodeMatchers) {
                if (m.test(node, base)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static <T> BiPredicate<SelectableNode<T>, SelectableNode<T>> andMatchers(List<BiPredicate<SelectableNode<T>, SelectableNode<T>>> BaseNodeMatchers) {
        if (BaseNodeMatchers.size() == 1) {
            return BaseNodeMatchers.get(0);
        }
        if (BaseNodeMatchers.size() == 2) {
            var v1 = BaseNodeMatchers.get(0);
            var v2 = BaseNodeMatchers.get(1);
            return (node, base) -> v1.test(node, base) && v2.test(node, base);
        }

        return (node, base) -> {
            for (var m : BaseNodeMatchers) {
                if (!m.test(node, base)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static <T> BiPredicate<SelectableNode<T>, SelectableNode<T>> matchAttr(String name, BiPredicate<String, SelectableNode.SelectableElement<?>> attributeValueMatcher) {
        return (node, base) -> {
            if (node instanceof SelectableNode.SelectableElement<?> elem) {
                var isHtml = Node.NAMESPACE_HTML.equals(elem.getNamespaceURI());
                var toCompareAttr = isHtml ? Common.convertToAsciiLowerCase(name) : name;
                return elem.containsAttribute(toCompareAttr) && attributeValueMatcher.test(elem.getAttributeValue(toCompareAttr), elem);
            }
            return false;
        };
    }
}
