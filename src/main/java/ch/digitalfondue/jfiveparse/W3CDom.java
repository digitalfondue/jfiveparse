/**
 * Copyright © 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public final class W3CDom {

    private W3CDom() {
    }

   public static class W3CDomSelector extends BaseSelector<org.w3c.dom.Node, W3CDomSelector> {

        private W3CDomSelector() {
            super(W3CDom::wrap, (toUnwrap) -> toUnwrap == null ? null : ((SelectableNodeWrapper) toUnwrap).node);
        }

        @Override
        protected W3CDomSelector inst() {
            return this;
        }

        @Override
        protected W3CDomSelector newInst() {
            return select();
        }
    }

    public static W3CDomSelector select() {
        return new W3CDomSelector();
    }

    public static Document toW3CDocument(ch.digitalfondue.jfiveparse.Document doc) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            //
            setFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
            setFeature(factory,"http://xml.org/sax/features/external-general-entities", false);
            setFeature(factory,"http://xml.org/sax/features/external-parameter-entities", false);
            setFeature(factory,"http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            //
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document d = builder.newDocument();
            doc.traverse(new W3CDNodeVisitor(d));
            return d;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }


    private static void setFeature(DocumentBuilderFactory dbFactory, String feature, boolean value) {
        try {
            dbFactory.setFeature(feature, value);
        } catch (ParserConfigurationException e) {
        }
    }

    public static class W3CDNodeVisitor implements NodesVisitor {

        protected final Document document;
        protected org.w3c.dom.Node currentNode;
        protected final Deque<Map<String, String>> xmlNamespaces = new ArrayDeque<>();

        public W3CDNodeVisitor(Document document) {
            this.document = document;
            this.currentNode = document;
            this.xmlNamespaces.push(new HashMap<>());
        }

        @Override
        public void start(Node node) {
            if (node instanceof Element elem) {
                this.xmlNamespaces.push(new HashMap<>(this.xmlNamespaces.peek()));
                org.w3c.dom.Element e = toElement(elem);
                currentNode.appendChild(e);
                currentNode = e;
            } else if (node instanceof Text text) {
                currentNode.appendChild(document.createTextNode(text.getData()));
            } else if (node instanceof Comment comment) {
                currentNode.appendChild(document.createComment(comment.getData()));
            }
        }


        private static String extractXmlnsPrefix(String xmlns) {
            int idx = xmlns.indexOf(':');
            return idx == -1 ? "" : xmlns.substring(idx + 1);
        }

        private static String extractXmlnsPrefixFromAttrOrElem(String elemOrAttr) {
            int idx = elemOrAttr.indexOf(':');
            return idx == -1 ? "" : elemOrAttr.substring(0, idx);
        }

        protected org.w3c.dom.Element buildNamespacedElement(Element element) {
            String elemPrefix = extractXmlnsPrefixFromAttrOrElem(element.getNodeName());
            String ns = element.getNamespaceURI();
            if (!elemPrefix.isEmpty() && xmlNamespaces.peek().containsKey(elemPrefix)) {
                ns = xmlNamespaces.peek().get(elemPrefix);
            }
            return document.createElementNS(ns, element.getNodeName());
        }

        protected org.w3c.dom.Element toElement(Element elem) {
            for (AttributeNode attr : elem.getAttributes()) {
                if ("xmlns".equals(attr.getName()) || attr.getName().startsWith("xmlns:")) {
                    xmlNamespaces.peek().put(extractXmlnsPrefix(attr.getName()), attr.getValue());
                }
            }

            org.w3c.dom.Element e = buildNamespacedElement(elem);

            for (AttributeNode attr : elem.getAttributes()) {
                if ("xmlns".equals(attr.getName()) || attr.getName().startsWith("xmlns:")) {
                    e.setAttributeNS("http://www.w3.org/2000/xmlns/", attr.getName(), attr.getValue());
                } else {
                    String prefix = extractXmlnsPrefixFromAttrOrElem(attr.getName());
                    String attrNs = prefix.isEmpty() ? attr.getNamespace() : xmlNamespaces.peek().getOrDefault(prefix, attr.getNamespace());
                    e.setAttributeNS(attrNs, attr.getName(), attr.getValue());
                }
            }
            return e;
        }

        @Override
        public void end(Node node) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                this.xmlNamespaces.pop();
                currentNode = currentNode.getParentNode();
            }
        }
    }

    private static org.w3c.dom.Node unwrap(SelectableNode<org.w3c.dom.Node> toUnwrap) {
        // we know we can do that, ugly, but at least it's in a single place
        return toUnwrap == null ? null : ((SelectableNodeWrapper) toUnwrap).node;
    }

    private static SelectableNode<org.w3c.dom.Node> wrap(org.w3c.dom.Node node) {
        if (node == null) {
            return null;
        }
        if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            return new SelectableElementWrapper((org.w3c.dom.Element) node);
        }
        return new SelectableNodeWrapper(node);
    }

    private static class SelectableNodeWrapper implements SelectableNode<org.w3c.dom.Node> {

        protected final org.w3c.dom.Node node;

        SelectableNodeWrapper(org.w3c.dom.Node node) {
            this.node = node;
        }

        @Override
        public int getNodeType() {
            return node.getNodeType();
        }

        @Override
        public String getNodeName() {
            return node.getNodeName();
        }

        @Override
        public org.w3c.dom.Node getParentNode() {
            return node.getParentNode();
        }

        @Override
        public org.w3c.dom.Node getFirstChild() {
            return node.getFirstChild();
        }

        @Override
        public List<org.w3c.dom.Node> getChildNodes() {
            var childNodes = node.getChildNodes();
            return new NodeList(childNodes::item, childNodes.getLength());
        }

        @Override
        public Stream<org.w3c.dom.Node> getAllNodesMatchingAsStream(NodeMatcher<org.w3c.dom.Node> matcher, boolean onlyFirst, org.w3c.dom.Node base) {
            return getAllNodesMatchingWrapped(node, matcher, onlyFirst, base);
        }

        @Override
        public org.w3c.dom.Node getLastChild() {
            return node.getLastChild();
        }

        @Override
        public org.w3c.dom.Element getFirstElementChild() {
            var childNodes = node.getChildNodes();
            var count = childNodes.getLength();
            for (int i = 0; i < count; i++) {
                var childNode = childNodes.item(i);
                if (childNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    return (org.w3c.dom.Element) childNode;
                }
            }
            return null;
        }

        @Override
        public org.w3c.dom.Element getLastElementChild() {
            var childNodes = node.getChildNodes();
            for (int i = childNodes.getLength() - 1; i >= 0; i--) {
                var childNode = childNodes.item(i);
                if (childNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    return (org.w3c.dom.Element) childNode;
                }
            }
            return null;
        }

        @Override
        public org.w3c.dom.Element getPreviousElementSibling() {
            var previous = node.getPreviousSibling();
            while (previous != null && previous.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                previous = previous.getPreviousSibling();
            }
            return previous != null ? (org.w3c.dom.Element) previous : null;
        }

        @Override
        public boolean isSameNode(org.w3c.dom.Node otherNode) {
            return  node.isSameNode(otherNode);
        }

        @Override
        public String getTextContent() {
            return node.getTextContent();
        }
    }

    private static final class SelectableElementWrapper extends SelectableNodeWrapper implements SelectableNode.SelectableElement<org.w3c.dom.Node> {

        SelectableElementWrapper(org.w3c.dom.Element node) {
            super(node);
        }

        @Override
        public String getNamespaceURI() {
            return node.getNamespaceURI();
        }

        @Override
        public boolean containsAttribute(String name) {
            return node.getAttributes().getNamedItem(name) != null;
        }

        @Override
        public String getAttributeValue(String name) {
            return node.getAttributes().getNamedItem(name).getNodeValue();
        }
    }

    /**
     * Match {@link org.w3c.dom.Node} using a {@link NodeMatcher}.
     *
     * @param node
     * @param matcher
     * @return
     */
    public static Stream<org.w3c.dom.Node> getAllNodesMatching(org.w3c.dom.Node node, NodeMatcher<org.w3c.dom.Node> matcher) {
        return getAllNodesMatching(node, matcher, false);
    }

    public static Stream<org.w3c.dom.Node> getAllNodesMatching(org.w3c.dom.Node node, NodeMatcher<org.w3c.dom.Node> matcher, boolean onlyFirstMatch) {
        return getAllNodesMatchingWrapped(node, matcher, onlyFirstMatch, node);
    }

    private static Stream<org.w3c.dom.Node> getAllNodesMatchingWrapped(org.w3c.dom.Node node, NodeMatcher<org.w3c.dom.Node> matcher, boolean onlyFirstMatch, org.w3c.dom.Node base) {
        var nm = new NodeMatchers<>((n, b) -> matcher.matcher.test(wrap(n), wrap(b)), onlyFirstMatch, base);
        traverse(node, nm);
        return nm.result().filter(Objects::nonNull);
    }


    private static void traverse(org.w3c.dom.Node rootNode, BaseNodesVisitor<org.w3c.dom.Node> visitor) {
        var node = rootNode.getFirstChild();
        while (node != null) {
            visitor.start(node);
            if (visitor.complete()) {
                return;
            }
            if (node.hasChildNodes()) {
                node = node.getFirstChild();
            } else {
                while (node != rootNode && node.getNextSibling() == null) {
                    visitor.end(node);
                    if (visitor.complete()) {
                        return;
                    }
                    node = node.getParentNode();
                }

                if (node == rootNode) {
                    break;
                }
                visitor.end(node);
                if (visitor.complete()) {
                    return;
                }
                node = node.getNextSibling();
            }
        }
    }

    private static final class NodeList extends AbstractList<org.w3c.dom.Node> {

        private final IntFunction<org.w3c.dom.Node> get;
        private final int size;

        NodeList(IntFunction<org.w3c.dom.Node> get, int size) {
            this.get = get;
            this.size = size;
        }

        @Override
        public org.w3c.dom.Node get(int index) {
            return get.apply(index);
        }

        @Override
        public int size() {
            return size;
        }
    }

}
