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

public class AttributeNode {

    String name;
    String originalName;

    private String value;
    private ResizableCharBuilder valueBuilder;

    //
    String prefix;
    String namespace;
    //

    // double/single/not quoted
    int attributeQuoteType = TokenizerState.ATTRIBUTE_VALUE_DOUBLE_QUOTED_STATE;

    public AttributeNode(String name, String value) {
        this.name = name;
        this.value = value;
    }

    AttributeNode(String name, String originalName, ResizableCharBuilder value, int attributeQuoteType) {
        this.name = name;
        this.valueBuilder = value;
        this.originalName = originalName;
        this.attributeQuoteType = attributeQuoteType;
    }

    AttributeNode(AttributeNode a) {
        this.name = a.name;
        this.originalName = a.originalName;
        this.value = a.value;
        this.valueBuilder = a.valueBuilder;
        this.prefix = a.prefix;
        this.namespace = a.namespace;
        this.attributeQuoteType = a.attributeQuoteType;
    }

    public AttributeNode(String name, String value, String prefix, String namespace) {
        this(name, value);
        this.prefix = prefix;
        this.namespace = namespace;
    }

    public String getValue() {
        if (value == null && valueBuilder != null) {
            value = valueBuilder.asString();
            valueBuilder = null;
        }
        return value;
    }
    
    public void setValue(String value) {
        this.valueBuilder = null;
        this.value = value;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, getValue(), prefix, namespace);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof AttributeNode) {
            AttributeNode other = (AttributeNode) obj;
            return name.equals(other.name) && //
                    getValue().equals(other.getValue()) && //
                    Objects.equals(prefix, other.prefix) && //
                    Objects.equals(namespace, other.namespace);
        } else {
            return false;
        }
    }
}
