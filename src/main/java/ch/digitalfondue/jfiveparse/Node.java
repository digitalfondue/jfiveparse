/**
 * Copyright (C) 2015 digitalfondue (info@digitalfondue.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ch.digitalfondue.jfiveparse.NodeMatchers.NodeMatcher;

public abstract class Node {

    private static final List<Node> EMPTY_LIST = Collections.emptyList();

    Node parentNode;

    public static final String NAMESPACE_HTML = "http://www.w3.org/1999/xhtml";
    public static final String NAMESPACE_SVG = "http://www.w3.org/2000/svg";
    public static final String NAMESPACE_MATHML = "http://www.w3.org/1998/Math/MathML";

    public static final String NAMESPACE_XMLNS = "http://www.w3.org/2000/xmlns/";
    public static final String NAMESPACE_XML = "http://www.w3.org/XML/1998/namespace";
    public static final String NAMESPACE_XLINK = "http://www.w3.org/1999/xlink";

    public static final byte ELEMENT_NODE = 1;
    public static final byte TEXT_NODE = 3;
    public static final byte COMMENT_NODE = 8;
    public static final byte DOCUMENT_NODE = 9;
    public static final byte DOCUMENT_TYPE_NODE = 10;

    public abstract byte getNodeType();

    public abstract String getNodeName();

    public Node getParentNode() {
        return parentNode;
    }

    List<Node> getMutableChildNodes() {
        return EMPTY_LIST;
    }

    public void empty() {
        List<Node> childs = getMutableChildNodes();
        if (childs == EMPTY_LIST) {
            return;
        }

        for (Node n : childs) {
            n.parentNode = null;
        }

        childs.clear();
    }

    public int getChildCount() {
        return getMutableChildNodes().size();
    }

    public void appendChild(Node node) {
        List<Node> childs = getMutableChildNodes();
        if (childs == EMPTY_LIST) {
            return;
        }

        if (node.parentNode == this) {
            childs.remove(node);
            node.parentNode = null;
        }

        insertChildren(childs.size(), node);
    }

    public void insertChildren(int position, Node node) {
        List<Node> childs = getMutableChildNodes();
        if (childs == EMPTY_LIST) {
            return;
        }

        Node previousParent = node.parentNode;

        node.parentNode = this;

        if (position == childs.size()) {
            childs.add(node);
        } else {
            childs.add(position, node);
        }

        if (previousParent != null) {
            previousParent.getMutableChildNodes().remove(node);
        }
    }

    public void removeChild(Node node) {
        List<Node> childs = getMutableChildNodes();
        if (childs == EMPTY_LIST) {
            return;
        }

        childs.remove(node);
    }

    public List<Node> getChildNodes() {
        return EMPTY_LIST;
    }

    public Node getFirstChild() {
        List<Node> childs = getChildNodes();
        return childs.isEmpty() ? null : childs.get(0);
    }

    public Element getFirstElementChild() {
        List<Node> childs = getChildNodes();
        for (Node n : childs) {
            if (n.getNodeType() == ELEMENT_NODE) {
                return (Element) n;
            }
        }
        return null;
    }

    public Element getLastElementChild() {
        List<Node> childs = getChildNodes();
        for (int i = childs.size(); i >= 0; i--) {
            Node n = childs.get(i);
            if (n.getNodeType() == ELEMENT_NODE) {
                return (Element) n;
            }
        }
        return null;
    }

    public Node getLastChild() {
        List<Node> childs = getChildNodes();
        return childs.isEmpty() ? null : childs.get(childs.size() - 1);
    }

    public Node getPreviousSibling() {
        if (parentNode == null) {
            return null;
        }

        List<Node> siblings = parentNode.getChildNodes();
        int currentElemIdx = siblings.indexOf(this);
        return currentElemIdx == 0 ? null : siblings.get(currentElemIdx - 1);
    }

    public Element getPreviousElementSibling() {
        Node n = getPreviousSibling();
        while (n != null) {
            if (n.getNodeType() == ELEMENT_NODE) {
                return (Element) n;
            }
            n = n.getPreviousSibling();
        }
        return null;
    }

    public Node getNextSibling() {
        if (parentNode == null) {
            return null;
        }

        List<Node> siblings = parentNode.getChildNodes();
        int currentElemIdx = siblings.indexOf(this);

        return currentElemIdx == siblings.size() - 1 ? null : siblings.get(currentElemIdx + 1);
    }

    public Element getNextElementSibling() {
        Node n = getNextSibling();
        while (n != null) {
            if (n.getNodeType() == ELEMENT_NODE) {
                return (Element) n;
            }
            n = n.getNextSibling();
        }
        return null;
    }

    public boolean hasChildNodes() {
        return !getChildNodes().isEmpty();
    }

    /**
     * Traverse tree. As described in <a href=
     * "http://www.drdobbs.com/database/a-generic-iterator-for-tree-traversal/184404325"
     * >http://www.drdobbs.com/database/a-generic-iterator-for-tree-traversal/
     * 184404325</a> ...
     */
    private void traverse(NodesVisitor visitor) {
        Node node = getFirstChild();
        while (node != null) {
            visitor.start(node);
            if (node.hasChildNodes()) {
                node = node.getFirstChild();
            } else {
                while (node != this && node.getNextSibling() == null) {
                    visitor.end(node);
                    node = node.getParentNode();
                }

                if (node == this) {
                    break;
                }
                visitor.end(node);

                node = node.getNextSibling();
            }
        }
    }

    public <T extends Node> List<T> getAllNodesMatching(NodeMatcher matcher) {
        List<T> l = new ArrayList<>();
        traverse(new NodeMatchers<>(matcher, l));
        return l;
    }

    public List<Element> getElementsByTagName(String name) {
        return getAllNodesMatching(new NodeMatchers.ElementHasTagName(name));
    }

    public List<Element> getElementsByTagNameNS(String name, String namespace) {
        return getAllNodesMatching(new NodeMatchers.ElementHasTagName(name, namespace));
    }

    public Element getElementById(String idValue) {
        List<Element> l = getAllNodesMatching(new NodeMatchers.HasAttribute("id", idValue));
        return l.isEmpty() ? null : l.get(0);
    }

    public String getTextContent() {
        List<Text> textNodes = getAllNodesMatching(NodeMatchers.text());
        StringBuilder sb = new StringBuilder();
        for (Text n : textNodes) {
            sb.append(n.getData());
        }
        return sb.toString();
    }

    public void traverseWithCurrentNode(NodesVisitor visitor) {
        visitor.start(this);
        traverse(visitor);
        visitor.end(this);
    }

    public String getInnerHTML(Set<Option> options) {
        StringBuilder sb = new StringBuilder();
        traverse(new HtmlSerializer(sb, options));
        return sb.toString();
    }

    public String getOuterHTML(Set<Option> options) {
        StringBuilder sb = new StringBuilder();
        traverseWithCurrentNode(new HtmlSerializer(sb, options));
        return sb.toString();
    }

    public String getInnerHTML() {
        return getInnerHTML(EnumSet.noneOf(Option.class));
    }

    public String getOuterHTML() {
        return getOuterHTML(EnumSet.noneOf(Option.class));
    }
}
