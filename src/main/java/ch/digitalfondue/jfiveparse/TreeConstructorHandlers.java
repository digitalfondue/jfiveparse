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

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static ch.digitalfondue.jfiveparse.TreeConstructor.*;
import static ch.digitalfondue.jfiveparse.Common.*;

final class TreeConstructorHandlers {

    static void afterHead(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == TT_CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == TT_COMMENT || tokenType == TT_PROCESSING_INSTRUCTION) {
            treeConstructor.insertCommentProcessInstruction(tokenType);
        } else if (tokenType == TT_DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_BODY_ID, tagNameID)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.framesetOkToFalse();
            treeConstructor.setInsertionMode(IM_IN_BODY);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_FRAMESET_ID, tagNameID)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(IM_IN_FRAMESET);
        } else if (tokenType == TT_START_TAG && (
                Common.ELEMENT_BASE_ID == tagNameID || //
                Common.ELEMENT_BASEFONT_ID == tagNameID || //
                Common.ELEMENT_BGSOUND_ID == tagNameID || //
                Common.ELEMENT_LINK_ID == tagNameID || //
                Common.ELEMENT_META_ID == tagNameID || //
                Common.ELEMENT_NOFRAMES_ID == tagNameID || //
                Common.ELEMENT_SCRIPT_ID == tagNameID || //
                Common.ELEMENT_STYLE_ID == tagNameID || //
                Common.ELEMENT_TEMPLATE_ID == tagNameID || //
                Common.ELEMENT_TITLE_ID == tagNameID
        )) {
            treeConstructor.emitParseError();
            treeConstructor.addToOpenElements(treeConstructor.getHead());
            TreeConstructorHandlers.inHead(tokenType, tagName, tagNameID, treeConstructor);
            treeConstructor.removeFromOpenElements(treeConstructor.getHead());
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_TEMPLATE_ID, tagNameID)) {
            TreeConstructorHandlers.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_END_TAG && (
                Common.ELEMENT_BODY_ID == tagNameID || //
                Common.ELEMENT_HTML_ID == tagNameID || //
                Common.ELEMENT_BR_ID == tagNameID
        )) {
            // anything below
            treeConstructor.insertHtmlElementWithEmptyAttributes("body", Common.ELEMENT_BODY_ID);
            treeConstructor.setInsertionMode(IM_IN_BODY);
            treeConstructor.dispatch();
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_HEAD_ID, tagNameID) || tokenType == TT_END_TAG) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            treeConstructor.insertHtmlElementWithEmptyAttributes("body", Common.ELEMENT_BODY_ID);
            treeConstructor.setInsertionMode(IM_IN_BODY);
            treeConstructor.dispatch();
        }
    }

    static void afterBody(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == TT_CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_COMMENT || tokenType == TT_PROCESSING_INSTRUCTION) {
            treeConstructor.insertCommentProcessingInstructionToHtmlElement(tokenType);
        } else if (tokenType == TT_DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            if (treeConstructor.isHtmlFragmentParsing) {
                treeConstructor.emitParseError();
            } else {
                treeConstructor.setInsertionMode(IM_AFTER_AFTER_BODY);
            }
        } else if (tokenType == TT_EOF) {
            treeConstructor.stopParsing();
        } else {
            treeConstructor.setInsertionMode(IM_IN_BODY);
            treeConstructor.dispatch();
        }
    }

    static void afterFrameset(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {

        if (tokenType == TT_CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == TT_COMMENT || tokenType == TT_PROCESSING_INSTRUCTION) {
            treeConstructor.insertCommentProcessInstruction(tokenType);
        } else if (tokenType == TT_DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            treeConstructor.setInsertionMode(IM_AFTER_AFTER_FRAMESET);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_NOFRAMES_ID, tagNameID)) {
            TreeConstructorHandlers.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_EOF) {
            treeConstructor.stopParsing();
        } else {
            treeConstructor.emitParseError();
            // ignore
        }
    }

    static void afterAfterBody(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == TT_COMMENT || tokenType == TT_PROCESSING_INSTRUCTION) {
            treeConstructor.insertCommentProcessingInstructionToDocument(tokenType);
        } else if (tokenType == TT_DOCTYPE || //
                (tokenType == TT_CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) || //
                Common.isStartTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_EOF) {
            treeConstructor.stopParsing();
        } else {
            treeConstructor.emitParseError();
            treeConstructor.setInsertionMode(IM_IN_BODY);
            treeConstructor.dispatch();
        }
    }

    static void afterAfterFrameset(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == TT_COMMENT || tokenType == TT_PROCESSING_INSTRUCTION) {
            treeConstructor.insertCommentProcessingInstructionToDocument(tokenType);
        } else if ((tokenType == TT_DOCTYPE) || //
                (tokenType == TT_CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) || //
                (Common.isStartTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID))) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_EOF) {
            treeConstructor.stopParsing();
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_NOFRAMES_ID, tagNameID)) {
            TreeConstructorHandlers.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else {
            treeConstructor.emitParseError();
            // ignore token
        }
    }

    // ------------ before --------------
    static void beforeHead(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {

        switch (tokenType) {
            case TT_CHARACTER:
                handleCharacterHead(treeConstructor);
                break;
            case TT_COMMENT, TT_PROCESSING_INSTRUCTION:
                treeConstructor.insertCommentProcessInstruction(tokenType);
                break;
            case TT_DOCTYPE:
                treeConstructor.emitParseError();
                // ignore
                break;
            case TT_EOF:
                anythingElseHead(treeConstructor);
                break;
            case TT_END_TAG:
                handleEndTagHead(tagNameID, treeConstructor);
                break;
            case TT_START_TAG:
                handleStartTagHead(tokenType, tagName, tagNameID, treeConstructor);
                break;
        }
    }

    private static void handleStartTagHead(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (Common.isStartTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_HEAD_ID, tagNameID)) {
            Element head = treeConstructor.insertHtmlElementToken();
            treeConstructor.setHead(head);
            treeConstructor.setInsertionMode(IM_IN_HEAD);
        } else {
            anythingElseHead(treeConstructor);
        }
    }

    private static void handleEndTagHead(int tagNameID, TreeConstructor treeConstructor) {
        if (
                Common.ELEMENT_HEAD_ID != tagNameID && Common.ELEMENT_BODY_ID != tagNameID &&
                Common.ELEMENT_HTML_ID != tagNameID && Common.ELEMENT_BR_ID != tagNameID
        ) {
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
        Element head = treeConstructor.insertHtmlElementWithEmptyAttributes("head", Common.ELEMENT_HEAD_ID);
        treeConstructor.setHead(head);
        treeConstructor.setInsertionMode(IM_IN_HEAD);
        treeConstructor.dispatch();
    }

    static void beforeHtml(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {

        switch (tokenType) {
            case TT_CHARACTER:
                handleCharacterHtml(treeConstructor);
                break;
            case TT_COMMENT, TT_PROCESSING_INSTRUCTION:
                treeConstructor.insertCommentProcessingInstructionToDocument(tokenType);
                break;
            case TT_DOCTYPE:
                treeConstructor.emitParseError();
                break;
            case TT_EOF:
                anythingElseHtml(treeConstructor);
                break;
            case TT_END_TAG:
                handleEndTagHtml(tagNameID, treeConstructor);
                break;
            case TT_START_TAG:
                handleStartTagHtml(tagName, tagNameID, treeConstructor);
                break;
        }
    }

    private static void handleStartTagHtml(String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (Common.ELEMENT_HTML_ID == tagNameID) {
            Element html = TreeConstructor.buildElement("html", Common.ELEMENT_HTML_ID, tagName, Node.NAMESPACE_HTML, Node.NAMESPACE_HTML_ID, treeConstructor.getAttributes());
            treeConstructor.addToOpenElements(html);
            treeConstructor.getDocument().appendChild(html);
            treeConstructor.setInsertionMode(IM_BEFORE_HEAD);
        } else {
            anythingElseHtml(treeConstructor);
        }
    }

    private static void handleEndTagHtml(int tagNameID, TreeConstructor treeConstructor) {
        if ((Common.ELEMENT_HEAD_ID != tagNameID && Common.ELEMENT_BODY_ID != tagNameID && //
                Common.ELEMENT_HTML_ID != tagNameID && Common.ELEMENT_BR_ID != tagNameID)) {
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
        Element html = TreeConstructor.buildElement("html", Common.ELEMENT_HTML_ID, "html", Node.NAMESPACE_HTML, Node.NAMESPACE_HTML_ID, null);
        treeConstructor.addToOpenElements(html);
        treeConstructor.getDocument().appendChild(html);
        treeConstructor.setInsertionMode(IM_BEFORE_HEAD);
        treeConstructor.dispatch();
    }

    // ----------- initial
    static void initial(int tokenType, TreeConstructor treeConstructor) {

        switch (tokenType) {
            case TT_CHARACTER:
                handleCharacters(treeConstructor);
                break;
            case TT_COMMENT, TT_PROCESSING_INSTRUCTION:
                treeConstructor.insertCommentProcessingInstructionToDocument(tokenType);
                break;
            case TT_DOCTYPE:
                handleDoctype(treeConstructor);
                break;
            case TT_EOF, TT_END_TAG, TT_START_TAG:
                initialOthers(treeConstructor);
                break;
        }
    }

    private static final List<String> PUBLIC_ID_PREFIXES = List.of(
            "+//silmaril//dtd html pro v0r11 19970101//",
            "-//as//dtd html 3.0 aswedit + extensions//",
            "-//advasoft ltd//dtd html 3.0 aswedit + extensions//",
            "-//ietf//dtd html 2.0 level 1//",
            "-//ietf//dtd html 2.0 level 2//",
            "-//ietf//dtd html 2.0 strict level 1//",
            "-//ietf//dtd html 2.0 strict level 2//",
            "-//ietf//dtd html 2.0 strict//",
            "-//ietf//dtd html 2.0//",
            "-//ietf//dtd html 2.1e//",
            "-//ietf//dtd html 3.0//",
            "-//ietf//dtd html 3.2 final//",
            "-//ietf//dtd html 3.2//",
            "-//ietf//dtd html 3//",
            "-//ietf//dtd html level 0//",
            "-//ietf//dtd html level 1//",
            "-//ietf//dtd html level 2//",
            "-//ietf//dtd html level 3//",
            "-//ietf//dtd html strict level 0//",
            "-//ietf//dtd html strict level 1//",
            "-//ietf//dtd html strict level 2//",
            "-//ietf//dtd html strict level 3//",
            "-//ietf//dtd html strict//",
            "-//ietf//dtd html//",
            "-//metrius//dtd metrius presentational//",
            "-//microsoft//dtd internet explorer 2.0 html strict//",
            "-//microsoft//dtd internet explorer 2.0 html//",
            "-//microsoft//dtd internet explorer 2.0 tables//",
            "-//microsoft//dtd internet explorer 3.0 html strict//",
            "-//microsoft//dtd internet explorer 3.0 html//",
            "-//microsoft//dtd internet explorer 3.0 tables//",
            "-//netscape comm. corp.//dtd html//",
            "-//netscape comm. corp.//dtd strict html//",
            "-//o'reilly and associates//dtd html 2.0//",
            "-//o'reilly and associates//dtd html extended 1.0//",
            "-//o'reilly and associates//dtd html extended relaxed 1.0//",
            "-//sq//dtd html 2.0 hotmetal + extensions//",
            "-//softquad software//dtd hotmetal pro 6.0::19990601::extensions to html 4.0//",
            "-//softquad//dtd hotmetal pro 4.0::19971010::extensions to html 4.0//",
            "-//spyglass//dtd html 2.0 extended//",
            "-//sun microsystems corp.//dtd hotjava html//",
            "-//sun microsystems corp.//dtd hotjava strict html//",
            "-//w3c//dtd html 3 1995-03-24//",
            "-//w3c//dtd html 3.2 draft//", "-//w3c//dtd html 3.2 final//",
            "-//w3c//dtd html 3.2//",
            "-//w3c//dtd html 3.2s draft//",
            "-//w3c//dtd html 4.0 frameset//",
            "-//w3c//dtd html 4.0 transitional//",
            "-//w3c//dtd html experimental 19960712//",
            "-//w3c//dtd html experimental 970421//",
            "-//w3c//dtd w3 html//",
            "-//w3o//dtd w3 html 3.0//",
            "-//webtechs//dtd mozilla html 2.0//",
            "-//webtechs//dtd mozilla html//");

    // 0 = no-quirks-mode, 1 =  limited-quirks mode, 2 = quirks-mode
    private static int quirksType(DocumentType documentType) {
        if (!"html".equals(documentType.getName())) {
            return 2;
        }
        var publicId = documentType.getPublicId();
        if (publicId != null) {
            publicId = publicId.toLowerCase(Locale.ROOT);
        }
        var systemId = documentType.getSystemId();
        if (systemId != null) {
            systemId = systemId.toLowerCase(Locale.ROOT);
        }
        if (Set.of("-//w3o//dtd w3 html strict 3.0//en//", "-/w3c/dtd html 4.0 transitional/en", "html").contains(publicId)) {
            return 2;
        }
        if ("http://www.ibm.com/data/dtd/v11/ibmxhtml1-transitional.dtd".equals(systemId)) {
            return 2;
        }

        for (var prefix : PUBLIC_ID_PREFIXES) {
            if (publicId != null && publicId.startsWith(prefix)) {
                return 2;
            }
        }
        if ((systemId == null || systemId.isEmpty()) && (
                publicId.startsWith("-//w3c//dtd html 4.01 frameset//") || publicId.startsWith("-//w3c//dtd html 4.01 transitional//")
        )) {
            return 2;
        }
        // we skip the iframe srcdoc section
        return 0;
    }

    // see https://html.spec.whatwg.org/#the-initial-insertion-mode "A DOCTYPE token"
    private static void handleDoctype(TreeConstructor treeConstructor) {
        DocumentType doctype = treeConstructor.buildDocumentType();
        Document doc = treeConstructor.getDocument();
        doc.appendChild(doctype);
        doc.setDoctype(doctype);

        if (quirksType(doctype) == 2) {
            treeConstructor.setQuirksMode(true);
        }

        treeConstructor.setInsertionMode(IM_BEFORE_HTML);
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
        treeConstructor.setInsertionMode(IM_BEFORE_HTML);
        treeConstructor.dispatch();
    }

    // --- in head ---

    static void inHead(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == TT_CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == TT_COMMENT || tokenType == TT_PROCESSING_INSTRUCTION) {
            treeConstructor.insertCommentProcessInstruction(tokenType);
        } else if (tokenType == TT_DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_START_TAG && (
                Common.ELEMENT_BASE_ID == tagNameID || //
                Common.ELEMENT_BASEFONT_ID == tagNameID || //
                Common.ELEMENT_BGSOUND_ID == tagNameID || //
                Common.ELEMENT_LINK_ID == tagNameID
        )) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_META_ID, tagNameID)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_TITLE_ID, tagNameID)) {
            genericRCDataParsing(treeConstructor);
        } else if (tokenType == TT_START_TAG && (//
                (Common.ELEMENT_NOSCRIPT_ID == tagNameID && treeConstructor.scriptingFlag) || //
                        (Common.ELEMENT_NOFRAMES_ID == tagNameID || Common.ELEMENT_STYLE_ID == tagNameID))) {
            genericRawTextElementParsing(treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_NOSCRIPT_ID, tagNameID) && !treeConstructor.scriptingFlag) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(IM_IN_HEAD_NOSCRIPT);
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_SCRIPT_ID, tagNameID)) {

            // TODO check
            treeConstructor.insertHtmlElementToken();
            treeConstructor.setTokenizerState(TokenizerState.SCRIPT_DATA_STATE);
            treeConstructor.saveInsertionMode();

            treeConstructor.setInsertionMode(IM_TEXT);

        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_HEAD_ID, tagNameID)) {
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(IM_AFTER_HEAD);
        } else if (tokenType == TT_END_TAG && (Common.ELEMENT_BODY_ID == tagNameID || Common.ELEMENT_HTML_ID == tagNameID || Common.ELEMENT_BR_ID == tagNameID)) {
            // do as anything else
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(IM_AFTER_HEAD);
            treeConstructor.dispatch();
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_TEMPLATE_ID, tagNameID)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.activeFormattingElements.insertMarker();
            treeConstructor.framesetOkToFalse();
            treeConstructor.setInsertionMode(IM_IN_TEMPLATE);
            treeConstructor.pushInStackTemplatesInsertionMode(IM_IN_TEMPLATE);
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_TEMPLATE_ID, tagNameID)) {
            if (!treeConstructor.stackOfOpenElementsContainsElementTemplateAndNamespaceHtml()) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                TreeConstructorHandlers.generateImpliedEndTagThoroughly(treeConstructor);
                if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_TEMPLATE_ID)) {
                    treeConstructor.emitParseError();
                }
                treeConstructor.popOpenElementsUntilWithHtmlNS(Common.ELEMENT_TEMPLATE_ID);
                treeConstructor.activeFormattingElements.clearUpToLastMarker();
                treeConstructor.popFromStackTemplatesInsertionMode();
                treeConstructor.resetInsertionModeAppropriately();
            }
        } else if ((Common.isStartTagNamed(tokenType, Common.ELEMENT_HEAD_ID, tagNameID)) || tokenType == TT_END_TAG) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(IM_AFTER_HEAD);
            treeConstructor.dispatch();
        }
    }

    static void inHeadNoScript(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        final int chr = treeConstructor.getChr();
        if (tokenType == TT_DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_NOSCRIPT_ID, tagNameID)) {
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(IM_IN_HEAD);
        } else if ((tokenType == TT_CHARACTER && (chr == Characters.TAB || //
                chr == Characters.LF || //
                chr == Characters.FF || chr == Characters.CR || chr == Characters.SPACE)) || //
                tokenType == TT_COMMENT || //
                tokenType == TT_PROCESSING_INSTRUCTION || //
                (tokenType == TT_START_TAG && (
                        Common.ELEMENT_BASEFONT_ID == tagNameID || //
                        Common.ELEMENT_BGSOUND_ID == tagNameID || //
                        Common.ELEMENT_LINK_ID == tagNameID || //
                        Common.ELEMENT_META_ID == tagNameID || //
                        Common.ELEMENT_NOFRAMES_ID == tagNameID ||
                        Common.ELEMENT_STYLE_ID == tagNameID
                ))) {
            inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_BR_ID, tagNameID)) {
            treeConstructor.emitParseError();
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(IM_IN_HEAD);
            treeConstructor.dispatch();
        } else if ((tokenType == TT_START_TAG && (Common.ELEMENT_HEAD_ID == tagNameID || Common.ELEMENT_NOSCRIPT_ID == tagNameID)) || tokenType == TT_END_TAG) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            treeConstructor.emitParseError();
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(IM_IN_HEAD);
            treeConstructor.dispatch();
        }
    }

    private static void generateImpliedEndTagThoroughly(TreeConstructor treeConstructor) {
        for (; ; ) {
            Element current = treeConstructor.getCurrentNode();
            if (Node.NAMESPACE_HTML_ID == current.namespaceID && Common.isImpliedTagsThoroughly(current.nodeNameID)) {
                treeConstructor.popCurrentNode();
                //continue;
            } else {
                break;
            }
        }
    }


    // ---------------- In frameset select template

    static void inFrameset(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {

        if (tokenType == TT_CHARACTER && isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == TT_COMMENT || tokenType == TT_PROCESSING_INSTRUCTION) {
            treeConstructor.insertCommentProcessInstruction(tokenType);
        } else if (tokenType == TT_DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (isStartTagNamed(tokenType, ELEMENT_HTML_ID, tagNameID)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (isStartTagNamed(tokenType, ELEMENT_FRAMESET_ID, tagNameID)) {
            treeConstructor.insertHtmlElementToken();
        } else if (isEndTagNamed(tokenType, ELEMENT_FRAMESET_ID, tagNameID)) {

            // TODO: should check if it's the root element and not only if it's
            // a html element?
            if (isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_HTML_ID)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popCurrentNode();
                if (!treeConstructor.isHtmlFragmentParsing && !isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_FRAMESET_ID)) {
                    treeConstructor.setInsertionMode(IM_AFTER_FRAMESET);
                }
            }
        } else if (isStartTagNamed(tokenType, ELEMENT_FRAME_ID, tagNameID)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (isStartTagNamed(tokenType, ELEMENT_NOFRAMES_ID, tagNameID)) {
            TreeConstructorHandlers.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_EOF) {
            if (!isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_HTML_ID)) {
                treeConstructor.emitParseError();
            }
            treeConstructor.stopParsing();
        } else {
            treeConstructor.emitParseError();
            // ignore token
        }
    }

    //-----------
    private static void popPushSetAndDispatch(TreeConstructor treeConstructor, int insertionMode) {
        treeConstructor.popFromStackTemplatesInsertionMode();
        treeConstructor.pushInStackTemplatesInsertionMode(insertionMode);
        treeConstructor.setInsertionMode(insertionMode);
        treeConstructor.dispatch();
    }

    static void inTemplate(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == TT_CHARACTER || tokenType == TT_COMMENT || tokenType == TT_DOCTYPE || tokenType == TT_PROCESSING_INSTRUCTION) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (
                (tokenType == TT_START_TAG && (
                        ELEMENT_BASE_ID == tagNameID || //
                                ELEMENT_BASEFONT_ID == tagNameID || //
                                ELEMENT_BGSOUND_ID == tagNameID || //
                                ELEMENT_LINK_ID == tagNameID || //
                                ELEMENT_META_ID == tagNameID || //
                                ELEMENT_NOFRAMES_ID == tagNameID || //
                                ELEMENT_SCRIPT_ID == tagNameID || //
                                ELEMENT_STYLE_ID == tagNameID || //
                                ELEMENT_TEMPLATE_ID == tagNameID || //
                                ELEMENT_TITLE_ID == tagNameID
                ))
                        || isEndTagNamed(tokenType, ELEMENT_TEMPLATE_ID, tagNameID)) {
            TreeConstructorHandlers.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_START_TAG && (
                ELEMENT_CAPTION_ID == tagNameID || //
                        ELEMENT_COLGROUP_ID == tagNameID || //
                        ELEMENT_TBODY_ID == tagNameID || //
                        ELEMENT_TFOOT_ID == tagNameID || //
                        ELEMENT_THEAD_ID == tagNameID)) {
            popPushSetAndDispatch(treeConstructor, IM_IN_TABLE);
        } else if (isStartTagNamed(tokenType, ELEMENT_COL_ID, tagNameID)) {
            popPushSetAndDispatch(treeConstructor, IM_IN_COLUMN_GROUP);
        } else if (isStartTagNamed(tokenType, ELEMENT_TR_ID, tagNameID)) {
            popPushSetAndDispatch(treeConstructor, IM_IN_TABLE_BODY);
        } else if (tokenType == TT_START_TAG && (ELEMENT_TD_ID == tagNameID || ELEMENT_TH_ID == tagNameID)) {
            popPushSetAndDispatch(treeConstructor, IM_IN_ROW);
        } else if (tokenType == TT_START_TAG) {
            popPushSetAndDispatch(treeConstructor, IM_IN_BODY);
        } else if (tokenType == TT_END_TAG) {
            treeConstructor.emitParseError();
            // ignore
        } else if (tokenType == TT_EOF) {
            if (!treeConstructor.stackOfOpenElementsContainsElementTemplateAndNamespaceHtml()) {
                treeConstructor.stopParsing();
            } else {
                treeConstructor.emitParseError();
                treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_TEMPLATE_ID);
                treeConstructor.activeFormattingElements.clearUpToLastMarker();
                treeConstructor.popFromStackTemplatesInsertionMode();
                treeConstructor.resetInsertionModeAppropriately();
                treeConstructor.dispatch();
            }
        }
    }
    //


    // in table
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
        } else if (tokenType == TT_COMMENT || tokenType == TT_PROCESSING_INSTRUCTION) {
            treeConstructor.insertCommentProcessInstruction(tokenType);
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
            TreeConstructorHandlers.inHead(tokenType, tagName, tagNameID, treeConstructor);
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
            inTable(tokenType, tagName, tagNameID, treeConstructor);
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
            if (!isAllSpaceCharacters(chars) && !treeConstructor.disableInTableTextForsterParenting) {
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
            inTable(tokenType, tagName, tagNameID, treeConstructor);
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
        } else if (tokenType == TT_COMMENT || tokenType == TT_PROCESSING_INSTRUCTION) {
            treeConstructor.insertCommentProcessInstruction(tokenType);
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
            TreeConstructorHandlers.inHead(tokenType, tagName, tagNameID, treeConstructor);
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
