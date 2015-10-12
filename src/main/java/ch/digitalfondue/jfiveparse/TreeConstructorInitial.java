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

class TreeConstructorInitial {
    static void initial(byte tokenType, String tagName, TreeConstructor treeConstructor) {

        switch (tokenType) {
        case CHARACTER:
            handleCharacters(treeConstructor);
            break;
        case COMMENT:
            treeConstructor.insertCommentToDocument();
            break;
        case DOCTYPE:
            handleDoctype(treeConstructor);
            break;
        case EOF:
            initialOthers(treeConstructor);
            break;
        case END_TAG:
            initialOthers(treeConstructor);
            break;
        case START_TAG:
            initialOthers(treeConstructor);
            break;
        }
    }

    private static void handleDoctype(TreeConstructor treeConstructor) {
        DocumentType doctype = treeConstructor.buildDocumentType();
        Document doc = treeConstructor.getDocument();
        doc.appendChild(doctype);
        doc.setDoctype(doctype);
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.BEFORE_HTML);
    }

    private static void handleCharacters(TreeConstructor treeConstructor) {
        if (Common.isTabLfFfCrOrSpace(treeConstructor.getChr())) {
            // ignore
        } else {
            initialOthers(treeConstructor);
        }
    }

    private static void initialOthers(TreeConstructor treeConstructor) {
        treeConstructor.setQuirksMode(true);
        treeConstructor.setInsertionMode(TreeConstructionInsertionMode.BEFORE_HTML);
        treeConstructor.dispatch();
    }
}
