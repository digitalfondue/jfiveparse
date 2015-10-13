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

public class NodeMatchers implements NodesVisitor {

    private final NodeMatcher matcher;
    private final List<Node> toAdd;

    public NodeMatchers(NodeMatcher matcher, List<Node> toAdd) {
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

        public ElementHasTagName(String tagName) {
            this.tagName = tagName;
        }

        @Override
        public boolean match(Node node) {
            return node.getNodeType() == Node.ELEMENT_NODE && tagName.equals(((Element) node).getNodeName());
        }

    }

    public static NodeMatcher element() {
        return new NodeHasType(Node.ELEMENT_NODE);
    }

    public static NodeMatcher text() {
        return new NodeHasType(Node.TEXT_NODE);
    }

    @Override
    public void start(Node node) {
        if (matcher.match(node)) {
            toAdd.add(node);
        }
    }

    @Override
    public void end(Node node) {
    }
}
