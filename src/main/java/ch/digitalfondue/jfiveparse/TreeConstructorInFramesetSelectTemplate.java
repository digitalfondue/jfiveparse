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

import static ch.digitalfondue.jfiveparse.TreeConstructor.CHARACTER;
import static ch.digitalfondue.jfiveparse.TreeConstructor.COMMENT;
import static ch.digitalfondue.jfiveparse.TreeConstructor.DOCTYPE;
import static ch.digitalfondue.jfiveparse.TreeConstructor.END_TAG;
import static ch.digitalfondue.jfiveparse.TreeConstructor.EOF;
import static ch.digitalfondue.jfiveparse.TreeConstructor.START_TAG;

class TreeConstructorInFramesetSelectTemplate {

    static void inFrameset(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {

        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, "frameset", tagName)) {
            treeConstructor.insertHtmlElementToken();
        } else if (Common.isEndTagNamed(tokenType, "frameset", tagName)) {

            // TODO: should check if it's the root element and not only if it's
            // a html element?
            if (Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_HTML_ID)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popCurrentNode();
                if (!treeConstructor.isHtmlFragmentParsing() && !Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_FRAMESET_ID)) {
                    treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_FRAMESET);
                }
            }
        } else if (Common.isStartTagNamed(tokenType, "frame", tagName)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (Common.isStartTagNamed(tokenType, "noframes", tagName)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == EOF) {
            if (!Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_HTML_ID)) {
                treeConstructor.emitParseError();
            }
            treeConstructor.stopParsing();
        } else {
            treeConstructor.emitParseError();
            // ignore token
        }
    }
    
    static void inSelect(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && treeConstructor.getChr() == Characters.NULL) {
            treeConstructor.emitParseError();
            // ignore
        } else if (tokenType == CHARACTER) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, "option", tagName)) {
            if (Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_OPTION_ID)) {
                treeConstructor.popCurrentNode();
            }
            treeConstructor.insertHtmlElementToken();
        } else if (Common.isStartTagNamed(tokenType, "optgroup", tagName)) {
            if (Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_OPTION_ID)) {
                treeConstructor.popCurrentNode();
            }
            if (Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_OPTGROUP_ID)) {
                treeConstructor.popCurrentNode();
            }
            treeConstructor.insertHtmlElementToken();
        } else if (Common.isStartTagNamed(tokenType, "hr", tagName)) {
            // see https://github.com/html5lib/html5lib-tests/commit/55aa183097fa52bb1328cd93633be6f88159d4b8
            if (Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_OPTION_ID)) {
                treeConstructor.popCurrentNode();
            }
            if (Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_OPTGROUP_ID)) {
                treeConstructor.popCurrentNode();
            }
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (Common.isEndTagNamed(tokenType, "optgroup", tagName)) {

            if (Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_OPTION_ID)
                    && Common.isHtmlNS(treeConstructor.openElementAt(treeConstructor.openElementsSize() - 2), Common.ELEMENT_OPTGROUP_ID)) {
                treeConstructor.popCurrentNode();
            }

            if (Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_OPTGROUP_ID)) {
                treeConstructor.popCurrentNode();
            } else {
                treeConstructor.emitParseError();
                // ignore
            }

        } else if (Common.isEndTagNamed(tokenType, "option", tagName)) {
            if (Common.isHtmlNS(treeConstructor.getCurrentNode(), Common.ELEMENT_OPTION_ID)) {
                treeConstructor.popCurrentNode();
            } else {
                treeConstructor.emitParseError();
                // ignore
            }
        } else if (Common.isEndTagNamed(tokenType, "select", tagName)) {
            if (!treeConstructor.hasElementInSelectScope(Common.ELEMENT_SELECT_ID)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popOpenElementsUntilWithHtmlNS("select");
                treeConstructor.resetInsertionModeAppropriately();
            }
        } else if (Common.isStartTagNamed(tokenType, "select", tagName)) {
            treeConstructor.emitParseError();
            if (!treeConstructor.hasElementInSelectScope(Common.ELEMENT_SELECT_ID)) {
                // ignore
            } else {
                treeConstructor.popOpenElementsUntilWithHtmlNS("select");
                treeConstructor.resetInsertionModeAppropriately();
            }
        } else if (tokenType == START_TAG && ("input".equals(tagName) || //
                "keygen".equals(tagName) || //
                "textarea".equals(tagName))) {
            treeConstructor.emitParseError();
            if (!treeConstructor.hasElementInSelectScope(Common.ELEMENT_SELECT_ID)) {
                // ignore
            } else {
                treeConstructor.popOpenElementsUntilWithHtmlNS("select");
                treeConstructor.resetInsertionModeAppropriately();
                treeConstructor.dispatch();
            }
        } else if ((tokenType == START_TAG && ("script".equals(tagName) || //
                "template".equals(tagName)))
                || Common.isEndTagNamed(tokenType, "template", tagName)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == EOF) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else {
            treeConstructor.emitParseError();
            // ignore
        }
    }

    static void inSelectTable(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        boolean isCaptionOrRelatedTags = "caption".equals(tagName) || //
                "table".equals(tagName) || //
                "tbody".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "thead".equals(tagName) || //
                "tr".equals(tagName) || //
                "td".equals(tagName) || //
                "th".equals(tagName);
        if (tokenType == START_TAG && isCaptionOrRelatedTags) {
            treeConstructor.emitParseError();
            treeConstructor.popOpenElementsUntilWithHtmlNS("select");
            treeConstructor.resetInsertionModeAppropriately();
            treeConstructor.dispatch();
        } else if (tokenType == END_TAG && isCaptionOrRelatedTags) {
            treeConstructor.emitParseError();
            if (!treeConstructor.hasElementInTableScope(tagName)) {
                // ignore token
            } else {
                treeConstructor.popOpenElementsUntilWithHtmlNS("select");
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

    static void inTemplate(byte tokenType, String tagName, byte tagNameID, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER || tokenType == COMMENT || tokenType == DOCTYPE) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, tagNameID, treeConstructor);
        } else if ((tokenType == START_TAG && ("base".equals(tagName) || //
                "basefont".equals(tagName) || //
                "bgsound".equals(tagName) || //
                "link".equals(tagName) || //
                "meta".equals(tagName) || //
                "noframes".equals(tagName) || //
                "script".equals(tagName) || //
                "style".equals(tagName) || //
                "template".equals(tagName) || //
                "title".equals(tagName)))
                || Common.isEndTagNamed(tokenType, "template", tagName)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, tagNameID, treeConstructor);
        } else if (tokenType == START_TAG && ("caption".equals(tagName) || //
                "colgroup".equals(tagName) || //
                "tbody".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "thead".equals(tagName))) {
            popPushSetAndDispatch(treeConstructor, TreeConstructionInsertionMode.IN_TABLE);
        } else if (Common.isStartTagNamed(tokenType, "col", tagName)) {
            popPushSetAndDispatch(treeConstructor, TreeConstructionInsertionMode.IN_COLUMN_GROUP);
        } else if (Common.isStartTagNamed(tokenType, "tr", tagName)) {
            popPushSetAndDispatch(treeConstructor, TreeConstructionInsertionMode.IN_TABLE_BODY);
        } else if (tokenType == START_TAG && ("td".equals(tagName) || //
                "th".equals(tagName))) {
            popPushSetAndDispatch(treeConstructor, TreeConstructionInsertionMode.IN_ROW);
        } else if (tokenType == START_TAG) {
            popPushSetAndDispatch(treeConstructor, TreeConstructionInsertionMode.IN_BODY);
        } else if (tokenType == END_TAG) {
            treeConstructor.emitParseError();
            // ignore
        } else if (tokenType == EOF) {
            if (!treeConstructor.stackOfOpenElementsContains(Common.ELEMENT_TEMPLATE_ID, Node.NAMESPACE_HTML_ID)) {
                treeConstructor.stopParsing();
            } else {
                treeConstructor.emitParseError();
                treeConstructor.popOpenElementsUntilWithHtmlNS("template");
                treeConstructor.clearUpToLastMarkerActiveFormattingElements();
                treeConstructor.popFromStackTemplatesInsertionMode();
                treeConstructor.resetInsertionModeAppropriately();
                treeConstructor.dispatch();
            }
        }
    }
}
