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

public final class AttributeNode {

    // qualified name: "xlink:href"
    String name;
    // local name: "href"
    String localName;
    String originalName;

    private String value;
    //
    String prefix;
    String namespace;
    //

    // double/single/not quoted
    int attributeQuoteType = TokenizerState.ATTRIBUTE_VALUE_DOUBLE_QUOTED_STATE;

    public AttributeNode(String name, String value) {
        this.name = name;
        this.localName = name;
        this.value = value;
    }

    AttributeNode(String name, String originalName, String value, int attributeQuoteType) {
        this.name = name;
        this.localName = name;
        this.value = value;
        this.originalName = originalName;
        this.attributeQuoteType = attributeQuoteType;
    }

    AttributeNode(AttributeNode a) {
        this.name = a.name;
        this.localName = a.localName;
        this.originalName = a.originalName;
        this.value = a.value;
        this.prefix = a.prefix;
        this.namespace = a.namespace;
        this.attributeQuoteType = a.attributeQuoteType;
    }

    public AttributeNode(String name, String value, String prefix, String namespace) {
        this(name, value);
        this.prefix = prefix;
        this.namespace = normalizeNamespace(namespace);
        if (prefix != null && !prefix.isEmpty()) {
            if (this.namespace == null) {
                throw new ParserException("A prefix is not allowed when the namespace is null: " + name);
            }
            if (name.startsWith(prefix + ":")) {
                this.localName = name.substring(prefix.length() + 1);
            }
        }
    }

    static AttributeNode createWithNamespace(String namespace, String qualifiedName, String value) {
        namespace = normalizeNamespace(namespace);
        var attribute = new AttributeNode(qualifiedName, value);
        attribute.namespace = namespace;
        var separator = qualifiedName.indexOf(':');
        if (separator > 0) {
            if (namespace == null) {
                throw new ParserException("A prefix is not allowed when the namespace is null: " + qualifiedName);
            }
            attribute.prefix = qualifiedName.substring(0, separator);
            attribute.localName = qualifiedName.substring(separator + 1);
        }
        return attribute;
    }

    static String normalizeNamespace(String namespace) {
        return namespace == null || namespace.isEmpty() ? null : namespace;
    }

    boolean matches(String namespace, String localName) {
        return Objects.equals(this.namespace, namespace) && Objects.equals(this.localName, localName);
    }

    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
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

    public String getLocalName() {
        return localName;
    }


    @Override
    public int hashCode() {
        return Objects.hash(namespace, localName, value);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof AttributeNode other &&
                Objects.equals(namespace, other.namespace) && //
                Objects.equals(localName, other.localName) && //
                Objects.equals(value, other.value)
        );
    }
}
