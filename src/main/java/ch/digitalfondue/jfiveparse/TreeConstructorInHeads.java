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
import static ch.digitalfondue.jfiveparse.TreeConstructor.START_TAG;
import static ch.digitalfondue.jfiveparse.TreeConstructor.genericRCDataParsing;
import static ch.digitalfondue.jfiveparse.TreeConstructor.genericRawTextElementParsing;

import java.util.Arrays;

class TreeConstructorInHeads {

    static void inHead(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyAndForeignContent.inBody(tokenType, tagName, treeConstructor);
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
                generateImpliedEndTagThoroughly(treeConstructor);
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
            TreeConstructorInBodyAndForeignContent.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, "noscript", tagName)) {
            treeConstructor.popCurrentNode();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_HEAD);
        } else if ((tokenType == CHARACTER && (chr == Characters.TAB || //
                chr == Characters.LF || //
                chr == Characters.FF || chr == Characters.CR)) || //
                tokenType == COMMENT || //
                (tokenType == START_TAG && ("basefont".equals(tagName) || //
                        "bgsound".equals(tagName) || //
                        "link".equals(tagName) || //
                        "meta".equals(tagName) || //
                        "noframes".equals(tagName) || "style".equals(tagName)))) {
            TreeConstructorInHeads.inHead(tokenType, tagName, treeConstructor);
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
        while (true) {
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
