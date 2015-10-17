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

import java.io.IOException;
import java.io.Reader;

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

    private boolean crFound;
    protected final ResizableIntBuffer buffer = new ResizableIntBuffer();

    abstract int read();

    int getCharAt(int position) {
        throw new IllegalArgumentException();
    }

    static class StringProcessedInputStream extends ProcessedInputStream {
        private int pos = 0;
        private final char[] input;

        StringProcessedInputStream(String input) {
            this.input = input.toCharArray();
        }

        @Override
        int read() {
            try {
                return input[pos++];
            } catch (IndexOutOfBoundsException s) {
                return -1;
            }
        }

        @Override
        int getCharAt(int position) {
            try {
                return input[position];
            } catch (IndexOutOfBoundsException s) {
                return -1;
            }
        }
    }

    static class ReaderProcessedInputStream extends ProcessedInputStream {

        private final Reader reader;

        ReaderProcessedInputStream(Reader reader) {
            this.reader = reader;
        }

        @Override
        int read() {
            try {
                return reader.read();
            } catch (IOException ioe) {
                throw new ParserException(ioe);
            }
        }
    }

    //
    private int readWithCRHandling(boolean crFoundInternal, int chr) {
        if (crFoundInternal) {
            chr = handleCrFoundInternal(chr);
        }

        if (chr == Characters.CR) {
            chr = handleChrIsCR();
        }
        return chr;
    }

    private int handleChrIsCR() {
        crFound = true;
        return Characters.LF;
    }

    private int handleCrFoundInternal(int chr) {
        crFound = false;
        if (chr == Characters.LF) {
            chr = read();
        }
        return chr;
    }

    int peekNextInputCharacter(int offset) {
        if (buffer.length() < offset) {
            // fill buffer
            for (int i = 0; i < offset; i++) {
                int chr = readWithCRHandling(crFound, read());
                buffer.add(chr);
            }
        }
        return buffer.getCharAt(offset);
    }

    /**
     * Ideally, it's a combination of getNextInputCharacter + consume in term of
     * behavior.
     * 
     * @return
     */
    int getNextInputCharacterAndConsume() {
        return consume();
    }

    int consume() {
        if (buffer.isEmpty()) {
            return readWithCRHandling(crFound, read());
        } else {
            return buffer.removeFirst();
        }
    }

    int getNextInputCharacter() {
        return peekNextInputCharacter(1);
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
