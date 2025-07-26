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

public class W3CDom {

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

            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element elem = (Element) node;
                    this.xmlNamespaces.push(new HashMap<>(this.xmlNamespaces.peek()));
                    org.w3c.dom.Element e = toElement(elem);
                    currentNode.appendChild(e);
                    currentNode = e;
                    break;
                case Node.TEXT_NODE:
                    currentNode.appendChild(document.createTextNode(((Text) node).getData()));
                    break;
                case Node.COMMENT_NODE:
                    currentNode.appendChild(document.createComment(((Comment) node).getData()));
                    break;
                /*case Node.DOCUMENT_TYPE_NODE:
                    break;*/
                default:
                    break;
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
            for (String attrName : elem.getAttributes().keySet()) {
                AttributeNode attr = elem.getAttributeNode(attrName);
                if ("xmlns".equals(attr.getName()) || attr.getName().startsWith("xmlns:")) {
                    xmlNamespaces.peek().put(extractXmlnsPrefix(attr.getName()), attr.getValue());
                }
            }

            org.w3c.dom.Element e = buildNamespacedElement(elem);

            for (String attrName : elem.getAttributes().keySet()) {
                AttributeNode attr = elem.getAttributeNode(attrName);
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

        @Override
        public boolean complete() {
            return false;
        }
    }

    static CommonNode wrap(org.w3c.dom.Node node) {
        if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            return new CommonElementWrapper((org.w3c.dom.Element) node);
        }
        return new CommonNodeWrapper(node);
    }

    static class CommonNodeWrapper implements CommonNode {

        protected final org.w3c.dom.Node node;

        CommonNodeWrapper(org.w3c.dom.Node node) {
            this.node = node;
        }

        @Override
        public byte getNodeType() {
            return (byte) node.getNodeType();
        }

        @Override
        public String getNodeName() {
            return node.getNodeName();
        }

        @Override
        public CommonNode getParentNode() {
            return wrap(node.getParentNode());
        }

        @Override
        public CommonNode getFirstChild() {
            return wrap(node.getFirstChild());
        }

        @Override
        public CommonNode getLastChild() {
            return wrap(node.getLastChild());
        }

        @Override
        public CommonElement getFirstElementChild() {
            var childNodes = node.getChildNodes();
            var count = childNodes.getLength();
            for (int i = 0; i < count; i++) {
                var childNode = childNodes.item(i);
                if (childNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    return new CommonElementWrapper((org.w3c.dom.Element) childNode);
                }
            }
            return null;
        }

        @Override
        public CommonElement getLastElementChild() {
            var childNodes = node.getChildNodes();
            for (int i = childNodes.getLength() - 1; i >= 0; i--) {
                var childNode = childNodes.item(i);
                if (childNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    return new CommonElementWrapper((org.w3c.dom.Element) childNode);
                }
            }
            return null;
        }

        @Override
        public boolean isSameNode(CommonNode otherNode) {
            return otherNode instanceof CommonNodeWrapper cnw && node.isSameNode(cnw.node);
        }
    }

    static final class CommonElementWrapper extends CommonNodeWrapper implements CommonNode.CommonElement {

        CommonElementWrapper(org.w3c.dom.Element node) {
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
}
