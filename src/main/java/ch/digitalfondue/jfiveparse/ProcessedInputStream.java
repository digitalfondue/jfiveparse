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

    protected abstract int read();

    static class StringProcessedInputStream extends ProcessedInputStream {
        protected int pos = 0;
        protected final char[] input;

        StringProcessedInputStream(String input) {
            this.input = input.toCharArray();
        }

        @Override
        protected int read() {
            try {
                return input[pos++];
            } catch (IndexOutOfBoundsException s) {
                return -1;
            }
        }
    }

    static final class ReaderProcessedInputStream extends ProcessedInputStream {

        private final Reader reader;

        ReaderProcessedInputStream(Reader reader) {
            this.reader = reader;
        }

        @Override
        protected int read() {
            try {
                return reader.read();
            } catch (IOException ioe) {
                throw new ParserException(ioe);
            }
        }
    }

    //
    private int readWithCRHandling() {
        int chr = read();
        if (crFound) {
            //chr = handleCrFoundInternal(chr);
            crFound = false;
            if (chr == Characters.LF) {
                chr = read();
            }
        }

        if (chr == Characters.CR) {
            // handleChrIsCR
            crFound = true;
            chr = Characters.LF;
        }
        return chr;
    }

    int peekNextInputCharacter(int offset) {
        if (buffer.length() < offset) {
            // fill buffer
            for (int i = buffer.length(); i < offset; i++) {
                buffer.add(readWithCRHandling());
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
        return !buffer.isEmpty ? buffer.removeFirst() : readWithCRHandling();
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
