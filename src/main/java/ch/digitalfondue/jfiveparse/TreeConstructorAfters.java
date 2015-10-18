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

class TreeConstructorAfters {

    static void afterHead(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyAndForeignContent.inBody(tokenType, tagName, treeConstructor);
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
            TreeConstructorInHeads.inHead(tokenType, tagName, treeConstructor);
            treeConstructor.removeFromOpenElements(treeConstructor.getHead());
        } else if (Common.isEndTagNamed(tokenType, "template", tagName)) {
            TreeConstructorInHeads.inHead(tokenType, tagName, treeConstructor);
        } else if (tokenType == END_TAG && ("body".equals(tagName) || //
                "html".equals(tagName) || //
                "br".equals(tagName))) {
            // anything below
            treeConstructor.insertHtmlElementWithEmptyAttributes("body");
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_BODY);
            treeConstructor.dispatch();
        } else if (Common.isStartTagNamed(tokenType, "head", tagName) || tokenType == END_TAG) {
            treeConstructor.emitParseError();
            // ignore token
        } else {
            treeConstructor.insertHtmlElementWithEmptyAttributes("body");
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_BODY);
            treeConstructor.dispatch();
        }
    }

    static void afterBody(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            TreeConstructorInBodyAndForeignContent.inBody(tokenType, tagName, treeConstructor);
        } else if (tokenType == COMMENT) {
            treeConstructor.insertCommentToHtmlElement();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyAndForeignContent.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, "html", tagName)) {
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

    static void afterFrameset(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        if (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            treeConstructor.insertCharacter();
        } else if (tokenType == COMMENT) {
            treeConstructor.insertComment();
        } else if (tokenType == DOCTYPE) {
            treeConstructor.emitParseError();
            // ignore
        } else if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyAndForeignContent.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isEndTagNamed(tokenType, "html", tagName)) {
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.AFTER_AFTER_FRAMESET);
        } else if (Common.isStartTagNamed(tokenType, "noframes", tagName)) {
            TreeConstructorInHeads.inHead(tokenType, tagName, treeConstructor);
        } else if (tokenType == EOF) {
            treeConstructor.stopParsing();
        } else {
            treeConstructor.emitParseError();
            // ignore
        }
    }

    static void afterAfterBody(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == COMMENT) {
            treeConstructor.insertCommentToDocument();
        } else if (tokenType == DOCTYPE || //
                (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) || //
                Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBodyAndForeignContent.inBody(tokenType, tagName, treeConstructor);
        } else if (tokenType == EOF) {
            treeConstructor.stopParsing();
        } else {
            treeConstructor.emitParseError();
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_BODY);
            treeConstructor.dispatch();
        }
    }

    static void afterAfterFrameset(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (tokenType == COMMENT) {
            treeConstructor.insertCommentToDocument();
        } else if ((tokenType == DOCTYPE) || //
                (tokenType == CHARACTER && Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) || //
                (Common.isStartTagNamed(tokenType, "html", tagName))) {
            TreeConstructorInBodyAndForeignContent.inBody(tokenType, tagName, treeConstructor);
        } else if (tokenType == EOF) {
            treeConstructor.stopParsing();
        } else if (Common.isStartTagNamed(tokenType, "noframes", tagName)) {
            TreeConstructorInHeads.inHead(tokenType, tagName, treeConstructor);
        } else {
            treeConstructor.emitParseError();
            // ignore token
        }
    }
}
