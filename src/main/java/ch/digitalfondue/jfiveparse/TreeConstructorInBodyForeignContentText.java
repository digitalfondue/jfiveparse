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

import static ch.digitalfondue.jfiveparse.Common.*;
import static ch.digitalfondue.jfiveparse.TreeConstructor.CHARACTER;
import static ch.digitalfondue.jfiveparse.TreeConstructor.COMMENT;
import static ch.digitalfondue.jfiveparse.TreeConstructor.DOCTYPE;
import static ch.digitalfondue.jfiveparse.TreeConstructor.END_TAG;
import static ch.digitalfondue.jfiveparse.TreeConstructor.EOF;
import static ch.digitalfondue.jfiveparse.TreeConstructor.START_TAG;
import static ch.digitalfondue.jfiveparse.TreeConstructor.genericRawTextElementParsing;

import java.util.HashMap;
import java.util.Map;

class TreeConstructorInBodyForeignContentText {

    private static void handleInBodyCharacter(TreeConstructor treeConstructor) {
        int chr = treeConstructor.getChr();
        if (chr == Characters.NULL) {
            treeConstructor.emitParseError();
            // ignore
        } else {

            treeConstructor.reconstructActiveFormattingElements();

            treeConstructor.insertCharacter((char) chr);
            if (!Common.isTabLfFfCrOrSpace(chr)) {
                treeConstructor.framesetOkToFalse();
            }
        }
    }

    private static void inBodyStartTag(String tagName, TreeConstructor treeConstructor) {
        switch (tagName) {
        case "html":
            startHtml(treeConstructor);
            break;
        case "base":
        case "basefont":
        case "bgsound":
        case "link":
        case "meta":
        case "noframes":
        case "script":
        case "style":
        case "template":
        case "title":
            TreeConstructorAftersBeforeInitialInHead.inHead(START_TAG, tagName, treeConstructor);
            break;
        case "body":
            startBody(treeConstructor);
            break;
        case "frameset":
            startFrameset(treeConstructor);
            break;
        case "address":
        case "article":
        case "aside":
        case "blockquote":
        case "center":
        case "details":
        case "dialog":
        case "dir":
        case "div":
        case "dl":
        case "fieldset":
        case "figcaption":
        case "figure":
        case "footer":
        case "header":
        case "hgroup":
        case "main":
        case "nav":
        case "ol":
        case "p":
        case "search":
        case "section":
        case "summary":
        case "ul":
            startAddressUl(treeConstructor);
            break;
        case "menu":
        	startMenu(treeConstructor);
        	break;
        case "h1":
        case "h2":
        case "h3":
        case "h4":
        case "h5":
        case "h6":
            startH1H6(treeConstructor);
            break;
        case "pre":
        case "listing":
            startPreListing(treeConstructor);
            break;
        case "form":
            startForm(treeConstructor);
            break;
        case "li":
            startLi(treeConstructor);
            break;
        case "dd":
        case "dt":
            startDdDt(treeConstructor);
            break;
        case "plaintext":
            startPlaintext(treeConstructor);
            break;
        case "button":
            startButton(treeConstructor);
            break;
        case "a":
            startA(treeConstructor);
            break;
        case "b":
        case "big":
        case "code":
        case "em":
        case "font":
        case "i":
        case "s":
        case "small":
        case "strike":
        case "strong":
        case "tt":
        case "u":
            startBU(treeConstructor);
            break;
        case "nobr":
            startNobr(tagName, treeConstructor);
            break;
        case "applet":
        case "marquee":
        case "object":
            startAppletObject(treeConstructor);
            break;
        case "table":
            startTable(treeConstructor);
            break;
        case "area":
        case "br":
        case "embed":
        case "img":
        case "keygen":
        case "wbr":
            startAreaWbr(treeConstructor);
            break;
        case "input":
            startInput(treeConstructor);
            break;
        case "param":
        case "source":
        case "track":
            startParamTrack(treeConstructor);
            break;
        case "hr":
            startHr(treeConstructor);
            break;
        case "image":
            startImage(treeConstructor);
            break;
        case "textarea":
            startTextarea(treeConstructor);
            break;
        case "xmp":
            startXmp(treeConstructor);
            break;
        case "iframe":
            startIframe(treeConstructor);
            break;
        case "noembed":
        case "noscript":
            startNoembedNoscript(tagName, treeConstructor);
            break;
        case "select":
            startSelect(treeConstructor);
            break;
        case "optgroup":
        case "option":
            startOptgroupOption(treeConstructor);
            break;
        case "rb":
        case "rtc":
            startRbRtc(treeConstructor);
            break;
        case "rp":
        case "rt":
            startRpRt(treeConstructor);
            break;
        case "math":
            startMath(tagName, treeConstructor);
            break;
        case "svg":
            startSvg(tagName, treeConstructor);
            break;
        case "caption":
        case "col":
        case "colgroup":
        case "frame":
        case "head":
        case "tbody":
        case "td":
        case "tfoot":
        case "th":
        case "thead":
        case "tr":
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

	private static void startSvg(String tagName, TreeConstructor treeConstructor) {
        Attributes attrs = treeConstructor.getAttributes();
        Common.adjustSVGAttributes(attrs);
        Common.adjustForeignAttributes(attrs);

        treeConstructor.insertElementToken(tagName, Node.NAMESPACE_SVG, attrs);
        if (treeConstructor.isSelfClosing()) {
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        }
    }

    private static void startMath(String tagName, TreeConstructor treeConstructor) {
        treeConstructor.reconstructActiveFormattingElements();

        Attributes attrs = treeConstructor.getAttributes();
        Common.adjustMathMLAttributes(attrs);
        Common.adjustForeignAttributes(attrs);

        treeConstructor.insertElementToken(tagName, Node.NAMESPACE_MATHML, attrs);
        if (treeConstructor.isSelfClosing()) {
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        }
    }

    private static void startRpRt(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInScope("ruby")) {
            treeConstructor.generateImpliedEndTag("rtc", Node.NAMESPACE_HTML);
        }

        if (!(Common.isHtmlNS(treeConstructor.getCurrentNode(), "ruby") || Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_RTC_ID))) {
            treeConstructor.emitParseError();
        }

        treeConstructor.insertHtmlElementToken();
    }

