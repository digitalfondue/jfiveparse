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
import java.util.List;

class TreeConstructor {

    // optimization: when adding a new character, the char builder where the
    // character has been appended is saved in this property: it can then be
    // used in the tokenizer for appending directly, bypassing the dispatch
    // logic
    private ResizableCharBuilder insertCharacterPreviousTextNode;
    //

    // token type
    static final int TT_CHARACTER = 0;
    static final int TT_COMMENT = 1;
    static final int TT_DOCTYPE = 2;
    static final int TT_EOF = 3;
    static final int TT_END_TAG = 4;
    static final int TT_START_TAG = 5;

    //
    boolean scriptingFlag;

    private Tokenizer tokenizer;

    private int insertionMode = IM_INITIAL;
    private int originalInsertionMode;

    private final ArrayList<Element> openElements = new ArrayList<>();

    final TreeConstructorActiveFormattingElements activeFormattingElements = new TreeConstructorActiveFormattingElements(this);

    private final ArrayList<Integer> stackTemplatesInsertionMode = new ArrayList<>();

    private final Document document = new Document();

    final boolean disableIgnoreTokenInBodyStartTag;
    final boolean interpretSelfClosingAnythingElse;
    final boolean disableInTableTextForsterParenting;

    // ----
    private int tokenType;
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
    private int tagNameID;
    private String originalTagName;
    private boolean selfClosing;
    private Attributes attrs;
    // --- ---
    private Element head;
    private Element form;
    Element context;
    private Boolean framesetOk;
    boolean isHtmlFragmentParsing;
    private boolean fosterParentingEnabled;
    // --- ----

    // when textarea is present, if the first character is LF, it must be
    // skipped
    private boolean ignoreCharacterTokenLF;
    //
    private final ResizableCharBuilder pendingTableCharactersToken = new ResizableCharBuilder();
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

    TreeConstructor(boolean disableIgnoreTokenInBodyStartTag, boolean interpretSelfClosingAnythingElse, boolean disableInTableTextForsterParenting) {
        this.disableIgnoreTokenInBodyStartTag = disableIgnoreTokenInBodyStartTag;
        this.interpretSelfClosingAnythingElse = interpretSelfClosingAnythingElse;
        this.disableInTableTextForsterParenting = disableInTableTextForsterParenting;
    }

    private void setTagNameAndSaveOriginal(ResizableCharBuilder rawTagName) {
        setTagName(rawTagName.toLowerCase());
        this.originalTagName = rawTagName.containsUpperCase ? rawTagName.toString() : this.tagName;
    }

    void setTagName(String lowerCasedTagName) {
        this.tagName = lowerCasedTagName;
        this.tagNameID = Common.tagNameToID(lowerCasedTagName);
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
            if (tokenType == TT_CHARACTER && chr == Characters.LF) {
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
            TreeConstructorInBodyForeignContentText.foreignContent(tokenType, tagName, tagNameID, this);
        }
    }

    private boolean checkIsInHtmlContent() {
        if (openElements.isEmpty()) {
            return true;
        }
        Element adjustedCurrentNode = getAdjustedCurrentNode();
        return (adjustedCurrentNode != null && (Node.NAMESPACE_HTML_ID == adjustedCurrentNode.namespaceID || checkIsInHtmlContentSVGMathML(adjustedCurrentNode)))
                || tokenType == TT_EOF;
    }

    private boolean checkIsInHtmlContentSVGMathML(Element adjustedCurrentNode) {
        return (Common.isMathMLIntegrationPoint(adjustedCurrentNode) && ((tokenType == TT_START_TAG && (!"mglyph".equals(tagName) && !"malignmark".equals(tagName))) || tokenType == TT_CHARACTER))
                || //
                (("annotation-xml".equals(adjustedCurrentNode.getNodeName()) && Node.NAMESPACE_MATHML_ID == adjustedCurrentNode.namespaceID) && tokenType == TT_START_TAG && "svg"
                        .equals(tagName)) || //
                (Common.isHtmlIntegrationPoint(adjustedCurrentNode) && (tokenType == TT_START_TAG || tokenType == TT_CHARACTER));
    }

    void insertionModeInHtmlContent() {

        // most used
        switch (insertionMode) {
        case IM_TEXT:
            TreeConstructorInBodyForeignContentText.text(tokenType, this);
            break;
        case IM_IN_BODY:
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_CELL:
            TreeConstructorInTable.inCell(tokenType, tagName, tagNameID, this);
            break;
        // end most used
        default:
            insertionModeInHtmlContentAll();
            break;
        }
    }

