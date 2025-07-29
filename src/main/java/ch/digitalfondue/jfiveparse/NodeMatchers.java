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

import java.util.stream.Stream;

class NodeMatchers<T extends CommonNode> implements NodesVisitor<T> {

    private final NodeMatcher matcher;
    private Stream.Builder<T> toAdd;
    private Stream<T> singleOrEmpty = Stream.empty();
    private final boolean completeOnFirstMatch;
    private boolean matched;

    NodeMatchers(NodeMatcher matcher, boolean completeOnFirstMatch) {
        this.matcher = matcher;
        this.completeOnFirstMatch = completeOnFirstMatch;
    }

    @Override
    public void start(T node) {
        if (matcher.match(node)) {
            matched = true;
            if (completeOnFirstMatch) {
                singleOrEmpty = Stream.of(node);
            } else {
                if (toAdd == null) {
                    toAdd = Stream.builder();
                }
                toAdd.accept(node);
            }
        }
    }

    @Override
    public boolean complete() {
        return completeOnFirstMatch && matched;
    }

    Stream<T> result() {
        return toAdd == null ? singleOrEmpty : toAdd.build();
    }
}
