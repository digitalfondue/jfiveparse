package ch.digitalfondue.jfiveparse;

import java.util.List;
import java.util.stream.Stream;

public interface SelectableNode {
    int getNodeType();
    String getNodeName();

    SelectableNode getParentNode();
    SelectableNode getFirstChild();
    SelectableNode getLastChild();
    SelectableElement getFirstElementChild();
    SelectableElement getLastElementChild();
    SelectableElement getPreviousElementSibling();
    List<? extends SelectableNode> getChildNodes();
    Stream<? extends SelectableNode> getAllNodesMatchingAsStream(NodeMatcher matcher, boolean onlyFirst, SelectableNode base);

    String getTextContent();

    boolean isSameNode(SelectableNode node);

    interface SelectableElement extends SelectableNode {
        String getNamespaceURI();

        boolean containsAttribute(String name);
        String getAttributeValue(String name);
    }
}
