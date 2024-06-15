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

import static ch.digitalfondue.jfiveparse.TreeConstructor.CHARACTER;
import static ch.digitalfondue.jfiveparse.TreeConstructor.COMMENT;
import static ch.digitalfondue.jfiveparse.TreeConstructor.DOCTYPE;
import static ch.digitalfondue.jfiveparse.TreeConstructor.END_TAG;
import static ch.digitalfondue.jfiveparse.TreeConstructor.EOF;
import static ch.digitalfondue.jfiveparse.TreeConstructor.START_TAG;

class TreeConstructorInTable {

    static void inTable(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {

        Element currentNodeTop = treeConstructor.getCurrentNode();

        if (tokenType == CHARACTER && (Common.isHtmlNS(currentNodeTop, Common.ELEMENT_TABLE_ID) || //
                Common.isHtmlNS(currentNodeTop, Common.ELEMENT_TBODY_ID) || //
                Common.isHtmlNS(currentNodeTop, Common.ELEMENT_TFOOT_ID) || //
                Common.isHtmlNS(currentNodeTop, Common.ELEMENT_THEAD_ID) || //
                Common.isHtmlNS(currentNodeTop, Common.ELEMENT_TR_ID))) {
            treeConstructor.createPendingTableCharactersToken();
            treeConstructor.saveInsertionMode();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE_TEXT);
            treeConstructor.dispatch();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore token
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_CAPTION_ID, tagNameID)) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertMarkerInActiveFormattingElements();
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_CAPTION);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_COLGROUP_ID, tagNameID)) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_COLUMN_GROUP);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_COL_ID, tagNameID)) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementWithEmptyAttributes("colgroup", Common.ELEMENT_COLGROUP_ID);
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_COLUMN_GROUP);
            treeConstructor.dispatch();
        } else if (tokenType == START_TAG && (Common.ELEMENT_TBODY_ID == tagNameID || //
                Common.ELEMENT_TFOOT_ID == tagNameID || Common.ELEMENT_THEAD_ID == tagNameID)) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE_BODY);
        } else if (tokenType == START_TAG && (
                Common.ELEMENT_TD_ID == tagNameID || //
                Common.ELEMENT_TH_ID == tagNameID ||
                Common.ELEMENT_TR_ID == tagNameID
        )) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementWithEmptyAttributes("tbody", Common.ELEMENT_TBODY_ID);
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE_BODY);
            treeConstructor.dispatch();
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_TABLE_ID, tagNameID)) {
            treeConstructor.emitParseError();

            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TABLE_ID)) {
                // ignore
            } else {
                treeConstructor.popOpenElementsUntilWithHtmlNS(Common.ELEMENT_TABLE_ID);
                treeConstructor.resetInsertionModeAppropriately();
                treeConstructor.dispatch();
            }
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_TABLE_ID, tagNameID)) {
            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TABLE_ID)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popOpenElementsUntilWithHtmlNS(Common.ELEMENT_TABLE_ID);
                treeConstructor.resetInsertionModeAppropriately();
            }
        } else if (tokenType == END_TAG && ("body".equals(tagName) || //
                "caption".equals(tagName) || //
                "col".equals(tagName) || //
                "colgroup".equals(tagName) || //
                "html".equals(tagName) || //
                "tbody".equals(tagName) || //
                "td".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "th".equals(tagName) || //
                "thead".equals(tagName) || //
                "tr".equals(tagName))) {
            // parser error
            // ignore token
        } else if (tokenType == START_TAG && ("style".equals(tagName) || //
                "script".equals(tagName) || //
                "template".equals(tagName))//
                || Common.isEndTagNamed(tokenType, Common.ELEMENT_TEMPLATE_ID, tagNameID)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_INPUT_ID, tagNameID)) {

            boolean hasTypeKey = treeConstructor.hasAttribute("type");
            if (!hasTypeKey || (!"hidden".equalsIgnoreCase(treeConstructor.getAttribute("type").getValue()))) {
                treeConstructor.emitParseError();
                treeConstructor.enableFosterParenting();
                TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
                treeConstructor.disableFosterParenting();
            } else {
                treeConstructor.emitParseError();
                treeConstructor.insertHtmlElementToken();
                treeConstructor.popCurrentNode();
                treeConstructor.ackSelfClosingTagIfSet();
            }
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_FORM_ID, tagNameID)) {
            treeConstructor.emitParseError();
            if (treeConstructor.stackOfOpenElementsContains(Common.ELEMENT_TEMPLATE_ID, Node.NAMESPACE_HTML_ID) || treeConstructor.getForm() != null) {
                // ignore
            } else {
                Element form = treeConstructor.insertHtmlElementToken();
                treeConstructor.setForm(form);
                treeConstructor.popCurrentNode();
            }
        } else if (tokenType == EOF) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else {
            treeConstructor.emitParseError();
            treeConstructor.enableFosterParenting();
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
            treeConstructor.disableFosterParenting();
        }
    }

    private static void cleanStackBackToTableContext(TreeConstructor treeConstructor) {
        while (true) {
            Element e = treeConstructor.getCurrentNode();
            if (Node.NAMESPACE_HTML_ID == e.namespaceID && //
                    ("table".equals(e.getNodeName()) || "template".equals(e.getNodeName()) || "html".equals(e.getNodeName()))) {
                break;
            }
            treeConstructor.popCurrentNode();
        }
    }

    // ----- in table body

    static void inTableBody(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {

        if (Common.isStartTagNamed(tokenType, Common.ELEMENT_TR_ID, tagNameID)) {
            clearStackBackToTableBodyContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_ROW);
        } else if (tokenType == START_TAG && ("th".equals(tagName) || "td".equals(tagName))) {
            treeConstructor.emitParseError();
            clearStackBackToTableBodyContext(treeConstructor);
            treeConstructor.insertHtmlElementWithEmptyAttributes("tr", Common.ELEMENT_TR_ID);
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_ROW);
            treeConstructor.dispatch();
        } else if (tokenType == END_TAG && ("tbody".equals(tagName) || "tfoot".equals(tagName) || "thead".equals(tagName))) {
            if (!treeConstructor.hasElementInTableScope(tagNameID)) { // tbody, tfoot, thead
                treeConstructor.emitParseError();
                // ignore token
            } else {
                clearStackBackToTableBodyContext(treeConstructor);
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE);
            }
        } else if ((tokenType == START_TAG && ("caption".equals(tagName) || "col".equals(tagName) || //
                "colgroup".equals(tagName) || "tbody".equals(tagName) || "tfoot".equals(tagName) || //
                "thead".equals(tagName)))
                || Common.isEndTagNamed(tokenType, Common.ELEMENT_TABLE_ID, tagNameID)) {

            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TBODY_ID) && !treeConstructor.hasElementInTableScope(Common.ELEMENT_TFOOT_ID)
                    && !treeConstructor.hasElementInTableScope(Common.ELEMENT_THEAD_ID)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                clearStackBackToTableBodyContext(treeConstructor);
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE);
                treeConstructor.dispatch();
            }
        } else if (tokenType == END_TAG
                && ("body".equals(tagName) || "caption".equals(tagName) || "col".equals(tagName) || "colgroup".equals(tagName) || "html".equals(tagName) || "td".equals(tagName)
                        || "th".equals(tagName) || "tr".equals(tagName))) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            TreeConstructorInTable.inTable(tokenType, tagName, tagNameID, treeConstructor);
        }
    }

    private static void clearStackBackToTableBodyContext(TreeConstructor treeConstructor) {
        while (true) {
            Element e = treeConstructor.getCurrentNode();
            String nodeName = e.nodeName;
            if (Node.NAMESPACE_HTML_ID == e.namespaceID && //
                    ("tbody".equals(nodeName) || //
                            "tfoot".equals(nodeName) || //
                            "thead".equals(nodeName) || //
                            "template".equals(nodeName) || //
                    "html".equals(nodeName))) {
                break;
            }
            treeConstructor.popCurrentNode();
        }
    }

    // -------------

    // in table text

    static void inTableText(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        int chr = treeConstructor.getChr();
        if (tokenType == CHARACTER && chr == Characters.NULL) {
            treeConstructor.emitParseError();
            // ignore
        } else if (tokenType == CHARACTER) {
            treeConstructor.appendToPendingTableCharactersToken(chr);
        } else {
            ResizableCharBuilder chars = treeConstructor.getPendingTableCharactersToken();
            if (!isAllSpaceCharacters(chars)) {
                // TODO CHECK

                treeConstructor.emitParseError();
                // Enable foster parenting, process the token using the rules
                // for the "in body" insertion mode, and then disable foster
                // parenting.

                // save tokenType and chr value

                final byte currentTokenType = treeConstructor.getTokenType();
                final int currentChar = treeConstructor.getChr();

                //
                treeConstructor.enableFosterParenting();
                //
                treeConstructor.setTokenType(CHARACTER);
                int pos = chars.pos();
                for (int i = 0; i < pos; i++) {
                    treeConstructor.setChr(chars.at(i));
                    TreeConstructorInBodyForeignContentText.inBody(CHARACTER, tagName, tagNameID, treeConstructor);
                }

                //
                treeConstructor.disableFosterParenting();

                // restore
                treeConstructor.setTokenType(currentTokenType);
                treeConstructor.setChr((char) currentChar);
            } else {
                int pos = chars.pos();
                for (int i = 0; i < pos; i++) {
                    treeConstructor.insertCharacter(chars.at(i));
                }
            }

            treeConstructor.switchToOriginalInsertionMode();
            treeConstructor.dispatch();
        }
    }

    private static boolean isAllSpaceCharacters(ResizableCharBuilder chars) {
        int pos = chars.pos();
        for(int i = 0; i < pos; i++) {
            if (!Common.isTabLfFfCrOrSpace(chars.at(i))) {
                return false;
            }
        }
        return true;
    }

    // ----- in row

    static void inRow(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {

        if (tokenType == START_TAG && ("th".equals(tagName) || "td".equals(tagName))) {
            clearStackBackToTableRowContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_CELL);
            treeConstructor.insertMarkerInActiveFormattingElements();
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_TR_ID, tagNameID)) {
            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TR_ID)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                clearStackBackToTableRowContext(treeConstructor);
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE_BODY);
            }
        } else if ((tokenType == START_TAG && ("caption".equals(tagName) || //
                "col".equals(tagName) || //
                "colgroup".equals(tagName) || //
                "tbody".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "thead".equals(tagName) || //
                "tr".equals(tagName)))
                || Common.isEndTagNamed(tokenType, Common.ELEMENT_TABLE_ID, tagNameID)) {
            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TR_ID)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                clearStackBackToTableRowContext(treeConstructor);
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE_BODY);
                treeConstructor.dispatch();
            }
        } else if (tokenType == END_TAG && ("tbody".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "thead".equals(tagName))) {

            if (!treeConstructor.hasElementInTableScope(tagNameID)) { //tbody, tfoot thread
                treeConstructor.emitParseError();
                // ignore token
            } else if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TR_ID)) {
                // ignore token
            } else {
                clearStackBackToTableRowContext(treeConstructor);
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE_BODY);
                treeConstructor.dispatch();
            }
        } else if (tokenType == END_TAG && ("body".equals(tagName) || //
                "caption".equals(tagName) || //
                "col".equals(tagName) || //
                "colgroup".equals(tagName) || //
                "html".equals(tagName) || //
                "td".equals(tagName) || //
                "th".equals(tagName))) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            TreeConstructorInTable.inTable(tokenType, tagName, tagNameID, treeConstructor);
        }

    }

    private static void clearStackBackToTableRowContext(TreeConstructor treeConstructor) {
        while (true) {
            Element e = treeConstructor.getCurrentNode();
            if (Node.NAMESPACE_HTML_ID == e.namespaceID && //
                    ("tr".equals(e.nodeName) || "template".equals(e.nodeName) || "html".equals(e.nodeName))) {
                break;
            }
            treeConstructor.popCurrentNode();
        }
    }

    // --------
    // in colgroup
    static void inColumnGroup(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {

        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_COL_ID, tagNameID)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_COLGROUP_ID, tagNameID)) {
            if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_COLGROUP_ID)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE);
            }
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_COL_ID, tagNameID)) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_TEMPLATE_ID, tagNameID) || Common.isEndTagNamed(tokenType, Common.ELEMENT_TEMPLATE_ID, tagNameID)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == EOF) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else {
            if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_COLGROUP_ID)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE);
                treeConstructor.dispatch();
            }
        }
    }

    // ----
    // in cell
    static void inCell(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        switch (tokenType) {
        case END_TAG:
            inCellEndTag(tagName, tagNameID, treeConstructor);
            break;
        case START_TAG:
            inCellStartTag(tagName, tagNameID, treeConstructor);
            break;
        default:
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
            break;
        }
    }

    private static void inCellEndTag(String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        if ((Common.ELEMENT_TD_ID == tagNameID || Common.ELEMENT_TH_ID == tagNameID)) {
            if (!treeConstructor.hasElementInTableScope(tagNameID)) { // TD or TH
                treeConstructor.emitParseError();
                // ignore token
            } else {
                treeConstructor.generateImpliedEndTag();
                if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), tagNameID)) { // we know it TD OR TH
                    treeConstructor.emitParseError();
                }
                treeConstructor.popOpenElementsUntilWithHtmlNS(tagNameID); // we know it TD OR TH

                treeConstructor.clearUpToLastMarkerActiveFormattingElements();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_ROW);
            }
        } else if (
                tagNameID == Common.ELEMENT_BODY_ID || //
                tagNameID == Common.ELEMENT_CAPTION_ID || //
                tagNameID == Common.ELEMENT_COL_ID || //
                tagNameID == Common.ELEMENT_COLGROUP_ID || //
                tagNameID == Common.ELEMENT_HTML_ID
        ) {
            treeConstructor.emitParseError();
            // ignore token
        } else if (
                Common.ELEMENT_TABLE_ID == tagNameID || //
                Common.ELEMENT_TBODY_ID == tagNameID || //
                Common.ELEMENT_TFOOT_ID == tagNameID || //
                Common.ELEMENT_THEAD_ID == tagNameID ||
                Common.ELEMENT_TR_ID == tagNameID
        ) {
            if (!treeConstructor.hasElementInTableScope(tagNameID)) { // table, tbody, tfoot, thead or tr
                treeConstructor.emitParseError();
                // ignore token
            } else {
                closeCell(treeConstructor);
                treeConstructor.dispatch();
            }
        } else {
            TreeConstructorInBodyForeignContentText.inBody(END_TAG, tagName, tagNameID, treeConstructor);
        }
    }

    private static void inCellStartTag(String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        if (("caption".equals(tagName) || //
                "col".equals(tagName) || //
                "colgroup".equals(tagName) || //
                "tbody".equals(tagName) || //
                "td".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "th".equals(tagName) || //
                "thead".equals(tagName) || "tr".equals(tagName))) {

            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TD_ID) && !treeConstructor.hasElementInTableScope(Common.ELEMENT_TH_ID)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                closeCell(treeConstructor);
                treeConstructor.dispatch();
            }
        } else {
            TreeConstructorInBodyForeignContentText.inBody(START_TAG, tagName, tagNameID, treeConstructor);
        }
    }

    private static void closeCell(TreeConstructor treeConstructor) {
        treeConstructor.generateImpliedEndTag();
        Element currentNode = treeConstructor.getCurrentNode();
        if (!(Common.isHtmlNS(currentNode, Common.ELEMENT_TD_ID) || Common.isHtmlNS(currentNode, Common.ELEMENT_TH_ID))) {
            treeConstructor.emitParseError();
        }
        while (true) {
            Element e = treeConstructor.popCurrentNode();
            if (Common.isHtmlNS(e, Common.ELEMENT_TD_ID) || Common.isHtmlNS(e, Common.ELEMENT_TH_ID)) {
                break;
            }
        }
        treeConstructor.clearUpToLastMarkerActiveFormattingElements();
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_ROW);
    }

    // ---- in caption

    static void inCaption(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {

        if (Common.isEndTagNamed(tokenType, Common.ELEMENT_CAPTION_ID, tagNameID)) {

            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_CAPTION_ID)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.generateImpliedEndTag();
                if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_CAPTION_ID)) {
                    treeConstructor.emitParseError();
                }

                treeConstructor.popOpenElementsUntilWithHtmlNS(Common.ELEMENT_CAPTION_ID);
                treeConstructor.clearUpToLastMarkerActiveFormattingElements();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE);
            }
        } else if ((tokenType == START_TAG && ("caption".equals(tagName) || //
                "col".equals(tagName) || //
                "colgroup".equals(tagName) || //
                "tbody".equals(tagName) || //
                "td".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "th".equals(tagName) || //
                "thead".equals(tagName) || //
                "tr".equals(tagName)))
                || Common.isEndTagNamed(tokenType, Common.ELEMENT_TABLE_ID, tagNameID)) {

            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_CAPTION_ID)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.generateImpliedEndTag();
                if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_CAPTION_ID)) {
                    treeConstructor.emitParseError();
                }

                treeConstructor.popOpenElementsUntilWithHtmlNS(Common.ELEMENT_CAPTION_ID);
                treeConstructor.clearUpToLastMarkerActiveFormattingElements();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE);

                treeConstructor.dispatch();
            }
        } else if (tokenType == END_TAG && ("body".equals(tagName) || //
                "col".equals(tagName) || //
                "colgroup".equals(tagName) || //
                "html".equals(tagName) || //
                "tbody".equals(tagName) || //
                "td".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "th".equals(tagName) || //
                "thead".equals(tagName) || //
                "tr".equals(tagName))) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        }
    }
}
