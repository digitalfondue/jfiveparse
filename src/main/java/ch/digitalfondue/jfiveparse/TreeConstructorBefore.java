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
import static ch.digitalfondue.jfiveparse.TreeConstructor.emptyAttrs;

class TreeConstructorBefore {

    static void beforeHead(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        switch (tokenType) {
        case CHARACTER:
            handleCharacterHead(treeConstructor);
            break;
        case COMMENT:
            treeConstructor.insertComment();
            break;
        case DOCTYPE:
            treeConstructor.emitParseError();
            // ignore
            break;
        case EOF:
            anythingElseHead(treeConstructor);
            break;
        case END_TAG:
            handleEndTagHead(tagName, treeConstructor);
            break;
        case START_TAG:
            handleStartTagHead(tokenType, tagName, treeConstructor);
            break;
        }
    }

    private static void handleStartTagHead(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            TreeConstructorInBody.inBody(tokenType, tagName, treeConstructor);
        } else if (Common.isStartTagNamed(tokenType, "head", tagName)) {
            Element head = treeConstructor.insertHtmlElementToken();
            treeConstructor.setHead(head);
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_HEAD);
        } else {
            anythingElseHead(treeConstructor);
        }
    }

    private static void handleEndTagHead(String tagName, TreeConstructor treeConstructor) {
        if ((!"head".equals(tagName) && !"body".equals(tagName) && //
                !"html".equals(tagName) && !"br".equals(tagName))) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            anythingElseHead(treeConstructor);
        }
    }

    private static void handleCharacterHead(TreeConstructor treeConstructor) {
        if (Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            // ignore
        } else {
            anythingElseHead(treeConstructor);
        }
    }

    private static void anythingElseHead(TreeConstructor treeConstructor) {
        Element head = treeConstructor.insertHtmlElementWithEmptyAttributes("head");
        treeConstructor.setHead(head);
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.IN_HEAD);
        treeConstructor.dispatch();
    }

    static void beforeHtml(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        switch (tokenType) {
        case CHARACTER:
            handleCharacterHtml(treeConstructor);
            break;
        case COMMENT:
            treeConstructor.insertCommentToDocument();
            break;
        case DOCTYPE:
            treeConstructor.emitParseError();
            break;
        case EOF:
            anythingElseHtml(treeConstructor);
            break;
        case END_TAG:
            handleEndTagHtml(tagName, treeConstructor);
            break;
        case START_TAG:
            handleStartTagHtml(tokenType, tagName, treeConstructor);
            break;
        }
    }

    private static void handleStartTagHtml(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        if (Common.isStartTagNamed(tokenType, "html", tagName)) {
            Element html = TreeConstructor.buildElement(tagName, Node.NAMESPACE_HTML, treeConstructor.getAttributes(), false);
            treeConstructor.addToOpenElements(html);
            treeConstructor.getDocument().appendChild(html);
            treeConstructor.setInsertionMode(TreeConstructionInsertionMode.BEFORE_HEAD);
        } else {
            anythingElseHtml(treeConstructor);
        }
    }

    private static void handleEndTagHtml(String tagName, TreeConstructor treeConstructor) {
        if ((!"head".equals(tagName) && !"body".equals(tagName) && //
                !"html".equals(tagName) && !"br".equals(tagName))) {
            treeConstructor.emitParseError();
            // ignore
        } else {
            anythingElseHtml(treeConstructor);
        }
    }

    private static void handleCharacterHtml(TreeConstructor treeConstructor) {
        if (Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            // ignore
        } else {
            anythingElseHtml(treeConstructor);
        }
    }

    private static void anythingElseHtml(TreeConstructor treeConstructor) {
        Element html = TreeConstructor.buildElement("html", Node.NAMESPACE_HTML, emptyAttrs(), false);
        treeConstructor.addToOpenElements(html);
        treeConstructor.getDocument().appendChild(html);
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.BEFORE_HEAD);
        treeConstructor.dispatch();
    }
}
