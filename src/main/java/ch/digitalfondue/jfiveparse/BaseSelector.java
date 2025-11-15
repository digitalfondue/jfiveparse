package ch.digitalfondue.jfiveparse;

import java.util.List;
import java.util.function.BiPredicate;

final class BaseSelector<T extends SelectableNode<T>> {

    final BiPredicate<T, T> IS_FIRST_CHILD = (node, base) -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getFirstChild());

    final BiPredicate<T, T> IS_FIRST_ELEMENT_CHILD = (node, base) -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getFirstElementChild());

    final BiPredicate<T, T> IS_LAST_CHILD = (node, base) -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getLastChild());

    final BiPredicate<T, T> IS_LAST_ELEMENT_CHILD = (node, base) -> node.getParentNode() != null && node.isSameNode(node.getParentNode().getLastElementChild());

    final BiPredicate<T, T> IS_ELEMENT = (node, base) -> node.getNodeType() == Node.ELEMENT_NODE;

    final BiPredicate<T, T> ROOT = (n, base) -> (n.getParentNode() == null || n.getParentNode().getNodeType() == Node.DOCUMENT_NODE) && n.getNodeType() == Node.ELEMENT_NODE;

    final BiPredicate<T, T> LAST_OF_TYPE = (node, base) -> {
        if (node.getParentNode() != null) {
            var nodeName = node.getNodeName();
            var childNodes = node.getParentNode().getChildNodes();
            for (int i = childNodes.size() - 1; i >= 0; i--) {
                var e = childNodes.get(i);
                if (IS_ELEMENT.test(e, base) && e.getNodeName().equals(nodeName)) {
                    return node.isSameNode(e);
                }
            }
        }
        return false;
    };

    final BiPredicate<T, T> FIRST_OF_TYPE = (node, base) -> {
        if (node.getParentNode() != null) {
            var nodeName = node.getNodeName();
            var childNodes = node.getParentNode().getChildNodes();
            for (var e : childNodes) {
                if (IS_ELEMENT.test(e, base) && e.getNodeName().equals(nodeName)) {
                    return node.isSameNode(e);
                }
            }
        }
        return false;
    };

    final BiPredicate<T, T> ONLY_CHILD = (node, base) -> node.getParentNode() == null ||
            (node.getParentNode().getChildNodes()
                    .stream()
                    .filter(f -> IS_ELEMENT.test(f, base))
                    .allMatch(n -> n.isSameNode(node)));

    final BiPredicate<T, T> IS_EMPTY = (node, base) -> node.getChildNodes().stream().noneMatch(s -> IS_ELEMENT.test(s, base) || s.getNodeType() == Node.TEXT_NODE);


    BiPredicate<T, T> orMatchers(List<BiPredicate<T, T>> BaseNodeMatchers) {
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

    BiPredicate<T, T> andMatchers(List<BiPredicate<T, T>> BaseNodeMatchers) {
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

    BiPredicate<T, T> withDescendant(BiPredicate<T, T> ancestorMatcher) {
        return (node, base) -> {
            while (node.getParentNode() != null) {
                node = node.getParentNode();
                if (ancestorMatcher.test(node, base)) {
                    return true;
                }
            }
            return false;
        };
    }

    BiPredicate<T, T> matchAttr(String name, BiPredicate<String, SelectableNode.SelectableElement<?>> attributeValueMatcher) {
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
