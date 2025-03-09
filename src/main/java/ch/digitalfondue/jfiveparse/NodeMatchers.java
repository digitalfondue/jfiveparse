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

import java.util.List;

class NodeMatchers<T extends Node> implements NodesVisitor {

    private final NodeMatcher matcher;
    private final List<T> toAdd;
    private final boolean completeOnFirstMatch;

    NodeMatchers(NodeMatcher matcher, List<T> toAdd, boolean completeOnFirstMatch) {
        this.matcher = matcher;
        this.toAdd = toAdd;
        this.completeOnFirstMatch = completeOnFirstMatch;
    }

    /**
     * Equivalent of [attr=value]
     */
    static final byte ATTRIBUTE_MATCH_VALUE_EQ = 0;

    /**
     * Equivalent of [attr~=value]
     */
    static final byte ATTRIBUTE_MATCH_VALUE_IN_LIST = 1;

    /**
     * Equivalent of [attr^=value]
     */
    static final byte ATTRIBUTE_MATCH_VALUE_START_WITH = 2;

    /**
     * Equivalent of [attr$=value]
     */
    static final byte ATTRIBUTE_MATCH_VALUE_END_WITH = 3;

    /**
     * Equivalent of [attr*=value]
     */
    static final byte ATTRIBUTE_MATCH_VALUE_CONTAINS = 4;

    static class HasAttribute implements NodeMatcher {
        private final byte matchType;
        private final String name;
        private final String value;

        HasAttribute(String name, String value, byte matchType) {
            this.name = name;
            this.value = value;
            this.matchType = matchType;
        }

        HasAttribute(String name) {
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
                String attrValue = elem.getAttribute(name);
                switch (matchType) {
                case ATTRIBUTE_MATCH_VALUE_EQ:
                    return value == null || value.equals(attrValue);
                case ATTRIBUTE_MATCH_VALUE_IN_LIST:
                    return new DOMTokenList(elem, name).contains(value);
                case ATTRIBUTE_MATCH_VALUE_START_WITH:
                    return attrValue != null && value != null && attrValue.startsWith(value);
                case ATTRIBUTE_MATCH_VALUE_END_WITH:
                    return attrValue != null && value != null && attrValue.endsWith(value);
                case ATTRIBUTE_MATCH_VALUE_CONTAINS:
                    return attrValue != null && value != null && attrValue.contains(value);
                default:
                    return false;
                }
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

    @Override
    public boolean complete() {
        return completeOnFirstMatch && !toAdd.isEmpty();
    }
}
