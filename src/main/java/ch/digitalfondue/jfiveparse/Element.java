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
import java.util.stream.Stream;

/**
 * Represent an Element (e.g. "&lt;div&gt;").
 */
public final class Element extends Node implements CommonNode.CommonElement {

    final String nodeName;
    final String originalNodeName;
    final String namespaceURI;
    final int namespaceID;
    final int nodeNameID;
    Attributes attributes;

    private List<Node> childNodes = null;

    /**
     * Create an element in the {@link Node#NAMESPACE_HTML} namespace with no
     * attributes.
     * 
     * @param name
     */
    public Element(String name) {
        this(name, Node.NAMESPACE_HTML);
    }

    /**
     * Create an element in the given namespace with no attributes.
     * 
     * @param name
     * @param nameSpace
     */
    public Element(String name, String nameSpace) {
        this(name, nameSpace, null);
    }

    /**
     * Create an element in the given namespace with attributes.
     * 
     * @param name
     * @param nameSpace
     * @param attributes
     */
    public Element(String name, String nameSpace, Attributes attributes) {
        this(name, Common.tagNameToID(name), name, nameSpace, Node.toNamespaceId(nameSpace), attributes);
    }

    Element(String name,
            int nameID,
            String originalName,
            String nameSpace,
            int namespaceID,
            Attributes attributes) {
        this.nodeName = name;
        this.nodeNameID = nameID;
        this.originalNodeName = originalName;
        this.namespaceURI = nameSpace;
        this.namespaceID = namespaceID;
        this.attributes = attributes;
    }

    private void ensureAttributesPresence() {
        if (attributes == null) {
            this.attributes = new Attributes();
        }
    }

    /**
     * @see Node#getChildNodes().
     */
    @Override
    public List<Node> getChildNodes() {
        return childNodes == null ? EMPTY_LIST : Collections.unmodifiableList(childNodes);
    }

    @Override
    public Stream<CommonNode> childNodes() {
        return childNodes == null ? Stream.empty() : childNodes.stream().map(CommonNode.class::cast);
    }

    @Override
    List<Node> getRawChildNodes() {
        return childNodes == null ? EMPTY_LIST : childNodes;
    }

    @Override
    List<Node> getMutableChildNodes() {
        if (childNodes == null) {
            this.childNodes = new ArrayList<>(2);
        }
        return childNodes;
    }

    @Override
    public int getNodeType() {
        return ELEMENT_NODE;
    }

    /**
     * Return the node name. If the {@link Element} is a "div" element, the
     * {@link String} "div" will be returned.
     */
    @Override
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Return the namespace.
     */
    @Override
    public String getNamespaceURI() {
        return namespaceURI;
    }


    // case-sensitive check
    @Override
    public boolean containsAttribute(String name) {
        return attributes != null && attributes.containsKey(name);
    }

    @Override
    public String getAttributeValue(String name) {
        return attributes != null ? attributes.getNamedItem(name) : null;
    }

    void setInnerHtml(List<Node> nodes) {
        empty();
        for (Node n : nodes) {
            appendChild(n);
        }
    }

    private Node insertAdjacentNode(String position, Node node) {
        Objects.requireNonNull(position, "position must be not null");
        position = position.toLowerCase(Locale.ROOT);
        Node parentNode = getParentNode();
        switch (position) {
            case "beforebegin":
                if (parentNode == null) {
                    throw new IllegalStateException("The element has no parent. Cannot use beforebegin.");
                }
                parentNode.insertBefore(node, this);
                break;
            case "afterbegin":
                Node firstChild = getFirstChild();
                insertBefore(node, firstChild);
                break;
            case "beforeend":
                appendChild(node);
                break;
            case "afterend":
                if (parentNode == null) {
                    throw new IllegalStateException("The element has no parent. Cannot use afterend.");
                }
                node.parentNode = parentNode;
                List<Node> parentChildNodes = parentNode.getMutableChildNodes();
                parentChildNodes.addAll(parentChildNodes.indexOf(this) + 1, Collections.singletonList(node));
                break;
            default:
                throw new IllegalStateException("The position provided ('" + position + "') is not one of 'beforeBegin', 'afterBegin', 'beforeEnd', or 'afterEnd'.");
        }
        return node;
    }

    public Element insertAdjacentElement(String position, Element element) {
        return (Element) insertAdjacentNode(position, element);
    }

    public void insertAdjacentText(String position, String text) {
        insertAdjacentNode(position, new Text(text));
    }

    public void insertAdjacentHTML(String position, String text) {
        Objects.requireNonNull(position, "position must be not null");
        position = position.toLowerCase(Locale.ROOT);
    	Parser parser = new Parser();
    	Node parentNode = getParentNode();

        switch (position) {
            case "beforebegin":
                if (parentNode == null) {
                    throw new IllegalStateException("The element has no parent. Cannot use beforebegin.");
                }
                for (Node node : parser.parseFragment((Element) parentNode, text)) {
                    parentNode.insertBefore(node, this);
                }
                break;
            case "afterbegin":
                Node firstChild = getFirstChild();
                for (Node node : parser.parseFragment(this, text)) {
                    insertBefore(node, firstChild);
                }
                break;
            case "beforeend":
                for (Node node : parser.parseFragment(this, text)) {
                	appendChild(node);
                }
                break;
            case "afterend":
                if (parentNode == null) {
                    throw new IllegalStateException("The element has no parent. Cannot use afterend.");
                }
                List<Node> newNodeList = parser.parseFragment((Element) parentNode, text);
                for (Node node: newNodeList) {
                    node.parentNode = parentNode;
                }
                List<Node> parentChildNodes = parentNode.getMutableChildNodes();
                parentChildNodes.addAll(parentChildNodes.indexOf(this) + 1, newNodeList);
                break;
            default:
                throw new IllegalStateException("The position provided ('" + position + "') is not one of 'beforeBegin', 'afterBegin', 'beforeEnd', or 'afterEnd'.");
        }
    }

