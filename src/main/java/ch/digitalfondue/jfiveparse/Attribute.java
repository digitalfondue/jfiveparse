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

import java.util.Objects;

public class Attribute {

    String name;
    String value;

    //
    String prefix;
    String namespace;

    //

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Attribute(String name, String value, String prefix, String namespace) {
        this(name, value);
        this.prefix = prefix;
        this.namespace = namespace;
    }

    public String getValue() {
        return value;
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
        return Objects.hash(name, value, prefix, namespace);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Attribute) {
            Attribute other = (Attribute) obj;
            return name.equals(other.name) && //
                    value.equals(other.value) && //
                    Objects.equals(prefix, other.prefix) && //
                    Objects.equals(namespace, other.namespace);
        } else {
            return false;
        }
    }
}
