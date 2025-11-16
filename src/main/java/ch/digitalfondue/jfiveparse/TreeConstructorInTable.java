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

import static ch.digitalfondue.jfiveparse.TreeConstructor.*;

final class TreeConstructorInTable {

    static void inTable(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {

        Element currentNodeTop = treeConstructor.getCurrentNode();

        if (tokenType == TT_CHARACTER && (Common.isHtmlNS(currentNodeTop, Common.ELEMENT_TABLE_ID) || //
                Common.isHtmlNS(currentNodeTop, Common.ELEMENT_TBODY_ID) || //
                Common.isHtmlNS(currentNodeTop, Common.ELEMENT_TFOOT_ID) || //
                Common.isHtmlNS(currentNodeTop, Common.ELEMENT_THEAD_ID) || //
                Common.isHtmlNS(currentNodeTop, Common.ELEMENT_TR_ID))) {
            treeConstructor.createPendingTableCharactersToken();
            treeConstructor.saveInsertionMode();
            treeConstructor.setInsertionMode(IM_IN_TABLE_TEXT);
            treeConstructor.dispatch();
        } else if (tokenType == TT_COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == TT_DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore token
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_CAPTION_ID, tagNameID)) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.activeFormattingElements.insertMarker();
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(IM_IN_CAPTION);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_COLGROUP_ID, tagNameID)) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(IM_IN_COLUMN_GROUP);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_COL_ID, tagNameID)) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementWithEmptyAttributes("colgroup", Common.ELEMENT_COLGROUP_ID);
            treeConstructor.setInsertionMode(IM_IN_COLUMN_GROUP);
            treeConstructor.dispatch();
        } else if (tokenType == TT_START_TAG && (Common.ELEMENT_TBODY_ID == tagNameID || //
                Common.ELEMENT_TFOOT_ID == tagNameID || Common.ELEMENT_THEAD_ID == tagNameID)) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(IM_IN_TABLE_BODY);
        } else if (tokenType == TT_START_TAG && (
                Common.ELEMENT_TD_ID == tagNameID || //
                Common.ELEMENT_TH_ID == tagNameID ||
                Common.ELEMENT_TR_ID == tagNameID
        )) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementWithEmptyAttributes("tbody", Common.ELEMENT_TBODY_ID);
            treeConstructor.setInsertionMode(IM_IN_TABLE_BODY);
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
        } else if (tokenType == TT_END_TAG && (
                Common.ELEMENT_BODY_ID == tagNameID || //
                        Common.ELEMENT_CAPTION_ID == tagNameID || //
                        Common.ELEMENT_COL_ID == tagNameID || //
                        Common.ELEMENT_COLGROUP_ID == tagNameID || //
                        Common.ELEMENT_HTML_ID == tagNameID || //
                        Common.ELEMENT_TBODY_ID == tagNameID || //
                        Common.ELEMENT_TD_ID == tagNameID || //
                        Common.ELEMENT_TFOOT_ID == tagNameID || //
                        Common.ELEMENT_TH_ID == tagNameID || //
                        Common.ELEMENT_THEAD_ID == tagNameID || //
                        Common.ELEMENT_TR_ID == tagNameID
        )) {
            // parser error
            // ignore token
        } else if (tokenType == TT_START_TAG && (
                Common.ELEMENT_STYLE_ID == tagNameID || //
                Common.ELEMENT_SCRIPT_ID == tagNameID || //
                Common.ELEMENT_TEMPLATE_ID == tagNameID)//
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
            if (treeConstructor.stackOfOpenElementsContainsElementTemplateAndNamespaceHtml() || treeConstructor.getForm() != null) {
                // ignore
            } else {
                Element form = treeConstructor.insertHtmlElementToken();
                treeConstructor.setForm(form);
                treeConstructor.popCurrentNode();
            }
        } else if (tokenType == TT_EOF) {
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
            if (Node.NAMESPACE_HTML_ID == e.namespaceID &&
                    (Common.ELEMENT_TABLE_ID == e.nodeNameID || Common.ELEMENT_TEMPLATE_ID == e.nodeNameID || Common.ELEMENT_HTML_ID == e.nodeNameID)
            ) {
                break;
            }
            treeConstructor.popCurrentNode();
        }
    }

    // ----- in table body

    static void inTableBody(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {

        if (Common.isStartTagNamed(tokenType, Common.ELEMENT_TR_ID, tagNameID)) {
            clearStackBackToTableBodyContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(IM_IN_ROW);
        } else if (tokenType == TT_START_TAG && (Common.ELEMENT_TH_ID == tagNameID || Common.ELEMENT_TD_ID == tagNameID)) {
            treeConstructor.emitParseError();
            clearStackBackToTableBodyContext(treeConstructor);
            treeConstructor.insertHtmlElementWithEmptyAttributes("tr", Common.ELEMENT_TR_ID);
            treeConstructor.setInsertionMode(IM_IN_ROW);
            treeConstructor.dispatch();
        } else if (tokenType == TT_END_TAG && (Common.ELEMENT_TBODY_ID == tagNameID || Common.ELEMENT_TFOOT_ID == tagNameID || Common.ELEMENT_THEAD_ID == tagNameID)) {
            if (!treeConstructor.hasElementInTableScope(tagNameID)) { // tbody, tfoot, thead
                treeConstructor.emitParseError();
                // ignore token
            } else {
                clearStackBackToTableBodyContext(treeConstructor);
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(IM_IN_TABLE);
            }
        } else if ((tokenType == TT_START_TAG &&
                (
                        Common.ELEMENT_CAPTION_ID == tagNameID ||
                        Common.ELEMENT_COL_ID == tagNameID ||
                        Common.ELEMENT_COLGROUP_ID == tagNameID ||
                        Common.ELEMENT_TBODY_ID == tagNameID ||
                        Common.ELEMENT_TFOOT_ID == tagNameID || //
                        Common.ELEMENT_THEAD_ID == tagNameID
                ))
                || Common.isEndTagNamed(tokenType, Common.ELEMENT_TABLE_ID, tagNameID)) {

            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TBODY_ID) && !treeConstructor.hasElementInTableScope(Common.ELEMENT_TFOOT_ID)
                    && !treeConstructor.hasElementInTableScope(Common.ELEMENT_THEAD_ID)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                clearStackBackToTableBodyContext(treeConstructor);
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(IM_IN_TABLE);
                treeConstructor.dispatch();
            }
        } else if (tokenType == TT_END_TAG
                &&
                (
                        Common.ELEMENT_BODY_ID == tagNameID ||
                        Common.ELEMENT_CAPTION_ID == tagNameID ||
                        Common.ELEMENT_COL_ID == tagNameID ||
                        Common.ELEMENT_COLGROUP_ID == tagNameID ||
                        Common.ELEMENT_HTML_ID == tagNameID ||
                        Common.ELEMENT_TD_ID == tagNameID ||
                        Common.ELEMENT_TH_ID == tagNameID ||
                        Common.ELEMENT_TR_ID == tagNameID
        )) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            TreeConstructorInTable.inTable(tokenType, tagName, tagNameID, treeConstructor);
        }
    }

    private static void clearStackBackToTableBodyContext(TreeConstructor treeConstructor) {
        while (true) {
            Element e = treeConstructor.getCurrentNode();
            int nodeNameID = e.nodeNameID;
            if (Node.NAMESPACE_HTML_ID == e.namespaceID && //
                    (
                            Common.ELEMENT_TBODY_ID == nodeNameID || //
                                    Common.ELEMENT_TFOOT_ID == nodeNameID || //
                                    Common.ELEMENT_THEAD_ID == nodeNameID || //
                                    Common.ELEMENT_TEMPLATE_ID == nodeNameID ||
                                    Common.ELEMENT_HTML_ID == nodeNameID
                    )
            ) {
                break;
            }
            treeConstructor.popCurrentNode();
        }
    }

    // -------------

    // in table text

    static void inTableText(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        int chr = treeConstructor.getChr();
        if (tokenType == TT_CHARACTER && chr == Characters.NULL) {
            treeConstructor.emitParseError();
            // ignore
        } else if (tokenType == TT_CHARACTER) {
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

                final int currentTokenType = treeConstructor.getTokenType();
                final int currentChar = treeConstructor.getChr();

                //
                treeConstructor.enableFosterParenting();
                //
                treeConstructor.setTokenType(TT_CHARACTER);
                int pos = chars.pos();
                for (int i = 0; i < pos; i++) {
                    treeConstructor.setChr(chars.at(i));
                    TreeConstructorInBodyForeignContentText.inBody(TT_CHARACTER, tagName, tagNameID, treeConstructor);
                }

                //
                treeConstructor.disableFosterParenting();

                // restore
                treeConstructor.setTokenType(currentTokenType);
                treeConstructor.setChr((char) currentChar);
            } else {
                int pos = chars.pos();
                ResizableCharBuilder insertCharacterPreviousTextNode = null;
                if (pos > 0) {
                    treeConstructor.insertCharacter(chars.at(0));
                    insertCharacterPreviousTextNode = treeConstructor.getInsertCharacterPreviousTextNode();
                }
                for (int i = 1; i < pos; i++) {
                    insertCharacterPreviousTextNode.append(chars.at(i));
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

    static void inRow(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {

        if (tokenType == TT_START_TAG && (Common.ELEMENT_TH_ID == tagNameID || Common.ELEMENT_TD_ID == tagNameID)) {
            clearStackBackToTableRowContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(IM_IN_CELL);
            treeConstructor.activeFormattingElements.insertMarker();
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_TR_ID, tagNameID)) {
            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TR_ID)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                clearStackBackToTableRowContext(treeConstructor);
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(IM_IN_TABLE_BODY);
            }
        } else if ((tokenType == TT_START_TAG && (
                Common.ELEMENT_CAPTION_ID == tagNameID || //
                Common.ELEMENT_COL_ID == tagNameID || //
                Common.ELEMENT_COLGROUP_ID == tagNameID || //
                Common.ELEMENT_TBODY_ID == tagNameID || //
                Common.ELEMENT_TFOOT_ID == tagNameID || //
                Common.ELEMENT_THEAD_ID == tagNameID || //
                Common.ELEMENT_TR_ID == tagNameID
        ))
                || Common.isEndTagNamed(tokenType, Common.ELEMENT_TABLE_ID, tagNameID)) {
            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TR_ID)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                clearStackBackToTableRowContext(treeConstructor);
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(IM_IN_TABLE_BODY);
                treeConstructor.dispatch();
            }
        } else if (tokenType == TT_END_TAG && (
                Common.ELEMENT_TBODY_ID == tagNameID || //
                Common.ELEMENT_TFOOT_ID == tagNameID || //
                Common.ELEMENT_THEAD_ID == tagNameID
        )) {

            if (!treeConstructor.hasElementInTableScope(tagNameID)) { //tbody, tfoot thread
                treeConstructor.emitParseError();
                // ignore token
            } else if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TR_ID)) {
                // ignore token
            } else {
                clearStackBackToTableRowContext(treeConstructor);
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(IM_IN_TABLE_BODY);
                treeConstructor.dispatch();
            }
        } else if (tokenType == TT_END_TAG && (
                Common.ELEMENT_BODY_ID == tagNameID || //
                Common.ELEMENT_CAPTION_ID == tagNameID || //
                Common.ELEMENT_COL_ID == tagNameID || //
                Common.ELEMENT_COLGROUP_ID == tagNameID || //
                Common.ELEMENT_HTML_ID == tagNameID || //
                Common.ELEMENT_TD_ID == tagNameID || //
                Common.ELEMENT_TH_ID == tagNameID
            )
        ) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            TreeConstructorInTable.inTable(tokenType, tagName, tagNameID, treeConstructor);
        }

    }

    private static void clearStackBackToTableRowContext(TreeConstructor treeConstructor) {
        while (true) {
            Element e = treeConstructor.getCurrentNode();
            int nodeNameID = e.nodeNameID;
            if (Node.NAMESPACE_HTML_ID == e.namespaceID && //
                    (Common.ELEMENT_TR_ID == nodeNameID || Common.ELEMENT_TEMPLATE_ID == nodeNameID || Common.ELEMENT_HTML_ID == nodeNameID)) {
                break;
            }
            treeConstructor.popCurrentNode();
        }
    }

    // --------
    // in colgroup
    static void inColumnGroup(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {

        if (tokenType == TT_CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == TT_COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == TT_DOCTYPE) {
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
                treeConstructor.setInsertionMode(IM_IN_TABLE);
            }
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_COL_ID, tagNameID)) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_TEMPLATE_ID, tagNameID) || Common.isEndTagNamed(tokenType, Common.ELEMENT_TEMPLATE_ID, tagNameID)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_EOF) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else {
            if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_COLGROUP_ID)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(IM_IN_TABLE);
                treeConstructor.dispatch();
            }
        }
    }

    // ----
    // in cell
    static void inCell(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == TT_END_TAG) {
            inCellEndTag(tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_START_TAG) {
            inCellStartTag(tagName, tagNameID, treeConstructor);
        } else {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        }
    }

    private static void inCellEndTag(String tagName, int tagNameID, TreeConstructor treeConstructor) {
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

                treeConstructor.activeFormattingElements.clearUpToLastMarker();
                treeConstructor.setInsertionMode(IM_IN_ROW);
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
            TreeConstructorInBodyForeignContentText.inBody(TT_END_TAG, tagName, tagNameID, treeConstructor);
        }
    }

    private static void inCellStartTag(String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (
                Common.ELEMENT_CAPTION_ID == tagNameID || //
                Common.ELEMENT_COL_ID == tagNameID  || //
                Common.ELEMENT_COLGROUP_ID == tagNameID || //
                Common.ELEMENT_TBODY_ID == tagNameID || //
                Common.ELEMENT_TD_ID == tagNameID || //
                Common.ELEMENT_TFOOT_ID == tagNameID || //
                Common.ELEMENT_TH_ID == tagNameID || //
                Common.ELEMENT_THEAD_ID == tagNameID  ||
                Common.ELEMENT_TR_ID == tagNameID
        ) {

            if (!treeConstructor.hasElementInTableScope(Common.ELEMENT_TD_ID) && !treeConstructor.hasElementInTableScope(Common.ELEMENT_TH_ID)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                closeCell(treeConstructor);
                treeConstructor.dispatch();
            }
        } else {
            TreeConstructorInBodyForeignContentText.inBody(TT_START_TAG, tagName, tagNameID, treeConstructor);
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
        treeConstructor.activeFormattingElements.clearUpToLastMarker();
        treeConstructor.setInsertionMode(IM_IN_ROW);
    }

    // ---- in caption

    static void inCaption(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {

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
                treeConstructor.activeFormattingElements.clearUpToLastMarker();
                treeConstructor.setInsertionMode(IM_IN_TABLE);
            }
        } else if ((tokenType == TT_START_TAG && (
                Common.ELEMENT_CAPTION_ID == tagNameID || //
                Common.ELEMENT_COL_ID == tagNameID || //
                Common.ELEMENT_COLGROUP_ID == tagNameID || //
                Common.ELEMENT_TBODY_ID == tagNameID || //
                Common.ELEMENT_TD_ID == tagNameID || //
                Common.ELEMENT_TFOOT_ID == tagNameID || //
                Common.ELEMENT_TH_ID == tagNameID || //
                Common.ELEMENT_THEAD_ID == tagNameID || //
                Common.ELEMENT_TR_ID == tagNameID
                ))
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
                treeConstructor.activeFormattingElements.clearUpToLastMarker();
                treeConstructor.setInsertionMode(IM_IN_TABLE);

                treeConstructor.dispatch();
            }
        } else if (tokenType == TT_END_TAG && (
                Common.ELEMENT_BODY_ID == tagNameID || //
                Common.ELEMENT_COL_ID == tagNameID || //
                Common.ELEMENT_COLGROUP_ID == tagNameID || //
                Common.ELEMENT_HTML_ID == tagNameID || //
                Common.ELEMENT_TBODY_ID == tagNameID || //
                Common.ELEMENT_TD_ID == tagNameID || //
                Common.ELEMENT_TFOOT_ID == tagNameID || //
                Common.ELEMENT_TH_ID == tagNameID || //
                Common.ELEMENT_THEAD_ID == tagNameID || //
                Common.ELEMENT_TR_ID == tagNameID)) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        }
    }
}
