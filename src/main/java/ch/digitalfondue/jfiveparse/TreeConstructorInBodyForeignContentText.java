/**
 * Copyright © 2015 digitalfondue (info@digitalfondue.ch)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import java.util.HashMap;
import java.util.Map;

import static ch.digitalfondue.jfiveparse.Common.*;
import static ch.digitalfondue.jfiveparse.TreeConstructor.*;

class TreeConstructorInBodyForeignContentText {

    private static void handleInBodyCharacter(TreeConstructor treeConstructor) {
        int chr = treeConstructor.getChr();
        if (chr != Characters.NULL) {
            treeConstructor.reconstructActiveFormattingElements();
            treeConstructor.insertCharacter((char) chr);
            if (!Common.isTabLfFfCrOrSpace(chr)) {
                treeConstructor.framesetOkToFalse();
            }
        } else {
            treeConstructor.emitParseError();
            // ignore
        }
    }

    private static void inBodyStartTag(String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        switch (tagNameID) {
            case ELEMENT_HTML_ID:
                startHtml(treeConstructor);
                break;
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
                TreeConstructorAftersBeforeInitialInHead.inHead(START_TAG, tagName, tagNameID, treeConstructor);
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
            case ELEMENT_H1_ID:
            case ELEMENT_H2_ID:
            case ELEMENT_H3_ID:
            case ELEMENT_H4_ID:
            case ELEMENT_H5_ID:
            case ELEMENT_H6_ID:
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
            case ELEMENT_PARAM_ID:
            case ELEMENT_SOURCE_ID:
            case ELEMENT_TRACK_ID:
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
            case ELEMENT_NOEMBED_ID:
            case ELEMENT_NOSCRIPT_ID:
                startNoembedNoscript(tagNameID, treeConstructor);
                break;
            case ELEMENT_SELECT_ID:
                startSelect(treeConstructor);
                break;
            case ELEMENT_OPTGROUP_ID:
            case ELEMENT_OPTION_ID:
                startOptgroupOption(treeConstructor);
                break;
            case ELEMENT_RB_ID:
            case ELEMENT_RTC_ID:
                startRbRtc(treeConstructor);
                break;
            case ELEMENT_RP_ID:
            case ELEMENT_RT_ID:
                startRpRt(treeConstructor);
                break;
            case ELEMENT_MATH_ID:
                startMath(tagName, treeConstructor);
                break;
            case ELEMENT_SVG_ID:
                startSvg(tagName, treeConstructor);
                break;
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

    private static void startSvg(String tagName, TreeConstructor treeConstructor) {
        Attributes attrs = treeConstructor.getAttributes();
        Common.adjustSVGAttributes(attrs);
        Common.adjustForeignAttributes(attrs);

        treeConstructor.insertElementToken(tagName, Node.NAMESPACE_SVG, Node.NAMESPACE_SVG_ID, attrs);
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

        treeConstructor.insertElementToken(tagName, Node.NAMESPACE_MATHML, Node.NAMESPACE_MATHML_ID, attrs);
        if (treeConstructor.isSelfClosing()) {
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        }
    }

    private static void startRpRt(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInScope(ELEMENT_RUBY_ID)) {
            treeConstructor.generateImpliedEndTag("rtc", Node.NAMESPACE_HTML);
        }

        if (!(Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_RUBY_ID) || Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_RTC_ID))) {
            treeConstructor.emitParseError();
        }

        treeConstructor.insertHtmlElementToken();
    }

    private static void startRbRtc(TreeConstructor treeConstructor) {
        if (treeConstructor.hasElementInScope(ELEMENT_RUBY_ID)) {
            treeConstructor.generateImpliedEndTag();
        }

        if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_RUBY_ID)) {
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

    private static void startNoembedNoscript(byte tagNameID, TreeConstructor treeConstructor) {
        if (ELEMENT_NOSCRIPT_ID == tagNameID && !treeConstructor.isScriptingFlag()) {
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

    private static void startNobr(TreeConstructor treeConstructor) {
        treeConstructor.reconstructActiveFormattingElements(); // we know it's NOBR
        if (treeConstructor.hasElementInScope(ELEMENT_NO_BR_ID)) {
            treeConstructor.emitParseError();
            treeConstructor.adoptionAgencyAlgorithm(ELEMENT_NO_BR_ID);
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
            treeConstructor.adoptionAgencyAlgorithm(ELEMENT_A_ID);
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
            treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_BUTTON_ID);
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
        if (Common.isHtmlNSBetween(e, ELEMENT_H1_ID, ELEMENT_H6_ID)) {
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

    private static void inBodyEndTag(String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        switch (tagNameID) {
            case ELEMENT_TEMPLATE_ID:
                TreeConstructorAftersBeforeInitialInHead.inHead(END_TAG, tagName, tagNameID, treeConstructor);
                break;
            case ELEMENT_BODY_ID:
                endBody(treeConstructor);
                break;
            case ELEMENT_HTML_ID:
                endHtml(treeConstructor);
                break;
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
            case ELEMENT_DD_ID:
            case ELEMENT_DT_ID:
                endDdDt(tagName, tagNameID, treeConstructor);
                break;
            case ELEMENT_H1_ID:
            case ELEMENT_H2_ID:
            case ELEMENT_H3_ID:
            case ELEMENT_H4_ID:
            case ELEMENT_H5_ID:
            case ELEMENT_H6_ID:
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

    private static void endAppletObject(byte tagNameID, TreeConstructor treeConstructor) {
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
            treeConstructor.clearUpToLastMarkerActiveFormattingElements();
        }
    }

    private static void endH1H6(byte tagNameID, TreeConstructor treeConstructor) {
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
                if (Common.isHtmlNSBetween(e, ELEMENT_H1_ID, ELEMENT_H6_ID)) {
                    break;
                }
            }
        }
    }

    // we know it's DD, DT
    private static void endDdDt(String tagName, byte tagNameID, TreeConstructor treeConstructor) {
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

    private static void endAddressUl(byte tagNameID, TreeConstructor treeConstructor) {
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
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_BODY);
            treeConstructor.dispatch();
        }
    }

    private static void endBody(TreeConstructor treeConstructor) {
        if (!treeConstructor.hasElementInScope(ELEMENT_BODY_ID)) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            // FIXME
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_BODY);
        }
    }

    static void inBody(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {

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
                inBodyEof(tokenType, tagName, tagNameID,treeConstructor);
                return;
            case END_TAG:
                inBodyEndTag(tagName, tagNameID, treeConstructor);
                break;
            case START_TAG:
                inBodyStartTag(tagName, tagNameID, treeConstructor);
        }
    }

    private static void inBodyEof(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        if (!treeConstructor.isStackTemplatesInsertionModeIsEmpty()) {
            TreeConstructorInFramesetSelectTemplate.inTemplate(tokenType, tagName, tagNameID, treeConstructor);
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

    static void foreignContent(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
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

        treeConstructor.insertElementToken(tagName, currentNode.getNamespaceURI(), currentNode.namespaceID, treeConstructor.getAttributes());

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
