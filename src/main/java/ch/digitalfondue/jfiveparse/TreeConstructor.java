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
import java.util.List;
import java.util.Set;

class TreeConstructor {

    // optimization: when adding a new character, the char builder where the
    // character has been appended is saved in this property: it can then be
    // used in the tokenizer for appending directly, bypassing the dispatch
    // logic
    private ResizableCharBuilder insertCharacterPreviousTextNode;
    //

    static final byte CHARACTER = 0;
    static final byte COMMENT = 1;
    static final byte DOCTYPE = 2;
    static final byte EOF = 3;
    static final byte END_TAG = 4;
    static final byte START_TAG = 5;

    //
    private boolean scriptingFlag;

    private Tokenizer tokenizer;

    private int insertionMode = TreeConstructionInsertionMode.INITIAL;
    private int originalInsertionMode;

    private final ArrayList<Element> openElements = new ArrayList<>();

    private final TreeConstructorActiveFormattingElements activeFormattingElements = new TreeConstructorActiveFormattingElements(this);

    private final ArrayList<Integer> stackTemplatesInsertionMode = new ArrayList<>();

    private final Document document = new Document();

    // ----
    private byte tokenType;
    // --- token values ---
    // --- character ---
    private char chr;
    // --- ---
    // --- comment---
    private ResizableCharBuilder comment;
    // --- ---

    // ---doctype related ---
    private StringBuilder doctypeName;
    private StringBuilder doctypePublicId;
    private StringBuilder doctypeSystemId;

    @SuppressWarnings("unused")
    private boolean correctness; // TODO set in the doctype node
    // --- ---

    // --- tag related ---
    private String tagName;
    private String originalTagName;
    private boolean selfClosing;
    private Attributes attrs;
    // --- ---
    private Element head;
    private Element form;
    private Element context;
    private Boolean framesetOk;
    private boolean isHtmlFragmentParsing;
    private boolean fosterParentingEnabled;
    // --- ----

    // when textarea is present, if the first character is LF, it must be
    // skipped
    private boolean ignoreCharacterTokenLF;
    //
    private ResizableCharBuilder pendingTableCharactersToken;
    //
    private boolean quirksMode;
    private boolean inHtmlContent;

    // ----

    void setTokenizerState(int state) {
        tokenizer.setState(state);
    }

    void setTokenizer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    private void setTagNameAndSaveOriginal(ResizableCharBuilder rawTagName) {
        setTagName(rawTagName);
        this.originalTagName = rawTagName.containsUpperCase ? rawTagName.asString() : this.tagName;
    }

    void setTagName(ResizableCharBuilder rawTagName) {
        String tagName = rawTagName.toLowerCase();
        String maybeCached = Common.ELEMENTS_NAME_CACHE_V2.get(tagName);
        this.tagName = maybeCached != null ? maybeCached : tagName;
    }

    //

    Element getAdjustedCurrentNode() {
        final int size = openElements.size();
        if (size == 0) {
            return null;
        } else if (isHtmlFragmentParsing && size == 1) {
            return context;
        } else {
            return openElements.get(size - 1);
        }
    }

    //

    void dispatch() {
        // handle textarea "skip LF" logic
        if (ignoreCharacterTokenLF) {
            ignoreCharacterTokenLF = false;
            if (tokenType == CHARACTER && chr == Characters.LF) {
                return;
            }
        }
        //

        inHtmlContent = checkIsInHtmlContent();

        // 12.2.5
        if (inHtmlContent) {
            // -> html
            insertionModeInHtmlContent();
        } else {
            // -> foreign
            TreeConstructorInBodyForeignContentText.foreignContent(tokenType, tagName, this);
        }
    }

    private boolean checkIsInHtmlContent() {
        Element adjustedCurrentNode = getAdjustedCurrentNode();
        return openElements.isEmpty()
                || (adjustedCurrentNode != null && (Node.NAMESPACE_HTML.equals(adjustedCurrentNode.getNamespaceURI()) || checkIsInHtmlContentSVGMathML(adjustedCurrentNode)))
                || tokenType == EOF;
    }

