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

import java.util.function.BiPredicate;
import java.util.stream.Stream;

class NodeMatchers<T extends SelectableNode<T>> implements BaseNodesVisitor<T> {

    private final BiPredicate<T, T> matcher;
    private final T baseNode;
    private Stream.Builder<T> toAdd;
    private Stream<T> singleOrEmpty;
    private final boolean completeOnFirstMatch;

    NodeMatchers(BiPredicate<T, T> matcher, boolean completeOnFirstMatch, T baseNode) {
        this.matcher = matcher;
        this.completeOnFirstMatch = completeOnFirstMatch;
        this.baseNode = baseNode;
    }

    @Override
    public void start(T node) {
        if (matcher.test(node, baseNode)) {
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
        return completeOnFirstMatch && singleOrEmpty != null;
    }

    Stream<T> result() {
        if (toAdd != null) {
            return toAdd.build();
        } else {
            return singleOrEmpty == null ? Stream.empty() : singleOrEmpty;
        }
    }
}
