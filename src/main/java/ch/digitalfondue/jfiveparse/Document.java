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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represent a document.
 */
public class Document extends Node {
    private final List<Node> childNodes = new ArrayList<>(2);
    private DocumentType doctype;

    @Override
    public byte getNodeType() {
        return DOCUMENT_NODE;
    }

    @Override
    public List<Node> getChildNodes() {
        return Collections.unmodifiableList(childNodes);
    }

    @Override
    List<Node> getMutableChildNodes() {
        return childNodes;
    }

    public void setDoctype(DocumentType doctype) {
        this.doctype = doctype;
    }

    public DocumentType getDoctype() {
        return doctype;
    }

    public Element getDocumentElement() {
        return getFirstElementChild();
    }

    /**
     * Return the {@link String} "#document".
     */
    @Override
    public String getNodeName() {
        return "#document";
    }

	@Override
	public Node cloneNode(boolean deep) {
        Document cloned = new Document();
        if (doctype != null) {
            cloned.doctype = (DocumentType) doctype.cloneNode(true);
            cloned.doctype.parentNode = this;
        }
        if (!deep) {
            return cloned;
        }
        for (Node child : childNodes) {
            Node clonedChild = child.cloneNode(true);
            clonedChild.parentNode = cloned;
            cloned.childNodes.add(clonedChild);
        }
        return cloned;
	}
}
