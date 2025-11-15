package ch.digitalfondue.jfiveparse;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

interface SelectableNode<T extends SelectableNode<T>> {
    int getNodeType();
    String getNodeName();

    T getParentNode();
    T getFirstChild();
    T getLastChild();
    T getFirstElementChild();
    T getLastElementChild();
    T getPreviousElementSibling();
    List<T> getChildNodes();
    Stream<T> getAllNodesMatchingAsStream(BiPredicate<T, T> matcher, boolean onlyFirst, T base);

    String getTextContent();

    boolean isSameNode(T node);

    interface SelectableElement<T extends SelectableNode<T>> extends SelectableNode<T> {
        String getNamespaceURI();

        boolean containsAttribute(String name);
        String getAttributeValue(String name);
    }
}
