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

import java.util.*;

/**
 * Base class for all the nodes.
 */
public sealed abstract class Node implements CommonNode permits Comment, Document, DocumentType, Element, Text {

    static final List<Node> EMPTY_LIST = List.of();

    Node parentNode;

    public static final String NAMESPACE_HTML = "http://www.w3.org/1999/xhtml";
    static final int NAMESPACE_HTML_ID = 1;
    public static final String NAMESPACE_SVG = "http://www.w3.org/2000/svg";
    static final int NAMESPACE_SVG_ID = 2;
    public static final String NAMESPACE_MATHML = "http://www.w3.org/1998/Math/MathML";
    static final int NAMESPACE_MATHML_ID = 3;

    public static final String NAMESPACE_XMLNS = "http://www.w3.org/2000/xmlns/";
    static final int NAMESPACE_XMLNS_ID = 4;
    public static final String NAMESPACE_XML = "http://www.w3.org/XML/1998/namespace";
    static final int NAMESPACE_XML_ID = 5;
    public static final String NAMESPACE_XLINK = "http://www.w3.org/1999/xlink";
    static final int NAMESPACE_XLINK_ID = 6;


    static int toNameSpaceId(String s) {
        if (s == null) {
            return 0;
        }
        return switch (s) {
            case NAMESPACE_HTML -> NAMESPACE_HTML_ID;
            case NAMESPACE_SVG -> NAMESPACE_SVG_ID;
            case NAMESPACE_MATHML -> NAMESPACE_MATHML_ID;
            case NAMESPACE_XMLNS -> NAMESPACE_XMLNS_ID;
            case NAMESPACE_XML -> NAMESPACE_XML_ID;
            case NAMESPACE_XLINK -> NAMESPACE_XLINK_ID;
            default -> 0;
        };
    }

    /**
     * {@link Element} node type value:
     */
    public static final int ELEMENT_NODE = 1;

    /**
     * {@link Text} node type value:
     */
    public static final int TEXT_NODE = 3;

    /**
     * {@link Comment} node type value:
     */
    public static final int COMMENT_NODE = 8;

    /**
     * {@link Document} node type value:
     */
    public static final int DOCUMENT_NODE = 9;

    /**
     * {@link DocumentType} node type value:
     */
    public static final int DOCUMENT_TYPE_NODE = 10;

    /**
     * @return the node type. See {@link #ELEMENT_NODE}, {@link #TEXT_NODE},
     *         {@link #COMMENT_NODE}, {@link #DOCUMENT_NODE} and
     *         {@link #DOCUMENT_TYPE_NODE}.
     */
    @Override
    public abstract int getNodeType();

    /**
     * @return the node name. Each concrete class will return a specific value.
     */
    @Override
    public abstract String getNodeName();

    /**
     * Get the parent node if present or else return null.
     */
    @Override
    public Node getParentNode() {
        return parentNode;
    }

    List<Node> getMutableChildNodes() {
        return EMPTY_LIST;
    }

    /**
     * Get the child nodes without generating allocations (wrapping with unmodifiable _or_ creating a mutable array).
     *
     * @return
     */
    List<Node> getRawChildNodes() {
        return EMPTY_LIST;
    }

    /**
     * Remove all child nodes from this node.
     */
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

    /**
     * Get the number of childs of the current node.
     */
    public int getChildCount() {
        return getRawChildNodes().size();
    }

