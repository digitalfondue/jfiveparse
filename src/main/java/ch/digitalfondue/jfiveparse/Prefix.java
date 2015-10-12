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
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Trie. Because I'm lazy, the childs are inserted in a TreeMap, when the whole
 * structure is completely initialized, then TreeMap will be compacted in a
 * simple array (this will leave some empty spaces).
 */
class Prefix {
    char c;
    char[] chars;

    // temporary holder
    private TreeMap<Character, Prefix> tmpChilds;
    //
    private int offset;
    private Prefix[] childsCompacted;
    //
    private final Prefix parent;

    Prefix(Prefix prefix) {
        this.parent = prefix;
    }

    void compact() {
        if (childsCompacted == null && tmpChilds != null) {
            offset = tmpChilds.firstKey();

            childsCompacted = new Prefix[tmpChilds.lastKey() - offset + 1];
            for (Entry<Character, Prefix> kv : tmpChilds.entrySet()) {
                childsCompacted[kv.getKey().charValue() - offset] = kv.getValue();
            }
        }
        tmpChilds = null;

        if (childsCompacted != null) {
            for (Prefix p : childsCompacted) {
                if (p != null) {
                    p.compact();
                }
            }
        }
    }

    String getString() {
        StringBuilder sb = new StringBuilder();

        sb.append(c);

        Prefix p = parent;
        while (p != null) {
            sb.append(p.c);
            p = p.parent;
        }

        return sb.reverse().toString();
    }

    Prefix getCompleteParent() {
        Prefix p = parent;
        while (p != null) {
            if (p.isComplete()) {
                return p;
            }
            p = p.parent;
        }
        return null;
    }

    boolean hasParentComplete() {
        Prefix p = parent;
        while (p != null) {
            if (p.isComplete()) {
                return true;
            }
            p = p.parent;
        }
        return false;
    }

    void addWord(String s, int[] codepoints) {
        c = s.charAt(0);
        if (s.length() == 1) {
            List<Character> l = new ArrayList<>();
            for (int codePoint : codepoints) {
                for (char chr : Character.toChars(codePoint)) {
                    l.add(chr);
                }
            }

            this.chars = new char[l.size()];
            for (int i = 0; i < l.size(); i++) {
                chars[i] = l.get(i);
            }
        } else {
            char nextVal = s.charAt(1);

            if (tmpChilds == null) {
                tmpChilds = new TreeMap<>();
            }

            if (!tmpChilds.containsKey(nextVal)) {
                tmpChilds.put(nextVal, new Prefix(this));
            }
            tmpChilds.get(nextVal).addWord(s.substring(1), codepoints);
        }
    }

    boolean isComplete() {
        return chars != null;
    }

    Prefix getNode(char c) {
        if (childsCompacted != null) {
            int idx = c - offset;
            if (idx < 0 || idx >= childsCompacted.length) {
                return null;
            } else {
                return childsCompacted[c - offset];
            }
        } else {
            return null;
        }
    }
}