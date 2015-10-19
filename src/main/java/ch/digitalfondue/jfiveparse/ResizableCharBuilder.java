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

import java.util.Arrays;

/**
 * Resizable char buffer with some custom methods/properties.
 */
class ResizableCharBuilder {

    char[] buff = new char[16];
    int pos = 0;
    
    /* this field is accurate only after calling toLowerCase*/
    boolean containsUpperCase;

    ResizableCharBuilder() {
    }

    ResizableCharBuilder(String s) {
        for (char c : s.toCharArray()) {
            append(c);
        }
    }

    void append(char c) {
        if (pos >= buff.length) {
            buff = Arrays.copyOf(buff, buff.length * 2 + 2);
        }
        buff[pos++] = c;
    }

    String toLowerCase() {
        containsUpperCase = false;
        for (char c : buff) {
            if (Common.isUpperCaseASCIILetter(c)) {
                containsUpperCase = true;
                break;
            }
        }
        if (containsUpperCase) {
            return lowerCaseInternal();
        } else {
            return asString();
        }
    }

    private String lowerCaseInternal() {
        char[] newBuff = Arrays.copyOf(buff, pos);
        for (int i = 0; i < pos; i++) {
            if (Common.isUpperCaseASCIILetter(newBuff[i])) {
                newBuff[i] += 0x0020;
            }
        }
        return new String(newBuff);
    }

    String asString() {
        return new String(buff, 0, pos);
    }

    @Override
    public String toString() {
        return asString();
    }

    boolean same(ResizableCharBuilder cb) {
        return pos == cb.pos && Arrays.equals(buff, cb.buff);
    }
}
