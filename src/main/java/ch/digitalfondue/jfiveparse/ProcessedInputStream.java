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
 * Even though the html5 specification is working with codepoints, this input
 * stream will only emit chars and "-1".
 * 
 * This has some interesting consequences that we will need to fully explore:
 * <ul>
 * <li>Character position is not the same as the current position
 * <li>other unknown issues??
 * </ul>
 */
abstract class ProcessedInputStream {

    protected final ResizableIntBuffer buffer = new ResizableIntBuffer();

    protected abstract int getNext();

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
        protected int getNext() {
            if (pos < input.length) {
                return input[pos++];
            }
            return -1;
        }

        @Override
        protected int readUntilInternal(ResizableCharBuilder builder, boolean stopAtAmpersand, boolean stopAtLessThan) {
            int n = input.length;
            int i = pos;
            while (i < n) {
                char c = input[i];
                if ((stopAtAmpersand && c == '&') || (stopAtLessThan && c == '<') || c == '\0') {
                    builder.append(input, pos, i - pos);
                    pos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(input, pos, n - pos);
            pos = n;
            return -1;
        }

        @Override
        protected int readUntilAttributeValueInternal(ResizableCharBuilder builder, int quoteChar, boolean stopAtAmpersand) {
            int n = input.length;
            int i = pos;
            while (i < n) {
                char c = input[i];
                if (c == quoteChar || (stopAtAmpersand && c == '&') || c == '\0') {
                    builder.append(input, pos, i - pos);
                    pos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(input, pos, n - pos);
            pos = n;
            return -1;
        }

        @Override
        protected int readUntilAttributeValueUnquotedInternal(ResizableCharBuilder builder) {
            int n = input.length;
            int i = pos;
            while (i < n) {
                char c = input[i];
                if (Common.isTabLfFfCrOrSpace(c) || c == '&' || c == '>' || c == '\0' ||
                        c == '"' || c == '\'' || c == '<' || c == '=' || c == '`') {
                    builder.append(input, pos, i - pos);
                    pos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(input, pos, n - pos);
            pos = n;
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
        protected int getNext() {
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

    int peekNextInputCharacter(int offset) {
        if (buffer.length() < offset) {
            // fill buffer
            for (int i = buffer.length(); i < offset; i++) {
                buffer.add(getNext());
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
        return buffer.isEmpty ? getNext() : buffer.removeFirst();
    }

    int readUntil(ResizableCharBuilder builder, boolean stopAtAmpersand, boolean stopAtLessThan) {
        int chr;
        while (!buffer.isEmpty) {
            chr = buffer.removeFirst();
            if ((stopAtAmpersand && chr == '&') || (stopAtLessThan && chr == '<') || chr == '\0' || chr == -1) {
                return chr;
            }
            builder.append((char) chr);
        }
        return readUntilInternal(builder, stopAtAmpersand, stopAtLessThan);
    }

    int readUntilAttributeValue(ResizableCharBuilder builder, int quoteChar, boolean stopAtAmpersand) {
        int chr;
        while (!buffer.isEmpty) {
            chr = buffer.removeFirst();
            if (chr == quoteChar || (stopAtAmpersand && chr == '&') || chr == '\0' || chr == -1) {
                return chr;
            }
            builder.append((char) chr);
        }
        return readUntilAttributeValueInternal(builder, quoteChar, stopAtAmpersand);
    }

    int readUntilAttributeValueUnquoted(ResizableCharBuilder builder) {
        int chr;
        while (!buffer.isEmpty) {
            chr = buffer.removeFirst();
            if (Common.isTabLfFfCrOrSpace(chr) || chr == '&' || chr == '>' || chr == '\0' ||
                    chr == '"' || chr == '\'' || chr == '<' || chr == '=' || chr == '`' || chr == -1) {
                return chr;
            }
            builder.append((char) chr);
        }
        return readUntilAttributeValueUnquotedInternal(builder);
    }

    protected int readUntilInternal(ResizableCharBuilder builder, boolean stopAtAmpersand, boolean stopAtLessThan) {
        int chr;
        while ((chr = getNext()) != -1) {
            if ((stopAtAmpersand && chr == '&') || (stopAtLessThan && chr == '<') || chr == '\0') {
                return chr;
            }
            builder.append((char) chr);
        }
        return -1;
    }

    protected int readUntilAttributeValueInternal(ResizableCharBuilder builder, int quoteChar, boolean stopAtAmpersand) {
        int chr;
        while ((chr = getNext()) != -1) {
            if (chr == quoteChar || (stopAtAmpersand && chr == '&') || chr == '\0') {
                return chr;
            }
            builder.append((char) chr);
        }
        return -1;
    }

    protected int readUntilAttributeValueUnquotedInternal(ResizableCharBuilder builder) {
        int chr;
        while ((chr = getNext()) != -1) {
            if (Common.isTabLfFfCrOrSpace(chr) || chr == '&' || chr == '>' || chr == '\0' ||
                    chr == '"' || chr == '\'' || chr == '<' || chr == '=' || chr == '`') {
                return chr;
            }
            builder.append((char) chr);
        }
        return -1;
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
