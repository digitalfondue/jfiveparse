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

class TreeConstructorInSelect {

    static void inSelect(byte tokenType, String tagName, TreeConstructor treeConstructor) {
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
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, "option", tagName)) {
            if (treeConstructor.getCurrentNode().is("option", Node.NAMESPACE_HTML)) {
                treeConstructor.popCurrentNode();
            }
            treeConstructor.insertHtmlElementToken();
        } else if (Common.isStartTagNamed(tokenType, "optgroup", tagName)) {
            if (treeConstructor.getCurrentNode().is("option", Node.NAMESPACE_HTML)) {
                treeConstructor.popCurrentNode();
            }
            if (treeConstructor.getCurrentNode().is("optgroup", Node.NAMESPACE_HTML)) {
                treeConstructor.popCurrentNode();
            }
            treeConstructor.insertHtmlElementToken();
        } else if (Common.isEndTagNamed(tokenType, "optgroup", tagName)) {

            if (treeConstructor.getCurrentNode().is("option", Node.NAMESPACE_HTML)
                    && treeConstructor.openElementAt(treeConstructor.openElementsSize() - 2).is("optgroup", Node.NAMESPACE_HTML)) {
                treeConstructor.popCurrentNode();
            }

            if (treeConstructor.getCurrentNode().is("optgroup", Node.NAMESPACE_HTML)) {
                treeConstructor.popCurrentNode();
            } else {
                treeConstructor.emitParseError();
                // ignore
            }

        } else if (Common.isEndTagNamed(tokenType, "option", tagName)) {
            if (treeConstructor.getCurrentNode().is("option", Node.NAMESPACE_HTML)) {
                treeConstructor.popCurrentNode();
            } else {
                treeConstructor.emitParseError();
                // ignore
            }
        } else if (Common.isEndTagNamed(tokenType, "select", tagName)) {
            if (!treeConstructor.hasElementInSelectScope("select")) {
                treeConstructor.emitParseError();
                // ignore
            } else {
                treeConstructor.popOpenElementsUntil("select", Node.NAMESPACE_HTML);
                treeConstructor.resetInsertionModeAppropriately();
            }
        } else if (Common.isStartTagNamed(tokenType, "select", tagName)) {
            treeConstructor.emitParseError();
            if (!treeConstructor.hasElementInSelectScope("select")) {
                // ignore
            } else {
                treeConstructor.popOpenElementsUntil("select", Node.NAMESPACE_HTML);
                treeConstructor.resetInsertionModeAppropriately();
            }
        } else if (tokenType == START_TAG && ("input".equals(tagName) || //
                "keygen".equals(tagName) || //
                "textarea".equals(tagName))) {
            treeConstructor.emitParseError();
            if (!treeConstructor.hasElementInSelectScope("select")) {
                // ignore
            } else {
                treeConstructor.popOpenElementsUntil("select", Node.NAMESPACE_HTML);
                treeConstructor.resetInsertionModeAppropriately();
                treeConstructor.dispatch();
            }
        } else if ((tokenType == START_TAG && ("script".equals(tagName) || //
                "template".equals(tagName)))
                || Common.isEndTagNamed(tokenType, "template", tagName)) {
            TreeConstructorAftersBeforeInitialInHead.inHead(tokenType, tagName, treeConstructor);
        } else if (tokenType == EOF) {
            TreeConstructorInBodyForeignContentText.inBody(tokenType, tagName, treeConstructor);
        } else {
            treeConstructor.emitParseError();
            // ignore
        }
    }

    static void inSelectTable(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == START_TAG && "caption".equals(tagName) || //
                "table".equals(tagName) || //
                "tbody".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "thead".equals(tagName) || //
                "tr".equals(tagName) || //
                "td".equals(tagName) || //
                "th".equals(tagName)) {
            treeConstructor.emitParseError();
            treeConstructor.popOpenElementsUntil("select", Node.NAMESPACE_HTML);
            treeConstructor.resetInsertionModeAppropriately();
            treeConstructor.dispatch();
        } else if (tokenType == END_TAG && "caption".equals(tagName) || //
                "table".equals(tagName) || //
                "tbody".equals(tagName) || //
                "tfoot".equals(tagName) || //
                "thead".equals(tagName) || //
                "tr".equals(tagName) || //
                "td".equals(tagName) || //
                "th".equals(tagName)) {
            treeConstructor.emitParseError();
            if (!treeConstructor.hasElementInTableScope(tagName)) {
                // ignore token
            } else {
                treeConstructor.popOpenElementsUntil("select", Node.NAMESPACE_HTML);
                treeConstructor.resetInsertionModeAppropriately();
                treeConstructor.dispatch();
            }
        } else {
            TreeConstructorInSelect.inSelect(tokenType, tagName, treeConstructor);
        }
    }
}
