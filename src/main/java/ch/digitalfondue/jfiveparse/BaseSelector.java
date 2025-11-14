package ch.digitalfondue.jfiveparse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

class BaseSelector<T extends SelectableNode<T>> {

    static class BaseSelectorState<T extends SelectableNode<T>> {
        private List<BaseNodeMatcher<T>> matchers = new ArrayList<>();
        private final BaseSelector<T> baseSelector;

        BaseSelectorState(BaseSelector<T> baseSelector) {
            this.baseSelector = baseSelector;
        }

        BaseNodeMatcher<T> toMatcher() {
            return matchers.size() == 1 ? matchers.get(0) : baseSelector.andMatchers(matchers);
        }

        void contains(String value) {
            matchers.add((node, base) -> {
                if (node instanceof SelectableNode.SelectableElement<?> e) {
                    var textContent = e.getTextContent();
                    return textContent != null && textContent.contains(value);
                }
                return false;
            });
        }

        void universal() {
            matchers.add(baseSelector.IS_ELEMENT);
        }

        void element(String name) {
            matchers.add((node, base) -> baseSelector.IS_ELEMENT.match(node, base) && name.equals(node.getNodeName()));
        }

        void element(String name, String namespace) {
            matchers.add((node, base) -> node instanceof SelectableNode.SelectableElement<?> ce && name.equals(ce.getNodeName())
                    && Objects.equals(namespace, ce.getNamespaceURI()));
        }

        void hasClass(String value) {
            attrValInList("class", value);
        }

        void isFirstChild() {
            matchers.add((node, base) -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getFirstChild()));
        }


