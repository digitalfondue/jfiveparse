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

import java.util.ArrayList;

class TreeConstructorActiveFormattingElements {

    final ArrayList<Element> activeFormattingElements = new ArrayList<>();
    private final TreeConstructor treeConstructor;
    Element bookmark;

    private static final class Marker extends Element {
        Marker() {
            super(null, null);
        }
    }

    private static final class Bookmark extends Element {
        Bookmark() {
            super(null, null);
        }
    }

    //
    TreeConstructorActiveFormattingElements(TreeConstructor treeConstructor) {
        this.treeConstructor = treeConstructor;
    }

    void insertMarker() {
        activeFormattingElements.add(new Marker());
    }

    void insertBookmark(Element formattingElement) {
        Bookmark b = new Bookmark();
        activeFormattingElements.add(activeFormattingElements.lastIndexOf(formattingElement), b);
        this.bookmark = b;
    }

    void removeBookmark() {
        activeFormattingElements.remove(bookmark);
        this.bookmark = null;
    }

    void moveBookmarkAfter(Element newNode) {
        activeFormattingElements.remove(bookmark);
        activeFormattingElements.add(activeFormattingElements.lastIndexOf(newNode) + 1, bookmark);
    }

    void push(Element element) {

        int lastMarkerPositionOrStart = 0;
        for (int i = activeFormattingElements.size() - 1; i >= 0; i--) {
            if (activeFormattingElements.get(i) instanceof Marker) {
                lastMarkerPositionOrStart = i;
                break;
            }
        }

        int sameElementCount = 0;
        final int size = activeFormattingElements.size();
        int sameElementPosition = -1;

        String elementName = element.getNodeName();
        String elementNS = element.getNamespaceURI();
        Attributes elementAttrs = element.getAttributes();

        // TODO: not optimal at all :D

        for (int i = lastMarkerPositionOrStart; i < size; i++) {
            Element current = activeFormattingElements.get(i);
            if (!(current instanceof Marker) && //
                    elementName.equals(current.getNodeName()) && //
                    elementNS.equals(current.getNamespaceURI()) && //
                    elementAttrs.equals(current.getAttributes())) {
                sameElementCount++;
                if (sameElementPosition == -1) {
                    sameElementPosition = i;
                }
            }
        }

        if (sameElementCount >= 3) {
            activeFormattingElements.remove(sameElementPosition);
        }

        activeFormattingElements.add(element);
    }

    // TODO: not optimal at all :D
    void reconstruct() {

        // 1
        if (activeFormattingElements.isEmpty()) {
            return;
        }

        // 2
        final int lastIndex = activeFormattingElements.size() - 1;
        Element last = activeFormattingElements.get(lastIndex);
        final boolean lastIsMarker = last instanceof Marker;
        if (lastIsMarker || (!lastIsMarker && treeConstructor.openElementsIndexOf(last) != -1)) {
            return;
        }

        reconstructionPhase(lastIndex, last);
    }

    private void reconstructionPhase(final int lastIndex, Element last) {
        // 3
        Element entry = last;
        int entryIndex = lastIndex;

        while (true) {
            // 4 rewind
            // If there are no entries before entry in the list of active
            // formatting elements, then jump to the step labeled create.
            if (entryIndex <= 0) {
                break;
            }

            // 5
            // Let entry be the entry one earlier than entry in the list of
            // active formatting elements.
            entryIndex--;
            entry = activeFormattingElements.get(entryIndex);

            final boolean entryIsMarker = entry instanceof Marker;

            // 6
            // If entry is neither a marker nor an element that is also in the
            // stack of open elements, go to the step labeled rewind.
            if (!entryIsMarker && treeConstructor.openElementsIndexOf(entry) == -1) {
                continue;
            }

            // 7
            // Advance: Let entry be the element one later than entry in the
            // list of active formatting elements.
            entryIndex++;
            entry = activeFormattingElements.get(entryIndex);
            break;

        }

        while (true) {

            // 8
            // Create: Insert an HTML element for the token for which the
            // element entry was created, to obtain new element.
            Element newElement = treeConstructor.insertElementToken(entry.getNodeName(), entry.getNamespaceURI(), entry.getAttributes().copy());

            // 9
            // Replace the entry for entry in the list with an entry for new
            // element.
            activeFormattingElements.set(entryIndex, newElement);

            // 10
            // If the entry for new element in the list of active formatting
            // elements is not the last entry in the list, return to the step
            // labeled advance.
            if (lastIndex != entryIndex) {
                // 7
                entryIndex++;
                entry = activeFormattingElements.get(entryIndex);
                continue;
            }
            break;
        }
    }

    void clearUpToLastMarker() {
        while (!activeFormattingElements.isEmpty()) {
            Element e = activeFormattingElements.remove(activeFormattingElements.size() - 1);
            if (e instanceof Marker) {
                break;
            }
        }
    }

    Element getElementAtIndex(int idx) {
        return activeFormattingElements.get(idx);
    }

    int getBetweenLastElementAndMarkerIndex(String tagName) {
        for (int i = activeFormattingElements.size() - 1; i >= 0; i--) {
            Element e = activeFormattingElements.get(i);
            if (e instanceof Marker) {
                return -1;
            } else if (tagName.equals(e.getNodeName())) {
                return i;
            }
        }
        return -1;
    }

    int getBetweenLastElementAndMarkerIndex(String tagName, String namespace) {
        for (int i = activeFormattingElements.size() - 1; i >= 0; i--) {
            Element e = activeFormattingElements.get(i);
            if (e instanceof Marker) {
                return -1;
            } else if (e.is(tagName, namespace)) {
                return i;
            }
        }
        return -1;
    }

    int indexOf(Element e) {
        return activeFormattingElements.lastIndexOf(e);
    }

    void set(int idx, Element element) {
        activeFormattingElements.set(idx, element);
    }

    boolean contains(Element e) {
        return activeFormattingElements.lastIndexOf(e) != -1;
    }

    void removeAtIndex(int index) {
        activeFormattingElements.remove(index);
    }

    void remove(Element e) {
        int idx = activeFormattingElements.lastIndexOf(e);
        if (idx != -1) {
            activeFormattingElements.remove(idx);
        }
    }

    void replace(Element node, Element elem) {
        int idx = activeFormattingElements.lastIndexOf(node);
        activeFormattingElements.set(idx, elem);
    }

}
