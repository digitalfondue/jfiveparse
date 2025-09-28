package ch.digitalfondue.jfiveparse;

import java.util.stream.Stream;

public interface CommonNode {
    int getNodeType();
    String getNodeName();

    CommonNode getParentNode();
    CommonNode getFirstChild();
    CommonNode getLastChild();
    CommonElement getFirstElementChild();
    CommonElement getLastElementChild();
    CommonElement getPreviousElementSibling();
    Stream<CommonNode> childNodes();

    String getTextContent();

    boolean isSameNode(CommonNode node);

    interface CommonElement extends CommonNode {
        String getNamespaceURI();

        boolean containsAttribute(String name);
        String getAttributeValue(String name);
    }
}
