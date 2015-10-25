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

    /**
     * Equivalent of [attr=value]
     */
    public static final byte ATTRIBUTE_MATCH_VALUE_EQ = 0;

    /**
     * Equivalent of [attr~=value]
     */
    public static final byte ATTRIBUTE_MATCH_VALUE_IN_LIST = 1;

    /**
     * Equivalent of [attr^=value]
     */
    public static final byte ATTRIBUTE_MATCH_VALUE_START_WITH = 2;

    /**
     * Equivalent of [attr$=value]
     */
    public static final byte ATTRIBUTE_MATCH_VALUE_END_WITH = 3;

    /**
     * Equivalent of [attr*=value]
     */
    public static final byte ATTRIBUTE_MATCH_VALUE_CONTAINS = 4;

    public static class HasAttribute implements NodeMatcher {
        private final byte matchType;
        private final String name;
        private final String value;

        public HasAttribute(String name, String value, byte matchType) {
            this.name = name;
            this.value = value;
            this.matchType = matchType;
        }

        public HasAttribute(String name) {
            this.name = name;
            this.value = null;
            this.matchType = ATTRIBUTE_MATCH_VALUE_EQ;
        }

        @Override
        public boolean match(Node node) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                return false;
            }

            Element elem = (Element) node;

            if (!elem.getAttributes().containsKey(name)) {
                return false;
            } else {
                switch (matchType) {
                case ATTRIBUTE_MATCH_VALUE_EQ:
                    return value == null || value.equals(elem.getAttributes().get(name).getValue());
                case ATTRIBUTE_MATCH_VALUE_IN_LIST:
                    return new DOMTokenList(elem, name).contains(value);
                case ATTRIBUTE_MATCH_VALUE_START_WITH: {
                    String attrValue = elem.getAttribute(name);
                    return attrValue != null && attrValue.startsWith(value);
                }
                case ATTRIBUTE_MATCH_VALUE_END_WITH: {
                    String attrValue = elem.getAttribute(name);
                    return attrValue != null && attrValue.endsWith(value);
                }
                case ATTRIBUTE_MATCH_VALUE_CONTAINS: {
                    String attrValue = elem.getAttribute(name);
                    return attrValue != null && attrValue.indexOf(value) >= 0;
                }
                default:
                    return false;
                }

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
