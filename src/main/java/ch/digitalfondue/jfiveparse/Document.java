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
public final class Document extends Node {
    private final List<Node> childNodes = new ArrayList<>(2);
    private DocumentType doctype;

    @Override
    public int getNodeType() {
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

    @Override
    List<Node> getRawChildNodes() {
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

    private Element getChildOfDocumentElementMatching(int nameId1, int nameId2) {
        Element e = getFirstElementChild();
        if (e != null) {
            for (Node c : e.getRawChildNodes()) {
                if (c instanceof Element ce && NAMESPACE_HTML_ID == ce.namespaceID && (ce.nodeNameID == nameId1 || ce.nodeNameID == nameId2)) {
                    return ce;
                }
            }
        }
        return null;
    }

    /**
     * @return the "head" element if present
     */
    public Element getHead() {
        return getChildOfDocumentElementMatching(Common.ELEMENT_HEAD_ID, Common.ELEMENT_HEAD_ID);
    }

    /**
     * @return return the "body" or "frameset" element if present
     */
    public Element getBody() {
        return getChildOfDocumentElementMatching(Common.ELEMENT_BODY_ID, Common.ELEMENT_FRAMESET_ID);
    }

    /**
     * Replace the current body. TODO: need to check if it's a body|frameset element in html namespace, handle the case when there is no body too
     *
     * @param element
     */
    public void setBody(Element element) {
        Element e = getBody();
        if (element != null && e != null && e.parentNode != null) {
            e.parentNode.replaceChild(element, e);
        }
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

    @Override
    public boolean isEqualNode(Node other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Document otherDocument) {
            if (!Node.nodesEquals(doctype, otherDocument.doctype)) {
                return false;
            }
            int count = getChildCount();
            if (count != otherDocument.getChildCount()) {
                return false;
            }
            for (var i = 0; i < count; i++) {
                if (!Node.nodesEquals(childNodes.get(i), otherDocument.childNodes.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
