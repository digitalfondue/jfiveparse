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
 * A resizable buffer that can be appended from both side
 * 
 * It does not compact on use, so I hope that it will not find cases that will
 * degenerate the internal buffer... :D
 */
class ResizableIntBuffer {
    private int[] buff = new int[32];
    private int start = 15;
    private int pos = 15;
    private boolean isEmpty = true;

    void addFirst(int c) {
        if (start == 0) {
            resizeForStart();
        }
        buff[--start] = c;

        isEmpty = length() == 0;
    }

    private void resizeForStart() {
        // we don't have space at the beginning...
        int[] buffNew = new int[buff.length * 2];
        System.arraycopy(buff, 0, buffNew, buff.length, buff.length);
        start += buff.length;
        pos += buff.length;
        buff = buffNew;
    }

    void add(int c) {
        if (pos >= buff.length) {
            resizeForEnd();
        }
        buff[pos++] = c;

        isEmpty = length() == 0;
    }

    private void resizeForEnd() {
        buff = Arrays.copyOf(buff, buff.length * 2 + 2);
    }

    int getCharAt(int offset) {
        return buff[start + offset - 1];
    }

    int length() {
        return pos - start;
    }

    int removeFirst() {
        int chr = buff[start++];
        isEmpty = length() == 0;
        return chr;
    }

    int getFirst() {
        return buff[start];
    }

    boolean isEmpty() {
        return isEmpty;
    }
}
