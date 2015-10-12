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
import static ch.digitalfondue.jfiveparse.TreeConstructor.END_TAG;
import static ch.digitalfondue.jfiveparse.TreeConstructor.EOF;

class TreeConstructorText {
    static void text(byte tokenType, String tagName, TreeConstructor treeConstructor) {
        switch (tokenType) {
        case CHARACTER:
            treeConstructor.insertCharacter();
            break;
        case EOF:
            textEof(treeConstructor);
            break;
        case END_TAG:
            textEndTag(tagName, treeConstructor);
            break;
        }
    }

    private static void textEndTag(String tagName, TreeConstructor treeConstructor) {
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