        void isFirstElementChild() {
            matchers.add((node, base) -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getFirstElementChild()));
        }

        void isLastChild() {
            matchers.add((node, base) -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getLastChild()));
        }

        void hasClass(String value, String... others) {
            hasClass(value);
            if (others != null) {
                for (String n : others) {
                    hasClass(n);
                }
            }
        }

        void id(String value) {
            attrValEq("id", value);
        }

        void isLastElementChild() {
            matchers.add((node, base) -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getLastElementChild()));
        }

        void attr(String name) {
            matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> true));
        }

        void attrValEq(String name, String value) {
            matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> Objects.equals(attrValue, value)));
        }

        void attrValInList(String name, String value) {
            matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> DOMTokenList.extractValues(elem, name).contains(value)));
        }

        void attrValStartWith(String name, String value) {
            matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.startsWith(value)));
        }

        void attrValEndWith(String name, String value) {
            matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.endsWith(value)));
        }

        void attrValContains(String name, String value) {
            matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.contains(value)));
        }

        void withDescendant() {
            var ancestorMatcher = collectMatchers();
            matchers.add(baseSelector.withDescendant(ancestorMatcher));
        }

        void withChild() {
            var rules = collectMatchers();
            matchers.add((node, base) -> node.getParentNode() != null && rules.match(node.getParentNode(), base));
        }

        void nextSibling() {
            var rules = collectMatchers();
            matchers.add((node, base) -> {
                var previousElementSibling = node.getPreviousElementSibling();
                return previousElementSibling != null && rules.match(previousElementSibling, base);
            });
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
        private void subsequentSibling() {
            var rules = collectMatchers();
            matchers.add((node, base) -> {
                var previousElementSibling = node.getPreviousElementSibling();
                while(previousElementSibling != null) {
                    if (rules.match(previousElementSibling, base)) {
                        return true;
                    }
                    previousElementSibling = previousElementSibling.getPreviousElementSibling();
                }
                return false;
            });
        }

        BaseNodeMatcher<T> collectMatchers() {
            var matcherToHandle = matchers;
            matchers = new ArrayList<>();
            return baseSelector.andMatchers(matcherToHandle);
        }
    }

    final BaseNodeMatcher<T> IS_ELEMENT = (node, base) -> node.getNodeType() == Node.ELEMENT_NODE;

    private final BaseNodeMatcher<T> ROOT = (n, base) -> (n.getParentNode() == null || n.getParentNode().getNodeType() == Node.DOCUMENT_NODE) && n.getNodeType() == Node.ELEMENT_NODE;

    private final BaseNodeMatcher<T> LAST_OF_TYPE = (node, base) -> {
        if (node.getParentNode() != null) {
            var nodeName = node.getNodeName();
            var childNodes = node.getParentNode().getChildNodes();
            for (int i = childNodes.size() - 1; i >= 0; i--) {
                var e = childNodes.get(i);
                if (IS_ELEMENT.match(e, base) && e.getNodeName().equals(nodeName)) {
                    return node.isSameNode(e);
                }
            }
        }
        return false;
    };

    private final BaseNodeMatcher<T> FIRST_OF_TYPE = (node, base) -> {
        if (node.getParentNode() != null) {
            var nodeName = node.getNodeName();
            var childNodes = node.getParentNode().getChildNodes();
            for (var e : childNodes) {
                if (IS_ELEMENT.match(e, base) && e.getNodeName().equals(nodeName)) {
                    return node.isSameNode(e);
                }
            }
        }
        return false;
    };

    private final BaseNodeMatcher<T> ONLY_CHILD = (node, base) -> node.getParentNode() == null ||
            (node.getParentNode().getChildNodes()
                    .stream()
                    .filter(f -> IS_ELEMENT.match(f, base))
                    .allMatch(n -> n.isSameNode(node)));

    private final BaseNodeMatcher<T> IS_EMPTY = (node, base) -> node.getChildNodes().stream().noneMatch(s -> IS_ELEMENT.match(s, base) || s.getNodeType() == Node.TEXT_NODE);


    BaseNodeMatcher<T> orMatchers(List<BaseNodeMatcher<T>> BaseNodeMatchers) {
        if (BaseNodeMatchers.size() == 1) {
            return BaseNodeMatchers.get(0);
        }
        if (BaseNodeMatchers.size() == 2) {
            var v1 = BaseNodeMatchers.get(0);
            var v2 = BaseNodeMatchers.get(1);
            return (node, base) -> v1.match(node, base) || v2.match(node, base);
        }

        return (node, base) -> {
            for (var m : BaseNodeMatchers) {
                if (m.match(node, base)) {
                    return true;
                }
            }
            return false;
        };
    }

    BaseNodeMatcher<T> toBaseNodeMatcher(List<CSS.CssSelector> selector) {
        var res = new BaseSelectorState<>(this);
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
                res.matchers.add((node, base) -> base.isSameNode(node));
            } else if (part instanceof CSS.PseudoSelector ps) {
                String name = ps.name();
                if ("contains".equals(name) && ps.data() instanceof CSS.DataString ds) {
                    res.contains(ds.value());
                } else if ("first-child".equals(name)) {
                    res.isFirstElementChild();
                } else if ("last-child".equals(name)) {
                    res.isLastElementChild();
                } else if ("empty".equals(name)) {
                    res.matchers.add(IS_EMPTY);
                } else if ("only-child".equals(name)) {
                    res.matchers.add(ONLY_CHILD);
                } else if ("first-of-type".equals(name)) {
                    res.matchers.add(FIRST_OF_TYPE);
                } else if ("last-of-type".equals(name)) {
                    res.matchers.add(LAST_OF_TYPE);
                } else if ("root".equals(name)) {
                    res.matchers.add(ROOT);
                } else if (("is".equals(name) || "not".equals(name)) && ps.data() instanceof CSS.DataSelectors ds) {
                    var isMatchers = orMatchers(ds.value().stream().map(this::toBaseNodeMatcher).toList());
                    var baseRule = res.collectMatchers();
                    var mustMatch = "is".equals(name);
                    res.matchers.add((node, base) -> baseRule.match(node, base) && isMatchers.match(node, base) == mustMatch);
                } else if ("has".equals(name) && ps.data() instanceof CSS.DataSelectors ds) {
                    // see https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_selectors/Selector_structure#relative_selector
                    BaseNodeMatcher<T> hasMatchers = orMatchers(ds.value().stream().map(s -> {
                        var r = new ArrayList<CSS.CssSelector>(s.size() + 2);
                        r.add(new CSS.InternalSelector("base"));
                        var comb = !s.isEmpty() && s.get(0) instanceof CSS.Combinator combinator ? combinator.type() : null;
                        if (comb == null) {
                            comb = CSS.CombinatorType.DESCENDANT;
                            r.add(new CSS.Combinator(comb));
                        }
                        r.addAll(s);
                        var nm = toBaseNodeMatcher(r);
                        return switch (comb) {
                            case CHILD, DESCENDANT -> (BaseNodeMatcher<T>) (node, base) -> node.getAllNodesMatchingAsStream(nm, true, base).count() == 1;
                            case SIBLING, ADJACENT -> (BaseNodeMatcher<T>) (node, base) -> node.getParentNode().getAllNodesMatchingAsStream(nm, true, base).count() == 1;
                            default -> throw new IllegalArgumentException("Combinator " + comb + " is not supported in :has");
                        };
                    }).toList());
                    var baseRule = res.collectMatchers();
                    res.matchers.add((node, base) -> baseRule.match(node, base) && hasMatchers.match(node, node));
                } else {
                    throw new IllegalArgumentException("PseudoSelector '" + name + "' is not supported");
                }
            } else if (part instanceof CSS.UniversalSelector) {
                res.universal();
            } else {
                throw new IllegalArgumentException(part + " is not supported");
            }
        }
        return andMatchers(List.of(IS_ELEMENT, res.toMatcher()));
    }

    BaseNodeMatcher<T> andMatchers(List<BaseNodeMatcher<T>> BaseNodeMatchers) {
        if (BaseNodeMatchers.size() == 1) {
            return BaseNodeMatchers.get(0);
        }
        if (BaseNodeMatchers.size() == 2) {
            var v1 = BaseNodeMatchers.get(0);
            var v2 = BaseNodeMatchers.get(1);
            return (node, base) -> v1.match(node, base) && v2.match(node, base);
        }

        return (node, base) -> {
            for (var m : BaseNodeMatchers) {
                if (!m.match(node, base)) {
                    return false;
                }
            }
            return true;
        };
    }

    private BaseNodeMatcher<T> withDescendant(BaseNodeMatcher<T> ancestorMatcher) {
        return (node, base) -> {
            while (node.getParentNode() != null) {
                node = node.getParentNode();
                if (ancestorMatcher.match(node, base)) {
                    return true;
                }
            }
            return false;
        };
    }

    private BaseNodeMatcher<T> matchAttr(String name, BiPredicate<String, SelectableNode.SelectableElement<?>> attributeValueMatcher) {
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
