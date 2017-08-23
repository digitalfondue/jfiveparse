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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class W3CDom {

    public static Document toW3CDocument(ch.digitalfondue.jfiveparse.Document doc) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document d = builder.newDocument();
            doc.traverse(new W3CDNodeVisitor(d));
            return d;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class W3CDNodeVisitor implements NodesVisitor {

        protected Document document;
        protected org.w3c.dom.Node currentNode;

        public W3CDNodeVisitor(Document document) {
            this.document = document;
            this.currentNode = document;
        }

        @Override
        public void start(Node node) {

            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element elem = (Element) node;
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
                case Node.DOCUMENT_TYPE_NODE:
                    break;
                default:
                    break;
            }
        }

        protected org.w3c.dom.Element toElement(Element elem) {
            org.w3c.dom.Element e = document.createElementNS(elem.getNamespaceURI(), elem.getNodeName());
            for (String attrName : elem.getAttributes().keySet()) {
                AttributeNode attr = elem.getAttributeNode(attrName);
                Attr copiedAttr = document.createAttributeNS(attr.getNamespace(), attr.getName());
                copiedAttr.setValue(attr.getValue());
                copiedAttr.setPrefix(attr.getPrefix());
                e.setAttributeNodeNS(copiedAttr);
            }
            return e;
        }

        @Override
        public void end(Node node) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                currentNode = currentNode.getParentNode();
            }
        }

        @Override
        public boolean complete() {
            return false;
        }
    }
}
