package ch.digitalfondue.jfiveparse;

sealed interface SelectableElement<T> extends SelectableNode<T> permits Element, W3CDom.SelectableElementWrapper {
    String getNamespaceURI();
    String getAttributeValue(String name);
}