    private boolean checkIsInHtmlContentSVGMathML(Element adjustedCurrentNode) {
        return (Common.isMathMLIntegrationPoint(adjustedCurrentNode) && ((tokenType == START_TAG && (!"mglyph".equals(tagName) && !"malignmark".equals(tagName))) || tokenType == CHARACTER))
                || //
                (("annotation-xml".equals(adjustedCurrentNode.getNodeName()) && Node.NAMESPACE_MATHML.equals(adjustedCurrentNode.getNamespaceURI())) && tokenType == START_TAG && "svg"
                        .equals(tagName)) || //
                (Common.isHtmlIntegrationPoint(adjustedCurrentNode) && (tokenType == START_TAG || tokenType == CHARACTER));
    }

    void insertionModeInHtmlContent() {

        // most used
        switch (insertionMode) {
        case TreeConstructionInsertionMode.TEXT:
            TreeConstructorInBodyForeignContentText.text(tokenType, this);
            break;
        case TreeConstructionInsertionMode.IN_BODY:
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_CELL:
            TreeConstructorInTable.inCell(tokenType, tagName, this);
            break;
        // end most used
        default:
            insertionModeInHtmlContentAll();
            break;
        }
    }

    private void insertionModeInHtmlContentAll() {
        switch (insertionMode) {
        case TreeConstructionInsertionMode.INITIAL:
            TreeConstructorAftersBeforeInitialInHead.initial(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.BEFORE_HTML:
            TreeConstructorAftersBeforeInitialInHead.beforeHtml(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.BEFORE_HEAD:
            TreeConstructorAftersBeforeInitialInHead.beforeHead(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_HEAD:
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_HEAD_NOSCRIPT:
            TreeConstructorAftersBeforeInitialInHead.inHeadNoScript(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.AFTER_HEAD:
            TreeConstructorAftersBeforeInitialInHead.afterHead(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_BODY:
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.TEXT:
            TreeConstructorInBodyForeignContentText.text(tokenType, this);
            break;
        case TreeConstructionInsertionMode.IN_TABLE:
            TreeConstructorInTable.inTable(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_TABLE_TEXT:
            TreeConstructorInTable.inTableText(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_CAPTION:
            TreeConstructorInTable.inCaption(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_COLUMN_GROUP:
            TreeConstructorInTable.inColumnGroup(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_TABLE_BODY:
            TreeConstructorInTable.inTableBody(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_ROW:
            TreeConstructorInTable.inRow(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_CELL:
            TreeConstructorInTable.inCell(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_SELECT:
            TreeConstructorInSelect.inSelect(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_SELECT_IN_TABLE:
            TreeConstructorInSelect.inSelectTable(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_TEMPLATE:
            TreeConstructorInTemplate.inTemplate(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.AFTER_BODY:
            TreeConstructorAftersBeforeInitialInHead.afterBody(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.IN_FRAMESET:
            TreeConstructorInFrameset.inFrameset(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.AFTER_FRAMESET:
            TreeConstructorAftersBeforeInitialInHead.afterFrameset(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.AFTER_AFTER_BODY:
            TreeConstructorAftersBeforeInitialInHead.afterAfterBody(tokenType, tagName, this);
            break;
        case TreeConstructionInsertionMode.AFTER_AFTER_FRAMESET:
            TreeConstructorAftersBeforeInitialInHead.afterAfterFrameset(tokenType, tagName, this);
            break;
        }
    }

    void stopParsing() {
        throw new StopParse();
    }

    void closePElement() {
        generateImpliedEndTag("p", Node.NAMESPACE_HTML);

        Element e = getCurrentNode();
        if (!("p".equals(e.getNodeName()) && Node.NAMESPACE_HTML.equals(e.getNamespaceURI()))) {
            emitParseError();
        }

        popOpenElementsUntil("p", Node.NAMESPACE_HTML);
    }

    Element getCurrentNode() {
        return openElements.get(openElements.size() - 1);
    }

    void generateImpliedEndTag() {
        generateImpliedEndTag(null, null);
    }

    // ------------

    void generateImpliedEndTag(String excludeTagName, String excludeNameSpace) {
        for (;;) {
            Element currentNode = getCurrentNode();
            final boolean check = Common.isImpliedTag(currentNode) && //
                    ((excludeTagName == null) || //
                    (!(currentNode.getNodeName().equals(excludeTagName) && currentNode.getNamespaceURI().equals(excludeNameSpace))));
            if (check) {
                popCurrentNode();
            } else {
                break;
            }
        }
    }

    // ------------

    boolean hasElementInScope(String tagName) {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element node = openElements.get(i);
            String nodeName = node.getNodeName();
            String nodeNameSpace = node.getNamespaceURI();
            if (nodeName.equals(tagName) && nodeNameSpace.equals(Node.NAMESPACE_HTML)) {
                return true;
            } else if (Common.isInCommonInScope(node.getNodeName(), node.getNamespaceURI())) {
                return false;
            }
        }
        return false;
    }

    boolean hasElementInScope(Element e) {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element node = openElements.get(i);
            if (node == e) { // same reference
                return true;
            } else if (Common.isInCommonInScope(node.getNodeName(), node.getNamespaceURI())) {
                return false;
            }
        }
        return false;
    }

    boolean hasElementInButtonScope(String tagName) {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element node = openElements.get(i);
            String nodeName = node.getNodeName();
            String nodeNameSpace = node.getNamespaceURI();
            if (nodeName.equals(tagName) && nodeNameSpace.equals(Node.NAMESPACE_HTML)) {
                return true;
            } else if (Common.isInCommonInScope(nodeName, nodeNameSpace) || (nodeName.equals("button") && nodeNameSpace.equals(Node.NAMESPACE_HTML))) {
                return false;
            }
        }
        return false;
    }

    boolean hasElementInListScope(String tagName) {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element node = openElements.get(i);
            String nodeName = node.getNodeName();
            String nodeNameSpace = node.getNamespaceURI();
            if (nodeName.equals(tagName) && nodeNameSpace.equals(Node.NAMESPACE_HTML)) {
                return true;
            } else if (Common.isInCommonInScope(node.getNodeName(), node.getNamespaceURI()) || node.is("ol", Node.NAMESPACE_HTML) || node.is("ul", Node.NAMESPACE_HTML)) {
                return false;
            }
        }
        return false;
    }

    boolean hasElementInTableScope(String tagName) {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element node = openElements.get(i);
            String nodeName = node.getNodeName();
            String nodeNameSpace = node.getNamespaceURI();
            if (nodeName.equals(tagName) && nodeNameSpace.equals(Node.NAMESPACE_HTML)) {
                return true;
            } else if ((node.is("html", Node.NAMESPACE_HTML) || node.is("table", Node.NAMESPACE_HTML) || node.is("template", Node.NAMESPACE_HTML))) {
                return false;
            }
        }
        return false;
    }

    boolean hasElementInSelectScope(String tagName) {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element node = openElements.get(i);
            String nodeName = node.getNodeName();
            String nodeNameSpace = node.getNamespaceURI();
            if (nodeName.equals(tagName) && nodeNameSpace.equals(Node.NAMESPACE_HTML)) {
                return true;
            }

            if (!node.is("option", Node.NAMESPACE_HTML) && !node.is("optgroup", Node.NAMESPACE_HTML)) {
                return false;
            }
        }
        return true;
    }

    // implementation of
    // https://html.spec.whatwg.org/multipage/syntax.html#adoption-agency-algorithm
    void adoptionAgencyAlgorithm(String subject) {
        Element current = getCurrentNode();
        // 1
        if (current.is(subject, Node.NAMESPACE_HTML) && !activeFormattingElements.contains(current)) {
            popCurrentNode();
            return;
        }

        // 2
        int outerLoopCounter = 0;

        while (true) {

            // 3
            if (outerLoopCounter >= 8) {
                break;
            }

            // 4
            outerLoopCounter++;

            // 5
            final int formattingElementIdx = activeFormattingElements.getBetweenLastElementAndMarkerIndex(subject);

            // no such element
            if (formattingElementIdx == -1) {
                // any other end tag
                TreeConstructorInBodyForeignContentText.anyOtherEndTag(this.tagName, this);
                return;
            }

            // 6
            final Element formattingElement = activeFormattingElements.getElementAtIndex(formattingElementIdx);
            if (openElements.lastIndexOf(formattingElement) == -1) {
                emitParseError();
                activeFormattingElements.removeAtIndex(formattingElementIdx);
                return;
            }

            // 7
            if (!hasElementInScope(formattingElement)) {
                emitParseError();
                return;
            }

            // 8
            if (formattingElement != getCurrentNode()) {
                emitParseError();
            }

            // 9
            Element furthestBlock = getFurthestBlock(formattingElement);

            // 10
            if (furthestBlock == null) {
                while (true) {
                    Element e = popCurrentNode();
                    if (e == formattingElement) {
                        break;
                    }
                }
                activeFormattingElements.removeAtIndex(formattingElementIdx);
                return;
            }

            // 11
            Element commonAncestor = openElements.get(openElements.lastIndexOf(formattingElement) - 1);

            // 12 Bookmark
            activeFormattingElements.insertBookmark(formattingElement);

            // 13
            Element node = furthestBlock;
            Element lastNode = furthestBlock;
            // 13.1
            int innerLoopCounter = 0;

            Element nodeBefore = null;

            while (true) {

                // 13.2
                innerLoopCounter++;

                // 13.3
                final int nodeIdx = openElements.lastIndexOf(node);
                if (nodeIdx != -1) {
                    node = openElements.get(nodeIdx - 1);
                } else {
                    node = nodeBefore;
                }

                // 13.4
                if (node == formattingElement) {
                    break;
                }

                // 13.5
                if (innerLoopCounter > 3 && activeFormattingElements.contains(node)) {
                    activeFormattingElements.remove(node);
                }

                // 13.6
                if (!activeFormattingElements.contains(node)) {
                    nodeBefore = openElements.get(openElements.lastIndexOf(node) - 1);
                    continue;
                }

                // 13.7
                Element newElement = buildElement(node.getNodeName(), node.originalNodeName, node.getNamespaceURI(), node.getAttributes().copy());
                commonAncestor.appendChild(newElement);
                activeFormattingElements.replace(node, newElement);
                openElements.set(openElements.lastIndexOf(node), newElement);
                node = newElement;

                // 13.8
                if (lastNode == furthestBlock) {
                    activeFormattingElements.moveBookmarkAfter(newElement);
                }

                // 13.9
                node.appendChild(lastNode);

                // 13.10
                lastNode = node;

                // 13.11 -> inner loop
            }

            // 14
            Node[] place = findAppropriatePlaceForInsertingNode(commonAncestor);
            Node toInsert;
            int position;
            if (place[1] == null) { // insert as a last child
                toInsert = place[0];
                position = toInsert.getChildCount();
            } else {
                toInsert = place[0];
                // insert before
                position = toInsert.getChildNodes().indexOf(place[1]);
            }
            toInsert.insertChildren(position, lastNode);

            // 15
            Element elem = buildElement(formattingElement.getNodeName(), formattingElement.originalNodeName, formattingElement.getNamespaceURI(), formattingElement.getAttributes()
                    .copy());

            // 16
            List<Node> childs = new ArrayList<>(furthestBlock.getChildNodes());
            furthestBlock.empty();
            for (Node n : childs) {
                elem.appendChild(n);
            }

            // 17
            furthestBlock.appendChild(elem);

            // 18
            activeFormattingElements.remove(formattingElement);
            activeFormattingElements.activeFormattingElements.set(activeFormattingElements.indexOf(activeFormattingElements.bookmark), elem);
            activeFormattingElements.removeBookmark();

            // 19
            openElements.remove(formattingElement);
            openElements.add(openElements.lastIndexOf(furthestBlock) + 1, elem);

            // 20 -> outer loop
        }
    }

    // Get "the topmost node in the stack of open elements that is lower in the
    // stack than formatting element, and is an element in the special category.
    // There might not be one."
    private Element getFurthestBlock(Element formattingElement) {

        Element furthestBlock = null;

        for (int idx = openElements.size() - 1; idx >= 0; idx--) {
            Element currentOpenElement = openElements.get(idx);
            if (Common.isSpecialCategory(currentOpenElement.getNodeName(), currentOpenElement.getNamespaceURI())) {
                furthestBlock = currentOpenElement;
            }

            if (currentOpenElement == formattingElement && furthestBlock != null) {
                return furthestBlock;
            }
        }

        return null;
    }

    static Attributes emptyAttrs() {
        return null;
    }

    static void genericRawTextElementParsing(TreeConstructor treeConstructor) {
        treeConstructor.insertHtmlElementToken();
        treeConstructor.tokenizer.setState(TokenizerState.RAWTEXT_STATE);
        treeConstructor.originalInsertionMode = treeConstructor.insertionMode;
        treeConstructor.insertionMode = TreeConstructionInsertionMode.TEXT;
    }

    static void genericRCDataParsing(TreeConstructor treeConstructor) {
        treeConstructor.insertHtmlElementToken();
        treeConstructor.tokenizer.setState(TokenizerState.RCDATA_STATE);
        treeConstructor.originalInsertionMode = treeConstructor.insertionMode;
        treeConstructor.insertionMode = TreeConstructionInsertionMode.TEXT;
    }

    void ackSelfClosingTagIfSet() {
        // TODO: check if useful
    }

    //
    void insertCharacters(char[] chars, int pos) {
        for (int i = 0; i < pos; i++) {
            insertCharacter(chars[i]);
        }
    }

    void insertCharacter() {
        insertCharacter(chr);
    }

    static Element buildElement(String name, String originalName, String namespace, Attributes attrs) {
        return new Element(name, originalName, namespace, attrs);
    }

    Element insertElementToken(String name, String namespace, Attributes attrs) {
        Element element = buildElement(name, name, namespace, attrs);
        return insertHtmlElementToken(element);
    }

    Element insertHtmlElementWithEmptyAttributes(String name) {
        Element element = buildElement(name, name, Node.NAMESPACE_HTML, emptyAttrs());
        return insertHtmlElementToken(element);
    }

    Element insertHtmlElementToken() {
        Element element = buildElement(tagName, originalTagName, Node.NAMESPACE_HTML, attrs);
        return insertHtmlElementToken(element);
    }

    // ------------------

    // appropriate place for inserting a node
    Node[] findAppropriatePlaceForInsertingNode(Element overrideTarget) {

        Element target = overrideTarget != null ? overrideTarget : getCurrentNode();
        String targetName = target.getNodeName();
        if (fosterParentingEnabled && (Node.NAMESPACE_HTML.equals(target.getNamespaceURI()) && ("table".equals(targetName) || //
                "tbody".equals(targetName) || //
                "tfoot".equals(targetName) || //
                "thead".equals(targetName) || //
                "tr".equals(targetName)))) {

            // 1
            int lastTemplatePos = findLastElementPositionMatching("template", Node.NAMESPACE_HTML);
            // 2
            int lastTablePos = findLastElementPositionMatching("table", Node.NAMESPACE_HTML);
            // 3
            if (lastTemplatePos != -1 && ((lastTablePos == -1) || (lastTemplatePos > lastTablePos))) {
                // inside the template
                return new Node[] { openElements.get(lastTemplatePos), null };
            }
            // 4
            if (lastTablePos == -1) {
                return new Node[] { openElements.get(0), null };
            }
            // 5
            Element lastTable = openElements.get(lastTablePos);
            if (lastTable.getParentNode() != null) {
                return new Node[] { lastTable.getParentNode(), lastTable };
            }
            // 6
            Element previous = openElements.get(lastTablePos - 1);
            // 7
            return new Node[] { previous, null };
        } else {
            return new Node[] { target, null };
        }
    }

    private int findLastElementPositionMatching(String name, String nameSpace) {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element e = openElements.get(i);
            if (e.is(name, nameSpace)) {
                return i;
            }
        }
        return -1;
    }

    boolean stackOfOpenElementsContains(String name, String namespace) {
        return findLastElementPositionMatching(name, namespace) != -1;
    }

    // FIXME optimize(?)
    void insertCharacter(char charToInsert) {
        Node toInsert;
        List<Node> nodes;
        int position;

        Node last;
        if (!fosterParentingEnabled) {
            toInsert = getCurrentNode();
            nodes = toInsert.getChildNodes();
            position = nodes.size();
        } else {
            Node[] place = findAppropriatePlaceForInsertingNode(null);
            if (place[1] == null) { // insert as a last child
                toInsert = place[0];
                nodes = toInsert.getChildNodes();
                position = nodes.size();
            } else { // insert before
                toInsert = place[0];
                nodes = toInsert.getChildNodes();
                position = nodes.indexOf(place[1]);
            }
        }

        Text t;

        if (!nodes.isEmpty() && position > 0 && (last = nodes.get(position - 1)).getNodeType() == Node.TEXT_NODE) {
            t = (Text) last;
            t.dataBuilder.append(charToInsert);
        } else {
            t = new Text();
            t.dataBuilder.append(charToInsert);
            toInsert.insertChildren(position, t);
        }
        // optimization
        insertCharacterPreviousTextNode = t.dataBuilder;
        //
    }

    private Element insertHtmlElementToken(Element element) {
        Node toInsert;
        int position;
        if (fosterParentingEnabled) {
            Node[] place = findAppropriatePlaceForInsertingNode(null);

            if (place[1] == null) { // insert as a last child
                toInsert = place[0];
                position = toInsert.getChildCount();
            } else {
                toInsert = place[0];
                // insert before
                position = toInsert.getChildNodes().indexOf(place[1]);
            }

        } else {
            if (openElements.isEmpty()) {
                // drop element
                return element;
            }
            toInsert = openElements.get(openElements.size() - 1);
            position = toInsert.getChildCount();
        }
        toInsert.insertChildren(position, element);
        openElements.add(element);
        return element;
    }

    void insertComment() {
        Node toInsert;
        int position;
        if (fosterParentingEnabled) {
            Node[] place = findAppropriatePlaceForInsertingNode(null);

            if (place[1] == null) { // insert as a last child
                toInsert = place[0];
                position = toInsert.getChildCount();
            } else {
                toInsert = place[0];
                // insert before
                position = toInsert.getChildNodes().indexOf(place[1]);
            }
        } else {
            toInsert = openElements.get(openElements.size() - 1);
            position = toInsert.getChildCount();
        }
        toInsert.insertChildren(position, new Comment(comment.asString()));
    }

    void insertCommentToDocument() {
        document.appendChild(new Comment(comment.asString()));
    }

    void insertCommentToHtmlElement() {
        openElements.get(0).appendChild(new Comment(comment.asString()));
    }

    // ------------------

    Element popCurrentNode() {
        return openElements.remove(openElements.size() - 1);
    }

    Element popOpenElementsUntil(String name, String nameSpace) {
        while (true) {
            Element e = popCurrentNode();
            if (e.is(name, nameSpace)) {
                return e;
            }
        }
    }

    void pushInStackTemplatesInsertionMode(int insertionMode) {
        stackTemplatesInsertionMode.add(insertionMode);
    }

    boolean isStackTemplatesInsertionModeIsEmpty() {
        return stackTemplatesInsertionMode.isEmpty();
    }

    void popFromStackTemplatesInsertionMode() {
        stackTemplatesInsertionMode.remove(stackTemplatesInsertionMode.size() - 1);
    }

    void emitParseError() {
    }

    void emitCharacter(char chr) {

        tokenType = CHARACTER;
        insertCharacterPreviousTextNode = null;
        this.chr = chr;

        dispatch();
    }

    void emitComment(ResizableCharBuilder comment) {
        this.comment = comment;
        tokenType = COMMENT;
        dispatch();
    }

    void emitDoctypeToken(StringBuilder doctypeName, StringBuilder doctypePublicId, StringBuilder doctypeSystemId, boolean correctness) {
        this.doctypeName = doctypeName;
        this.doctypePublicId = doctypePublicId;
        this.doctypeSystemId = doctypeSystemId;
        this.correctness = correctness;
        tokenType = DOCTYPE;
        dispatch();
    }

    void emitEOF() {
        tokenType = EOF;
        dispatch();
        throw new StopParse();
    }

    void emitEndTagToken(ResizableCharBuilder name) {
        setTagName(name);
        tokenType = END_TAG;
        dispatch();
    }

    void emitStartTagToken(ResizableCharBuilder name, Attributes attrs, boolean selfClosing) {
        setTagNameAndSaveOriginal(name);
        this.attrs = attrs;
        this.selfClosing = selfClosing;
        tokenType = START_TAG;
        dispatch();
    }

    Document getDocument() {
        return document;
    }

    boolean isScriptingFlag() {
        return scriptingFlag;
    }

    void setScriptingFlag(boolean scriptingFlag) {
        this.scriptingFlag = scriptingFlag;
    }

    void resetInsertionModeAppropriately() {
        boolean last = false;
        int counter = openElements.size() - 1;
        Element node = openElements.get(counter);
        while (true) {
            if (node == openElements.get(0)) {
                last = true;
            }
            if (isHtmlFragmentParsing) {
                node = context;
            }

            if (node.is("select", Node.NAMESPACE_HTML)) {
                if (last) {
                    insertionMode = TreeConstructionInsertionMode.IN_SELECT;
                    break;
                } else {
                    int ancestorIdx = counter;
                    Element ancestor = node;
                    while (true) {
                        if (ancestor == openElements.get(0)) {
                            break;
                        }
                        ancestorIdx--;
                        ancestor = openElements.get(ancestorIdx);
                        if (ancestor.is("template", Node.NAMESPACE_HTML)) {
                            break;
                        } else if (ancestor.is("table", Node.NAMESPACE_HTML)) {
                            insertionMode = TreeConstructionInsertionMode.IN_SELECT_IN_TABLE;
                            return;
                        }
                    }
                    insertionMode = TreeConstructionInsertionMode.IN_SELECT;
                    break;
                }
            } else if ((node.is("td", Node.NAMESPACE_HTML) || node.is("th", Node.NAMESPACE_HTML)) && !last) {
                insertionMode = TreeConstructionInsertionMode.IN_CELL;
                break;
            } else if (node.is("tr", Node.NAMESPACE_HTML)) {
                insertionMode = TreeConstructionInsertionMode.IN_ROW;
                break;
            } else if (node.is("tbody", Node.NAMESPACE_HTML) || node.is("thead", Node.NAMESPACE_HTML) || node.is("tfoot", Node.NAMESPACE_HTML)) {
                insertionMode = TreeConstructionInsertionMode.IN_TABLE_BODY;
                break;
            } else if (node.is("caption", Node.NAMESPACE_HTML)) {
                insertionMode = TreeConstructionInsertionMode.IN_CAPTION;
                break;
            } else if (node.is("colgroup", Node.NAMESPACE_HTML)) {
                insertionMode = TreeConstructionInsertionMode.IN_COLUMN_GROUP;
                break;
            } else if (node.is("table", Node.NAMESPACE_HTML)) {
                insertionMode = TreeConstructionInsertionMode.IN_TABLE;
                break;
            } else if (node.is("template", Node.NAMESPACE_HTML)) {
                insertionMode = stackTemplatesInsertionMode.get(stackTemplatesInsertionMode.size() - 1);
                break;
            } else if (node.is("head", Node.NAMESPACE_HTML)) {
                insertionMode = TreeConstructionInsertionMode.IN_HEAD;
                break;
            } else if (node.is("body", Node.NAMESPACE_HTML)) {
                insertionMode = TreeConstructionInsertionMode.IN_BODY;
                break;
            } else if (node.is("frameset", Node.NAMESPACE_HTML)) {
                insertionMode = TreeConstructionInsertionMode.IN_FRAMESET;
                break;
            } else if (node.is("html", Node.NAMESPACE_HTML)) {
                if (head == null) {
                    insertionMode = TreeConstructionInsertionMode.BEFORE_HEAD;
                } else {
                    insertionMode = TreeConstructionInsertionMode.AFTER_HEAD;
                }
                break;
            } else if (last) {
                insertionMode = TreeConstructionInsertionMode.IN_BODY;
                break;
            }
            counter--;
            node = openElements.get(counter);
        }
    }

    byte getTokenType() {
        return tokenType;
    }

    void setTokenType(byte tokenType) {
        this.tokenType = tokenType;
    }

    // ------------------------------------------

    //

    char getChr() {
        return chr;
    }

    void setChr(char chr) {
        this.chr = chr;
    }

    ResizableCharBuilder getInsertCharacterPreviousTextNode() {
        return insertCharacterPreviousTextNode;
    }

    void resetInsertCharacterPreviousTextNode() {
        this.insertCharacterPreviousTextNode = null;
    }

    boolean isInHtmlContent() {
        return inHtmlContent;
    }

    boolean isQuirksMode() {
        return quirksMode;
    }

    void setQuirksMode(boolean quirksMode) {
        this.quirksMode = quirksMode;
    }

    DocumentType buildDocumentType() {
        return new DocumentType(toStringOrEmptyString(doctypeName),//
                toStringOrEmptyString(doctypePublicId),//
                toStringOrEmptyString(doctypeSystemId));
    }

    private static String toStringOrEmptyString(StringBuilder sb) {
        return sb == null ? "" : sb.toString();
    }

    boolean isSelfClosing() {
        return selfClosing;
    }

    void ignoreCharacterTokenLF() {
        this.ignoreCharacterTokenLF = true;
    }

    void pushIntoStackTemplatesInsertionMode(int insMode) {
        stackTemplatesInsertionMode.add(insMode);
    }

    Element getHead() {
        return head;
    }

    void setHead(Element head) {
        this.head = head;
    }

    void enableFosterParenting() {
        fosterParentingEnabled = true;
    }

    void disableFosterParenting() {
        fosterParentingEnabled = false;
    }

    // ----
    void createPendingTableCharactersToken() {
        pendingTableCharactersToken = new ResizableCharBuilder();
    }

    void appendToPendingTableCharactersToken(int chr) {
        pendingTableCharactersToken.append((char) chr);
    }

    ResizableCharBuilder getPendingTableCharactersToken() {
        return pendingTableCharactersToken;
    }

    Element getForm() {
        return form;
    }

    void setForm(Element form) {
        this.form = form;
    }

    void setContext(Element node) {
        this.context = node;
    }

    boolean isHtmlFragmentParsing() {
        return isHtmlFragmentParsing;
    }

    void setHtmlFragmentParsing(boolean isHtmlFragmentParsing) {
        this.isHtmlFragmentParsing = isHtmlFragmentParsing;
    }

    Boolean getFramesetOk() {
        return framesetOk;
    }

    void framesetOkToFalse() {
        framesetOk = false;
    }

    //
    void clearUpToLastMarkerActiveFormattingElements() {
        activeFormattingElements.clearUpToLastMarker();
    }

    void reconstructActiveFormattingElements() {
        activeFormattingElements.reconstruct();
    }

    void insertMarkerInActiveFormattingElements() {
        activeFormattingElements.insertMarker();
    }

    void pushInActiveFormattingElements(Element element) {
        activeFormattingElements.push(element);
    }

    void removeInActiveFormattingElements(Element element) {
        activeFormattingElements.remove(element);
    }

    Element getActiveFormattingElementAt(int idx) {
        return activeFormattingElements.getElementAtIndex(idx);
    }

    int getIndexInActiveFormattingElementsBetween(String name, String namespace) {
        return activeFormattingElements.getBetweenLastElementAndMarkerIndex(name, namespace);
    }

    //

    void saveInsertionMode() {
        originalInsertionMode = insertionMode;
    }

    void switchToOriginalInsertionMode() {
        insertionMode = originalInsertionMode;
    }

    int getInsertionMode() {
        return insertionMode;
    }

    void setInsertionMode(int insertionMode) {
        this.insertionMode = insertionMode;
    }

    //

    int openElementsSize() {
        return openElements.size();
    }

    void removeFromOpenElements(Element e) {
        openElements.remove(e);
    }

    void addToOpenElements(Element e) {
        openElements.add(e);
    }

    Element openElementAt(int idx) {
        return openElements.get(idx);
    }

    int openElementsIndexOf(Element elem) {
        return openElements.lastIndexOf(elem);
    }

    //

    boolean hasAttribute(String key) {
        return attrs.containsKey(key);
    }

    Attribute getAttribute(String key) {
        return attrs.get(key);
    }

    Attributes getAttributes() {
        return attrs;
    }

    void removeAttributes() {
        attrs = null;
    }

    Set<String> getKeySetOfAttributes() {
        return attrs.keySet();
    }

}
