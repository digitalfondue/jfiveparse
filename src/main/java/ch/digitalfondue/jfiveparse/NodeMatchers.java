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

import java.util.List;

public class NodeMatchers<T extends Node> implements NodesVisitor {

    private final NodeMatcher matcher;
    private final List<T> toAdd;

    public NodeMatchers(NodeMatcher matcher, List<T> toAdd) {
        this.matcher = matcher;
        this.toAdd = toAdd;
    }

    public interface NodeMatcher {
        boolean match(Node node);
    }

    public static class NodeHasType implements NodeMatcher {

        private final int type;

        public NodeHasType(int type) {
            this.type = type;
        }

        @Override
        public boolean match(Node node) {
            return node.getNodeType() == type;
        }
    }

    public static class ElementHasTagName implements NodeMatcher {
        private final String tagName;
        private final String nameSpace;

        public ElementHasTagName(String tagName) {
            this.tagName = tagName;
            this.nameSpace = null;
        }

        public ElementHasTagName(String tagName, String namespace) {
            this.tagName = tagName;
            this.nameSpace = namespace;
        }

        @Override
        public boolean match(Node node) {
            return node.getNodeType() == Node.ELEMENT_NODE && tagName.equals(((Element) node).getNodeName())
                    && (nameSpace == null ? true : nameSpace.equals(((Element) node).getNamespaceURI()));
        }

    }

    public static class HasAttribute implements NodeMatcher {
        private final String name;
        private final String value;

        public HasAttribute(String name) {
            this(name, null);
        }

        public HasAttribute(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean match(Node node) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                return false;
            }

            Element elem = (Element) node;

            if (!elem.getAttributes().containsKey(name)) {
                return false;
            } else if (value == null) {
                return true;
            } else {
                return value.equals(elem.getAttributes().get(name).getValue());
            }
        }

    }

    public static class IsLastChild implements NodeMatcher {
        @Override
        public boolean match(Node node) {
            if (node.parentNode != null) {
                return node.parentNode.getLastChild() == node;
            } else {
                return false;
            }
        }
    }

    public static class IsFirstChild implements NodeMatcher {
        @Override
        public boolean match(Node node) {
            if (node.parentNode != null) {
                return node.parentNode.getFirstChild() == node;
            } else {
                return false;
            }
        }
    }

    public static class IsFirstElementChild implements NodeMatcher {
        @Override
        public boolean match(Node node) {
            if (node.parentNode != null) {
                return node.parentNode.getFirstElementChild() == node;
            } else {
                return false;
            }
        }
    }

    public static class IsLastElementChild implements NodeMatcher {
        @Override
        public boolean match(Node node) {
            if (node.parentNode != null) {
                return node.parentNode.getLastElementChild() == node;
            } else {
                return false;
            }
        }
    }

    public static NodeMatcher element() {
        return new NodeHasType(Node.ELEMENT_NODE);
    }

    public static NodeMatcher text() {
        return new NodeHasType(Node.TEXT_NODE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(Node node) {
        if (matcher.match(node)) {
            toAdd.add((T) node);
        }
    }

    @Override
    public void end(Node node) {
    }
}
