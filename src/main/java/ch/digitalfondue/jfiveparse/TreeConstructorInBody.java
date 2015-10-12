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
import static ch.digitalfondue.jfiveparse.TreeConstructor.genericRawTextElementParsing;

class TreeConstructorInBody {

    static void handleInBodyCharacter(TreeConstructor treeConstructor) {
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
            TreeConstructorInHeads.inHead(START_TAG, tagName, treeConstructor);
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
        case "menu":
        case "nav":
        case "ol":
        case "p":
        case "section":
        case "summary":
        case "ul":
            startAddressUl(treeConstructor);
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
        case "menuitem":
        case "param":
        case "source":
        case "track":
            startMenuitemTrack(treeConstructor);
            break;
        case "hr":
            startHr(treeConstructor);
            break;
        case "image":
            startImage(treeConstructor);
            break;
        case "isindex":
            startIsIndex(treeConstructor);
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
            treeConstructor.emitParseError();
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
        if (treeConstructor.hasElementInScope("ruby", Node.NAMESPACE_HTML)) {
            treeConstructor.generateImpliedEndTag("rtc", Node.NAMESPACE_HTML);
        }

        if (!(treeConstructor.getCurrentNode().is("ruby", Node.NAMESPACE_HTML) || treeConstructor.getCurrentNode().is("rtc", Node.NAMESPACE_HTML))) {
            treeConstructor.emitParseError();
        }

        treeConstructor.insertHtmlElementToken();
    }

    private static void startRbRtc(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInScope("ruby", Node.NAMESPACE_HTML)) {
            treeConstructor.generateImpliedEndTag();
        }

