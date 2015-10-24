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
import static ch.digitalfondue.jfiveparse.TreeConstructor.EOF;

class TreeConstructorInFrameset {

    static void inFrameset(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, "frameset", tagName)) {
            treeConstructor.insertHtmlElementToken();
        } else if (Common.isEndTagNamed(tokenType, "frameset", tagName)) {

            // TODO: should check if it's the root element and not only if it's
            // a html element?
            if (treeConstructor.getCurrentNode().is("html", Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popCurrentNode();
                if (!treeConstructor.isHtmlFragmentParsing() && !treeConstructor.getCurrentNode().is("frameset", Node.NAMESPACE_HTML)) {
                    treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_FRAMESET);
                }
            }
        } else if (Common.isStartTagNamed(tokenType, "frame", tagName)) {
            treeConstructor.insertHtmlElementToken();
            treeConstructor.popCurrentNode();
            treeConstructor.ackSelfClosingTagIfSet();
        } else if (Common.isStartTagNamed(tokenType, "noframes", tagName)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, treeConstructor);
        } else if (tokenType == EOF) {
            if (!treeConstructor.getCurrentNode().is("html", Node.NAMESPACE_HTML)) {
                treeConstructor.emitParseError();
            }
            treeConstructor.stopParsing();
        } else {
            treeConstructor.emitParseError();
            // ignore token
        }
    }
}