    private static void startRbRtc(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInScope("ruby")) {
            treeConstructor.generateImpliedEndTag();
        }

        if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), "ruby")) {
            treeConstructor.emitParseError();
        }
        treeConstructor.insertHtmlElementToken();
    }

    private static void startOptgroupOption(TreeConstructor treeConstructor) {
        if (Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_OPTION_ID)) {
            treeConstructor.popCurrentNode();
        }

        treeConstructor.reconstructActiveFormattingElements();
        treeConstructor.insertHtmlElementToken();
    }

    private static void startSelect(TreeConstructor treeConstructor) {
        treeConstructor.reconstructActiveFormattingElements();
        treeConstructor.insertHtmlElementToken();
        treeConstructor.framesetOkToFalse();

        final int insertionMode = treeConstructor.getInsertionMode();

        if (insertionMode == TreeConstructionInsertionMode.IN_TABLE || //
                insertionMode == TreeConstructionInsertionMode.IN_CAPTION || //
                insertionMode == TreeConstructionInsertionMode.IN_TABLE_BODY || //
                insertionMode == TreeConstructionInsertionMode.IN_ROW || //
                insertionMode == TreeConstructionInsertionMode.IN_CELL) {
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_SELECT_IN_TABLE);
        } else {
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_SELECT);
        }
    }

    private static void startNoembedNoscript(String tagName, TreeConstructor treeConstructor) {
        if ("noscript".equals(tagName) && !treeConstructor.isScriptingFlag()) {
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
        treeConstructor.reconstructActiveFormattingElements();
        treeConstructor.framesetOkToFalse();
        genericRawTextElementParsing(treeConstructor);
    }

    private static void startTextarea(TreeConstructor treeConstructor) {
        treeConstructor.insertHtmlElementToken();

        treeConstructor.setTokenizerState(TokenizerState.RCDATA_STATE);
        treeConstructor.saveInsertionMode();

        treeConstructor.framesetOkToFalse();
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.TEXT);
        treeConstructor.ignoreCharacterTokenLF();
    }

    private static void startImage(TreeConstructor treeConstructor) {
        treeConstructor.emitParseError();
        treeConstructor.setTagName(new ResizableCharBuilder("img"));
        treeConstructor.dispatch();
    }

    private static void startHr(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.closePElement();
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

    private static void startInput(TreeConstructor treeConstructor) {

        treeConstructor.reconstructActiveFormattingElements();
        Element element = treeConstructor.insertHtmlElementToken();
        treeConstructor.popCurrentNode();
        treeConstructor.ackSelfClosingTagIfSet();
        boolean hasTypeAttr = element.getAttributes().containsKey("type");
        if (!hasTypeAttr || (!"hidden".equalsIgnoreCase(element.getAttributes().get("type").getValue()))) {
            treeConstructor.framesetOkToFalse();
        }
    }

    private static void startAreaWbr(TreeConstructor treeConstructor) {
        treeConstructor.reconstructActiveFormattingElements();
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
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_TABLE);
    }

    private static void startAppletObject(TreeConstructor treeConstructor) {
        treeConstructor.reconstructActiveFormattingElements();
        treeConstructor.insertHtmlElementToken();
        treeConstructor.insertMarkerInActiveFormattingElements();
        treeConstructor.framesetOkToFalse();
    }

    private static void startNobr(String tagName, TreeConstructor treeConstructor) {
        treeConstructor.reconstructActiveFormattingElements();
        if (treeConstructor.hasElementInScope("nobr")) {
            treeConstructor.emitParseError();
            treeConstructor.adoptionAgencyAlgorithm(tagName);
            treeConstructor.reconstructActiveFormattingElements();
        }
        Element nobr = treeConstructor.insertHtmlElementToken();
        treeConstructor.pushInActiveFormattingElements(nobr);
    }

    private static void startBU(TreeConstructor treeConstructor) {
        treeConstructor.reconstructActiveFormattingElements();
        Element element = treeConstructor.insertHtmlElementToken();

        treeConstructor.pushInActiveFormattingElements(element);
    }

    private static void startA(TreeConstructor treeConstructor) {
        final int aIdx = treeConstructor.getIndexInActiveFormattingElementsBetween(Common.ELEMENT_A_ID, Node.NAMESPACE_HTML_ID);
        if (aIdx != -1) {
            Element a = treeConstructor.getActiveFormattingElementAt(aIdx);
            treeConstructor.emitParseError();
            treeConstructor.adoptionAgencyAlgorithm("a");
            treeConstructor.removeInActiveFormattingElements(a);
            treeConstructor.removeFromOpenElements(a);
        }
        treeConstructor.reconstructActiveFormattingElements();
        Element createdA = treeConstructor.insertHtmlElementToken();
        treeConstructor.pushInActiveFormattingElements(createdA);
    }

    private static void startButton(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope(ELEMENT_BUTTON_ID)) {
            treeConstructor.emitParseError();
            treeConstructor.generateImpliedEndTag();
            treeConstructor.popOpenElementsUntilWithHtmlNS("button");
        }

        treeConstructor.reconstructActiveFormattingElements();
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
                treeConstructor.popOpenElementsUntilWithHtmlNS("dd");
                break;
            }

            if (Common.isHtmlNS(node, ELEMENT_DT_ID)) {
                treeConstructor.generateImpliedEndTag("dt", Node.NAMESPACE_HTML);
                if (Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_DT_ID)) {
                    // parser error
                }
                treeConstructor.popOpenElementsUntilWithHtmlNS("dt");
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

                treeConstructor.popOpenElementsUntilWithHtmlNS("li");
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
        boolean templateIsNotPresent = !treeConstructor.stackOfOpenElementsContains(Common.ELEMENT_TEMPLATE_ID, Node.NAMESPACE_HTML_ID);
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
        if (Node.NAMESPACE_HTML_ID == e.namespaceID && e.getNodeName().length() == 2 && //
                e.getNodeName().charAt(0) == 'h' && //
                (e.getNodeName().charAt(1) >= '1' && e.getNodeName().charAt(1) <= '6')) {
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
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_FRAMESET);
        }
    }

    private static void startBody(TreeConstructor treeConstructor) {
        treeConstructor.emitParseError();

        if (treeConstructor.openElementsSize() == 1 || //
                !Common.isHtmlNS(treeConstructor.openElementAt(1), ELEMENT_BODY_ID) || //
                treeConstructor.stackOfOpenElementsContains(Common.ELEMENT_TEMPLATE_ID, Node.NAMESPACE_HTML_ID)) {
            // ignore
        } else {
            treeConstructor.framesetOkToFalse();
            Element secondInserted = treeConstructor.openElementAt(1);
            for (String attr : treeConstructor.getKeySetOfAttributes()) {
                if (!secondInserted.getAttributes().containsKey(attr)) {
                    secondInserted.getAttributes().put(treeConstructor.getAttribute(attr));
                }
            }
        }
    }

    private static void startHtml(TreeConstructor treeConstructor) {
        treeConstructor.emitParseError();

        // we ignore the token if template is present
        if (!treeConstructor.stackOfOpenElementsContains(Common.ELEMENT_TEMPLATE_ID, Node.NAMESPACE_HTML_ID)) {
            Element firstInserted = treeConstructor.openElementAt(0);
            for (String attr : treeConstructor.getKeySetOfAttributes()) {
                if (!firstInserted.getAttributes().containsKey(attr)) {
                    firstInserted.getAttributes().put(treeConstructor.getAttribute(attr));
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
            treeConstructor.reconstructActiveFormattingElements();
            treeConstructor.insertHtmlElementToken();
        }
    }

    private static void inBodyEndTag(String tagName, TreeConstructor treeConstructor) {
        switch (tagName) {
        case "template":
            TreeConstructorAftersBeforeInitialInHead.inHead(END_TAG, tagName, treeConstructor);
            break;
        case "body":
            endBody(treeConstructor);
            break;
        case "html":
            endHtml(treeConstructor);
            break;
        case "address":
        case "article":
        case "aside":
        case "blockquote":
        case "button":
        case "center":
        case "details":
        case "dialog":
        case "dir":
        case "div":
        case "dl":
        case "fieldset":
        case "figcaption":
        case "figure":
        case "footer":
        case "header":
        case "hgroup":
        case "listing":
        case "main":
        case "menu":
        case "nav":
        case "ol":
        case "pre":
        case "search":
        case "section":
        case "summary":
        case "ul":
            endAddressUl(tagName, treeConstructor);
            break;
        case "form":
            endForm(treeConstructor);
            break;
        case "p":
            endP(treeConstructor);
            break;
        case "li":
            endLi(treeConstructor);
            break;
        case "dd":
        case "dt":
            endDdDt(tagName, treeConstructor);
            break;
        case "h1":
        case "h2":
        case "h3":
        case "h4":
        case "h5":
        case "h6":
            endH1H6(tagName, treeConstructor);
            break;
        case "a":
        case "b":
        case "big":
        case "code":
        case "em":
        case "font":
        case "i":
        case "nobr":
        case "s":
        case "small":
        case "strike":
        case "strong":
        case "tt":
        case "u":
            treeConstructor.adoptionAgencyAlgorithm(tagName);
            break;
        case "applet":
        case "marquee":
        case "object":
            endAppletObject(tagName, treeConstructor);
            break;
        case "br":
            endBr(treeConstructor);
            break;
        default:
            anyOtherEndTag(tagName, treeConstructor);
            break;
        }
    }

    private static void endBr(TreeConstructor treeConstructor) {
        treeConstructor.emitParseError();

        treeConstructor.removeAttributes();

        treeConstructor.reconstructActiveFormattingElements();
        treeConstructor.insertHtmlElementToken();
        treeConstructor.popCurrentNode();
        treeConstructor.ackSelfClosingTagIfSet();
        treeConstructor.framesetOkToFalse();
    }

    private static void endAppletObject(String tagName, TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope(tagName)) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            treeConstructor.generateImpliedEndTag();
            if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), tagName)) {
                treeConstructor.emitParseError();
            }
            treeConstructor.popOpenElementsUntilWithHtmlNS(tagName);
            treeConstructor.clearUpToLastMarkerActiveFormattingElements();
        }
    }

    private static void endH1H6(String tagName, TreeConstructor treeConstructor) {
        if (!(treeConstructor.hasElementInScope("h1") || //
                treeConstructor.hasElementInScope("h2") || //
                treeConstructor.hasElementInScope("h3") || //
                treeConstructor.hasElementInScope("h4") || //
                treeConstructor.hasElementInScope("h5") || treeConstructor.hasElementInScope("h6"))) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            treeConstructor.generateImpliedEndTag();

            if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), tagName)) {
                treeConstructor.emitParseError();
            }

            while (true) {
                Element e = treeConstructor.popCurrentNode();
                if (Common.isHtmlNSBetween(e, ELEMENT_H1_ID, ELEMENT_H6_ID)) {
                    break;
                }
            }
        }
    }

    private static void endDdDt(String tagName, TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope(tagName)) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            treeConstructor.generateImpliedEndTag(tagName, Node.NAMESPACE_HTML);
            if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), tagName)) {
                treeConstructor.emitParseError();
            }
            treeConstructor.popOpenElementsUntilWithHtmlNS(tagName);
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
            treeConstructor.popOpenElementsUntilWithHtmlNS("li");
        }
    }

    private static void endP(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInButtonScope(ELEMENT_P_ID)) {
            treeConstructor.emitParseError();
            treeConstructor.insertHtmlElementWithEmptyAttributes("p");
        }
        treeConstructor.closePElement();
    }

    private static void endForm(TreeConstructor treeConstructor) {
        boolean templateIsNotPresent = !treeConstructor.stackOfOpenElementsContains(Common.ELEMENT_TEMPLATE_ID, Node.NAMESPACE_HTML_ID);
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
            if (!treeConstructor.hasElementInScope("form")) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                treeConstructor.generateImpliedEndTag();
                if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_FORM_ID)) {
                    treeConstructor.emitParseError();
                }
                treeConstructor.popOpenElementsUntilWithHtmlNS("form");
            }
        }
    }

    private static void endAddressUl(String tagName, TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope(tagName)) {
            treeConstructor.emitParseError();
        } else {
            treeConstructor.generateImpliedEndTag();
            Element currentNode = treeConstructor.getCurrentNode();
            if (currentNode.namespaceID != Node.NAMESPACE_HTML_ID || !currentNode.getNodeName().equals(tagName)) {
                treeConstructor.emitParseError();
            }

            treeConstructor.popOpenElementsUntilWithHtmlNS(tagName);
        }
    }

    private static void endHtml(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope("body")) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            // FIXME
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_BODY);
            treeConstructor.dispatch();
        }
    }

    private static void endBody(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope("body")) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            // FIXME
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_BODY);
        }
    }

    static void inBody(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        switch (tokenType) {
        case CHARACTER:
            handleInBodyCharacter(treeConstructor);
            return;
        case COMMENT:
            treeConstructor.insertComment();
            return;
        case DOCTYPE:
            treeConstructor.emitParseError(); // ignore
            return;
        case EOF:
            inBodyEof(tokenType, tagName, treeConstructor);
            return;
        case END_TAG:
            inBodyEndTag(tagName, treeConstructor);
            break;
        case START_TAG:
            inBodyStartTag(tagName, treeConstructor);
        }
    }

    private static void inBodyEof(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (!treeConstructor.isStackTemplatesInsertionModeIsEmpty()) {
            TreeConstructorInFramesetSelectTemplate.inTemplate(tokenType, tagName, treeConstructor);
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
    }

    static void anyOtherEndTag(String tagName, TreeConstructor treeConstructor) {
        int idx = treeConstructor.openElementsSize() - 1;
        Element node = treeConstructor.openElementAt(idx);

        while (true) {
            if (Common.isHtmlNS(node, tagName)) {
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

    static void foreignContent(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && treeConstructor.getChr() == Characters.NULL) {
            treeConstructor.emitParseError();
            treeConstructor.insertCharacter(Characters.REPLACEMENT_CHARACTER);
        } else if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == CHARACTER) {
            treeConstructor.insertCharacter();
            treeConstructor.framesetOkToFalse();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore token
        } else if (tokenType == START_TAG && (("b".equals(tagName) || //
                "big".equals(tagName) || //
                "blockquote".equals(tagName) || //
                "body".equals(tagName) || //
                "br".equals(tagName) || //
                "center".equals(tagName) || //
                "code".equals(tagName) || //
                "dd".equals(tagName) || //
                "div".equals(tagName) || //
                "dl".equals(tagName) || //
                "dt".equals(tagName) || //
                "em".equals(tagName) || //
                "embed".equals(tagName) || //
                "h1".equals(tagName) || //
                "h2".equals(tagName) || //
                "h3".equals(tagName) || //
                "h4".equals(tagName) || //
                "h5".equals(tagName) || //
                "h6".equals(tagName) || //
                "head".equals(tagName) || //
                "hr".equals(tagName) || //
                "i".equals(tagName) || //
                "img".equals(tagName) || //
                "li".equals(tagName) || //
                "listing".equals(tagName) || //
                "menu".equals(tagName) || //
                "meta".equals(tagName) || //
                "nobr".equals(tagName) || //
                "ol".equals(tagName) || //
                "p".equals(tagName) || //
                "pre".equals(tagName) || //
                "ruby".equals(tagName) || //
                "s".equals(tagName) || //
                "small".equals(tagName) || //
                "span".equals(tagName) || //
                "strong".equals(tagName) || //
                "strike".equals(tagName) || //
                "sub".equals(tagName) || //
                "sup".equals(tagName) || //
                "table".equals(tagName) || //
                "tt".equals(tagName) || //
                "u".equals(tagName) || //
                "ul".equals(tagName) || //
                "var".equals(tagName)) || //
                ("font".equals(tagName) && (treeConstructor.hasAttribute("color") || //
                        treeConstructor.hasAttribute("face") || //
                /*    */treeConstructor.hasAttribute("size")))) ||
                (tokenType == END_TAG && ("br".equals(tagName) || "p".equals(tagName)))) {
            treeConstructor.emitParseError();

            while (true) {
                Element cur = treeConstructor.getCurrentNode();
                if (Node.NAMESPACE_HTML_ID == cur.namespaceID || isMathMLIntegrationPoint(cur) || isHtmlIntegrationPoint(cur)) {
                    break;
                }
                treeConstructor.popCurrentNode();
            }
            treeConstructor.insertionModeInHtmlContent();
        } else if (tokenType == START_TAG) {
            anyOtherStartTag(tagName, treeConstructor);
        } else if (tokenType == END_TAG && Common.is(treeConstructor.getCurrentNode(), ELEMENT_SCRIPT_ID, Node.NAMESPACE_SVG_ID)) {
            // we don't execute scripts
            treeConstructor.popCurrentNode();
        } else if (tokenType == END_TAG) {

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

        treeConstructor.insertElementToken(tagName, currentNode.getNamespaceURI(), treeConstructor.getAttributes());

        if (treeConstructor.isSelfClosing()) {
            // we don't execute scripts
            treeConstructor.popCurrentNode();
        }
    }


    private static final Map<String, String> SVG_ELEMENT_CASE_CORRECTION = new HashMap<>();
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

    static void text(byte tokenType, TreeConstructor treeConstructor) {
        switch (tokenType) {
        case CHARACTER:
            treeConstructor.insertCharacter();
            break;
        case EOF:
            textEof(treeConstructor);
            break;
        case END_TAG:
            textEndTag(treeConstructor);
            break;
        }
    }

    private static void textEndTag(TreeConstructor treeConstructor) {
        // if ("script".equals(tagName)) {
        // // TODO check
        // treeConstructor.popCurrentNode();
        // treeConstructor.insertionMode =
        // treeConstructor.originalInsertionMode;
        // } else {
        treeConstructor.popCurrentNode();
        treeConstructor.switchToOriginalInsertionMode();

        // }
    }

    private static void textEof(TreeConstructor treeConstructor) {
        // Element currentNode = treeConstructor.getCurrentNode();
        // if (currentNode != null &&
        // "script".equals(currentNode.getNodeName())) {
        // // "already started".TODO
        // }
        treeConstructor.popCurrentNode();
        treeConstructor.switchToOriginalInsertionMode();
        treeConstructor.dispatch();
    }
}