    public void setInnerHTML(String html) {
        setInnerHtml(new Parser().parseFragment(this, html));
    }

    public Attributes getAttributes() {
        ensureAttributesPresence();
        return attributes;
    }

    /**
     * Get the attribute value. Return null if the attribute is not present.
     * 
     * Case-insensitive.
     * 
     * @param name
     * @return
     */
    public String getAttribute(String name) {
        return attributes != null ? attributes.getNamedItem(Common.convertToAsciiLowerCase(name)) : null;
    }

    /**
     * Get the attribute node.
     * 
     * @param name
     * @return
     */
    public AttributeNode getAttributeNode(String name) {
        return attributes != null ? attributes.get(Common.convertToAsciiLowerCase(name)) : null;
    }

    /**
     * Set the attribute name to the given value.
     * 
     * The name will be converted to lowercase.
     * 
     * @param name
     * @param value
     */
    public void setAttribute(String name, String value) {
        ensureAttributesPresence();
        attributes.put(Common.convertToAsciiLowerCase(name), value);
    }

    /**
     * Remove the attributed with the given name.
     * 
     * Case-insensitive.
     * 
     * @param name
     */
    public void removeAttribute(String name) {
        if (attributes != null) {
            attributes.remove(Common.convertToAsciiLowerCase(name));
        }
    }

    /**
     * See {@link #getNodeName()}.
     */
    @Override
    public String toString() {
        return nodeName;
    }

    /**
     * Get a space separated list of class names.
     */
    public String getClassName() {
        return String.join(" ", getClassList());
    }

    /**
     * Get a list of class names. Changes on this list is reflected on the class
     * attribute.
     */
    public DOMTokenList getClassList() {
        return new DOMTokenList(this, "class");
    }

    /**
     * Get the element id, or null if not defined.
     * 
     * @return
     */
    public String getId() {
        return getAttribute("id");
    }

    /**
     * Set the element id
     * 
     * @param id
     */
    public void setId(String id) {
        getAttributes().put("id", id);
    }

    /**
     * Return true if it has at least one attribute.
     * 
     * @return
     */
    public boolean hasAttributes() {
        return attributes != null && !attributes.isEmpty();
    }

    /**
     * Return true if the Element has an attribute with the name. The comparison
     * is case-insensitive.
     * 
     * @param name
     * @return
     */
    public boolean hasAttribute(String name) {
        return attributes != null && attributes.containsKey(Common.convertToAsciiLowerCase(name));
    }

	@Override
	public Node cloneNode(boolean deep) {
        Element clone = new Element(nodeName, nodeNameID, originalNodeName, namespaceURI, namespaceID, attributes == null ? null : attributes.copy());
        if (!deep) {
            return clone;
        }
        if (childNodes != null) {
            clone.childNodes = new ArrayList<>(childNodes.size());
            for (Node node : childNodes) {
                Node clonedChild = node.cloneNode(true);
                clonedChild.parentNode = clone;
                clone.childNodes.add(clonedChild);
            }
        }
        return clone;
	}

    @Override
    public boolean isEqualNode(Node other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Element otherElement) {
            var count = getChildCount();
            var equalityCheck = Objects.equals(getNodeName(), otherElement.getNodeName()) &&
                    Objects.equals(getNamespaceURI(), otherElement.getNamespaceURI()) &&
                    Objects.equals(count, otherElement.getChildCount()) &&
                    Objects.equals(getAttributes(), otherElement.getAttributes());

            if (!equalityCheck) {
                return false;
            }

            for (var i = 0; i < count; i++) {
                if (!Node.nodesEquals(childNodes.get(i), otherElement.childNodes.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Get the html content of the child of this node.
     */
    public String getInnerHTML() {
        return getInnerHTML(EnumSet.noneOf(Option.class));
    }

    /**
     *
     * Get the html content of the child of this node.
     *
     * @param options
     *            serialization {@link Option}.
     * @return
     */
    public String getInnerHTML(Set<Option> options) {
        StringBuilder sb = new StringBuilder();
        traverse(new HtmlSerializer(sb, options));
        return sb.toString();
    }

    /**
     * Get the html content of this node and his child.
     */
    public String getOuterHTML() {
        return getOuterHTML(EnumSet.noneOf(Option.class));
    }

    /**
     * Get the html content of the child of this node.
     *
     * @param options
     *            serialization {@link Option}.
     * @return
     */
    public String getOuterHTML(Set<Option> options) {
        StringBuilder sb = new StringBuilder();
        traverseWithCurrentNode(new HtmlSerializer(sb, options));
        return sb.toString();
    }


    /**
     * Return the tag name. See https://developer.mozilla.org/en-US/docs/Web/API/Element/tagName.
     *
     * @return
     */
    public String getTagName() {
        if (Node.NAMESPACE_HTML_ID == namespaceID) {
            return nodeName.toUpperCase(Locale.ENGLISH);
        } else {
            return originalNodeName;
        }
    }
}
