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

class TreeConstructorInTemplate {

    private static void popPushSetAndDispatch(TreeConstructor treeConstructor, int insertionMode) {
        treeConstructor.popFromStackTemplatesInsertionMode();
        treeConstructor.pushInStackTemplatesInsertionMode(insertionMode);
        treeConstructor.setInsertionMode(insertionMode);
        treeConstructor.dispatch();
    }

    static void inTemplate(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER || tokenType == COMMENT || tokenType == DOCTYPE) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
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
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, treeConstructor);
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
            if (!treeConstructor.stackOfOpenElementsContains("template", Node.NAMESPACE_HTML)) {
                treeConstructor.stopParsing();
            } else {
                treeConstructor.emitParseError();
                treeConstructor.popOpenElementsUntil("template", Node.NAMESPACE_HTML);
                treeConstructor.clearUpToLastMarkerActiveFormattingElements();
                treeConstructor.popFromStackTemplatesInsertionMode();
                treeConstructor.resetInsertionModeAppropriately();
                treeConstructor.dispatch();
            }
        }
    }
}
