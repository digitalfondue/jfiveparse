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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface Token {

    class CharacterToken implements Token {
        final ResizableCharBuilder chr;

        CharacterToken(ResizableCharBuilder chr) {
            this.chr = chr;
        }

        @Override
        public String toString() {
            return Arrays.asList(TokenType.character, chr.asString()).toString();
        }
    }

    class CommentToken implements Token {
        final String comment;

        CommentToken(String comment) {
            this.comment = comment;
        }

        @Override
        public String toString() {
            return Arrays.asList(TokenType.comment, comment).toString();
        }
    }

    class DoctypeToken implements Token {

        final StringBuilder name;
        final StringBuilder publicId;
        final StringBuilder systemId;
        final boolean correctness;

        public DoctypeToken(StringBuilder name, StringBuilder publicId, StringBuilder systemId, boolean correctness) {
            this.name = name;
            this.publicId = publicId;
            this.systemId = systemId;
            this.correctness = correctness;
        }

        @Override
        public String toString() {
            return Arrays.asList(TokenType.doctype, name, publicId, systemId, correctness).toString();
        }
    }

    class StartTagToken implements Token {
        public StartTagToken(String name, Map<String, AttributeNode> attributes, boolean selfClosing) {
            this.name = name;
            this.attributes = attributes;
            this.selfClosing = selfClosing;
        }

        final String name;
        final Map<String, AttributeNode> attributes;
        final boolean selfClosing;

        @Override
        public String toString() {
            List<Object> reprs = new ArrayList<>();
            reprs.add(TokenType.startTag);
            reprs.add(name.toString());

            Map<String, String> converted = new LinkedHashMap<String, String>();
            for (Entry<String, AttributeNode> kv : attributes.entrySet()) {
                converted.put(kv.getKey(), kv.getValue().getValue());
            }

            reprs.add(converted);
            if (selfClosing) {
                reprs.add(true);
            }
            return reprs.toString();
        }
    }

    class EndTagToken implements Token {
        final String name;

        public EndTagToken(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return Arrays.asList(TokenType.endTag, name).toString();
        }
    }
}