        if (!treeConstructor.getCurrentNode().is("ruby", Node.NAMESPACE_HTML)) {
            treeConstructor.emitParseError();
        }
        treeConstructor.insertHtmlElementToken();
    }

    private static void startOptgroupOption(TreeConstructor treeConstructor) {
        if (treeConstructor.getCurrentNode().is("option", Node.NAMESPACE_HTML)) {
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
        if (treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
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

    private static void startIsIndex(TreeConstructor treeConstructor) {

        treeConstructor.emitParseError();
        if (!treeConstructor.stackOfOpenElementsContains("template", Node.NAMESPACE_HTML) && treeConstructor.getForm() != null) {
            // ignore token
        } else {
            treeConstructor.ackSelfClosingTagIfSet();
            treeConstructor.framesetOkToFalse();
            if (treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
                treeConstructor.closePElement();
            }

            Element form = treeConstructor.insertHtmlElementWithEmptyAttributes("form");

            if (!treeConstructor.stackOfOpenElementsContains("template", Node.NAMESPACE_HTML)) {
                treeConstructor.setForm(form);
            }

            if (treeConstructor.hasAttribute("action")) {
                form.getAttributes().put(treeConstructor.getAttribute("action"));
            }

            treeConstructor.insertHtmlElementWithEmptyAttributes("hr");
            treeConstructor.popCurrentNode();// hr
            treeConstructor.insertHtmlElementWithEmptyAttributes("label");

            String firstStream = "This is a searchable index. Enter search keywords: ";
            if (treeConstructor.hasAttribute("prompt")) {
                firstStream = treeConstructor.getAttribute("prompt").getValue();
            }
            treeConstructor.insertCharacters(firstStream.toCharArray());

            Attributes attrs = treeConstructor.getAttributes().copy();
            attrs.remove("name");
            attrs.remove("action");
            attrs.remove("prompt");
            attrs.put(new Attribute("name", "isindex"));

            treeConstructor.insertElementToken("input", Node.NAMESPACE_HTML, attrs);
            treeConstructor.popCurrentNode();// input

            treeConstructor.popCurrentNode();// label

            treeConstructor.insertHtmlElementWithEmptyAttributes("hr");
            treeConstructor.popCurrentNode();// hr

            treeConstructor.popCurrentNode();// form

            if (!treeConstructor.stackOfOpenElementsContains("template", Node.NAMESPACE_HTML)) {
                treeConstructor.setForm(null);
            }
        }
    }

    private static void startImage(TreeConstructor treeConstructor) {
        treeConstructor.emitParseError();
        treeConstructor.setTagName("img");
        treeConstructor.dispatch();
    }

    private static void startHr(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
            treeConstructor.closePElement();
        }
        treeConstructor.insertHtmlElementToken();
        treeConstructor.popCurrentNode();
        treeConstructor.ackSelfClosingTagIfSet();
        treeConstructor.framesetOkToFalse();
    }

    private static void startMenuitemTrack(TreeConstructor treeConstructor) {
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
        if (!hasTypeAttr || (hasTypeAttr && !"hidden".equalsIgnoreCase(element.getAttributes().get("type").getValue()))) {
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
        if (!treeConstructor.isQuirksMode() && treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
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
        if (treeConstructor.hasElementInScope("nobr", Node.NAMESPACE_HTML)) {
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
        final int aIdx = treeConstructor.getIndexInActiveFormattingElementsBetween("a", Node.NAMESPACE_HTML);
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
        if (treeConstructor.hasElementInButtonScope("button", Node.NAMESPACE_HTML)) {
            treeConstructor.emitParseError();
            treeConstructor.generateImpliedEndTag();
            treeConstructor.popOpenElementsUntil("button", Node.NAMESPACE_HTML);
        }

        treeConstructor.reconstructActiveFormattingElements();
        treeConstructor.insertHtmlElementToken();
        treeConstructor.framesetOkToFalse();
    }

    private static void startPlaintext(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
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
            if (node.is("dd", Node.NAMESPACE_HTML)) {
                treeConstructor.generateImpliedEndTag("dd", Node.NAMESPACE_HTML);
                if (treeConstructor.getCurrentNode().is("dd", Node.NAMESPACE_HTML)) {
                    treeConstructor.emitParseError();
                }
                treeConstructor.popOpenElementsUntil("dd", Node.NAMESPACE_HTML);
                break;
            }

            if (node.is("dt", Node.NAMESPACE_HTML)) {
                treeConstructor.generateImpliedEndTag("dt", Node.NAMESPACE_HTML);
                if (treeConstructor.getCurrentNode().is("dt", Node.NAMESPACE_HTML)) {
                    // parser error
                }
                treeConstructor.popOpenElementsUntil("dt", Node.NAMESPACE_HTML);
                break;
            }

            if (Common.isSpecialCategory(node.getNodeName(), node.getNamespaceURI()) && //
                    !(node.is("address", Node.NAMESPACE_HTML) || //
                            node.is("div", Node.NAMESPACE_HTML) || node.is("p", Node.NAMESPACE_HTML))) {
                break;
            }

            idx = idx - 1;
            node = treeConstructor.openElementAt(idx);
        }
        if (treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
            treeConstructor.closePElement();
        }

        treeConstructor.insertHtmlElementToken();
    }

    private static void startLi(TreeConstructor treeConstructor) {
        treeConstructor.framesetOkToFalse();

        int idx = treeConstructor.openElementsSize() - 1;
        Element node = treeConstructor.openElementAt(idx);
        while (true) {
            String nodeName = node.getNodeName();
            String nodeNameSpaceUri = node.getNamespaceURI();
            if ("li".equals(nodeName) && Node.NAMESPACE_HTML.equals(nodeNameSpaceUri)) {
                treeConstructor.generateImpliedEndTag("li", Node.NAMESPACE_HTML);
                if (!treeConstructor.getCurrentNode().is("li", Node.NAMESPACE_HTML)) {
                    treeConstructor.emitParseError();
                }

                treeConstructor.popOpenElementsUntil("li", Node.NAMESPACE_HTML);
                break;
            }

            if (Common.isSpecialCategory(nodeName, nodeNameSpaceUri) && //
                    !(node.is("address", Node.NAMESPACE_HTML) || //
                            node.is("div", Node.NAMESPACE_HTML) || node.is("p", Node.NAMESPACE_HTML))) {
                break;
            }
            idx = idx - 1;
            node = treeConstructor.openElementAt(idx);

        }

        if (treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
            treeConstructor.closePElement();
        }

        treeConstructor.insertHtmlElementToken();
    }

    private static void startForm(TreeConstructor treeConstructor) {
        boolean templateIsNotPresent = !treeConstructor.stackOfOpenElementsContains("template", Node.NAMESPACE_HTML);
        if (treeConstructor.getForm() != null && templateIsNotPresent) {
            treeConstructor.emitParseError();
            // ignore the token
        } else {
            if (treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
                treeConstructor.closePElement();
            }
            Element formElement = treeConstructor.insertHtmlElementToken();
            if (templateIsNotPresent) {
                treeConstructor.setForm(formElement);
            }
        }
    }

    private static void startPreListing(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
            treeConstructor.closePElement();
        }
        treeConstructor.insertHtmlElementToken();
        treeConstructor.ignoreCharacterTokenLF();
        ;
        treeConstructor.framesetOkToFalse();
    }

    private static void startH1H6(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
            treeConstructor.closePElement();
        }
        Element e = treeConstructor.getCurrentNode();
        if (Node.NAMESPACE_HTML.equals(e.getNamespaceURI()) && e.getNodeName().length() == 2 && //
                e.getNodeName().charAt(0) == 'h' && //
                (e.getNodeName().charAt(1) >= '1' && e.getNodeName().charAt(1) <= '6')) {
            treeConstructor.emitParseError();
            treeConstructor.popCurrentNode();
        }
        treeConstructor.insertHtmlElementToken();
    }

    static void startAddressUl(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
            treeConstructor.closePElement();
        }
        treeConstructor.insertHtmlElementToken();
    }

    private static void startFrameset(TreeConstructor treeConstructor) {
        treeConstructor.emitParseError();
        if (treeConstructor.openElementsSize() == 1 || !treeConstructor.openElementAt(1).is("body", Node.NAMESPACE_HTML)) {
            // ignore
        } else if (Boolean.FALSE.equals(treeConstructor.getFramesetOk())) {
            // ignore
        } else {

            Element second = treeConstructor.openElementAt(1);
            if (second.getParentNode() != null) {
                second.getParentNode().removeChild(second);
            }

            //
            while (!treeConstructor.getCurrentNode().is("html", Node.NAMESPACE_HTML)) {
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
                !treeConstructor.openElementAt(1).is("body", Node.NAMESPACE_HTML) || //
                treeConstructor.stackOfOpenElementsContains("template", Node.NAMESPACE_HTML)) {
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
        if (!treeConstructor.stackOfOpenElementsContains("template", Node.NAMESPACE_HTML)) {
            Element firstInserted = treeConstructor.openElementAt(0);
            for (String attr : treeConstructor.getKeySetOfAttributes()) {
                if (!firstInserted.getAttributes().containsKey(attr)) {
                    firstInserted.getAttributes().put(treeConstructor.getAttribute(attr));
                }
            }
        }
    }

    private static void inBodyStartTagAnythingElse(TreeConstructor treeConstructor) {
        treeConstructor.reconstructActiveFormattingElements();
        treeConstructor.insertHtmlElementToken();
    }

    private static void inBodyEndTag(String tagName, TreeConstructor treeConstructor) {
        switch (tagName) {
        case "template":
            TreeConstructorInHeads.inHead(END_TAG, tagName, treeConstructor);
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
        ;

        treeConstructor.reconstructActiveFormattingElements();
        treeConstructor.insertHtmlElementToken();
        treeConstructor.popCurrentNode();
        treeConstructor.ackSelfClosingTagIfSet();
        treeConstructor.framesetOkToFalse();
    }

    private static void endAppletObject(String tagName, TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope(tagName, Node.NAMESPACE_HTML)) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            treeConstructor.generateImpliedEndTag();
            if (!treeConstructor.getCurrentNode().is(tagName, Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
            }
            treeConstructor.popOpenElementsUntil(tagName, Node.NAMESPACE_HTML);
            treeConstructor.clearUpToLastMarkerActiveFormattingElements();
        }
    }

    private static void endH1H6(String tagName, TreeConstructor treeConstructor) {
        if (!(treeConstructor.hasElementInScope("h1", Node.NAMESPACE_HTML) || //
                treeConstructor.hasElementInScope("h2", Node.NAMESPACE_HTML) || //
                treeConstructor.hasElementInScope("h3", Node.NAMESPACE_HTML) || //
                treeConstructor.hasElementInScope("h4", Node.NAMESPACE_HTML) || //
                treeConstructor.hasElementInScope("h5", Node.NAMESPACE_HTML) || treeConstructor.hasElementInScope("h6", Node.NAMESPACE_HTML))) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            treeConstructor.generateImpliedEndTag();

            if (!treeConstructor.getCurrentNode().is(tagName, Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
            }

            while (true) {
                Element e = treeConstructor.popCurrentNode();
                if (e.is("h1", Node.NAMESPACE_HTML) || //
                        e.is("h2", Node.NAMESPACE_HTML) || //
                        e.is("h3", Node.NAMESPACE_HTML) || //
                        e.is("h4", Node.NAMESPACE_HTML) || //
                        e.is("h5", Node.NAMESPACE_HTML) || //
                        e.is("h6", Node.NAMESPACE_HTML)) {
                    break;
                }
            }
        }
    }

    private static void endDdDt(String tagName, TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope(tagName, Node.NAMESPACE_HTML)) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            treeConstructor.generateImpliedEndTag(tagName, Node.NAMESPACE_HTML);
            if (!treeConstructor.getCurrentNode().is(tagName, Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
            }
            treeConstructor.popOpenElementsUntil(tagName, Node.NAMESPACE_HTML);
        }
    }

    private static void endLi(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInListScope("li", Node.NAMESPACE_HTML)) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            treeConstructor.generateImpliedEndTag("li", Node.NAMESPACE_HTML);
            if (!treeConstructor.getCurrentNode().is("li", Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
            }
            treeConstructor.popOpenElementsUntil("li", Node.NAMESPACE_HTML);
        }
    }

    private static void endP(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInButtonScope("p", Node.NAMESPACE_HTML)) {
            treeConstructor.emitParseError();
            treeConstructor.insertHtmlElementWithEmptyAttributes("p");
        }
        treeConstructor.closePElement();
    }

    private static void endForm(TreeConstructor treeConstructor) {
        boolean templateIsNotPresent = !treeConstructor.stackOfOpenElementsContains("template", Node.NAMESPACE_HTML);
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
            if (!treeConstructor.hasElementInScope("form", Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
                // ignore token
            } else {
                treeConstructor.generateImpliedEndTag();
                if (!treeConstructor.getCurrentNode().is("form", Node.NAMESPACE_HTML)) {
                    treeConstructor.emitParseError();
                }
                treeConstructor.popOpenElementsUntil("form", Node.NAMESPACE_HTML);
            }
        }
    }

    private static void endAddressUl(String tagName, TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope(tagName, Node.NAMESPACE_HTML)) {
            treeConstructor.emitParseError();
        } else {
            treeConstructor.generateImpliedEndTag();
            Element currentNode = treeConstructor.getCurrentNode();
            if (!currentNode.getNamespaceURI().equals(Node.NAMESPACE_HTML) || !currentNode.getNodeName().equals(tagName)) {
                treeConstructor.emitParseError();
            }

            treeConstructor.popOpenElementsUntil(tagName, Node.NAMESPACE_HTML);
        }
    }

    private static void endHtml(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope("body", Node.NAMESPACE_HTML)) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            // FIXME
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_BODY);
            treeConstructor.dispatch();
        }
    }

    private static void endBody(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope("body", Node.NAMESPACE_HTML)) {
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
            TreeConstructorInTemplate.inTemplate(tokenType, tagName, treeConstructor);
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
            if (node.is(tagName, Node.NAMESPACE_HTML)) {
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
            } else if (Common.isSpecialCategory(node.getNodeName(), node.getNamespaceURI())) {
                treeConstructor.emitParseError();
                return;
            }

            idx = idx - 1;
            node = treeConstructor.openElementAt(idx);
        }
    }

}
