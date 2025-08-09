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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class TokenSaver extends TreeConstructor {

    final List<Token> tokens = new ArrayList<>();

    public TokenSaver() {
        super(false, false);
    }

    @Override
    public void emitParseError() {
    }

    @Override
    public void emitCharacter(char chr) {
        ResizableCharBuilder cb = new ResizableCharBuilder();
        cb.append(chr);
        tokens.add(new Token.CharacterToken(cb));
    }

    @Override
    public void emitComment(ResizableCharBuilder comment) {
        tokens.add(new Token.CommentToken(comment.asString()));
    }

    @Override
    public void emitDoctypeToken(StringBuilder doctypeName, StringBuilder doctypePublicId, StringBuilder doctypeSystemId, boolean correctness) {
        tokens.add(new Token.DoctypeToken(doctypeName, doctypePublicId, doctypeSystemId, correctness));
    }

    @Override
    public void emitEOF() {
        throw new StopParse();
    }

    @Override
    public void emitEndTagToken(ResizableCharBuilder name) {
        // ENSURE that name.toLowerCase() is called in the TreeConstructor!
        tokens.add(new Token.EndTagToken(name.toLowerCase()));
    }

    @Override
    public void emitStartTagToken(ResizableCharBuilder name, Attributes attrs, boolean selfClosing) {
        Map<String, AttributeNode> m = new LinkedHashMap<>();
        if (attrs != null) {
            for (String key : attrs.keySet()) {
                m.put(key, attrs.get(key));
            }
        }
        // ENSURE that name.toLowerCase() is called in the TreeConstructor!
        tokens.add(new Token.StartTagToken(name.toLowerCase(), m, selfClosing));
    }

    public List<Token> getTokens() {
        return tokens;
    }

    @Override
    public Element getAdjustedCurrentNode() {
        return null;
    }

    @Override
    public Document getDocument() {
        // TODO Auto-generated method stub
        return null;
    }

}
