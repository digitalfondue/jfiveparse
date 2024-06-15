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

class TreeConstructorAftersBeforeInitialInHead {

    static void afterHead(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
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
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
            treeConstructor.removeFromOpenElements(treeConstructor.getHead());
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_TEMPLATE_ID, tagNameID)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == END_TAG && ("body".equals(tagName) || //
                "html".equals(tagName) || //
                "br".equals(tagName))) {
            // anything below
            treeConstructor.insertHtmlElementWithEmptyAttributes("body", Common.ELEMENT_BODY_ID);
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_BODY);
            treeConstructor.dispatch();
        } else if (Common.isStartTagNamed(tokenType, "head", tagName) || tokenType == END_TAG) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            treeConstructor.insertHtmlElementWithEmptyAttributes("body", Common.ELEMENT_BODY_ID);
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_BODY);
            treeConstructor.dispatch();
        }
    }

    static void afterBody(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == COMMENT) {
            treeConstructor.insertCommentToHtmlElement();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
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

    static void afterFrameset(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {

        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_HTML_ID, tagNameID)) {
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_AFTER_FRAMESET);
        } else if (Common.isStartTagNamed(tokenType, "noframes", tagName)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == EOF) {
            treeConstructor.stopParsing();
        } else {
            treeConstructor.emitParseError();
            // ignore
        }
    }

    static void afterAfterBody(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == COMMENT) {
            treeConstructor.insertCommentToDocument();
        } else if (tokenType == DOCTYPE || //
                (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) || //
                Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == EOF) {
            treeConstructor.stopParsing();
        } else {
            treeConstructor.emitParseError();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_BODY);
            treeConstructor.dispatch();
        }
    }

    static void afterAfterFrameset(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == COMMENT) {
            treeConstructor.insertCommentToDocument();
        } else if ((tokenType == DOCTYPE) || //
                (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) || //
                (Common.isStartTagNamed(tokenType, "html", tagName))) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == EOF) {
            treeConstructor.stopParsing();
        } else if (Common.isStartTagNamed(tokenType, "noframes", tagName)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else {
            treeConstructor.emitParseError();
            // ignore token
        }
    }

    // ------------ before --------------
    static void beforeHead(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {

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
                handleStartTagHead(tokenType, tagName, tagNameID, treeConstructor);
                break;
        }
    }

    private static void handleStartTagHead(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
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
        Element head = treeConstructor.insertHtmlElementWithEmptyAttributes("head", Common.ELEMENT_HEAD_ID);
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
            Element html = TreeConstructor.buildElement(tagName, Common.ELEMENT_HTML_ID, tagName, Node.NAMESPACE_HTML, Node.NAMESPACE_HTML_ID, treeConstructor.getAttributes());
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
        Element html = TreeConstructor.buildElement("html", Common.ELEMENT_HTML_ID, "html", Node.NAMESPACE_HTML, Node.NAMESPACE_HTML_ID, null);
        treeConstructor.addToOpenElements(html);
        treeConstructor.getDocument().appendChild(html);
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.BEFORE_HEAD);
        treeConstructor.dispatch();
    }

    // ----------- initial
    static void initial(byte tokenType, TreeConstructor treeConstructor) {

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
            /*initialOthers(treeConstructor);
            break;*/
            case END_TAG:
            /*initialOthers(treeConstructor);
            break;*/
            case START_TAG:
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
    private static byte quirksType(DocumentType documentType) {
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
        if ((systemId == null || "".equals(systemId)) && (
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

    static void inHead(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
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

        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_HEAD_ID, tagNameID)) {
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
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_TEMPLATE_ID, tagNameID)) {
            if (!treeConstructor.stackOfOpenElementsContains(Common.ELEMENT_TEMPLATE_ID, Node.NAMESPACE_HTML_ID)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                TreeConstructorAftersBeforeInitialInHead.generateImpliedEndTagThoroughly(treeConstructor);
                if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_TEMPLATE_ID)) {
                    treeConstructor.emitParseError();
                }
                treeConstructor.popOpenElementsUntilWithHtmlNS("template");
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

    static void inHeadNoScript(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        final int chr = treeConstructor.getChr();
        if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_NOSCRIPT_ID, tagNameID)) {
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
            inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, Common.ELEMENT_BR_ID, tagNameID)) {
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
        for (; ; ) {
            Element current = treeConstructor.getCurrentNode();
            if (Node.NAMESPACE_HTML_ID == current.namespaceID && Common.isImpliedTagsThoroughly(current)) {
                treeConstructor.popCurrentNode();
                continue;
            } else {
                break;
            }
        }
    }
}
