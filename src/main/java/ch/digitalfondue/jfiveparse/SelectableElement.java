package ch.digitalfondue.jfiveparse;

interface SelectableElement<T> extends SelectableNode<T> {
    String getNamespaceURI();
    String getAttributeValue(String name);
}
