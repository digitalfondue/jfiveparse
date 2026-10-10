package ch.digitalfondue.jfiveparse;

import java.util.List;
import java.util.stream.Stream;

sealed interface SelectableNode<T> permits Node, SelectableElement, W3CDom.SelectableNodeWrapper {
    int getNodeType();
    String getNodeName();
    T getParentNode();
    T getFirstChild();
    T getLastChild();
    T getFirstElementChild();
    T getLastElementChild();
    T getPreviousElementSibling();
    List<T> getChildNodes();
    Stream<T> getAllNodesMatchingAsStream(NodeMatcher<T> matcher, boolean onlyFirst, T base);
    String getTextContent();
    boolean isSameNode(T node);
}