    /**
     * Append the {@link Node} at the end of this node. If the node has a
     * parentNode defined, it will be removed from the original parent.
     */
    public void appendChild(Node node) {
        Objects.requireNonNull(node);
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

    /**
     * Insert the {@link Node} at the given position. If the node has a
     * parentNode defined, it will be removed from the original parent.
     *
     * @param position
     *            the index
     * @param node
     *            the node to be inserted
     */
    public void insertChildren(int position, Node node) {
        Objects.requireNonNull(node);
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

    /**
     * Insert the {@link Node} before another {@link Node}.
     *
     * @param toInsert
     *            the node to be inserted
     * @param before
     *            if the node is not a child of this node, the insertion will
     *            fail silently.
     */
    public void insertBefore(Node toInsert, Node before) {
        Objects.requireNonNull(toInsert);
        Objects.requireNonNull(before);
        int idx = getRawChildNodes().indexOf(before);
        if (idx >= 0) {
            insertChildren(idx, toInsert);
        }
    }

    /**
     * Replace a node with another one.
     *
     * @param node
     *            the new node
     * @param oldChild
     *            the node to be replaced
     */
    public void replaceChild(Node node, Node oldChild) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(oldChild);
        List<Node> childs = getMutableChildNodes();
        if (childs == EMPTY_LIST) {
            return;
        }
        int idx = childs.indexOf(oldChild);
        if (idx >= 0) {
            Node previousParent = node.parentNode;
            node.parentNode = this;
            childs.set(idx, node);
            if (previousParent != null) {
                previousParent.getMutableChildNodes().remove(node);
            }
            oldChild.parentNode = null;
        }
    }

    /**
     * Remove a child node.
     *
     * @param node
     *            the node to be removed
     */
    public void removeChild(Node node) {
        Objects.requireNonNull(node);
        List<Node> childs = getMutableChildNodes();
        if (childs == EMPTY_LIST) {
            return;
        }
        if (childs.remove(node)) {
            node.parentNode = null;
        }
    }

    /**
     * Get the child nodes. The list is <strong>not</strong> modifiable.
     */
    public List<Node> getChildNodes() {
        return EMPTY_LIST;
    }

    /**
     * Get the first child, if present or else null.
     */
    public Node getFirstChild() {
        List<Node> childs = getRawChildNodes();
        return childs.isEmpty() ? null : childs.get(0);
    }

    /**
     * Get the last child, if present or else null.
     */
    @Override
    public Node getLastChild() {
        List<Node> childs = getRawChildNodes();
        return childs.isEmpty() ? null : childs.get(childs.size() - 1);
    }

    /**
     * Get the first <strong>{@link Element}</strong> child, if present or else
     * null.
     */
    @Override
    public Element getFirstElementChild() {
        List<Node> childs = getRawChildNodes();
        for (Node n : childs) {
            if (n instanceof Element e) {
                return e;
            }
        }
        return null;
    }

    /**
     * Get the last <strong>{@link Element}</strong> child, if present or else
     * null.
     */
    @Override
    public Element getLastElementChild() {
        List<Node> childs = getRawChildNodes();
        for (int i = childs.size() - 1; i >= 0; i--) {
            Node n = childs.get(i);
            if (n instanceof Element e) {
                return e;
            }
        }
        return null;
    }

    /**
     * Get the previous sibling {@link Node} if present, or else null.
     */
    public Node getPreviousSibling() {
        if (parentNode == null) {
            return null;
        }

        List<Node> siblings = parentNode.getRawChildNodes();
        int currentElemIdx = siblings.indexOf(this);
        return currentElemIdx == 0 ? null : siblings.get(currentElemIdx - 1);
    }

    /**
     * Get the previous <strong>{@link Element}</strong> sibling if present, or
     * else null.
     */
    public Element getPreviousElementSibling() {
        if (parentNode == null) {
            return null;
        }
        List<Node> siblings = parentNode.getRawChildNodes();
        int currentElemIdx = siblings.indexOf(this);
        for (int i = currentElemIdx - 1; i >= 0; i--) {
            if (siblings.get(i) instanceof Element e) {
                return e;
            }
        }
        return null;
    }

    /**
     * Get the next sibling {@link Node} if present, or else null.
     */
    public Node getNextSibling() {
        if (parentNode == null) {
            return null;
        }

        List<Node> siblings = parentNode.getRawChildNodes();
        int currentElemIdx = siblings.indexOf(this);

        return currentElemIdx == siblings.size() - 1 ? null : siblings.get(currentElemIdx + 1);
    }

    /**
     * Get the next <strong>{@link Element}</strong> sibling if present, or else
     * null.
     */
    public Element getNextElementSibling() {
        if (parentNode == null) {
            return null;
        }
        List<Node> siblings = parentNode.getRawChildNodes();
        int currentElemIdx = siblings.indexOf(this);
        int count = siblings.size();

        for (int i = currentElemIdx + 1; i < count; i++) {
            if (siblings.get(i) instanceof Element e) {
                return e;
            }
        }
        return null;
    }

    /**
     * @return true if this node has at least one child.
     */
    public boolean hasChildNodes() {
        return !getRawChildNodes().isEmpty();
    }

    /**
     * Traverse the childs of this node in <a href=
     * "https://html.spec.whatwg.org/multipage/infrastructure.html#tree-order"
     * >"tree order"</a>.
     */
    // As described in
    // http://www.drdobbs.com/database/a-generic-iterator-for-tree-traversal/184404325
    public void traverse(NodesVisitor visitor) {
        Node node = getFirstChild();
        while (node != null) {
            visitor.start(node);
            if (visitor.complete()) {
                return;
            }
            if (node.hasChildNodes()) {
                node = node.getFirstChild();
            } else {
                while (node != this && node.getNextSibling() == null) {
                    visitor.end(node);
                    if (visitor.complete()) {
                        return;
                    }
                    node = node.getParentNode();
                }

                if (node == this) {
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

    /**
     * Traverse this node and his child.
     */
    public void traverseWithCurrentNode(NodesVisitor visitor) {
        visitor.start(this);
        traverse(visitor);
        visitor.end(this);
    }

    /**
     * Get all the nodes matching the given matcher. The nodes will be returned
     * in "tree order". See {@link Selector}.
     */
    public <T extends Node> List<T> getAllNodesMatching(NodeMatcher matcher) {
        return getAllNodesMatching(matcher, false);
    }

    /**
     * Get all the nodes matching the given matcher. The nodes will be returned
     * in "tree order". If the second parameter is true, the traversal will stop
     * on the first match. See {@link Selector}.
     */
    public <T extends Node> List<T> getAllNodesMatching(NodeMatcher matcher, boolean onlyFirstMatch) {
        List<T> l = new ArrayList<>();
        traverse(new NodeMatchers<>(matcher, l, onlyFirstMatch));
        return l;
    }

    /**
     * Get all the {@link Element} that match the given name. The elements will
     * be returned in "tree order". The name is case sensitive.
     */
    public List<Element> getElementsByTagName(String name) {
        return getAllNodesMatching(Selector.select().element(name).toMatcher());
    }

    /**
     * Get all the {@link Element} that match the given name and namespace. The
     * elements will be returned in "tree order". The name and namespace are
     * case sensitive.
     */
    public List<Element> getElementsByTagNameNS(String name, String namespace) {
        return getAllNodesMatching(Selector.select().element(name, namespace).toMatcher());
    }

    /**
     * Get the element with the given id. The id is case sensitive. If in the
     * documents there are more than one element with the same id, the first
     * element found during the traversal will be returned.
     */
    public Element getElementById(String idValue) {
        List<Element> l = getAllNodesMatching(Selector.select().id(idValue).toMatcher(), true);
        return l.isEmpty() ? null : l.get(0);
    }

    /**
     * Return true if node is descendant.
     *
     * @param node
     * @return
     */
    public boolean contains(Node node) {
        // check same reference
        return !getAllNodesMatching((n) -> n == node, true).isEmpty();
    }

    /**
     * Get the text content of the node.
     */
    public String getTextContent() {
		if (getNodeType() == TEXT_NODE) {
			return ((Text) this).getData();
		}

        List<Text> textNodes = getAllNodesMatching((node) -> node.getNodeType() == TEXT_NODE);
        StringBuilder sb = new StringBuilder();
        for (Text n : textNodes) {
            sb.append(n.getData());
        }
        return sb.toString();
    }

    /**
     * The normalize() method removes empty Text nodes, and joins adjacent Text nodes.
     */
    public void normalize() {

		List<Node> childs = new ArrayList<>(getRawChildNodes());

		// iterator helpers
		Node text = null;
		StringBuilder concatenatedText = null;
		for (Node n : childs) {
			// start accumulating texts node
			if (text == null && n.getNodeType() == Node.TEXT_NODE) {
				text = n;
				concatenatedText = new StringBuilder();
				concatenatedText.append(((Text) n).getData());
			}
			// continue accumulating texts node
			else if (text != null && n.getNodeType() == Node.TEXT_NODE) {
				concatenatedText.append(((Text) n).getData());
				removeChild(n);
			}
			// stop accumulating node and add current
			else if (text != null) {
				replaceTextNodeWith(text, concatenatedText);
				text = null;
				n.normalize();
			} else {
				n.normalize();
			}
		}

		if (concatenatedText != null) {
			replaceTextNodeWith(text, concatenatedText);
		}
    }

    /**
     * Clone only the node.
     *
     * @return
     */
    public Node cloneNode() {
    	return cloneNode(false);
    }

    /**
     * Tests whether two nodes are the same, that is if they reference the same object.
     *
     * See https://developer.mozilla.org/en-US/docs/Web/API/Node/isSameNode
     *
     * @param other
     * @return
     */
    @Override
    public boolean isSameNode(CommonNode other) {
        return this == other;
    }

    /**
     * Clone the node. If the deep parameter is true, the copy will include the children.
     *
     * @param deep
     * @return
     */
    public abstract Node cloneNode(boolean deep);

    /**
     * Check if the node is equal to the parameter.
     *
     * See <a href="https://dom.spec.whatwg.org/#concept-node-equals">...</a>
     *
     * @param other
     * @return
     */
    public abstract boolean isEqualNode(Node other);

    /**
     * Null safe static isEqualNode helper
     *
     * @param a
     * @param b
     * @return
     */
    static boolean nodesEquals(Node a, Node b) {
        if (a != null && b != null) {
            return a.isEqualNode(b);
        } else {
            return a == null && b == null;
        }
    }

	private void replaceTextNodeWith(Node text, StringBuilder concatenatedText) {
		if(concatenatedText.isEmpty()) {
			removeChild(text);
		} else {
			replaceChild(new Text(concatenatedText.toString()), text);
		}
	}
}
