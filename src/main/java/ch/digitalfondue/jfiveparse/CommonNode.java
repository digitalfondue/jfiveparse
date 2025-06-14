package ch.digitalfondue.jfiveparse;

public interface CommonNode {
    byte getNodeType();
    String getNodeName();

    CommonNode getParentNode();
    CommonNode getFirstChild();
    CommonNode getLastChild();
    CommonElement getFirstElementChild();
    CommonElement getLastElementChild();

    interface CommonElement extends CommonNode {
        String getNamespaceURI();

        boolean containsAttribute(String name);
        String getAttributeValue(String name);
    }
}
