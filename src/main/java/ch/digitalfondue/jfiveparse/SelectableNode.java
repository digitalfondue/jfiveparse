package ch.digitalfondue.jfiveparse;

import java.util.List;

public interface SelectableNode {
    int getNodeType();
    String getNodeName();

    SelectableNode getParentNode();
    SelectableNode getFirstChild();
    SelectableNode getLastChild();
    SelectableElement getFirstElementChild();
    SelectableElement getLastElementChild();
    SelectableElement getPreviousElementSibling();
    List<SelectableNode> childNodes();

    String getTextContent();

    boolean isSameNode(SelectableNode node);

    interface SelectableElement extends SelectableNode {
        String getNamespaceURI();

        boolean containsAttribute(String name);
        String getAttributeValue(String name);
    }
}
