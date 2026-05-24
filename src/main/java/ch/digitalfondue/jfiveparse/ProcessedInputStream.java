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

/**
 * Wrapped and abstracted input. Can most likely be optimized.
 */
abstract class ProcessedInputStream {

    protected final ResizableIntBuffer buffer = new ResizableIntBuffer();

    protected abstract int read();

    int readUntil(ResizableCharBuilder builder, boolean stopAtAmpersand, boolean stopAtLessThan) {
        int chr;
        while (!buffer.isEmpty) {
            chr = buffer.removeFirst();
            if ((stopAtAmpersand && chr == Characters.AMPERSAND) || (stopAtLessThan && chr == Characters.LESSTHAN_SIGN) || chr == Characters.NULL || chr == Characters.EOF) {
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
            if (chr == quoteChar || (stopAtAmpersand && chr == Characters.AMPERSAND) || chr == Characters.NULL || chr == Characters.EOF) {
                return chr;
            }
            builder.append((char) chr);
        }
        return readUntilAttributeValueInternal(builder, quoteChar, stopAtAmpersand);
    }

    private static boolean mustStopReadUntilAttributeValueUnquoted(int chr) {
        return Common.isTabLfFfCrOrSpace(chr) || chr == Characters.AMPERSAND || chr == Characters.GREATERTHAN_SIGN
                || chr == Characters.NULL || chr == Characters.QUOTATION_MARK ||
                chr == Characters.APOSTROPHE || chr == Characters.LESSTHAN_SIGN ||
                chr == Characters.EQUALS_SIGN || chr == Characters.GRAVE_ACCENT || chr == Characters.EOF;
    }

    int readUntilAttributeValueUnquoted(ResizableCharBuilder builder) {
        int chr;
        while (!buffer.isEmpty) {
            chr = buffer.removeFirst();
            if (mustStopReadUntilAttributeValueUnquoted(chr)) {
                return chr;
            }
            builder.append((char) chr);
        }
        return readUntilAttributeValueUnquotedInternal(builder);
    }

    int readUntilTagName(ResizableCharBuilder builder) {
        int chr;
        while (!buffer.isEmpty) {
            chr = buffer.removeFirst();
            if (Common.isTabLfFfCrOrSpace(chr) || chr == Characters.SOLIDUS || chr == Characters.GREATERTHAN_SIGN || chr == Characters.NULL || chr == Characters.EOF) {
                return chr;
            }
            builder.append((char) chr);
        }
        return readUntilTagNameInternal(builder);
    }

    private static boolean mustStopReadUntilAttributeName(int chr) {
        return Common.isTabLfFfCrOrSpace(chr) || chr == Characters.SOLIDUS || chr == Characters.EQUALS_SIGN || chr == Characters.GREATERTHAN_SIGN || chr == Characters.NULL ||
                chr == Characters.QUOTATION_MARK || chr == Characters.APOSTROPHE || chr == Characters.LESSTHAN_SIGN || chr == Characters.EOF;
    }

    int readUntilAttributeName(ResizableCharBuilder builder) {
        int chr;
        while (!buffer.isEmpty) {
            chr = buffer.removeFirst();
            if (mustStopReadUntilAttributeName(chr)) {
                return chr;
            }
            builder.append((char) chr);
        }
        return readUntilAttributeNameInternal(builder);
    }

    int readUntilComment(ResizableCharBuilder builder) {
        int chr;
        while (!buffer.isEmpty) {
            chr = buffer.removeFirst();
            if (chr == Characters.HYPHEN_MINUS || chr == Characters.NULL || chr == Characters.EOF) {
                return chr;
            }
            builder.append((char) chr);
        }
        return readUntilCommentInternal(builder);
    }

    protected int readUntilInternal(ResizableCharBuilder builder, boolean stopAtAmpersand, boolean stopAtLessThan) {
        int chr;
        while ((chr = read()) != Characters.EOF) {
            if ((stopAtAmpersand && chr == Characters.AMPERSAND) || (stopAtLessThan && chr == Characters.LESSTHAN_SIGN) || chr == Characters.NULL) {
                return chr;
            }
            builder.append((char) chr);
        }
        return Characters.EOF;
    }

    protected int readUntilAttributeValueInternal(ResizableCharBuilder builder, int quoteChar, boolean stopAtAmpersand) {
        int chr;
        while ((chr = read()) != Characters.EOF) {
            if (chr == quoteChar || (stopAtAmpersand && chr == Characters.AMPERSAND) || chr == Characters.NULL) {
                return chr;
            }
            builder.append((char) chr);
        }
        return Characters.EOF;
    }

    protected int readUntilAttributeValueUnquotedInternal(ResizableCharBuilder builder) {
        int chr;
        while ((chr = read()) != Characters.EOF) {
            if (mustStopReadUntilAttributeValueUnquoted(chr)) {
                return chr;
            }
            builder.append((char) chr);
        }
        return Characters.EOF;
    }

    protected int readUntilTagNameInternal(ResizableCharBuilder builder) {
        int chr;
        while ((chr = read()) != Characters.EOF) {
            if (Common.isTabLfFfCrOrSpace(chr) || chr == Characters.SOLIDUS || chr == Characters.GREATERTHAN_SIGN || chr == Characters.NULL) {
                return chr;
            }
            builder.append((char) chr);
        }
        return Characters.EOF;
    }

    protected int readUntilAttributeNameInternal(ResizableCharBuilder builder) {
        int chr;
        while ((chr = read()) != Characters.EOF) {
            if (mustStopReadUntilAttributeName(chr)) {
                return chr;
            }
            builder.append((char) chr);
        }
        return Characters.EOF;
    }

    protected int readUntilCommentInternal(ResizableCharBuilder builder) {
        int chr;
        while ((chr = read()) != Characters.EOF) {
            if (chr == Characters.HYPHEN_MINUS || chr == Characters.NULL) {
                return chr;
            }
            builder.append((char) chr);
        }
        return Characters.EOF;
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



    static class StringProcessedInputStream extends ProcessedInputStream {
        private int pos = 0;
        private final char[] input;
        private final int length;


        StringProcessedInputStream(String input) {
            char[] toNormalize = input.toCharArray();
            int j = 0;
            for (int i = 0; i < toNormalize.length; i++) {
                char c = toNormalize[i];
                if (c == Characters.CR) {
                    toNormalize[j++] = Characters.LF;
                    if (i + 1 < toNormalize.length && toNormalize[i + 1] == Characters.LF) {
                        i++;
                    }
                } else {
                    toNormalize[j++] = c;
                }
            }
            this.input = toNormalize;
            this.length = j;
        }

        // used for test
        protected int getCharAt(int pos) {
            if (pos >= length) {
                return Characters.EOF;
            }
            return input[pos];
        }

        @Override
        protected int read() {
            if (pos < length) {
                return input[pos++];
            }
            return Characters.EOF;
        }

        @Override
        protected int readUntilInternal(ResizableCharBuilder builder, boolean stopAtAmpersand, boolean stopAtLessThan) {
            int n = length;
            int i = pos;
            while (i < n) {
                char c = input[i];
                if ((stopAtAmpersand && c == Characters.AMPERSAND) || (stopAtLessThan && c == Characters.LESSTHAN_SIGN) || c == Characters.NULL) {
                    builder.append(input, pos, i - pos);
                    pos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(input, pos, n - pos);
            pos = n;
            return Characters.EOF;
        }

        @Override
        protected int readUntilAttributeValueInternal(ResizableCharBuilder builder, int quoteChar, boolean stopAtAmpersand) {
            int n = length;
            int i = pos;
            while (i < n) {
                char c = input[i];
                if (c == quoteChar || (stopAtAmpersand && c == Characters.AMPERSAND) || c == Characters.NULL) {
                    builder.append(input, pos, i - pos);
                    pos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(input, pos, n - pos);
            pos = n;
            return Characters.EOF;
        }

        @Override
        protected int readUntilAttributeValueUnquotedInternal(ResizableCharBuilder builder) {
            int n = length;
            int i = pos;
            while (i < n) {
                char c = input[i];
                if (mustStopReadUntilAttributeValueUnquoted(c)) {
                    builder.append(input, pos, i - pos);
                    pos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(input, pos, n - pos);
            pos = n;
            return Characters.EOF;
        }

        @Override
        protected int readUntilTagNameInternal(ResizableCharBuilder builder) {
            int n = length;
            int i = pos;
            while (i < n) {
                char c = input[i];
                if (Common.isTabLfFfCrOrSpace(c) || c == Characters.SOLIDUS || c == Characters.GREATERTHAN_SIGN || c == Characters.NULL) {
                    builder.append(input, pos, i - pos);
                    pos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(input, pos, n - pos);
            pos = n;
            return Characters.EOF;
        }

        @Override
        protected int readUntilAttributeNameInternal(ResizableCharBuilder builder) {
            int n = length;
            int i = pos;
            while (i < n) {
                char c = input[i];
                if (mustStopReadUntilAttributeName(c)) {
                    builder.append(input, pos, i - pos); // append remaining
                    pos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(input, pos, n - pos);
            pos = n;
            return Characters.EOF;
        }

        @Override
        protected int readUntilCommentInternal(ResizableCharBuilder builder) {
            int n = length;
            int i = pos;
            while (i < n) {
                char c = input[i];
                if (c == Characters.HYPHEN_MINUS || c == Characters.NULL) {
                    builder.append(input, pos, i - pos);
                    pos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(input, pos, n - pos);
            pos = n;
            return Characters.EOF;
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
}
