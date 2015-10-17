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
import java.util.List;

public class Element extends Node {

    private String nodeName;
    private String namespaceURI;
    private Attributes attributes;

    private List<Node> childNodes = null;
    
    final boolean selfClosing;

    public Element(String name) {
        this(name, Node.NAMESPACE_HTML);
    }

    public Element(String name, String nameSpace) {
        this(name, nameSpace, null);
    }

    public Element(String name, String nameSpace, Attributes attributes) {
        this(name, nameSpace, attributes, false);
    }
    
    Element(String name, String nameSpace, Attributes attributes, boolean selfClosing) {
        this.nodeName = name;
        this.namespaceURI = nameSpace;
        this.attributes = attributes;
        this.selfClosing = selfClosing;
    }

    private void ensureAttributesPresence() {
        if (attributes == null) {
            this.attributes = new Attributes();
        }
    }

    @Override
    public List<Node> getChildNodes() {
        return childNodes == null ? Collections.<Node>emptyList() : Collections.unmodifiableList(childNodes);
    }

    @Override
    List<Node> getMutableChildNodes() {
        if(childNodes == null) {
            this.childNodes = new ArrayList<>(2);
        }
        return childNodes;
    }

    @Override
    public byte getNodeType() {
        return ELEMENT_NODE;
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    boolean is(String name, String nameSpace) {
        return this.nodeName.equals(name) && this.namespaceURI.equals(nameSpace);
    }

    public Attributes getAttributes() {
        ensureAttributesPresence();
        return attributes;
    }

    @Override
    public String toString() {
        return nodeName;
    }
}
