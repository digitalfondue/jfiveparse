package ch.digitalfondue.jfiveparse;

public interface CommonNode {
    int getNodeType();
    String getNodeName();

    CommonNode getParentNode();
    CommonNode getFirstChild();
    CommonNode getLastChild();
    CommonElement getFirstElementChild();
    CommonElement getLastElementChild();

    boolean isSameNode(CommonNode node);

    interface CommonElement extends CommonNode {
        String getNamespaceURI();

        boolean containsAttribute(String name);
        String getAttributeValue(String name);
    }
}
