package ch.digitalfondue.jfiveparse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

abstract class BaseSelectorState<T extends SelectableNode<T>, R extends BaseSelectorState<T, R>> {

    private final BaseSelector<T> baseSelector;
    private List<BiPredicate<T, T>> matchers = new ArrayList<>();

    BaseSelectorState(BaseSelector<T> baseSelector) {
        this.baseSelector = baseSelector;
    }

    R contains(String value) {
        matchers.add((node, base) -> {
            if (node instanceof SelectableNode.SelectableElement<?> e) {
                var textContent = e.getTextContent();
                return textContent != null && textContent.contains(value);
            }
            return false;
        });
        return inst();
    }

    public R element(String name) {
        matchers.add((node, base) -> baseSelector.IS_ELEMENT.test(node, base) && name.equals(node.getNodeName()));
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
        matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> true));
        return inst();
    }

    public R attrValEq(String name, String value) {
        matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> Objects.equals(attrValue, value)));
        return inst();
    }

    public R attrValInList(String name, String value) {
        matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> DOMTokenList.extractValues(elem, name).contains(value)));
        return inst();
    }

    public R attrValStartWith(String name, String value) {
        matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.startsWith(value)));
        return inst();
    }

    public R attrValEndWith(String name, String value) {
        matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.endsWith(value)));
        return inst();
    }

    public R attrValContains(String name, String value) {
        matchers.add(baseSelector.matchAttr(name, (attrValue, elem) -> attrValue != null && value != null && attrValue.contains(value)));
        return inst();
    }

    public R withDescendant() {
        var ancestorMatcher = collectMatchers();
        matchers.add(baseSelector.withDescendant(ancestorMatcher));
        return inst();
    }

    public R withChild() {
        var rules = collectMatchers();
        matchers.add((node, base) -> node.getParentNode() != null && rules.test(node.getParentNode(), base));
        return inst();
    }

    R nextSibling() {
        var rules = collectMatchers();
        matchers.add((node, base) -> {
            var previousElementSibling = node.getPreviousElementSibling();
            return previousElementSibling != null && rules.test(previousElementSibling, base);
        });
        return inst();
    }

    BiPredicate<T, T> collectMatchers() {
        var matcherToHandle = matchers;
        matchers = new ArrayList<>();
        return baseSelector.andMatchers(matcherToHandle);
    }

    BiPredicate<T, T> parseSelectorInstance(String selector) {
        var res = CSS.parseSelector(selector).stream().map(l -> toBaseNodeMatcher(l, this::newInst)).toList();
        return baseSelector.andMatchers(List.of(baseSelector.IS_ELEMENT, res.size() == 1 ? res.get(0) : baseSelector.orMatchers(res)));
    }

    BiPredicate<T, T> internalToMatcher() {
        return matchers.size() == 1 ? matchers.get(0) : baseSelector.andMatchers(matchers);
    }

    abstract R inst();

    abstract R newInst();

    public R universal() {
        matchers.add(baseSelector.IS_ELEMENT);
        return inst();
    }

    public R isFirstChild() {
        matchers.add(baseSelector.IS_FIRST_CHILD);
        return inst();
    }

    public R isFirstElementChild() {
        matchers.add(baseSelector.IS_FIRST_ELEMENT_CHILD);
        return inst();
    }

    public R isLastElementChild() {
        matchers.add(baseSelector.IS_LAST_ELEMENT_CHILD);
        return inst();
    }

    public R isLastChild() {
        matchers.add(baseSelector.IS_LAST_CHILD);
        return inst();
    }

    private void subsequentSibling() {
        var rules = collectMatchers();
        matchers.add((node, base) -> {
            var previousElementSibling = node.getPreviousElementSibling();
            while(previousElementSibling != null) {
                if (rules.test(previousElementSibling, base)) {
                    return true;
                }
                previousElementSibling = previousElementSibling.getPreviousElementSibling();
            }
            return false;
        });
    }

    BiPredicate<T, T> toBaseNodeMatcher(List<CSS.CssSelector> selector, Supplier<BaseSelectorState<T, R>> stateSupplier) {
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
                res.matchers.add((node, base) -> base.isSameNode(node));
            } else if (part instanceof CSS.PseudoSelector ps) {
                String name = ps.name();
                if ("contains".equals(name) && ps.data() instanceof CSS.DataString ds) {
                    res.contains(ds.value());
                } else if ("first-child".equals(name)) {
                    res.matchers.add(baseSelector.IS_FIRST_ELEMENT_CHILD);
                } else if ("last-child".equals(name)) {
                    res.matchers.add(baseSelector.IS_LAST_ELEMENT_CHILD);
                } else if ("empty".equals(name)) {
                    res.matchers.add(baseSelector.IS_EMPTY);
                } else if ("only-child".equals(name)) {
                    res.matchers.add(baseSelector.ONLY_CHILD);
                } else if ("first-of-type".equals(name)) {
                    res.matchers.add(baseSelector.FIRST_OF_TYPE);
                } else if ("last-of-type".equals(name)) {
                    res.matchers.add(baseSelector.LAST_OF_TYPE);
                } else if ("root".equals(name)) {
                    res.matchers.add(baseSelector.ROOT);
                } else if (("is".equals(name) || "not".equals(name)) && ps.data() instanceof CSS.DataSelectors ds) {
                    var isMatchers = baseSelector.orMatchers(ds.value().stream().map((l) -> toBaseNodeMatcher(l, stateSupplier)).toList());
                    var baseRule = res.collectMatchers();
                    var mustMatch = "is".equals(name);
                    res.matchers.add((node, base) -> baseRule.test(node, base) && isMatchers.test(node, base) == mustMatch);
                } else if ("has".equals(name) && ps.data() instanceof CSS.DataSelectors ds) {
                    // see https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_selectors/Selector_structure#relative_selector
                    BiPredicate<T, T> hasMatchers = baseSelector.orMatchers(ds.value().stream().map(s -> {
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
                            case CHILD, DESCENDANT -> (BiPredicate<T, T>) (node, base) -> node.getAllNodesMatchingAsStream(nm, true, base).count() == 1;
                            case SIBLING, ADJACENT -> (BiPredicate<T, T>) (node, base) -> node.getParentNode().getAllNodesMatchingAsStream(nm, true, base).count() == 1;
                            default -> throw new IllegalArgumentException("Combinator " + comb + " is not supported in :has");
                        };
                    }).toList());
                    var baseRule = res.collectMatchers();
                    res.matchers.add((node, base) -> baseRule.test(node, base) && hasMatchers.test(node, node));
                } else {
                    throw new IllegalArgumentException("PseudoSelector '" + name + "' is not supported");
                }
            } else if (part instanceof CSS.UniversalSelector) {
                res.matchers.add(res.baseSelector.IS_ELEMENT);
            } else {
                throw new IllegalArgumentException(part + " is not supported");
            }
        }
        return baseSelector.andMatchers(List.of(baseSelector.IS_ELEMENT, res.internalToMatcher()));
    }
}
