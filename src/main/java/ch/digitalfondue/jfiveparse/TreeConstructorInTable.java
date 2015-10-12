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

import static ch.digitalfondue.jfiveparse.TreeConstructor.CHARACTER;
import static ch.digitalfondue.jfiveparse.TreeConstructor.COMMENT;
import static ch.digitalfondue.jfiveparse.TreeConstructor.DOCTYPE;
import static ch.digitalfondue.jfiveparse.TreeConstructor.END_TAG;
import static ch.digitalfondue.jfiveparse.TreeConstructor.EOF;
import static ch.digitalfondue.jfiveparse.TreeConstructor.START_TAG;

class TreeConstructorInTable {

    static void inTable(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        Element currentNodeTop = treeConstructor.getCurrentNode();

        if (tokenType == CHARACTER && (currentNodeTop.is("table", Node.NAMESPACE_HTML) || //
                currentNodeTop.is("tbody", Node.NAMESPACE_HTML) || //
                currentNodeTop.is("tfoot", Node.NAMESPACE_HTML) || //
                currentNodeTop.is("thead", Node.NAMESPACE_HTML) || //
                currentNodeTop.is("tr", Node.NAMESPACE_HTML))) {
            treeConstructor.createPendingTableCharactersToken();
            treeConstructor.saveInsertionMode();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE_TEXT);
            treeConstructor.dispatch();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore token
        } else if (Common.isStartTagNamed(tokenType, "caption", tagName)) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertMarkerInActiveFormattingElements();
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_CAPTION);
        } else if (Common.isStartTagNamed(tokenType, "colgroup", tagName)) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_COLUMN_GROUP);
        } else if (Common.isStartTagNamed(tokenType, "col", tagName)) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementWithEmptyAttributes("colgroup");
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_COLUMN_GROUP);
            treeConstructor.dispatch();
        } else if (tokenType == START_TAG && ("tbody".equals(tagName) || //
                "tfoot".equals(tagName) || "thead".equals(tagName))) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE_BODY);
        } else if (tokenType == START_TAG && ("td".equals(tagName) || //
                "th".equals(tagName) || "tr".equals(tagName))) {
            cleanStackBackToTableContext(treeConstructor);
            treeConstructor.insertHtmlElementWithEmptyAttributes("tbody");
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE_BODY);
            treeConstructor.dispatch();
        } else if (Common.isStartTagNamed(tokenType, "table", tagName)) {
            treeConstructor.emitParseError();

            if (!treeConstructor.hasElementInTableScope("table", Node.NAMESPACE_HTML)) {
                // ignore
            } else {
                treeConstructor.popOpenElementsUntil("table", Node.NAMESPACE_HTML);
                treeConstructor.resetInsertionModeAppropriately();
                treeConstructor.dispatch();
            }
        } else if (Common.isEndTagNamed(tokenType, "table", tagName)) {
            if (!treeConstructor.hasElementInTableScope("table", Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popOpenElementsUntil("table", Node.NAMESPACE_HTML);
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
                || Common.isEndTagNamed(tokenType, "template", tagName)) {
            TreeConstructorInHeads.inHead(tokenType, tagName, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, "input", tagName)) {

            boolean hasTypeKey = treeConstructor.hasAttribute("type");
            if (!hasTypeKey || (hasTypeKey && !"hidden".equalsIgnoreCase(treeConstructor.getAttribute("type").getValue()))) {
                treeConstructor.emitParseError();
                treeConstructor.enableFosterParenting();
                TreeConstructorInBody.inBody(tokenType, tagName, treeConstructor);
                treeConstructor.disableFosterParenting();
            } else {
                treeConstructor.emitParseError();
                treeConstructor.insertHtmlElementToken();
                treeConstructor.popCurrentNode();
                treeConstructor.ackSelfClosingTagIfSet();
            }
        } else if (Common.isStartTagNamed(tokenType, "form", tagName)) {
            treeConstructor.emitParseError();
            if (treeConstructor.stackOfOpenElementsContains("template", Node.NAMESPACE_HTML) || treeConstructor.getForm() != null) {
                // ignore
            } else {
                Element form = treeConstructor.insertHtmlElementToken();
                treeConstructor.setForm(form);
                treeConstructor.popCurrentNode();
            }
        } else if (tokenType == EOF) {
            TreeConstructorInBody.inBody(tokenType, tagName, treeConstructor);
        } else {
            treeConstructor.emitParseError();
            treeConstructor.enableFosterParenting();
            TreeConstructorInBody.inBody(tokenType, tagName, treeConstructor);
            treeConstructor.disableFosterParenting();
        }
    }

    private static void cleanStackBackToTableContext(TreeConstructor treeConstructor) {
        while (true) {
            Element e = treeConstructor.getCurrentNode();
            if (e.getNamespaceURI() == Node.NAMESPACE_HTML && //
                    ("table".equals(e.getNodeName()) || "template".equals(e.getNodeName()) || "html".equals(e.getNodeName()))) {
                break;
            }
            treeConstructor.popCurrentNode();
        }
    }

    // ----- in table body

    static void inTableBody(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        if (Common.isStartTagNamed(tokenType, "tr", tagName)) {
            clearStackBackToTableBodyContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_ROW);
        } else if (tokenType == START_TAG && ("th".equals(tagName) || "td".equals(tagName))) {
            treeConstructor.emitParseError();
            clearStackBackToTableBodyContext(treeConstructor);
            treeConstructor.insertHtmlElementWithEmptyAttributes("tr");
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_ROW);
            treeConstructor.dispatch();
        } else if (tokenType == END_TAG && ("tbody".equals(tagName) || "tfoot".equals(tagName) || "thead".equals(tagName))) {
            if (!treeConstructor.hasElementInTableScope(tagName, Node.NAMESPACE_HTML)) {
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
                || Common.isEndTagNamed(tokenType, "table", tagName)) {

            if (!treeConstructor.hasElementInTableScope("tbody", Node.NAMESPACE_HTML) && !treeConstructor.hasElementInTableScope("tfoot", Node.NAMESPACE_HTML)
                    && !treeConstructor.hasElementInTableScope("thead", Node.NAMESPACE_HTML)) {
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
            TreeConstructorInTable.inTable(tokenType, tagName, treeConstructor);
        }
    }

    private static void clearStackBackToTableBodyContext(TreeConstructor treeConstructor) {
        while (true) {
            Element e = treeConstructor.getCurrentNode();
            if (e.getNamespaceURI() == Node.NAMESPACE_HTML && //
                    ("tbody".equals(e.getNodeName()) || //
                            "tfoot".equals(e.getNodeName()) || //
                            "thead".equals(e.getNodeName()) || //
                            "template".equals(e.getNodeName()) || //
                    "html".equals(e.getNodeName()))) {
                break;
            }
            treeConstructor.popCurrentNode();
        }
    }

    // -------------

    // in table text

    static void inTableText(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        int chr = treeConstructor.getChr();
        if (tokenType == CHARACTER && chr == Characters.NULL) {
            treeConstructor.emitParseError();
            // ignore
        } else if (tokenType == CHARACTER) {
            treeConstructor.appendToPendingTableCharactersToken(chr);
        } else {
            char[] chars = treeConstructor.getPendingTableCharactersToken();
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
                for (char c : chars) {
                    treeConstructor.setChr(c);
                    TreeConstructorInBody.inBody(CHARACTER, tagName, treeConstructor);
                }

                //
                treeConstructor.disableFosterParenting();

                // restore
                treeConstructor.setTokenType(currentTokenType);
                treeConstructor.setChr((char) currentChar);
            } else {
                treeConstructor.insertCharacters(chars);
            }

            treeConstructor.switchToOriginalInsertionMode();
            treeConstructor.dispatch();
        }
    }

    private static boolean isAllSpaceCharacters(char[] chars) {
        for (char c : chars) {
            if (!Common.isTabLfFfCrOrSpace(c)) {
                return false;
            }
        }
        return true;
    }

    // ----- in row

    static void inRow(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        if (tokenType == START_TAG && ("th".equals(tagName) || "td".equals(tagName))) {
            clearStackBackToTableRowContext(treeConstructor);
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_CELL);
            treeConstructor.insertMarkerInActiveFormattingElements();
        } else if (Common.isEndTagNamed(tokenType, "tr", tagName)) {
            if (!treeConstructor.hasElementInTableScope("tr", Node.NAMESPACE_HTML)) {
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
                || Common.isEndTagNamed(tokenType, "table", tagName)) {
            if (!treeConstructor.hasElementInTableScope("tr", Node.NAMESPACE_HTML)) {
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

            if (!treeConstructor.hasElementInTableScope(tagName, Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
                // ignore token
            } else if (!treeConstructor.hasElementInTableScope("tr", Node.NAMESPACE_HTML)) {
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
            TreeConstructorInTable.inTable(tokenType, tagName, treeConstructor);
        }

    }

    private static void clearStackBackToTableRowContext(TreeConstructor treeConstructor) {
        while (true) {
            Element e = treeConstructor.getCurrentNode();
            if (e.getNamespaceURI() == Node.NAMESPACE_HTML && //
                    ("tr".equals(e.getNodeName()) || "template".equals(e.getNodeName()) || "html".equals(e.getNodeName()))) {
                break;
            }
            treeConstructor.popCurrentNode();
        }
    }

    // --------
    // in colgroup
    static void inColumnGroup(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBody.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, "col", tagName)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (Common.isEndTagNamed(tokenType, "colgroup", tagName)) {
            if (!treeConstructor.getCurrentNode().is("colgroup", Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popCurrentNode();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE);
            }
        } else if (Common.isEndTagNamed(tokenType, "col", tagName)) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "template", tagName) || Common.isEndTagNamed(tokenType, "template", tagName)) {
            TreeConstructorInHeads.inHead(tokenType, tagName, treeConstructor);
        } else if (tokenType == EOF) {
            TreeConstructorInBody.inBody(tokenType, tagName, treeConstructor);
        } else {
            if (!treeConstructor.getCurrentNode().is("colgroup", Node.NAMESPACE_HTML)) {
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
    static void inCell(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        switch (tokenType) {
        case END_TAG:
            inCellEndTag(tagName, treeConstructor);
            break;
        case START_TAG:
            inCellStartTag(tagName, treeConstructor);
            break;
        default:
            TreeConstructorInBody.inBody(tokenType, tagName, treeConstructor);
            break;
        }
    }

    private static void inCellEndTag(String tagName, TreeConstructor treeConstructor) {
        if (("td".equals(tagName) || "th".equals(tagName))) {
            if (!treeConstructor.hasElementInTableScope(tagName, Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                treeConstructor.generateImpliedEndTag();
                if (!treeConstructor.getCurrentNode().is(tagName, Node.NAMESPACE_HTML)) {
                    treeConstructor.emitParseError();
                }
                treeConstructor.popOpenElementsUntil(tagName, Node.NAMESPACE_HTML);

                treeConstructor.clearUpToLastMarkerActiveFormattingElements();
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_ROW);
            }
        } else if (("body".equals(tagName) || //
                "caption".equals(tagName) || //
                "col".equals(tagName) || //
                "colgroup".equals(tagName) || "html".equals(tagName))) {
            treeConstructor.emitParseError();
            // ignore token
        } else if (("table".equals(tagName) || //
                "tbody".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "thead".equals(tagName) || "tr".equals(tagName))) {
            if (!treeConstructor.hasElementInTableScope(tagName, Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                closeCell(treeConstructor);
                treeConstructor.dispatch();
            }
        } else {
            TreeConstructorInBody.inBody(END_TAG, tagName, treeConstructor);
        }
    }

    private static void inCellStartTag(String tagName, TreeConstructor treeConstructor) {
        if (("caption".equals(tagName) || //
                "col".equals(tagName) || //
                "colgroup".equals(tagName) || //
                "tbody".equals(tagName) || //
                "td".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "th".equals(tagName) || //
                "thead".equals(tagName) || "tr".equals(tagName))) {

            if (!treeConstructor.hasElementInTableScope("td", Node.NAMESPACE_HTML) && !treeConstructor.hasElementInTableScope("th", Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                closeCell(treeConstructor);
                treeConstructor.dispatch();
            }
        } else {
            TreeConstructorInBody.inBody(START_TAG, tagName, treeConstructor);
        }
    }

    private static void closeCell(TreeConstructor treeConstructor) {
        treeConstructor.generateImpliedEndTag();
        Element currentNode = treeConstructor.getCurrentNode();
        if (!(currentNode.is("td", Node.NAMESPACE_HTML) || currentNode.is("th", Node.NAMESPACE_HTML))) {
            treeConstructor.emitParseError();
        }
        while (true) {
            Element e = treeConstructor.popCurrentNode();
            if (e.is("td", Node.NAMESPACE_HTML) || e.is("th", Node.NAMESPACE_HTML)) {
                break;
            }
        }
        treeConstructor.clearUpToLastMarkerActiveFormattingElements();
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_ROW);
    }

    // ---- in caption

    static void inCaption(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        if (Common.isEndTagNamed(tokenType, "caption", tagName)) {

            if (!treeConstructor.hasElementInTableScope("caption", Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.generateImpliedEndTag();
                if (!treeConstructor.getCurrentNode().is("caption", Node.NAMESPACE_HTML)) {
                    treeConstructor.emitParseError();
                }

                treeConstructor.popOpenElementsUntil("caption", Node.NAMESPACE_HTML);
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
                || Common.isEndTagNamed(tokenType, "table", tagName)) {

            if (!treeConstructor.hasElementInTableScope("caption", Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.generateImpliedEndTag();
                if (!treeConstructor.getCurrentNode().is("caption", Node.NAMESPACE_HTML)) {
                    treeConstructor.emitParseError();
                }

                treeConstructor.popOpenElementsUntil("caption", Node.NAMESPACE_HTML);
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
            TreeConstructorInBody.inBody(tokenType, tagName, treeConstructor);
        }
    }
}
