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

import java.util.Objects;

public final class ProcessingInstruction extends Node {

    private String target;
    private String data;

    public ProcessingInstruction(String target, String data) {
        this.target = target;
        this.data = data;
    }

    public String getTarget() {
        return target;
    }

    public String getData() {
        return data;
    }

    @Override
    public int getNodeType() {
        return PROCESSING_INSTRUCTION_NODE;
    }

    @Override
    public String getNodeName() {
        return target;
    }

    @Override
    public Node cloneNode(boolean deep) {
        return new ProcessingInstruction(target, data);
    }

    @Override
    public boolean isEqualNode(Node other) {
        return this == other || (other instanceof ProcessingInstruction pi && Objects.equals(target, pi.target) && Objects.equals(data, pi.data));
    }
}
