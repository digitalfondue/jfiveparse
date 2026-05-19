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

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

/**
 * Wrapped and abstracted input. Can most likely be optimized.
 */
abstract class ProcessedInputStream {

    protected final ResizableIntBuffer buffer = new ResizableIntBuffer();

    protected abstract int read();

    static class StringProcessedInputStream extends ProcessedInputStream {
        protected int pos = 0;
        protected final char[] input;

        StringProcessedInputStream(String input) {
            this.input = normalize(input);
        }

        private static char[] normalize(String s) {
            char[] arr = s.toCharArray();
            int n = arr.length;
            int j = 0;
            for (int i = 0; i < n; i++) {
                char c = arr[i];
                if (c == '\r') {
                    arr[j++] = '\n';
                    if (i + 1 < n && arr[i + 1] == '\n') {
                        i++;
                    }
                } else {
                    arr[j++] = c;
                }
            }
            return j == n ? arr : Arrays.copyOf(arr, j);
        }

        @Override
        protected int read() {
            if (pos < input.length) {
                return input[pos++];
            }
            return -1;
        }
    }

    static final class ReaderProcessedInputStream extends ProcessedInputStream {

        private final Reader reader;
        private boolean crFound;

        ReaderProcessedInputStream(Reader reader) {
            this.reader = reader;
        }

        @Override
        protected int read() {
            try {
                int chr = reader.read();
                if (crFound) {
                    crFound = false;
                    if (chr == Characters.LF) {
                        chr = reader.read();
                    }
                }

                if (chr == Characters.CR) {
                    crFound = true;
                    chr = Characters.LF;
                }
                return chr;
            } catch (IOException ioe) {
                throw new ParserException(ioe);
            }
        }
    }

    //
    int peekNextInputCharacter(int offset) {
        if (buffer.length() < offset) {
            // fill buffer
            for (int i = buffer.length(); i < offset; i++) {
                buffer.add(read());
            }
        }
        return buffer.getCharAt(offset);
    }

    /**
     * Ideally, it's a combination of getNextInputCharacter + consume in terms of
     * behavior.
     * 
     * @return
     */
    int getNextInputCharacterAndConsume() {
        return consume();
    }

    int consume() {
        return buffer.isEmpty ? read() : buffer.removeFirst();
    }

    void reconsume(int chr) {
        buffer.addFirst(chr);
    }

    void consume(int count) {
        for (int i = 0; i < count; i++) {
            consume();
        }
    }
}
