package ch.digitalfondue.jfiveparse;

sealed interface SelectableElement<T> extends SelectableNode<T> permits Element, W3CDom.SelectableElementWrapper {
    String getNamespaceURI();
    String getAttribute(String name);
    String getAttributeNS(String namespace, String localName);
}
