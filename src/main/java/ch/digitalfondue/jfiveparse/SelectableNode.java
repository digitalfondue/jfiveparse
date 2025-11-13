package ch.digitalfondue.jfiveparse;

import java.util.List;
import java.util.stream.Stream;

public interface SelectableNode<T extends SelectableNode<T>> {
    int getNodeType();
    String getNodeName();

    T getParentNode();
    T getFirstChild();
    T getLastChild();
    SelectableElement<T> getFirstElementChild();
    SelectableElement<T> getLastElementChild();
    SelectableElement<T> getPreviousElementSibling();
    List<T> getChildNodes();
    Stream<T> getAllNodesMatchingAsStream(NodeMatcher matcher, boolean onlyFirst, T base);

    String getTextContent();

    boolean isSameNode(T node);

    interface SelectableElement<T extends SelectableNode<T>> extends SelectableNode<T> {
        String getNamespaceURI();

        boolean containsAttribute(String name);
        String getAttributeValue(String name);
    }
}
