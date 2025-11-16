package ch.digitalfondue.jfiveparse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

abstract class BaseSelector<T, R extends BaseSelector<T, R>> {


    private List<BiPredicate<SelectableNode<T>, SelectableNode<T>>> matchers = new ArrayList<>();
    private final Function<T, SelectableNode<T>> wrapper;
    private final Function<SelectableNode<T>, T> unwrapper;

    BaseSelector(Function<T, SelectableNode<T>> wrapper, Function<SelectableNode<T>, T> unwrapper) {
        this.wrapper = wrapper;
        this.unwrapper = unwrapper;
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

    public R element(String name) {
        matchers.add((node, base) -> isElement(node) && name.equals(node.getNodeName()));
        return inst();
    }

    public R element(String name, String namespace) {
        matchers.add((node, base) -> node instanceof SelectableNode.SelectableElement<?> ce && name.equals(ce.getNodeName())
                && Objects.equals(namespace, ce.getNamespaceURI()));
        return inst();
    }

    public R hasClass(String value) {
        attrValInList("class", value);
        return inst();
    }

    public R hasClass(String value, String... others) {
        hasClass(value);
        if (others != null) {
            for (String n : others) {
                hasClass(n);
            }
        }
        return inst();
    }

    public R id(String value) {
        attrValEq("id", value);
        return inst();
    }

    public R attr(String name) {
        matchers.add(matchAttr(name, (attrValue, elem) -> true));
        return inst();
    }

    public R attrValEq(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> Objects.equals(attrValue, value)));
        return inst();
    }

    public R attrValInList(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> DOMTokenList.extractValues(elem, name).contains(value)));
        return inst();
    }

    public R attrValStartWith(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.startsWith(value)));
        return inst();
    }

    public R attrValEndWith(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.endsWith(value)));
        return inst();
    }

    public R attrValContains(String name, String value) {
        matchers.add(matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.contains(value)));
        return inst();
    }

    public R withDescendant() {
        var ancestorMatcher = collectMatchers();
        matchers.add(withDescendant(ancestorMatcher));
        return inst();
    }

    public R withChild() {
        var rules = collectMatchers();
        matchers.add((node, base) -> node.getParentNode() != null && rules.test(wrapper.apply(node.getParentNode()), base));
        return inst();
    }

    private void nextSibling() {
        var rules = collectMatchers();
        matchers.add((node, base) -> {
            var previousElementSibling = wrapper.apply(node.getPreviousElementSibling());
            return previousElementSibling != null && rules.test(previousElementSibling, base);
        });
    }

    private BiPredicate<SelectableNode<T>, SelectableNode<T>> collectMatchers() {
        var matcherToHandle = matchers;
        matchers = new ArrayList<>();
        return andMatchers(matcherToHandle);
    }

    BiPredicate<SelectableNode<T>, SelectableNode<T>> parseSelectorInstance(String selector) {
        var res = CSS.parseSelector(selector).stream().map(l -> toBaseNodeMatcher(l, this::newInst)).toList();
        return andMatchers(List.of(BaseSelector::isElement, res.size() == 1 ? res.get(0) : orMatchers(res)));
    }

    private BiPredicate<SelectableNode<T>, SelectableNode<T>> internalToMatcher() {
        return matchers.size() == 1 ? matchers.get(0) : andMatchers(matchers);
    }

    public NodeMatcher<T> toMatcher() {
        return new NodeMatcher<>(internalToMatcher());
    }

    protected abstract R inst();

    protected abstract R newInst();

    public R universal() {
        matchers.add(BaseSelector::isElement);
        return inst();
    }

    public R isFirstChild() {
        matchers.add(this::isFirstChild);
        return inst();
    }

    public R isFirstElementChild() {
        matchers.add(this::isFirstElementChild);
        return inst();
    }

    public R isLastElementChild() {
        matchers.add(this::isLastElementChild);
        return inst();
    }

    public R isLastChild() {
        matchers.add(this::isLastChild);
        return inst();
    }

    private void subsequentSibling() {
        var rules = collectMatchers();
        matchers.add((node, base) -> {
            var previousElementSibling = wrapper.apply(node.getPreviousElementSibling());
            while(previousElementSibling != null) {
                if (rules.test(previousElementSibling, base)) {
                    return true;
                }
                previousElementSibling = wrapper.apply(previousElementSibling.getPreviousElementSibling());
            }
            return false;
        });
    }

    BiPredicate<SelectableNode<T>, SelectableNode<T>> toBaseNodeMatcher(List<CSS.CssSelector> selector, Supplier<BaseSelector<T, R>> stateSupplier) {
        var res = stateSupplier.get();
        for (var part : selector) {
            if (part instanceof CSS.Combinator c) {
                switch (c.type()) {
                    case DESCENDANT -> res.withDescendant();
                    case CHILD -> res.withChild();
                    case ADJACENT -> res.nextSibling();
                    case SIBLING -> res.subsequentSibling();
                    case PARENT, COLUMN_COMBINATOR -> throw new IllegalArgumentException("Combinator " + c + " is not supported");
                };
            } else if (part instanceof CSS.TagSelector t) {
                if (t.namespace() == null) {
                    res.element(t.name());
                } else {
                    res.element(t.name(), t.namespace());
                }
            } else if (part instanceof CSS.AttributeSelector a) {
                var action = a.action();
                var name = a.name();
                var value = a.value();
                if (action == CSS.AttributeAction.EQUALS) {
                    res.attrValEq(name, value);
                } else if (action == CSS.AttributeAction.ELEMENT && "class".equals(name)) {
                    res.hasClass(value);
                } else if (action == CSS.AttributeAction.EXISTS) {
                    res.attr(name);
                } else if (action == CSS.AttributeAction.START) {
                    res.attrValStartWith(name, value);
                } else if (action == CSS.AttributeAction.ANY) {
                    res.attrValContains(name, value);
                } else if (action == CSS.AttributeAction.END) {
                    res.attrValEndWith(name, value);
                } else {
                    throw new IllegalArgumentException("AttributeSelector " + a + " is not supported");
                }
            } else if (part instanceof CSS.PseudoElement pe) {
                throw new IllegalStateException("to implement");
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
                    res.matchers.add(this::isOnlyChild);
                } else if ("first-of-type".equals(name)) {
                    res.matchers.add(this::isFirstOfType);
                } else if ("last-of-type".equals(name)) {
                    res.matchers.add(this::isLastOfType);
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
                        var comb = !s.isEmpty() && s.get(0) instanceof CSS.Combinator combinator ? combinator.type() : null;
                        if (comb == null) {
                            comb = CSS.CombinatorType.DESCENDANT;
                            r.add(new CSS.Combinator(comb));
                        }
                        r.addAll(s);
                        var nm = toBaseNodeMatcher(r, stateSupplier);
                        return switch (comb) {
                            case CHILD, DESCENDANT -> (BiPredicate<SelectableNode<T>, SelectableNode<T>>) (node, base) -> node.getAllNodesMatchingAsStream(new NodeMatcher<>(nm), true, unwrapper.apply(base)).count() == 1;
                            case SIBLING, ADJACENT -> (BiPredicate<SelectableNode<T>, SelectableNode<T>>) (node, base) -> wrapper.apply(node.getParentNode()).getAllNodesMatchingAsStream(new NodeMatcher<>(nm), true, unwrapper.apply(base)).count() == 1;
                            default -> throw new IllegalArgumentException("Combinator " + comb + " is not supported in :has");
                        };
                    }).toList());
                    var baseRule = res.collectMatchers();
                    res.matchers.add((node, base) -> baseRule.test(node, base) && hasMatchers.test(node, node));
                } else {
                    throw new IllegalArgumentException("PseudoSelector '" + name + "' is not supported");
                }
            } else if (part instanceof CSS.UniversalSelector) {
                res.matchers.add(BaseSelector::isElement);
            } else {
                throw new IllegalArgumentException(part + " is not supported");
            }
        }
        return andMatchers(List.of(BaseSelector::isElement, res.internalToMatcher()));
    }

    //
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
        return (n.getParentNode() == null || wrapper.apply(n.getParentNode()).getNodeType() == Node.DOCUMENT_NODE) && n.getNodeType() == Node.ELEMENT_NODE;
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

    private boolean isOnlyChild(SelectableNode<T> node, SelectableNode<T> base) {
        if (node.getParentNode() == null) {
            return true;
        }
        var found = wrapper.apply(node.getParentNode()).getChildNodes()
                        .stream()
                        .map(wrapper)
                        .filter(BaseSelector::isElement).toList();
        return found.size() == 1 && found.get(0).isSameNode(unwrapper.apply(node));
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
