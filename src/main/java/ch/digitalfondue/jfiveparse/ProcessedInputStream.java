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
 * Wrapped and abstracted input with on-demand buffering from a {@link Reader}.
 */
// can be improved, see SWAR..., also we could improve code sharing with the readUntil*Internal code
class ProcessedInputStream {

    protected final ResizableIntBuffer buffer = new ResizableIntBuffer();

    private final Reader reader;
    private final char[] buff = new char[8192];
    private int buffPos = 0;
    private int buffCount = 0;
    private boolean eof = false;
    private boolean crFound = false;

    ProcessedInputStream(Reader reader) {
        this.reader = reader;
    }

    protected int read() {
        if (buffPos < buffCount) {
            return buff[buffPos++];
        }
        if (fill()) {
            return buff[buffPos++];
        }
        return Characters.EOF;
    }

    private boolean fill() {
        if (eof) {
            return false;
        }
        try {
            int out = 0;
            while (out == 0) {
                int read = reader.read(buff);
                if (read == -1) {
                    eof = true;
                    buffCount = 0;
                    buffPos = 0;
                    return false;
                }
                for (int in = 0; in < read; in++) {
                    char c = buff[in];
                    if (crFound) {
                        crFound = false;
                        if (c == Characters.LF) {
                            continue;
                        }
                    }
                    if (c == Characters.CR) {
                        crFound = true;
                        c = Characters.LF;
                    }
                    buff[out++] = c;
                }
            }
            buffPos = 0;
            buffCount = out;
            return true;
        } catch (IOException ioe) {
            throw new ParserException(ioe);
        }
    }

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

    private int readUntilInternal(ResizableCharBuilder builder, boolean stopAtAmpersand, boolean stopAtLessThan) {
        for (;;) {
            if (buffPos >= buffCount) {
                if (!fill()) {
                    return Characters.EOF;
                }
            }
            int n = buffCount;
            int i = buffPos;
            while (i < n) {
                char c = buff[i];
                if ((stopAtAmpersand && c == Characters.AMPERSAND) || (stopAtLessThan && c == Characters.LESSTHAN_SIGN) || c == Characters.NULL) {
                    builder.append(buff, buffPos, i - buffPos);
                    buffPos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(buff, buffPos, n - buffPos);
            buffPos = n;
        }
    }

    private int readUntilAttributeValueInternal(ResizableCharBuilder builder, int quoteChar, boolean stopAtAmpersand) {
        for (;;) {
            if (buffPos >= buffCount) {
                if (!fill()) {
                    return Characters.EOF;
                }
            }
            int n = buffCount;
            int i = buffPos;
            while (i < n) {
                char c = buff[i];
                if (c == quoteChar || (stopAtAmpersand && c == Characters.AMPERSAND) || c == Characters.NULL) {
                    builder.append(buff, buffPos, i - buffPos);
                    buffPos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(buff, buffPos, n - buffPos);
            buffPos = n;
        }
    }

    private int readUntilAttributeValueUnquotedInternal(ResizableCharBuilder builder) {
        for (;;) {
            if (buffPos >= buffCount) {
                if (!fill()) {
                    return Characters.EOF;
                }
            }
            int n = buffCount;
            int i = buffPos;
            while (i < n) {
                char c = buff[i];
                if (mustStopReadUntilAttributeValueUnquoted(c)) {
                    builder.append(buff, buffPos, i - buffPos);
                    buffPos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(buff, buffPos, n - buffPos);
            buffPos = n;
        }
    }

    private int readUntilTagNameInternal(ResizableCharBuilder builder) {
        for (;;) {
            if (buffPos >= buffCount) {
                if (!fill()) {
                    return Characters.EOF;
                }
            }
            int n = buffCount;
            int i = buffPos;
            while (i < n) {
                char c = buff[i];
                if (Common.isTabLfFfCrOrSpace(c) || c == Characters.SOLIDUS || c == Characters.GREATERTHAN_SIGN || c == Characters.NULL) {
                    builder.append(buff, buffPos, i - buffPos);
                    buffPos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(buff, buffPos, n - buffPos);
            buffPos = n;
        }
    }

    private int readUntilAttributeNameInternal(ResizableCharBuilder builder) {
        for (;;) {
            if (buffPos >= buffCount) {
                if (!fill()) {
                    return Characters.EOF;
                }
            }
            int n = buffCount;
            int i = buffPos;
            while (i < n) {
                char c = buff[i];
                if (mustStopReadUntilAttributeName(c)) {
                    builder.append(buff, buffPos, i - buffPos);
                    buffPos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(buff, buffPos, n - buffPos);
            buffPos = n;
        }
    }

    private int readUntilCommentInternal(ResizableCharBuilder builder) {
        for (;;) {
            if (buffPos >= buffCount) {
                if (!fill()) {
                    return Characters.EOF;
                }
            }
            int n = buffCount;
            int i = buffPos;
            while (i < n) {
                char c = buff[i];
                if (c == Characters.HYPHEN_MINUS || c == Characters.NULL) {
                    builder.append(buff, buffPos, i - buffPos);
                    buffPos = i + 1;
                    return c;
                }
                i++;
            }
            builder.append(buff, buffPos, n - buffPos);
            buffPos = n;
        }
    }

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
