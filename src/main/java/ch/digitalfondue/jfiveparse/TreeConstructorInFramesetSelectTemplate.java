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
import static ch.digitalfondue.jfiveparse.TreeConstructor.*;

class TreeConstructorInFramesetSelectTemplate {

    static void inFrameset(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {

        if (tokenType == TT_CHARACTER && isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == TT_COMMENT) {
            treeConstructor.insertComment();
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
                if (!treeConstructor.isHtmlFragmentParsing() && !isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_FRAMESET_ID)) {
                    treeConstructor.setInsertionMode(IM_AFTER_FRAMESET);
                }
            }
        } else if (isStartTagNamed(tokenType, ELEMENT_FRAME_ID, tagNameID)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (isStartTagNamed(tokenType, ELEMENT_NOFRAMES_ID, tagNameID)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
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
    
    static void inSelect(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == TT_CHARACTER && treeConstructor.getChr() == Characters.NULL) {
            treeConstructor.emitParseError();
            // ignore
        } else if (tokenType == TT_CHARACTER) {
            treeConstructor.insertCharacter();
        } else if (tokenType == TT_COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == TT_DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (isStartTagNamed(tokenType, ELEMENT_HTML_ID, tagNameID)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (isStartTagNamed(tokenType, ELEMENT_OPTION_ID, tagNameID)) {
            if (isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_OPTION_ID)) {
                treeConstructor.popCurrentNode();
            }
            treeConstructor.insertHtmlElementToken();
        } else if (isStartTagNamed(tokenType, ELEMENT_OPTGROUP_ID, tagNameID)) {
            if (isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_OPTION_ID)) {
                treeConstructor.popCurrentNode();
            }
            if (isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_OPTGROUP_ID)) {
                treeConstructor.popCurrentNode();
            }
            treeConstructor.insertHtmlElementToken();
        } else if (isStartTagNamed(tokenType, ELEMENT_HR_ID, tagNameID)) {
            // see https://github.com/html5lib/html5lib-tests/commit/55aa183097fa52bb1328cd93633be6f88159d4b8
            if (isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_OPTION_ID)) {
                treeConstructor.popCurrentNode();
            }
            if (isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_OPTGROUP_ID)) {
                treeConstructor.popCurrentNode();
            }
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (isEndTagNamed(tokenType, ELEMENT_OPTGROUP_ID, tagNameID)) {

            if (isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_OPTION_ID)
                    && isHtmlNS(treeConstructor.openElementAt(treeConstructor.openElementsSize() - 2), ELEMENT_OPTGROUP_ID)) {
                treeConstructor.popCurrentNode();
            }

            if (isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_OPTGROUP_ID)) {
                treeConstructor.popCurrentNode();
            } else {
                treeConstructor.emitParseError();
                // ignore
            }

        } else if (isEndTagNamed(tokenType, ELEMENT_OPTION_ID, tagNameID)) {
            if (isHtmlNS(treeConstructor.getCurrentNode(), ELEMENT_OPTION_ID)) {
                treeConstructor.popCurrentNode();
            } else {
                treeConstructor.emitParseError();
                // ignore
            }
        } else if (isEndTagNamed(tokenType, ELEMENT_SELECT_ID, tagNameID)) {
            if (!treeConstructor.hasElementInSelectScope(ELEMENT_SELECT_ID)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_SELECT_ID);
                treeConstructor.resetInsertionModeAppropriately();
            }
        } else if (isStartTagNamed(tokenType, ELEMENT_SELECT_ID, tagNameID)) {
            treeConstructor.emitParseError();
            if (!treeConstructor.hasElementInSelectScope(ELEMENT_SELECT_ID)) {
                // ignore
            } else {
                treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_SELECT_ID);
                treeConstructor.resetInsertionModeAppropriately();
            }
        } else if (tokenType == TT_START_TAG && (ELEMENT_INPUT_ID == tagNameID || ELEMENT_KEYGEN_ID == tagNameID || ELEMENT_TEXTAREA_ID == tagNameID)) {
            treeConstructor.emitParseError();
            if (!treeConstructor.hasElementInSelectScope(ELEMENT_SELECT_ID)) {
                // ignore
            } else {
                treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_SELECT_ID);
                treeConstructor.resetInsertionModeAppropriately();
                treeConstructor.dispatch();
            }
        } else if ((tokenType == TT_START_TAG && (ELEMENT_SCRIPT_ID == tagNameID || ELEMENT_TEMPLATE_ID == tagNameID)) || isEndTagNamed(tokenType, ELEMENT_TEMPLATE_ID, tagNameID)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == TT_EOF) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else {
            treeConstructor.emitParseError();
            // ignore
        }
    }

    static void inSelectTable(int tokenType, String tagName, int tagNameID, TreeConstructor treeConstructor) {
        boolean isCaptionOrRelatedTags = ELEMENT_CAPTION_ID == tagNameID || //
                ELEMENT_TABLE_ID == tagNameID || //
                ELEMENT_TBODY_ID == tagNameID || //
                ELEMENT_TFOOT_ID == tagNameID || //
                ELEMENT_THEAD_ID == tagNameID || //
                ELEMENT_TR_ID == tagNameID || //
                ELEMENT_TD_ID == tagNameID || //
                ELEMENT_TH_ID == tagNameID;
        if (tokenType == TT_START_TAG && isCaptionOrRelatedTags) {
            treeConstructor.emitParseError();
            treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_SELECT_ID);
            treeConstructor.resetInsertionModeAppropriately();
            treeConstructor.dispatch();
        } else if (tokenType == TT_END_TAG && isCaptionOrRelatedTags) {
            treeConstructor.emitParseError();
            if (!treeConstructor.hasElementInTableScope(tagNameID)) { // known ID
                // ignore token
            } else {
                treeConstructor.popOpenElementsUntilWithHtmlNS(ELEMENT_SELECT_ID);
                treeConstructor.resetInsertionModeAppropriately();
                treeConstructor.dispatch();
            }
        } else {
            inSelect(tokenType, tagName, tagNameID, treeConstructor);
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
        if (tokenType == TT_CHARACTER || tokenType == TT_COMMENT || tokenType == TT_DOCTYPE) {
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
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
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
}
