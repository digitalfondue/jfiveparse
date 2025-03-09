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
