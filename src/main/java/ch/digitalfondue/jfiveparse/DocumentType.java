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

/**
 * Represent a document type.
 */
public class DocumentType extends Node {

    private final String name;
    private final String publicId;
    private final String systemId;

    public DocumentType(String name, String publicId, String systemId) {
        this.name = name;
        this.publicId = publicId;
        this.systemId = systemId;
    }

    @Override
    public byte getNodeType() {
        return DOCUMENT_TYPE_NODE;
    }

    public String getName() {
        return name;
    }

    public String getPublicId() {
        return publicId;
    }

    public String getSystemId() {
        return systemId;
    }

    /**
     * Return the {@link String} "#doctype".
     */
    @Override
    public String getNodeName() {
        return "#doctype";
    }

	@Override
	public Node cloneNode(boolean deep) {
		return new DocumentType(getName(), getPublicId(), getSystemId());
	}

    @Override
    public boolean isEqualNode(Node other) {
        if (this == other) {
            return true;
        }
        if (other instanceof DocumentType) {
            DocumentType otherDocType = (DocumentType) other;
            return Objects.equals(name, otherDocType.name) &&
                    Objects.equals(publicId, otherDocType.publicId) &&
                    Objects.equals(systemId, otherDocType.systemId);
        }
        return false;
    }
}
