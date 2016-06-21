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
import static ch.digitalfondue.jfiveparse.TreeConstructor.emptyAttrs;
import static ch.digitalfondue.jfiveparse.TreeConstructor.genericRCDataParsing;
import static ch.digitalfondue.jfiveparse.TreeConstructor.genericRawTextElementParsing;

import java.util.Arrays;

class TreeConstructorAftersBeforeInitialInHead {

    static void afterHead(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, "body", tagName)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.framesetOkToFalse();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_BODY);
        } else if (Common.isStartTagNamed(tokenType, "frameset", tagName)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_FRAMESET);
        } else if (tokenType == START_TAG && ("base".equals(tagName) || //
                "basefont".equals(tagName) || //
                "bgsound".equals(tagName) || //
                "link".equals(tagName) || //
                "meta".equals(tagName) || //
                "noframes".equals(tagName) || //
                "script".equals(tagName) || //
                "style".equals(tagName) || //
                "template".equals(tagName) || //
                "title".equals(tagName))) {
            treeConstructor.emitParseError();
            treeConstructor.addToOpenElements(treeConstructor.getHead());
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, treeConstructor);
            treeConstructor.removeFromOpenElements(treeConstructor.getHead());
        } else if (Common.isEndTagNamed(tokenType, "template", tagName)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, treeConstructor);
        } else if (tokenType == END_TAG && ("body".equals(tagName) || //
                "html".equals(tagName) || //
                "br".equals(tagName))) {
            // anything below
            treeConstructor.insertHtmlElementWithEmptyAttributes("body");
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_BODY);
            treeConstructor.dispatch();
        } else if (Common.isStartTagNamed(tokenType, "head", tagName) || tokenType == END_TAG) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            treeConstructor.insertHtmlElementWithEmptyAttributes("body");
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_BODY);
            treeConstructor.dispatch();
        }
    }

    static void afterBody(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else if (tokenType == COMMENT) {
            treeConstructor.insertCommentToHtmlElement();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, "html", tagName)) {
            if (treeConstructor.isHtmlFragmentParsing()) {
                treeConstructor.emitParseError();
            } else {
                treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_AFTER_BODY);
            }
        } else if (tokenType == EOF) {
            treeConstructor.stopParsing();
        } else {
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_BODY);
            treeConstructor.dispatch();
        }
    }

    static void afterFrameset(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, "html", tagName)) {
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_AFTER_FRAMESET);
        } else if (Common.isStartTagNamed(tokenType, "noframes", tagName)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, treeConstructor);
        } else if (tokenType == EOF) {
            treeConstructor.stopParsing();
        } else {
            treeConstructor.emitParseError();
            // ignore
        }
    }

    static void afterAfterBody(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == COMMENT) {
            treeConstructor.insertCommentToDocument();
        } else if (tokenType == DOCTYPE || //
                (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) || //
                Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else if (tokenType == EOF) {
            treeConstructor.stopParsing();
        } else {
            treeConstructor.emitParseError();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_BODY);
            treeConstructor.dispatch();
        }
    }

    static void afterAfterFrameset(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == COMMENT) {
            treeConstructor.insertCommentToDocument();
        } else if ((tokenType == DOCTYPE) || //
                (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) || //
                (Common.isStartTagNamed(tokenType, "html", tagName))) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else if (tokenType == EOF) {
            treeConstructor.stopParsing();
        } else if (Common.isStartTagNamed(tokenType, "noframes", tagName)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, treeConstructor);
        } else {
            treeConstructor.emitParseError();
            // ignore token
        }
    }

    // ------------ before --------------
    static void beforeHead(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        switch (tokenType) {
        case CHARACTER:
            handleCharacterHead(treeConstructor);
            break;
        case COMMENT:
            treeConstructor.insertComment();
            break;
        case DOCTYPE:
            treeConstructor.emitParseError();
            // ignore
            break;
        case EOF:
            anythingElseHead(treeConstructor);
            break;
        case END_TAG:
            handleEndTagHead(tagName, treeConstructor);
            break;
        case START_TAG:
            handleStartTagHead(tokenType, tagName, treeConstructor);
            break;
        }
    }

    private static void handleStartTagHead(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, "head", tagName)) {
            Element head = treeConstructor.insertHtmlElementToken();
            treeConstructor.setHead(head);
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_HEAD);
        } else {
            anythingElseHead(treeConstructor);
        }
    }

    private static void handleEndTagHead(String tagName, TreeConstructor treeConstructor) {
        if ((!"head".equals(tagName) && !"body".equals(tagName) && //
                !"html".equals(tagName) && !"br".equals(tagName))) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            anythingElseHead(treeConstructor);
        }
    }

    private static void handleCharacterHead(TreeConstructor treeConstructor) {
        if (Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            // ignore
        } else {
            anythingElseHead(treeConstructor);
        }
    }

    private static void anythingElseHead(TreeConstructor treeConstructor) {
        Element head = treeConstructor.insertHtmlElementWithEmptyAttributes("head");
        treeConstructor.setHead(head);
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_HEAD);
        treeConstructor.dispatch();
    }

    static void beforeHtml(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        switch (tokenType) {
        case CHARACTER:
            handleCharacterHtml(treeConstructor);
            break;
        case COMMENT:
            treeConstructor.insertCommentToDocument();
            break;
        case DOCTYPE:
            treeConstructor.emitParseError();
            break;
        case EOF:
            anythingElseHtml(treeConstructor);
            break;
        case END_TAG:
            handleEndTagHtml(tagName, treeConstructor);
            break;
        case START_TAG:
            handleStartTagHtml(tagName, treeConstructor);
            break;
        }
    }

    private static void handleStartTagHtml(String tagName, TreeConstructor treeConstructor) {
        if ("html".equals(tagName)) {
            Element html = TreeConstructor.buildElement(tagName, tagName, Node.NAMESPACE_HTML, treeConstructor.getAttributes());
            treeConstructor.addToOpenElements(html);
            treeConstructor.getDocument().appendChild(html);
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.BEFORE_HEAD);
        } else {
            anythingElseHtml(treeConstructor);
        }
    }

    private static void handleEndTagHtml(String tagName, TreeConstructor treeConstructor) {
        if ((!"head".equals(tagName) && !"body".equals(tagName) && //
                !"html".equals(tagName) && !"br".equals(tagName))) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            anythingElseHtml(treeConstructor);
        }
    }

    private static void handleCharacterHtml(TreeConstructor treeConstructor) {
        if (Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            // ignore
        } else {
            anythingElseHtml(treeConstructor);
        }
    }

    private static void anythingElseHtml(TreeConstructor treeConstructor) {
        Element html = TreeConstructor.buildElement("html", "html", Node.NAMESPACE_HTML, emptyAttrs());
        treeConstructor.addToOpenElements(html);
        treeConstructor.getDocument().appendChild(html);
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.BEFORE_HEAD);
        treeConstructor.dispatch();
    }

    // ----------- initial
    static void initial(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        switch (tokenType) {
        case CHARACTER:
            handleCharacters(treeConstructor);
            break;
        case COMMENT:
            treeConstructor.insertCommentToDocument();
            break;
        case DOCTYPE:
            handleDoctype(treeConstructor);
            break;
        case EOF:
            initialOthers(treeConstructor);
            break;
        case END_TAG:
            initialOthers(treeConstructor);
            break;
        case START_TAG:
            initialOthers(treeConstructor);
            break;
        }
    }

    private static void handleDoctype(TreeConstructor treeConstructor) {
        DocumentType doctype = treeConstructor.buildDocumentType();
        Document doc = treeConstructor.getDocument();
        doc.appendChild(doctype);
        doc.setDoctype(doctype);
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.BEFORE_HTML);
    }

    private static void handleCharacters(TreeConstructor treeConstructor) {
        if (Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            // ignore
        } else {
            initialOthers(treeConstructor);
        }
    }

    private static void initialOthers(TreeConstructor treeConstructor) {
        treeConstructor.setQuirksMode(true);
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.BEFORE_HTML);
        treeConstructor.dispatch();
    }

    // --- in head ---

    static void inHead(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else if (tokenType == START_TAG && ("base".equals(tagName) || //
                "basefont".equals(tagName) || //
                "bgsound".equals(tagName) || //
                "link".equals(tagName))) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (Common.isStartTagNamed(tokenType, "meta", tagName)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (Common.isStartTagNamed(tokenType, "title", tagName)) {
            genericRCDataParsing(treeConstructor);
        } else if (tokenType == START_TAG && (//
                ("noscript".equals(tagName) && treeConstructor.isScriptingFlag()) || //
                ("noframes".equals(tagName) || "style".equals(tagName)))) {
            genericRawTextElementParsing(treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, "noscript", tagName) && !treeConstructor.isScriptingFlag()) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_HEAD_NOSCRIPT);
        } else if (Common.isStartTagNamed(tokenType, "script", tagName)) {

            // TODO check
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setTokenizerState(TokenizerState.SCRIPT_DATA_STATE);
            treeConstructor.saveInsertionMode();

            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.TEXT);

        } else if (Common.isEndTagNamed(tokenType, "head", tagName)) {
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_HEAD);
        } else if (tokenType == END_TAG && ("body".equals(tagName) || "html".equals(tagName) || "br".equals(tagName))) {
            // do as anything else
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_HEAD);
            treeConstructor.dispatch();
        } else if (Common.isStartTagNamed(tokenType, "template", tagName)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.insertMarkerInActiveFormattingElements();
            treeConstructor.framesetOkToFalse();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TEMPLATE);
            treeConstructor.pushInStackTemplatesInsertionMode(TreeConstructionInsertionMode.IN_TEMPLATE);
        } else if (Common.isEndTagNamed(tokenType, "template", tagName)) {
            if (!treeConstructor.stackOfOpenElementsContains("template", Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                TreeConstructorAftersBeforeInitialInHead.generateImpliedEndTagThoroughly(treeConstructor);
                if (!treeConstructor.getCurrentNode().is("template", Node.NAMESPACE_HTML)) {
                    treeConstructor.emitParseError();
                }
                treeConstructor.popOpenElementsUntil("template", Node.NAMESPACE_HTML);
                treeConstructor.clearUpToLastMarkerActiveFormattingElements();
                treeConstructor.popFromStackTemplatesInsertionMode();
                treeConstructor.resetInsertionModeAppropriately();
            }
        } else if ((Common.isStartTagNamed(tokenType, "head", tagName)) || tokenType == END_TAG) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_HEAD);
            treeConstructor.dispatch();
        }
    }

    static void inHeadNoScript(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        final int chr = treeConstructor.getChr();
        if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, "noscript", tagName)) {
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_HEAD);
        } else if ((tokenType == CHARACTER && (chr == Characters.TAB || //
                chr == Characters.LF || //
                chr == Characters.FF || chr == Characters.CR || chr == Characters.SPACE)) || //
                tokenType == COMMENT || //
                (tokenType == START_TAG && ("basefont".equals(tagName) || //
                        "bgsound".equals(tagName) || //
                        "link".equals(tagName) || //
                        "meta".equals(tagName) || //
                        "noframes".equals(tagName) || "style".equals(tagName)))) {
            inHead(tokenType, tagName, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, "br", tagName)) {
            treeConstructor.emitParseError();
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_HEAD);
            treeConstructor.dispatch();
        } else if ((tokenType == START_TAG && ("head".equals(tagName) || "noscript".equals(tagName))) || tokenType == END_TAG) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            treeConstructor.emitParseError();
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_HEAD);
            treeConstructor.dispatch();
        }
    }

    private static void generateImpliedEndTagThoroughly(TreeConstructor treeConstructor) {
        for (;;) {
            Element current = treeConstructor.getCurrentNode();
            if (Node.NAMESPACE_HTML.equals(current.getNamespaceURI()) && Arrays.binarySearch(Common.IMPLIED_TAGS_THOROUGHLY, current.getNodeName()) >= 0) {
                treeConstructor.popCurrentNode();
                continue;
            } else {
                break;
            }
        }
    }
}
