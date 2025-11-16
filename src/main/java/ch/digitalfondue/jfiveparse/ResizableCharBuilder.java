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

import java.util.Arrays;

/**
 * Resizable char buffer with some custom methods/properties.
 */
final class ResizableCharBuilder {

    private char[] buff;
    private int pos = 0;

    /* this field is accurate only after calling toLowerCase */
    boolean containsUpperCase;

    ResizableCharBuilder() {
        buff = new char[16];
    }

    // reset and set the given string
    void set(String s) {
        buff = s.toCharArray();
        pos = buff.length;
        containsUpperCase = false;
    }

    void reset() {
        pos = 0;
        containsUpperCase = false;
    }

    char at(int i) {
        return buff[i];
    }

    int pos() {
        return pos;
    }

    void append(char c) {
        try {
            buff[pos++] = c;
        } catch (IndexOutOfBoundsException e) {
            buff = Arrays.copyOf(buff, buff.length * 2 + 2);
            buff[pos - 1] = c;
        }
    }

    String toLowerCase() {
        containsUpperCase = false;
        for (int i = 0; i < pos; i++) {
            if (Common.isUpperCaseASCIILetter(buff[i])) {
                containsUpperCase = true;
                return lowerCaseInternal();
            }
        }
        return toString();
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

    @Override
    public String toString() {
        return new String(buff, 0, pos);
    }

    boolean equalsASCIICaseInsensitive(char[] cb) {
        if (pos != cb.length) {
            return false;
        }
        for (int i = 0; i < pos; i++) {
            if (Common.toLowerCase(buff[i]) != Common.toLowerCase(cb[i])) {
                return false;
            }
        }
        return true;
    }

    char[] copyBackingCharArray() {
        return Arrays.copyOf(buff, pos);
    }


}
