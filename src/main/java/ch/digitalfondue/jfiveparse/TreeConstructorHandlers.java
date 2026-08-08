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

import java.util.HashMap;
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_COMMENT || tokenType == TT_PROCESSING_INSTRUCTION) {
            treeConstructor.insertCommentProcessingInstructionToHtmlElement(tokenType);
        } else if (tokenType == TT_DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
                inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else {
            treeConstructor.emitParseError();
            treeConstructor.enableFosterParenting();
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
                    inBody(TT_CHARACTER, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            inBody(TT_END_TAG, tagName, tagNameID, treeConstructor);
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
            inBody(TT_START_TAG, tagName, tagNameID, treeConstructor);
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
            inBody(tokenType, tagName, tagNameID, treeConstructor);
        }
    }


    // --------------------------------------
    private static void inBodyStartTag(String tagName, int tagNameID, TreeConstructor treeConstructor) {
        switch (tagNameID) {
            case ELEMENT_HTML_ID: {
                // startHtml(treeConstructor);
                treeConstructor.emitParseError();

                // we ignore the token if template is present
                if (!treeConstructor.stackOfOpenElementsContainsElementTemplateAndNamespaceHtml()) {
                    Element firstInserted = treeConstructor.openElementAt(0);
                    if (treeConstructor.getAttributes() != null) {
                        for (String attr : treeConstructor.getAttributes().keySet()) {
                            if (!firstInserted.getAttributes().containsKey(attr)) {
                                firstInserted.getAttributes().put(treeConstructor.getAttribute(attr));
                            }
                        }
                    }
                }
                break;
            }
            case ELEMENT_BASE_ID:
            case ELEMENT_BASEFONT_ID:
            case ELEMENT_BGSOUND_ID:
            case ELEMENT_LINK_ID:
            case ELEMENT_META_ID:
            case ELEMENT_NOFRAMES_ID:
            case ELEMENT_SCRIPT_ID:
            case ELEMENT_STYLE_ID:
            case ELEMENT_TEMPLATE_ID:
            case ELEMENT_TITLE_ID:
                TreeConstructorHandlers.inHead(TT_START_TAG, tagName, tagNameID, treeConstructor);
                break;
            case ELEMENT_BODY_ID:
                startBody(treeConstructor);
                break;
            case ELEMENT_FRAMESET_ID:
                startFrameset(treeConstructor);
                break;
            case ELEMENT_ADDRESS_ID:
            case ELEMENT_ARTICLE_ID:
            case ELEMENT_ASIDE_ID:
            case ELEMENT_BLOCKQUOTE_ID:
            case ELEMENT_CENTER_ID:
            case ELEMENT_DETAILS_ID:
            case ELEMENT_DIALOG_ID:
            case ELEMENT_DIR_ID:
            case ELEMENT_DIV_ID:
            case ELEMENT_DL_ID:
            case ELEMENT_FIELDSET_ID:
            case ELEMENT_FIGCAPTION_ID:
            case ELEMENT_FIGURE_ID:
            case ELEMENT_FOOTER_ID:
            case ELEMENT_HEADER_ID:
            case ELEMENT_HGROUP_ID:
            case ELEMENT_MAIN_ID:
            case ELEMENT_NAV_ID:
            case ELEMENT_OL_ID:
            case ELEMENT_P_ID:
            case ELEMENT_SEARCH_ID:
            case ELEMENT_SECTION_ID:
            case ELEMENT_SUMMARY_ID:
            case ELEMENT_UL_ID:
                startAddressUl(treeConstructor);
                break;
            case ELEMENT_MENU_ID:
                startMenu(treeConstructor);
                break;
            case ELEMENT_H1_ID, ELEMENT_H2_ID, ELEMENT_H3_ID, ELEMENT_H4_ID, ELEMENT_H5_ID, ELEMENT_H6_ID:
                startH1H6(treeConstructor);
                break;
            case ELEMENT_PRE_ID:
            case ELEMENT_LISTING_ID:
                startPreListing(treeConstructor);
                break;
            case ELEMENT_FORM_ID:
                startForm(treeConstructor);
                break;
            case ELEMENT_LI_ID:
                startLi(treeConstructor);
                break;
            case ELEMENT_DD_ID:
            case ELEMENT_DT_ID:
                startDdDt(treeConstructor);
                break;
            case ELEMENT_PLAINTEXT_ID:
                startPlaintext(treeConstructor);
                break;
            case ELEMENT_BUTTON_ID:
                startButton(treeConstructor);
                break;
            case ELEMENT_A_ID:
                startA(treeConstructor);
                break;
            case ELEMENT_B_ID:
            case ELEMENT_BIG_ID:
            case ELEMENT_CODE_ID:
            case ELEMENT_EM_ID:
            case ELEMENT_FONT_ID:
            case ELEMENT_I_ID:
            case ELEMENT_S_ID:
            case ELEMENT_SMALL_ID:
            case ELEMENT_STRIKE_ID:
            case ELEMENT_STRONG_ID:
            case ELEMENT_TT_ID:
            case ELEMENT_U_ID:
                startBU(treeConstructor);
                break;
            case ELEMENT_NO_BR_ID:
                startNobr(treeConstructor);
                break;
            case ELEMENT_APPLET_ID:
            case ELEMENT_MARQUEE_ID:
            case ELEMENT_OBJECT_ID:
                startAppletObject(treeConstructor);
                break;
            case ELEMENT_TABLE_ID:
                startTable(treeConstructor);
                break;
            case ELEMENT_AREA_ID:
            case ELEMENT_BR_ID:
            case ELEMENT_EMBED_ID:
            case ELEMENT_IMG_ID:
            case ELEMENT_KEYGEN_ID:
            case ELEMENT_WBR_ID:
                startAreaWbr(treeConstructor);
                break;
            case ELEMENT_INPUT_ID:
                startInput(treeConstructor);
                break;
            case ELEMENT_PARAM_ID, ELEMENT_SOURCE_ID, ELEMENT_TRACK_ID:
                startParamTrack(treeConstructor);
                break;
            case ELEMENT_HR_ID:
                startHr(treeConstructor);
                break;
            case ELEMENT_IMAGE_ID:
                startImage(treeConstructor);
                break;
            case ELEMENT_TEXTAREA_ID:
                startTextarea(treeConstructor);
                break;
            case ELEMENT_XMP_ID:
                startXmp(treeConstructor);
                break;
            case ELEMENT_IFRAME_ID:
                startIframe(treeConstructor);
                break;
            case ELEMENT_NOEMBED_ID, ELEMENT_NOSCRIPT_ID:
                startNoembedNoscript(tagNameID, treeConstructor);
                break;
            case ELEMENT_SELECT_ID:
                startSelect(treeConstructor);
                break;
            case ELEMENT_OPTGROUP_ID:
                startOptgroup(treeConstructor);
                break;
            case ELEMENT_OPTION_ID: {
                // startOption(treeConstructor);
                // https://html.spec.whatwg.org/multipage/parsing.html#parsing-main-inbody
                // A start tag whose tag name is "option"
                if (treeConstructor.hasElementInScope(ELEMENT_SELECT_ID)) {
                    treeConstructor.generateImpliedEndTag("optgroup", Node.NAMESPACE_HTML);
                    // If the stack of open elements has an option element in scope, then this is a parse error.
                } else if (isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_OPTION_ID)) {
                    treeConstructor.popCurrentNode();
                }
                treeConstructor.activeFormattingElements.reconstruct();
                treeConstructor.insertHtmlElementToken();
                break;
            }
            case ELEMENT_RB_ID, ELEMENT_RTC_ID: {
                // startRbRtc(treeConstructor);
                if (treeConstructor.hasElementInScope(ELEMENT_RUBY_ID)) {
                    treeConstructor.generateImpliedEndTag();
                }

                if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_RUBY_ID)) {
                    treeConstructor.emitParseError();
                }
                treeConstructor.insertHtmlElementToken();
                break;
            }
            case ELEMENT_RP_ID, ELEMENT_RT_ID: {
                // startRpRt(treeConstructor);
                if (treeConstructor.hasElementInScope(ELEMENT_RUBY_ID)) {
                    treeConstructor.generateImpliedEndTag("rtc", Node.NAMESPACE_HTML);
                }

                if (!(Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_RUBY_ID) || Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_RTC_ID))) {
                    treeConstructor.emitParseError();
                }

                treeConstructor.insertHtmlElementToken();
                break;
            }
            case ELEMENT_MATH_ID: {
                // startMath(treeConstructor);
                treeConstructor.activeFormattingElements.reconstruct();

                Attributes attrs = treeConstructor.getAttributes();
                Common.adjustMathMLAttributes(attrs);
                Common.adjustForeignAttributes(attrs);

                treeConstructor.insertElementToken("math", Common.ELEMENT_MATH_ID, Node.NAMESPACE_MATHML, Node.NAMESPACE_MATHML_ID, attrs);
                if (treeConstructor.isSelfClosing()) {
                    treeConstructor.popCurrentNode();
                    treeConstructor.ackSelfClosingTagIfSet();
                }
                break;
            }
            case ELEMENT_SVG_ID: {
                // startSvg(treeConstructor);
                Attributes attrs = treeConstructor.getAttributes();
                Common.adjustSVGAttributes(attrs);
                Common.adjustForeignAttributes(attrs);

                treeConstructor.insertElementToken("svg", Common.ELEMENT_SVG_ID, Node.NAMESPACE_SVG, Node.NAMESPACE_SVG_ID, attrs);
                if (treeConstructor.isSelfClosing()) {
                    treeConstructor.popCurrentNode();
                    treeConstructor.ackSelfClosingTagIfSet();
                }
                break;
            }
            case ELEMENT_CAPTION_ID:
            case ELEMENT_COL_ID:
            case ELEMENT_COLGROUP_ID:
            case ELEMENT_FRAME_ID:
            case ELEMENT_HEAD_ID:
            case ELEMENT_TBODY_ID:
            case ELEMENT_TD_ID:
            case ELEMENT_TFOOT_ID:
            case ELEMENT_TH_ID:
            case ELEMENT_THEAD_ID:
            case ELEMENT_TR_ID:
                // ignore token
                if (treeConstructor.disableIgnoreTokenInBodyStartTag) {
                    inBodyStartTagAnythingElse(treeConstructor);
                } else {
                    treeConstructor.emitParseError();
                }
                break;
            default:
                inBodyStartTagAnythingElse(treeConstructor);
                break;
        }
    }

    // https://html.spec.whatwg.org/multipage/parsing.html#parsing-main-inbody
    // A start tag whose tag name is "optgroup"
    private static void startOptgroup(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInScope(ELEMENT_SELECT_ID)) {
            treeConstructor.generateImpliedEndTag();
            // If the stack of open elements has an option element in scope or has an optgroup element in scope, then this is a parse error.
        } else if (isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_OPTION_ID)) {
            treeConstructor.popCurrentNode();
        }
        treeConstructor.activeFormattingElements.reconstruct();
        treeConstructor.insertHtmlElementToken();
    }

    // https://html.spec.whatwg.org/multipage/parsing.html#parsing-main-inbody
    // A start tag whose tag name is "select"
    private static void startSelect(TreeConstructor treeConstructor) {

        if (treeConstructor.isHtmlFragmentParsing && treeConstructor.context.nodeNameID == ELEMENT_SELECT_ID) {
            treeConstructor.emitParseError();
            // ignore token
            return;
        } else if (treeConstructor.hasElementInScope(ELEMENT_SELECT_ID)) {
            treeConstructor.emitParseError();
            // ignore token
            treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_SELECT_ID);
        } else {
            treeConstructor.activeFormattingElements.reconstruct();
            treeConstructor.insertHtmlElementToken();
            treeConstructor.framesetOkToFalse();
        }
    }

    private static void startNoembedNoscript(int tagNameID, TreeConstructor treeConstructor) {
        if (ELEMENT_NOSCRIPT_ID == tagNameID && !treeConstructor.scriptingFlag) {
            inBodyStartTagAnythingElse(treeConstructor);
        } else {
            genericRawTextElementParsing(treeConstructor);
        }
    }

    private static void startIframe(TreeConstructor treeConstructor) {
        treeConstructor.framesetOkToFalse();
        genericRawTextElementParsing(treeConstructor);
    }

    private static void startXmp(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.closePElement();
        }
        treeConstructor.activeFormattingElements.reconstruct();
        treeConstructor.framesetOkToFalse();
        genericRawTextElementParsing(treeConstructor);
    }

    private static void startTextarea(TreeConstructor treeConstructor) {
        treeConstructor.insertHtmlElementToken();

        treeConstructor.setTokenizerState(TokenizerState.RCDATA_STATE);
        treeConstructor.saveInsertionMode();

        treeConstructor.framesetOkToFalse();
        treeConstructor.setInsertionMode(IM_TEXT);
        treeConstructor.ignoreCharacterTokenLF();
    }

    private static void startImage(TreeConstructor treeConstructor) {
        treeConstructor.emitParseError();
        treeConstructor.setTagName("img");
        treeConstructor.dispatch();
    }

    // https://html.spec.whatwg.org/multipage/parsing.html#parsing-main-inbody
    // A start tag whose tag name is "hr"
    private static void startHr(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.closePElement();
        }

        if (treeConstructor.hasElementInScope(ELEMENT_SELECT_ID)) {
            treeConstructor.generateImpliedEndTag();
            // If the stack of open elements has an option element in scope or has an optgroup element in scope, then this is a parse error.
        }

        treeConstructor.insertHtmlElementToken();
        treeConstructor.popCurrentNode();
        treeConstructor.ackSelfClosingTagIfSet();
        treeConstructor.framesetOkToFalse();
    }

    private static void startParamTrack(TreeConstructor treeConstructor) {
        treeConstructor.insertHtmlElementToken();
        treeConstructor.popCurrentNode();
        treeConstructor.ackSelfClosingTagIfSet();
    }

    // https://html.spec.whatwg.org/multipage/parsing.html#parsing-main-inbody
    // A start tag whose tag name is "input"
    private static void startInput(TreeConstructor treeConstructor) {

        if (treeConstructor.isHtmlFragmentParsing && treeConstructor.context.nodeNameID == ELEMENT_SELECT_ID) {
            treeConstructor.emitParseError();
            return;
        }
        if (treeConstructor.hasElementInScope(ELEMENT_SELECT_ID)) {
            treeConstructor.emitParseError();
            treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_SELECT_ID);
        }

        treeConstructor.activeFormattingElements.reconstruct();
        Element element = treeConstructor.insertHtmlElementToken();
        treeConstructor.popCurrentNode();
        treeConstructor.ackSelfClosingTagIfSet();
        boolean hasTypeAttr = element.getAttributes().containsKey("type");
        if (!hasTypeAttr || (!"hidden".equalsIgnoreCase(element.getAttributes().get("type").getValue()))) {
            treeConstructor.framesetOkToFalse();
        }
    }

    private static void startAreaWbr(TreeConstructor treeConstructor) {
        treeConstructor.activeFormattingElements.reconstruct();
        treeConstructor.insertHtmlElementToken();
        treeConstructor.popCurrentNode();
        treeConstructor.ackSelfClosingTagIfSet();
        treeConstructor.framesetOkToFalse();
    }

    private static void startTable(TreeConstructor treeConstructor) {
        if (!treeConstructor.isQuirksMode() && treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.closePElement();
        }
        treeConstructor.insertHtmlElementToken();
        treeConstructor.framesetOkToFalse();
        treeConstructor.setInsertionMode(IM_IN_TABLE);
    }

    private static void startAppletObject(TreeConstructor treeConstructor) {
        treeConstructor.activeFormattingElements.reconstruct();
        treeConstructor.insertHtmlElementToken();
        treeConstructor.activeFormattingElements.insertMarker();
        treeConstructor.framesetOkToFalse();
    }

    private static void startNobr(TreeConstructor treeConstructor) {
        treeConstructor.activeFormattingElements.reconstruct(); // we know it's NOBR
        if (treeConstructor.hasElementInScope(ELEMENT_NO_BR_ID)) {
            treeConstructor.emitParseError();
            treeConstructor.adoptionAgencyAlgorithm(ELEMENT_NO_BR_ID);
            treeConstructor.activeFormattingElements.reconstruct();
        }
        Element nobr = treeConstructor.insertHtmlElementToken();
        treeConstructor.activeFormattingElements.push(nobr);
    }

    private static void startBU(TreeConstructor treeConstructor) {
        treeConstructor.activeFormattingElements.reconstruct();
        Element element = treeConstructor.insertHtmlElementToken();

        treeConstructor.activeFormattingElements.push(element);
    }

    private static void startA(TreeConstructor treeConstructor) {
        final int aIdx = treeConstructor.activeFormattingElements.getBetweenLastElementAndMarkerIndexElementANamespaceHtml();
        if (aIdx != -1) {
            Element a = treeConstructor.activeFormattingElements.getElementAtIndex(aIdx);
            treeConstructor.emitParseError();
            treeConstructor.adoptionAgencyAlgorithm(ELEMENT_A_ID);
            treeConstructor.activeFormattingElements.remove(a);
            treeConstructor.removeFromOpenElements(a);
        }
        treeConstructor.activeFormattingElements.reconstruct();
        Element createdA = treeConstructor.insertHtmlElementToken();
        treeConstructor.activeFormattingElements.push(createdA);
    }

    private static void startButton(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope(ELEMENT_BUTTON_ID)) {
            treeConstructor.emitParseError();
            treeConstructor.generateImpliedEndTag();
            treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_BUTTON_ID);
        }

        treeConstructor.activeFormattingElements.reconstruct();
        treeConstructor.insertHtmlElementToken();
        treeConstructor.framesetOkToFalse();
    }

    private static void startPlaintext(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.closePElement();
        }
        treeConstructor.insertHtmlElementToken();
        treeConstructor.setTokenizerState(TokenizerState.PLAINTEXT_STATE);
    }

    private static void startDdDt(TreeConstructor treeConstructor) {
        treeConstructor.framesetOkToFalse();
        int idx = treeConstructor.openElementsSize() - 1;
        Element node = treeConstructor.openElementAt(idx);
        while (true) {
            if (Common.isHtmlNS(node, ELEMENT_DD_ID)) {
                treeConstructor.generateImpliedEndTag("dd", Node.NAMESPACE_HTML);
                if (Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_DD_ID)) {
                    treeConstructor.emitParseError();
                }
                treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_DD_ID);
                break;
            }

            if (Common.isHtmlNS(node, ELEMENT_DT_ID)) {
                treeConstructor.generateImpliedEndTag("dt", Node.NAMESPACE_HTML);
                if (Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_DT_ID)) {
                    // parser error
                }
                treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_DT_ID);
                break;
            }

            if (Common.isSpecialCategory(node) && //
                    !(Common.isHtmlNS(node, ELEMENT_ADDRESS_ID) || //
                            Common.isHtmlNS(node, ELEMENT_DIV_ID) || Common.isHtmlNS(node, ELEMENT_P_ID))) {
                break;
            }

            idx = idx - 1;
            node = treeConstructor.openElementAt(idx);
        }
        if (treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.closePElement();
        }

        treeConstructor.insertHtmlElementToken();
    }

    private static void startLi(TreeConstructor treeConstructor) {
        treeConstructor.framesetOkToFalse();

        int idx = treeConstructor.openElementsSize() - 1;
        Element node = treeConstructor.openElementAt(idx);
        while (true) {
            if (Common.isHtmlNS(node, Common.ELEMENT_LI_ID)) {
                treeConstructor.generateImpliedEndTag("li", Node.NAMESPACE_HTML);
                if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_LI_ID)) {
                    treeConstructor.emitParseError();
                }

                treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_LI_ID);
                break;
            }

            if (Common.isSpecialCategory(node) && //
                    !(Common.isHtmlNS(node, ELEMENT_ADDRESS_ID) || //
                            Common.isHtmlNS(node, ELEMENT_DIV_ID) || Common.isHtmlNS(node, ELEMENT_P_ID))) {
                break;
            }
            idx = idx - 1;
            node = treeConstructor.openElementAt(idx);

        }

        if (treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.closePElement();
        }

        treeConstructor.insertHtmlElementToken();
    }

    private static void startForm(TreeConstructor treeConstructor) {
        boolean templateIsNotPresent = !treeConstructor.stackOfOpenElementsContainsElementTemplateAndNamespaceHtml();
        if (treeConstructor.getForm() != null && templateIsNotPresent) {
            treeConstructor.emitParseError();
            // ignore the token
        } else {
            if (treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
                treeConstructor.closePElement();
            }
            Element formElement = treeConstructor.insertHtmlElementToken();
            if (templateIsNotPresent) {
                treeConstructor.setForm(formElement);
            }
        }
    }

    private static void startPreListing(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.closePElement();
        }
        treeConstructor.insertHtmlElementToken();
        treeConstructor.ignoreCharacterTokenLF();
        treeConstructor.framesetOkToFalse();
    }

    private static void startH1H6(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.closePElement();
        }
        Element e = treeConstructor.getCurrentNode();
        if (Common.isHtmlNSBetweenH1H6(e)) {
            treeConstructor.emitParseError();
            treeConstructor.popCurrentNode();
        }
        treeConstructor.insertHtmlElementToken();
    }

    private static void startAddressUl(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.closePElement();
        }
        treeConstructor.insertHtmlElementToken();
    }

    private static void startMenu(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.closePElement();
        }
        treeConstructor.insertHtmlElementToken();
    }

    private static void startFrameset(TreeConstructor treeConstructor) {
        treeConstructor.emitParseError();
        if (treeConstructor.openElementsSize() == 1 || !Common.isHtmlNS(treeConstructor.openElementAt(1), ELEMENT_BODY_ID)) {
            // ignore
        } else if (Boolean.FALSE.equals(treeConstructor.getFramesetOk())) {
            // ignore
        } else {

            Element second = treeConstructor.openElementAt(1);
            if (second.getParentNode() != null) {
                second.getParentNode().removeChild(second);
            }

            //
            while (!Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_HTML_ID)) {
                treeConstructor.popCurrentNode();
            }
            //

            treeConstructor.insertHtmlElementToken();
            treeConstructor.setInsertionMode(IM_IN_FRAMESET);
        }
    }

    private static void startBody(TreeConstructor treeConstructor) {
        treeConstructor.emitParseError();

        if (treeConstructor.openElementsSize() == 1 || //
                !Common.isHtmlNS(treeConstructor.openElementAt(1), ELEMENT_BODY_ID) || //
                treeConstructor.stackOfOpenElementsContainsElementTemplateAndNamespaceHtml()) {
            // ignore
        } else {
            treeConstructor.framesetOkToFalse();
            Element secondInserted = treeConstructor.openElementAt(1);
            if (treeConstructor.getAttributes() != null) {
                for (String attr : treeConstructor.getAttributes().keySet()) {
                    if (!secondInserted.getAttributes().containsKey(attr)) {
                        secondInserted.getAttributes().put(treeConstructor.getAttribute(attr));
                    }
                }
            }
        }
    }

    private static void inBodyStartTagAnythingElse(TreeConstructor treeConstructor) {
        if (treeConstructor.interpretSelfClosingAnythingElse && treeConstructor.isSelfClosing()) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else {
            treeConstructor.activeFormattingElements.reconstruct();
            treeConstructor.insertHtmlElementToken();
        }
    }

    // https://html.spec.whatwg.org/multipage/parsing.html#parsing-main-inbody
    private static void inBodyEndTag(String tagName, int tagNameID, TreeConstructor treeConstructor) {
        switch (tagNameID) {
            case ELEMENT_TEMPLATE_ID:
                TreeConstructorHandlers.inHead(TT_END_TAG, tagName, tagNameID, treeConstructor);
                break;
            case ELEMENT_BODY_ID:
                endBody(treeConstructor);
                break;
            case ELEMENT_HTML_ID:
                endHtml(treeConstructor);
                break;
            // An end tag whose tag name is one of: "address", ...,  "ul"
            case ELEMENT_ADDRESS_ID:
            case ELEMENT_ARTICLE_ID:
            case ELEMENT_ASIDE_ID:
            case ELEMENT_BLOCKQUOTE_ID:
            case ELEMENT_BUTTON_ID:
            case ELEMENT_CENTER_ID:
            case ELEMENT_DETAILS_ID:
            case ELEMENT_DIALOG_ID:
            case ELEMENT_DIR_ID:
            case ELEMENT_DIV_ID:
            case ELEMENT_DL_ID:
            case ELEMENT_FIELDSET_ID:
            case ELEMENT_FIGCAPTION_ID:
            case ELEMENT_FIGURE_ID:
            case ELEMENT_FOOTER_ID:
            case ELEMENT_HEADER_ID:
            case ELEMENT_HGROUP_ID:
            case ELEMENT_LISTING_ID:
            case ELEMENT_MAIN_ID:
            case ELEMENT_MENU_ID:
            case ELEMENT_NAV_ID:
            case ELEMENT_OL_ID:
            case ELEMENT_PRE_ID:
            case ELEMENT_SEARCH_ID:
            case ELEMENT_SECTION_ID:
            case ELEMENT_SELECT_ID:
            case ELEMENT_SUMMARY_ID:
            case ELEMENT_UL_ID:
                endAddressUl(tagNameID, treeConstructor);
                break;
            case ELEMENT_FORM_ID:
                endForm(treeConstructor);
                break;
            case ELEMENT_P_ID:
                endP(treeConstructor);
                break;
            case ELEMENT_LI_ID:
                endLi(treeConstructor);
                break;
            case ELEMENT_DD_ID, ELEMENT_DT_ID:
                endDdDt(tagName, tagNameID, treeConstructor);
                break;
            case ELEMENT_H1_ID, ELEMENT_H2_ID, ELEMENT_H3_ID, ELEMENT_H4_ID, ELEMENT_H5_ID, ELEMENT_H6_ID:
                endH1H6(tagNameID, treeConstructor);
                break;
            case ELEMENT_A_ID:
            case ELEMENT_B_ID:
            case ELEMENT_BIG_ID:
            case ELEMENT_CODE_ID:
            case ELEMENT_EM_ID:
            case ELEMENT_FONT_ID:
            case ELEMENT_I_ID:
            case ELEMENT_NO_BR_ID:
            case ELEMENT_S_ID:
            case ELEMENT_SMALL_ID:
            case ELEMENT_STRIKE_ID:
            case ELEMENT_STRONG_ID:
            case ELEMENT_TT_ID:
            case ELEMENT_U_ID:
                treeConstructor.adoptionAgencyAlgorithm(tagNameID);
                break;
            case ELEMENT_APPLET_ID:
            case ELEMENT_MARQUEE_ID:
            case ELEMENT_OBJECT_ID:
                endAppletObject(tagNameID, treeConstructor);
                break;
            case ELEMENT_BR_ID:
                endBr(treeConstructor);
                break;
            default:
                foreignAnyOtherEndTag(tagName, treeConstructor);
                break;
        }
    }

    private static void endBr(TreeConstructor treeConstructor) {
        treeConstructor.emitParseError();

        treeConstructor.removeAttributes();

        treeConstructor.activeFormattingElements.reconstruct();
        treeConstructor.insertHtmlElementToken();
        treeConstructor.popCurrentNode();
        treeConstructor.ackSelfClosingTagIfSet();
        treeConstructor.framesetOkToFalse();
    }

    private static void endAppletObject(int tagNameID, TreeConstructor treeConstructor) {
        // we know tagNameID = applet, marquee, object
        if (!treeConstructor.hasElementInScope(tagNameID)) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            treeConstructor.generateImpliedEndTag();

            if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), tagNameID)) {
                treeConstructor.emitParseError();
            }
            treeConstructor.popOpenElementsUntilWithHtmlNS(tagNameID);
            treeConstructor.activeFormattingElements.clearUpToLastMarker();
        }
    }

    private static void endH1H6(int tagNameID, TreeConstructor treeConstructor) {
        if (!(treeConstructor.hasElementInScope(ELEMENT_H1_ID) || //
                treeConstructor.hasElementInScope(ELEMENT_H2_ID) || //
                treeConstructor.hasElementInScope(ELEMENT_H3_ID) || //
                treeConstructor.hasElementInScope(ELEMENT_H4_ID) || //
                treeConstructor.hasElementInScope(ELEMENT_H5_ID) ||
                treeConstructor.hasElementInScope(ELEMENT_H6_ID))) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            treeConstructor.generateImpliedEndTag();

            if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), tagNameID)) { // we know it's h1-h6
                treeConstructor.emitParseError();
            }

            while (true) {
                Element e = treeConstructor.popCurrentNode();
                if (Common.isHtmlNSBetweenH1H6(e)) {
                    break;
                }
            }
        }
    }

    // we know it's DD, DT
    private static void endDdDt(String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope(tagNameID)) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            treeConstructor.generateImpliedEndTag(tagName, Node.NAMESPACE_HTML);
            if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), tagNameID)) {
                treeConstructor.emitParseError();
            }
            treeConstructor.popOpenElementsUntilWithHtmlNS(tagNameID);
        }
    }

    private static void endLi(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasLiElementInListScope()) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            treeConstructor.generateImpliedEndTag("li", Node.NAMESPACE_HTML);
            if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_LI_ID)) {
                treeConstructor.emitParseError();
            }
            treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_LI_ID);
        }
    }

    private static void endP(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.emitParseError();
            treeConstructor.insertHtmlElementWithEmptyAttributes("p", ELEMENT_P_ID);
        }
        treeConstructor.closePElement();
    }

    private static void endForm(TreeConstructor treeConstructor) {
        boolean templateIsNotPresent = !treeConstructor.stackOfOpenElementsContainsElementTemplateAndNamespaceHtml();
        if (templateIsNotPresent) {
            Element node = treeConstructor.getForm();
            treeConstructor.setForm(null);
            if (node == null || !treeConstructor.hasElementInScope(node)) {
                treeConstructor.emitParseError();
            } else {
                treeConstructor.generateImpliedEndTag();
                if (treeConstructor.getCurrentNode() != node) {
                    treeConstructor.emitParseError();
                }
                treeConstructor.removeFromOpenElements(node);
            }
        } else {
            if (!treeConstructor.hasElementInScope(ELEMENT_FORM_ID)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                treeConstructor.generateImpliedEndTag();
                if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_FORM_ID)) {
                    treeConstructor.emitParseError();
                }
                treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_FORM_ID);
            }
        }
    }

    private static void endAddressUl(int tagNameID, TreeConstructor treeConstructor) {
        //we know the ID are known
        if (!treeConstructor.hasElementInScope(tagNameID)) {
            treeConstructor.emitParseError();
        } else {
            treeConstructor.generateImpliedEndTag();
            Element currentNode = treeConstructor.getCurrentNode();
            if (currentNode.namespaceID != Node.NAMESPACE_HTML_ID || currentNode.nodeNameID != tagNameID) {
                treeConstructor.emitParseError();
            }

            treeConstructor.popOpenElementsUntilWithHtmlNS(tagNameID);
        }
    }

    private static void endHtml(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope(ELEMENT_BODY_ID)) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            // FIXME
            treeConstructor.setInsertionMode(IM_AFTER_BODY);
            treeConstructor.dispatch();
        }
    }

    private static void endBody(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope(ELEMENT_BODY_ID)) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            // FIXME
            treeConstructor.setInsertionMode(IM_AFTER_BODY);
        }
    }

    static void inBody(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        switch (tokenType) {
            case TT_CHARACTER: {
                // in body character
                int chr = treeConstructor.getChr();
                if (chr != Characters.NULL) {
                    treeConstructor.activeFormattingElements.reconstruct();
                    treeConstructor.insertCharacter((char) chr);
                    if (!Common.isTabLfFfCrOrSpace(chr)) {
                        treeConstructor.framesetOkToFalse();
                    }
                } else {
                    treeConstructor.emitParseError();
                    // ignore
                }
                return;
            }
            case TT_COMMENT, TT_PROCESSING_INSTRUCTION:
                treeConstructor.insertCommentProcessInstruction(tokenType);
                return;
            case TT_DOCTYPE:
                treeConstructor.emitParseError(); // ignore
                return;
            case TT_EOF: {
                if (!treeConstructor.isStackTemplatesInsertionModeIsEmpty()) {
                    TreeConstructorHandlers.inTemplate(tokenType, tagName, tagNameID, treeConstructor);
                } else {
                    // FIXME add check:
                    // If there is a node in the stack of open elements that is not
                    // either a dd element, a dt element, an li element, an optgroup
                    // element, an option element, a p element, an rb element, an rp
                    // element, an rt element, an rtc element, a tbody element, a td
                    // element, a tfoot element, a th element, a thead element, a tr
                    // element, the body element, or the html element, then this is
                    // a parse error.
                    treeConstructor.stopParsing();
                }
                return;
            }
            case TT_END_TAG:
                inBodyEndTag(tagName, tagNameID, treeConstructor);
                break;
            case TT_START_TAG:
                inBodyStartTag(tagName, tagNameID, treeConstructor);
                break;
        }
    }

    static void foreignAnyOtherEndTag(String tagName, TreeConstructor treeConstructor) {
        int idx = treeConstructor.openElementsSize() - 1;
        Element node = treeConstructor.openElementAt(idx);

        while (true) {
            // Common.isHtmlNS
            if (node.namespaceID == Node.NAMESPACE_HTML_ID && node.nodeName.equals(tagName)) {
                treeConstructor.generateImpliedEndTag(tagName, Node.NAMESPACE_HTML);
                if (node != treeConstructor.getCurrentNode()) {
                    treeConstructor.emitParseError();
                }

                while (true) {
                    Element e = treeConstructor.popCurrentNode();
                    if (e == node) {
                        break;
                    }
                }

                break;
            } else if (Common.isSpecialCategory(node)) {
                treeConstructor.emitParseError();
                return;
            }

            idx = idx - 1;
            node = treeConstructor.openElementAt(idx);
        }
    }

    // ---------------------------

    static void foreignContent(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == TT_CHARACTER && treeConstructor.getChr() == Characters.NULL) {
            treeConstructor.emitParseError();
            treeConstructor.insertCharacter(Characters.REPLACEMENT_CHARACTER);
        } else if (tokenType == TT_CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == TT_CHARACTER) {
            treeConstructor.insertCharacter();
            treeConstructor.framesetOkToFalse();
        } else if (tokenType == TT_COMMENT || tokenType == TT_PROCESSING_INSTRUCTION) {
            treeConstructor.insertCommentProcessInstruction(tokenType);
        } else if (tokenType == TT_DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore token
        } else if (tokenType == TT_START_TAG && (
                (
                        ELEMENT_B_ID == tagNameID || //
                                ELEMENT_BIG_ID == tagNameID || //
                                ELEMENT_BLOCKQUOTE_ID == tagNameID || //
                                ELEMENT_BODY_ID == tagNameID || //
                                ELEMENT_BR_ID == tagNameID || //
                                ELEMENT_CENTER_ID == tagNameID || //
                                ELEMENT_CODE_ID == tagNameID || //
                                ELEMENT_DD_ID == tagNameID || //
                                ELEMENT_DIV_ID == tagNameID || //
                                ELEMENT_DL_ID == tagNameID || //
                                ELEMENT_DT_ID == tagNameID || //
                                ELEMENT_EM_ID == tagNameID || //
                                ELEMENT_EMBED_ID == tagNameID || //
                                ELEMENT_H1_ID == tagNameID || //
                                ELEMENT_H2_ID == tagNameID || //
                                ELEMENT_H3_ID == tagNameID || //
                                ELEMENT_H4_ID == tagNameID || //
                                ELEMENT_H5_ID == tagNameID || //
                                ELEMENT_H6_ID == tagNameID || //
                                ELEMENT_HEAD_ID == tagNameID || //
                                ELEMENT_HR_ID == tagNameID || //
                                ELEMENT_I_ID == tagNameID || //
                                ELEMENT_IMG_ID == tagNameID || //
                                ELEMENT_LI_ID == tagNameID || //
                                ELEMENT_LISTING_ID == tagNameID || //
                                ELEMENT_MENU_ID == tagNameID || //
                                ELEMENT_META_ID == tagNameID || //
                                ELEMENT_NO_BR_ID == tagNameID || //
                                ELEMENT_OL_ID == tagNameID || //
                                ELEMENT_P_ID == tagNameID || //
                                ELEMENT_PRE_ID == tagNameID || //
                                ELEMENT_RUBY_ID == tagNameID || //
                                ELEMENT_S_ID == tagNameID || //
                                ELEMENT_SMALL_ID == tagNameID || //
                                ELEMENT_SPAN_ID == tagNameID || //
                                ELEMENT_STRONG_ID == tagNameID || //
                                ELEMENT_STRIKE_ID == tagNameID || //
                                ELEMENT_SUB_ID == tagNameID || //
                                ELEMENT_SUP_ID == tagNameID || //
                                ELEMENT_TABLE_ID == tagNameID || //
                                ELEMENT_TT_ID == tagNameID || //
                                ELEMENT_U_ID == tagNameID || //
                                ELEMENT_UL_ID == tagNameID || //
                                ELEMENT_VAR_ID == tagNameID//
                ) || //
                        (ELEMENT_FONT_ID == tagNameID && (treeConstructor.hasAttribute("color") || //
                                treeConstructor.hasAttribute("face") ||
                                treeConstructor.hasAttribute("size")))) ||
                (tokenType == TT_END_TAG && (ELEMENT_BR_ID == tagNameID || ELEMENT_P_ID == tagNameID))) {
            treeConstructor.emitParseError();

            while (true) {
                Element cur = treeConstructor.getCurrentNode();
                if (Node.NAMESPACE_HTML_ID == cur.namespaceID || isMathMLIntegrationPoint(cur) || isHtmlIntegrationPoint(cur)) {
                    break;
                }
                treeConstructor.popCurrentNode();
            }
            treeConstructor.insertionModeInHtmlContent();
        } else if (tokenType == TT_START_TAG) {
            anyOtherStartTag(tagName, treeConstructor);
        } else if (tokenType == TT_END_TAG && Common.isScriptSVGNS(treeConstructor.getCurrentNode())) {
            // we don't execute scripts
            treeConstructor.popCurrentNode();
        } else if (tokenType == TT_END_TAG) {

            Element node = treeConstructor.getCurrentNode();
            if (!tagName.equals(Common.convertToAsciiLowerCase(node.getNodeName()))) {
                treeConstructor.emitParseError();
            }

            int idx = treeConstructor.openElementsSize() - 1;

            while (true) {
                if (node == treeConstructor.openElementAt(0)) {
                    return;
                }

                if (tagName.equals(Common.convertToAsciiLowerCase(node.getNodeName()))) {
                    while (node != treeConstructor.popCurrentNode()) {
                    }
                    return;
                }

                idx--;
                node = treeConstructor.openElementAt(idx);
                if (Node.NAMESPACE_HTML_ID == node.namespaceID) {
                    treeConstructor.insertionModeInHtmlContent();
                    return;
                }
            }
        }
    }

    private static void anyOtherStartTag(String tagName, TreeConstructor treeConstructor) {
        Element currentNode = treeConstructor.getAdjustedCurrentNode();
        if (Node.NAMESPACE_MATHML_ID == currentNode.namespaceID) {
            adjustMathMLAttributes(treeConstructor.getAttributes());
        }
        if (Node.NAMESPACE_SVG_ID == currentNode.namespaceID) {

            if (SVG_ELEMENT_CASE_CORRECTION.containsKey(tagName)) {
                tagName = SVG_ELEMENT_CASE_CORRECTION.get(tagName);
            }
            adjustSVGAttributes(treeConstructor.getAttributes());
        }

        adjustForeignAttributes(treeConstructor.getAttributes());

        treeConstructor.insertElementToken(tagName, Common.tagNameToID(tagName), currentNode.getNamespaceURI(), currentNode.namespaceID, treeConstructor.getAttributes());

        if (treeConstructor.isSelfClosing()) {
            // we don't execute scripts
            treeConstructor.popCurrentNode();
        }
    }


    private static final HashMap<String, String> SVG_ELEMENT_CASE_CORRECTION = new HashMap<>();

    static {
        SVG_ELEMENT_CASE_CORRECTION.put("altglyph", "altGlyph");
        SVG_ELEMENT_CASE_CORRECTION.put("altglyphdef", "altGlyphDef");
        SVG_ELEMENT_CASE_CORRECTION.put("altglyphitem", "altGlyphItem");
        SVG_ELEMENT_CASE_CORRECTION.put("animatecolor", "animateColor");
        SVG_ELEMENT_CASE_CORRECTION.put("animatemotion", "animateMotion");
        SVG_ELEMENT_CASE_CORRECTION.put("animatetransform", "animateTransform");
        SVG_ELEMENT_CASE_CORRECTION.put("clippath", "clipPath");
        SVG_ELEMENT_CASE_CORRECTION.put("feblend", "feBlend");
        SVG_ELEMENT_CASE_CORRECTION.put("fecolormatrix", "feColorMatrix");
        SVG_ELEMENT_CASE_CORRECTION.put("fecomponenttransfer", "feComponentTransfer");
        SVG_ELEMENT_CASE_CORRECTION.put("fecomposite", "feComposite");
        SVG_ELEMENT_CASE_CORRECTION.put("feconvolvematrix", "feConvolveMatrix");
        SVG_ELEMENT_CASE_CORRECTION.put("fediffuselighting", "feDiffuseLighting");
        SVG_ELEMENT_CASE_CORRECTION.put("fedisplacementmap", "feDisplacementMap");
        SVG_ELEMENT_CASE_CORRECTION.put("fedistantlight", "feDistantLight");
        SVG_ELEMENT_CASE_CORRECTION.put("fedropshadow", "feDropShadow");
        SVG_ELEMENT_CASE_CORRECTION.put("feflood", "feFlood");
        SVG_ELEMENT_CASE_CORRECTION.put("fefunca", "feFuncA");
        SVG_ELEMENT_CASE_CORRECTION.put("fefuncb", "feFuncB");
        SVG_ELEMENT_CASE_CORRECTION.put("fefuncg", "feFuncG");
        SVG_ELEMENT_CASE_CORRECTION.put("fefuncr", "feFuncR");
        SVG_ELEMENT_CASE_CORRECTION.put("fegaussianblur", "feGaussianBlur");
        SVG_ELEMENT_CASE_CORRECTION.put("feimage", "feImage");
        SVG_ELEMENT_CASE_CORRECTION.put("femerge", "feMerge");
        SVG_ELEMENT_CASE_CORRECTION.put("femergenode", "feMergeNode");
        SVG_ELEMENT_CASE_CORRECTION.put("femorphology", "feMorphology");
        SVG_ELEMENT_CASE_CORRECTION.put("feoffset", "feOffset");
        SVG_ELEMENT_CASE_CORRECTION.put("fepointlight", "fePointLight");
        SVG_ELEMENT_CASE_CORRECTION.put("fespecularlighting", "feSpecularLighting");
        SVG_ELEMENT_CASE_CORRECTION.put("fespotlight", "feSpotLight");
        SVG_ELEMENT_CASE_CORRECTION.put("fetile", "feTile");
        SVG_ELEMENT_CASE_CORRECTION.put("feturbulence", "feTurbulence");
        SVG_ELEMENT_CASE_CORRECTION.put("foreignobject", "foreignObject");
        SVG_ELEMENT_CASE_CORRECTION.put("glyphref", "glyphRef");
        SVG_ELEMENT_CASE_CORRECTION.put("lineargradient", "linearGradient");
        SVG_ELEMENT_CASE_CORRECTION.put("radialgradient", "radialGradient");
        SVG_ELEMENT_CASE_CORRECTION.put("textpath", "textPath");
    }

    // ----------- text

    static void text(int tokenType, TreeConstructor treeConstructor) {
        switch (tokenType) {
            case TT_CHARACTER:
                treeConstructor.insertCharacter();
                break;
            case TT_EOF: {
                // Element currentNode = treeConstructor.getCurrentNode();
                // if (currentNode != null &&
                // "script".equals(currentNode.getNodeName())) {
                // // "already started".TODO
                // }
                treeConstructor.popCurrentNode();
                treeConstructor.switchToOriginalInsertionMode();
                treeConstructor.dispatch();
                break;
            }
            case TT_END_TAG: {
                // if ("script".equals(tagName)) {
                // // TODO check
                // treeConstructor.popCurrentNode();
                // treeConstructor.insertionMode =
                // treeConstructor.originalInsertionMode;
                // } else {
                treeConstructor.popCurrentNode();
                treeConstructor.switchToOriginalInsertionMode();

                // }
                break;
            }
        }
    }
}
