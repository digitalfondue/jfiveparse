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
    private String value;

    // optional fields, only allocated for namespaced/foreign or case-preserved
    // attributes
    private Misc misc;

    // double/single/not quoted
    int attributeQuoteType = TokenizerState.ATTRIBUTE_VALUE_DOUBLE_QUOTED_STATE;

    private static final class Misc {
        // null means: same as name
        String localName;
        // null means: no original case to preserve
        String originalName;
        String prefix;
        String namespace;
    }

    public AttributeNode(String name, String value) {
        this.name = name;
        this.value = value;
    }

    AttributeNode(String name, String originalName, String value, int attributeQuoteType) {
        this.name = name;
        this.value = value;
        this.attributeQuoteType = attributeQuoteType;
        if (!Objects.equals(name, originalName)) {
            misc().originalName = originalName;
        }
    }

    AttributeNode(AttributeNode a) {
        this.name = a.name;
        this.value = a.value;
        this.attributeQuoteType = a.attributeQuoteType;
        if (a.misc != null) {
            var e = new Misc();
            e.localName = a.misc.localName;
            e.originalName = a.misc.originalName;
            e.prefix = a.misc.prefix;
            e.namespace = a.misc.namespace;
            this.misc = e;
        }
    }

    public AttributeNode(String name, String value, String prefix, String namespace) {
        this(name, value);
        String normalizedNamespace = normalizeNamespace(namespace);
        if (prefix != null && !prefix.isEmpty()) {
            if (normalizedNamespace == null) {
                throw new ParserException("A prefix is not allowed when the namespace is null: " + name);
            }
            misc().prefix = prefix;
            if (name.startsWith(prefix + ":")) {
                misc().localName = name.substring(prefix.length() + 1);
            }
        }
        if (normalizedNamespace != null) {
            misc().namespace = normalizedNamespace;
        }
    }

    static AttributeNode createWithNamespace(String namespace, String qualifiedName, String value) {
        namespace = normalizeNamespace(namespace);
        var attribute = new AttributeNode(qualifiedName, value);
        if (namespace != null) {
            attribute.misc().namespace = namespace;
        }
        var separator = qualifiedName.indexOf(':');
        if (separator > 0) {
            if (namespace == null) {
                throw new ParserException("A prefix is not allowed when the namespace is null: " + qualifiedName);
            }
            attribute.misc().prefix = qualifiedName.substring(0, separator);
            attribute.misc().localName = qualifiedName.substring(separator + 1);
        }
        return attribute;
    }

    private Misc misc() {
        if (misc == null) {
            misc = new Misc();
        }
        return misc;
    }

    static String normalizeNamespace(String namespace) {
        return namespace == null || namespace.isEmpty() ? null : namespace;
    }

    boolean matches(String namespace, String localName) {
        return Objects.equals(getNamespace(), namespace) && Objects.equals(getLocalName(), localName);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getNamespace() {
        return misc == null ? null : misc.namespace;
    }

    public String getPrefix() {
        return misc == null ? null : misc.prefix;
    }

    public String getName() {
        return name;
    }

    public String getLocalName() {
        if (misc != null && misc.localName != null) {
            return misc.localName;
        }
        return name;
    }

    String getOriginalName() {
        if (misc != null && misc.originalName != null) {
            return misc.originalName;
        }
        return name;
    }

    int getAttributeQuoteType() {
        return attributeQuoteType;
    }

    void setNameAndLocalName(String name) {
        this.name = name;
        if (misc != null) {
            misc.localName = null;
        }
    }

    void setForeignAttributes(String prefix, String localName, String namespace) {
        var e = misc();
        e.prefix = prefix;
        e.localName = localName;
        e.namespace = namespace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNamespace(), getLocalName(), value);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof AttributeNode other &&
                Objects.equals(getNamespace(), other.getNamespace()) && //
                Objects.equals(getLocalName(), other.getLocalName()) && //
                Objects.equals(value, other.value)
        );
    }
}