    private void insertionModeInHtmlContentAll() {
        switch (insertionMode) {
        case IM_INITIAL:
            TreeConstructorAftersBeforeInitialInHead.initial(tokenType, this);
            break;
        case IM_BEFORE_HTML:
            TreeConstructorAftersBeforeInitialInHead.beforeHtml(tokenType, tagName, tagNameID, this);
            break;
        case IM_BEFORE_HEAD:
            TreeConstructorAftersBeforeInitialInHead.beforeHead(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_HEAD:
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_HEAD_NOSCRIPT:
            TreeConstructorAftersBeforeInitialInHead.inHeadNoScript(tokenType, tagName, tagNameID, this);
            break;
        case IM_AFTER_HEAD:
            TreeConstructorAftersBeforeInitialInHead.afterHead(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_BODY:
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, this);
            break;
        case IM_TEXT:
            TreeConstructorInBodyForeignContentText.text(tokenType, this);
            break;
        case IM_IN_TABLE:
            TreeConstructorInTable.inTable(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_TABLE_TEXT:
            TreeConstructorInTable.inTableText(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_CAPTION:
            TreeConstructorInTable.inCaption(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_COLUMN_GROUP:
            TreeConstructorInTable.inColumnGroup(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_TABLE_BODY:
            TreeConstructorInTable.inTableBody(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_ROW:
            TreeConstructorInTable.inRow(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_CELL:
            TreeConstructorInTable.inCell(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_TEMPLATE:
            TreeConstructorInFramesetSelectTemplate.inTemplate(tokenType, tagName, tagNameID, this);
            break;
        case IM_AFTER_BODY:
            TreeConstructorAftersBeforeInitialInHead.afterBody(tokenType, tagName, tagNameID, this);
            break;
        case IM_IN_FRAMESET:
            TreeConstructorInFramesetSelectTemplate.inFrameset(tokenType, tagName, tagNameID, this);
            break;
        case IM_AFTER_FRAMESET:
            TreeConstructorAftersBeforeInitialInHead.afterFrameset(tokenType, tagName, tagNameID, this);
            break;
        case IM_AFTER_AFTER_BODY:
            TreeConstructorAftersBeforeInitialInHead.afterAfterBody(tokenType, tagName, tagNameID, this);
            break;
        case IM_AFTER_AFTER_FRAMESET:
            TreeConstructorAftersBeforeInitialInHead.afterAfterFrameset(tokenType, tagName, tagNameID, this);
            break;
        }
    }

    void stopParsing() {
        throw new StopParse();
    }

    void closePElement() {
        generateImpliedEndTag("p", Node.NAMESPACE_HTML);

        Element e = getCurrentNode();
        if (!(Common.isHtmlNS(e, Common.ELEMENT_P_ID))) {
            emitParseError();
        }

        popOpenElementsUntilWithHtmlNS(Common.ELEMENT_P_ID);
    }

    Element getCurrentNode() {
        return openElements.get(openElements.size() - 1);
    }

    void generateImpliedEndTag() {
        generateImpliedEndTag(null, null);
    }

    // ------------

    void generateImpliedEndTag(String excludeTagName, String excludeNamespace) {
        for (;;) {
            Element currentNode = getCurrentNode();
            final boolean check = Common.isImpliedTag(currentNode) && //
                    ((excludeTagName == null) || //
                    (!(currentNode.getNodeName().equals(excludeTagName) && currentNode.getNamespaceURI().equals(excludeNamespace))));
            if (check) {
                popCurrentNode();
            } else {
                break;
            }
        }
    }

    // ------------

    boolean hasElementInScope(int tagNameID) {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element node = openElements.get(i);
            if (Common.isHtmlNS(node, tagNameID)) {
                return true;
            } else if (Common.isInCommonInScope(node)) {
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
            } else if (Common.isInCommonInScope(node)) {
                return false;
            }
        }
        return false;
    }

    boolean hasElementInButtonScope(int tagNameID) {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element node = openElements.get(i);
            if (Common.isHtmlNS(node, tagNameID)) {
                return true;
            } else if (Common.isInCommonInScope(node) || Common.isHtmlNS(node, Common.ELEMENT_BUTTON_ID)) {
                return false;
            }
        }
        return false;
    }

    boolean hasLiElementInListScope() {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element node = openElements.get(i);
            if (Common.isHtmlNS(node, Common.ELEMENT_LI_ID)) {
                return true;
            } else if (Common.isInCommonInScope(node) || Common.isHtmlNS(node, Common.ELEMENT_OL_ID) || Common.isHtmlNS(node, Common.ELEMENT_UL_ID)) {
                return false;
            }
        }
        return false;
    }

    boolean hasElementInTableScope(int tagNameID) {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element node = openElements.get(i);
            if (Common.isHtmlNS(node, tagNameID)) {
                return true;
            } else if ((Common.isHtmlNS(node, Common.ELEMENT_HTML_ID) || Common.isHtmlNS(node, Common.ELEMENT_TABLE_ID) || Common.isHtmlNS(node, Common.ELEMENT_TEMPLATE_ID))) {
                return false;
            }
        }
        return false;
    }

    // implementation of
    // https://html.spec.whatwg.org/multipage/syntax.html#adoption-agency-algorithm
    void adoptionAgencyAlgorithm(int subjectID) {
        Element current = getCurrentNode();
        // 1
        if (Common.isHtmlNS(current, subjectID) && !activeFormattingElements.activeFormattingElements.contains(current)) {
            popCurrentNode();
            return;
        }

        // 2
        int outerLoopCounter = 0;

        while (outerLoopCounter < 8) { // 3



            // 4
            outerLoopCounter++;

            // 5
            final int formattingElementIdx = activeFormattingElements.getBetweenLastElementAndMarkerIndex(subjectID);

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
                activeFormattingElements.activeFormattingElements.remove(formattingElementIdx);
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
                activeFormattingElements.activeFormattingElements.remove(formattingElementIdx);
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
                if (innerLoopCounter > 3 && activeFormattingElements.activeFormattingElements.contains(node)) {
                    activeFormattingElements.remove(node);
                }

                // 13.6
                if (!activeFormattingElements.activeFormattingElements.contains(node)) {
                    nodeBefore = openElements.get(openElements.lastIndexOf(node) - 1);
                    continue;
                }

                // 13.7
                Element newElement = buildElement(node.nodeName, node.nodeNameID, node.originalNodeName, node.namespaceURI, node.namespaceID, node.getAttributes().copy());
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
                position = toInsert.getRawChildNodes().indexOf(place[1]);
            }
            toInsert.insertChildren(position, lastNode);

            // 15
            Element elem = buildElement(
                    formattingElement.getNodeName(),
                    formattingElement.nodeNameID,
                    formattingElement.originalNodeName,
                    formattingElement.getNamespaceURI(),
                    formattingElement.namespaceID,
                    formattingElement.getAttributes().copy()
            );

            // 16
            List<Node> childs = new ArrayList<>(furthestBlock.getRawChildNodes());
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
            if (Common.isSpecialCategory(currentOpenElement)) {
                furthestBlock = currentOpenElement;
            }

            if (currentOpenElement == formattingElement && furthestBlock != null) {
                return furthestBlock;
            }
        }

        return null;
    }

    static void genericRawTextElementParsing(TreeConstructor treeConstructor) {
        treeConstructor.insertHtmlElementToken();
        treeConstructor.tokenizer.setState(TokenizerState.RAWTEXT_STATE);
        treeConstructor.originalInsertionMode = treeConstructor.insertionMode;
        treeConstructor.insertionMode = IM_TEXT;
    }

    static void genericRCDataParsing(TreeConstructor treeConstructor) {
        treeConstructor.insertHtmlElementToken();
        treeConstructor.tokenizer.setState(TokenizerState.RCDATA_STATE);
        treeConstructor.originalInsertionMode = treeConstructor.insertionMode;
        treeConstructor.insertionMode = IM_TEXT;
    }

    void ackSelfClosingTagIfSet() {
        // TODO: check if useful
    }

    void insertCharacter() {
        insertCharacter(chr);
    }

    static Element buildElement(String name, int nameID, String originalName, String namespace, int namespaceID, Attributes attrs) {
        return new Element(name, nameID, originalName, namespace, namespaceID, attrs);
    }

    Element insertElementToken(String name, int nameId, String namespace, int nameSpaceID, Attributes attrs) {
        Element element = buildElement(name, nameId, name, namespace, nameSpaceID, attrs);
        return insertHtmlElementToken(element);
    }

    Element insertHtmlElementWithEmptyAttributes(String name, int nameID) {
        Element element = buildElement(name, nameID, name, Node.NAMESPACE_HTML, Node.NAMESPACE_HTML_ID, null);
        return insertHtmlElementToken(element);
    }

    Element insertHtmlElementToken() {
        Element element = buildElement(tagName, tagNameID, originalTagName, Node.NAMESPACE_HTML, Node.NAMESPACE_HTML_ID, attrs);
        return insertHtmlElementToken(element);
    }

    // ------------------

    // appropriate place for inserting a node
    Node[] findAppropriatePlaceForInsertingNode(Element overrideTarget) {
        Element target = overrideTarget != null ? overrideTarget : getCurrentNode();
        int targetNameID = target.nodeNameID;
        if (fosterParentingEnabled && (Node.NAMESPACE_HTML_ID == target.namespaceID && (
                Common.ELEMENT_TABLE_ID == targetNameID || //
                Common.ELEMENT_TBODY_ID == targetNameID || //
                Common.ELEMENT_TFOOT_ID == targetNameID || //
                Common.ELEMENT_THEAD_ID == targetNameID || //
                Common.ELEMENT_TR_ID == targetNameID
        ))) {

            // 1
            int lastTemplatePos = findLastElementPositionMatchingInNamespaceHtml(Common.ELEMENT_TEMPLATE_ID);
            // 2
            int lastTablePos = findLastElementPositionMatchingInNamespaceHtml(Common.ELEMENT_TABLE_ID);
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

    private int findLastElementPositionMatchingInNamespaceHtml(int nameID) {
        for (int i = openElements.size() - 1; i >= 0; i--) {
            Element e = openElements.get(i);
            if (Common.isHtmlNS(e, nameID)) {
                return i;
            }
        }
        return -1;
    }

    boolean stackOfOpenElementsContainsElementTemplateAndNamespaceHtml() {
        return findLastElementPositionMatchingInNamespaceHtml(Common.ELEMENT_TEMPLATE_ID) != -1;
    }

    // FIXME optimize(?)
    void insertCharacter(char charToInsert) {
        Node toInsert;
        List<Node> nodes;
        int position;


        if (!fosterParentingEnabled) {
            toInsert = getCurrentNode();
            nodes = toInsert.getRawChildNodes();
            position = nodes.size();
        } else {
            Node[] place = findAppropriatePlaceForInsertingNode(null);
            if (place[1] == null) { // insert as a last child
                toInsert = place[0];
                nodes = toInsert.getRawChildNodes();
                position = nodes.size();
            } else { // insert before
                toInsert = place[0];
                nodes = toInsert.getRawChildNodes();
                position = nodes.indexOf(place[1]);
            }
        }

        Text t;

        if (!nodes.isEmpty() && position > 0 && (nodes.get(position - 1)) instanceof Text lastText) {
            t = lastText;
            t.dataBuilder.append(charToInsert);
        } else {
            t = new Text(new ResizableCharBuilder());
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
                position = toInsert.getRawChildNodes().indexOf(place[1]);
            }

        } else {
            var size = openElements.size();
            if (size == 0) {
                // drop element
                return element;
            }
            toInsert = openElements.get(size - 1);
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
                position = toInsert.getRawChildNodes().indexOf(place[1]);
            }
        } else {
            toInsert = openElements.get(openElements.size() - 1);
            position = toInsert.getChildCount();
        }
        toInsert.insertChildren(position, new Comment(comment));
    }

    void insertCommentToDocument() {
        document.appendChild(new Comment(comment));
    }

    void insertCommentToHtmlElement() {
        openElements.get(0).appendChild(new Comment(comment));
    }

    // ------------------

    Element popCurrentNode() {
        return openElements.remove(openElements.size() - 1);
    }

    void popOpenElementsUntilWithHtmlNS(int nameID) {
        while (true) {
            Element e = popCurrentNode();
            if (Common.isHtmlNS(e, nameID)) {
                return;
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
        tokenType = TT_CHARACTER;
        insertCharacterPreviousTextNode = null;
        this.chr = chr;
        dispatch();
    }

    void emitComment(ResizableCharBuilder comment) {
        this.comment = comment;
        tokenType = TT_COMMENT;
        dispatch();
    }

    void emitDoctypeToken(StringBuilder doctypeName, StringBuilder doctypePublicId, StringBuilder doctypeSystemId, boolean correctness) {
        this.doctypeName = doctypeName;
        this.doctypePublicId = doctypePublicId;
        this.doctypeSystemId = doctypeSystemId;
        this.correctness = correctness;
        tokenType = TT_DOCTYPE;
        dispatch();
    }

    void emitEOF() {
        tokenType = TT_EOF;
        dispatch();
        throw new StopParse();
    }

    void emitEndTagToken(ResizableCharBuilder name) {
        setTagName(name.toLowerCase());
        tokenType = TT_END_TAG;
        dispatch();
    }

    void emitStartTagToken(ResizableCharBuilder name, Attributes attrs, boolean selfClosing) {
        setTagNameAndSaveOriginal(name);
        this.attrs = attrs;
        this.selfClosing = selfClosing;
        tokenType = TT_START_TAG;
        dispatch();
    }

    Document getDocument() {
        return document;
    }

    // see https://html.spec.whatwg.org/multipage/parsing.html#reset-the-insertion-mode-appropriately
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

            if ((Common.isHtmlNS(node, Common.ELEMENT_TD_ID) || Common.isHtmlNS(node, Common.ELEMENT_TH_ID)) && !last) {
                insertionMode = IM_IN_CELL;
                break;
            } else if (Common.isHtmlNS(node, Common.ELEMENT_TR_ID)) {
                insertionMode = IM_IN_ROW;
                break;
            } else if (Common.isHtmlNS(node, Common.ELEMENT_TBODY_ID) || Common.isHtmlNS(node, Common.ELEMENT_THEAD_ID) || Common.isHtmlNS(node, Common.ELEMENT_TFOOT_ID)) {
                insertionMode = IM_IN_TABLE_BODY;
                break;
            } else if (Common.isHtmlNS(node, Common.ELEMENT_CAPTION_ID)) {
                insertionMode = IM_IN_CAPTION;
                break;
            } else if (Common.isHtmlNS(node, Common.ELEMENT_COLGROUP_ID)) {
                insertionMode = IM_IN_COLUMN_GROUP;
                break;
            } else if (Common.isHtmlNS(node, Common.ELEMENT_TABLE_ID)) {
                insertionMode = IM_IN_TABLE;
                break;
            } else if (Common.isHtmlNS(node, Common.ELEMENT_TEMPLATE_ID)) {
                insertionMode = stackTemplatesInsertionMode.get(stackTemplatesInsertionMode.size() - 1);
                break;
            } else if (Common.isHtmlNS(node, Common.ELEMENT_HEAD_ID)) {
                insertionMode = IM_IN_HEAD;
                break;
            } else if (Common.isHtmlNS(node, Common.ELEMENT_BODY_ID)) {
                insertionMode = IM_IN_BODY;
                break;
            } else if (Common.isHtmlNS(node, Common.ELEMENT_FRAMESET_ID)) {
                insertionMode = IM_IN_FRAMESET;
                break;
            } else if (Common.isHtmlNS(node, Common.ELEMENT_HTML_ID)) {
                if (head == null) {
                    insertionMode = IM_BEFORE_HEAD;
                } else {
                    insertionMode = IM_AFTER_HEAD;
                }
                break;
            } else if (last) {
                insertionMode = IM_IN_BODY;
                break;
            }
            counter--;
            node = openElements.get(counter);
        }
    }

    int getTokenType() {
        return tokenType;
    }

    void setTokenType(int tokenType) {
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
        pendingTableCharactersToken.reset();
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

    Boolean getFramesetOk() {
        return framesetOk;
    }

    void framesetOkToFalse() {
        framesetOk = Boolean.FALSE;
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
        return attrs != null && attrs.containsKey(key);
    }

    AttributeNode getAttribute(String key) {
        return attrs.get(key);
    }

    Attributes getAttributes() {
        return attrs;
    }

    void removeAttributes() {
        attrs = null;
    }

    // insertion modes
    static final int IM_TEXT = 0;
    static final int IM_IN_BODY = 1;
    static final int IM_IN_CELL = 2;
    static final int IM_INITIAL = 3;
    static final int IM_BEFORE_HTML = 4;
    static final int IM_BEFORE_HEAD = 5;
    static final int IM_IN_HEAD = 6;
    static final int IM_IN_HEAD_NOSCRIPT = 7;
    static final int IM_AFTER_HEAD = 8;
    static final int IM_IN_TABLE = 9;
    static final int IM_IN_TABLE_TEXT = 10;
    static final int IM_IN_CAPTION = 11;
    static final int IM_IN_COLUMN_GROUP = 12;
    static final int IM_IN_TABLE_BODY = 13;
    static final int IM_IN_ROW = 14;
    static final int IM_IN_TEMPLATE = 15;
    static final int IM_AFTER_BODY = 16;
    static final int IM_IN_FRAMESET = 17;
    static final int IM_AFTER_FRAMESET = 18;
    static final int IM_AFTER_AFTER_BODY = 19;
    static final int IM_AFTER_AFTER_FRAMESET = 20;
    //

}
