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

import ch.digitalfondue.jfiveparse.ProcessedInputStream.StringProcessedInputStream;

class ProcessedInputStreamWithParseError extends StringProcessedInputStream {

    private final TreeConstructor tokenHandler;
    private int alreadyVisitedPosition = -1;
    private int position = -1;

    ProcessedInputStreamWithParseError(String input, TokenSaver tokenHandler) {
        super(input);
        this.tokenHandler = tokenHandler;
    }

    private int previousCharacter = -1;

    ProcessedInputStreamWithParseError(String input, TreeConstructor tokenHandler) {
        super(input);
        this.tokenHandler = tokenHandler;
    }

    @Override
    void reconsume(int chr) {
        super.reconsume(chr);
        position--;
    }

    @Override
    int consume() {
        previousCharacter = getCurrentInputCharacter();
        int chr = super.consume();
        position++;
        consumeWithCheck();
        return chr;
    }

    private int getCurrentInputCharacter() {
        if (buffer.length() > 0) {
            return buffer.getCharAt(0);
        } else {
            return getCharAt(position);
        }
    }

    @Override
    int getNextInputCharacterAndConsume() {
        int c = getNextInputCharacter();
        consume();
        return c;
    }

    @Override
    void consume(int count) {
        for (int i = 0; i < count; i++) {
            consume();
        }
    }

    private void consumeWithCheck() {
        final int currentInputChar = getCurrentInputCharacter();
        final int peekedNextInputChar = peekNextInputCharacter(1);
        final int previousInputChar = previousCharacter;
        boolean isCurrentCharHighSurrogate = Character.isHighSurrogate((char) currentInputChar);
        boolean isLowSurrogatePeekedNextInputChar = Character.isLowSurrogate((char) peekedNextInputChar);

        boolean isLowSurrogate = Character.isLowSurrogate((char) currentInputChar);
        boolean isHighSurrogatePeekendPreviousInputChar = Character.isHighSurrogate((char) previousInputChar);
        // various check here:
        // - if invalid char
        // - check the surrogate pair
        if (position > alreadyVisitedPosition && (invalidCharacters(currentInputChar) || //
                (isLowSurrogate && !isHighSurrogatePeekendPreviousInputChar) || //
                (isCurrentCharHighSurrogate && !isLowSurrogatePeekedNextInputChar) || //
                (isCurrentCharHighSurrogate && isLowSurrogatePeekedNextInputChar && invalidCharacters(Character.toCodePoint((char) currentInputChar,
                        (char) peekedNextInputChar))))) {
            tokenHandler.emitParseError();
        }

        alreadyVisitedPosition = Math.max(position, alreadyVisitedPosition);
    }
    
    
    
 // U+FFFE, U+FFFF, U+1FFFE, U+1FFFF, U+2FFFE, U+2FFFF,
    // U+3FFFE, U+3FFFF, U+4FFFE, U+4FFFF, U+5FFFE, U+5FFFF,
    // U+6FFFE, U+6FFFF, U+7FFFE, U+7FFFF, U+8FFFE, U+8FFFF,
    // U+9FFFE, U+9FFFF, U+AFFFE, U+AFFFF, U+BFFFE, U+BFFFF,
    // U+CFFFE, U+CFFFF, U+DFFFE, U+DFFFF, U+EFFFE, U+EFFFF,
    // U+FFFFE, U+FFFFF, U+10FFFE, U+10FFFF

    private static boolean isInInvalidChars(int chr) {
        return chr == 0x000B || //
                chr == 0xFFFE || //
                chr == 0xFFFF || //
                chr == 0x1FFFE || //
                chr == 0x1FFFF || //
                chr == 0x2FFFE || //
                chr == 0x2FFFF || //
                chr == 0x3FFFE || //
                chr == 0x3FFFF || //
                chr == 0x4FFFE || //
                chr == 0x4FFFF || //
                chr == 0x5FFFE || //
                chr == 0x5FFFF || //
                chr == 0x6FFFE || //
                chr == 0x6FFFF || //
                chr == 0x7FFFE || //
                chr == 0x7FFFF || //
                chr == 0x8FFFE || //
                chr == 0x8FFFF || //
                chr == 0x9FFFE || //
                chr == 0x9FFFF || //
                chr == 0xAFFFE || //
                chr == 0xAFFFF || //
                chr == 0xBFFFE || //
                chr == 0xBFFFF || //
                chr == 0xCFFFE || //
                chr == 0xCFFFF || //
                chr == 0xDFFFE || //
                chr == 0xDFFFF || //
                chr == 0xEFFFE || //
                chr == 0xEFFFF || //
                chr == 0xFFFFE || //
                chr == 0xFFFFF || //
                chr == 0x10FFFE || //
                chr == 0x10FFFF;
    }

    // https://html.spec.whatwg.org/multipage/syntax.html#preprocessing-the-input-stream
    static boolean invalidCharacters(int chr) {
        return (chr >= 0x0001 && chr <= 0x0008) || // U+0001 to U+0008
                (chr >= 0x000E && chr <= 0x001F) || // U+000E to U+001F
                (chr >= 0x007F && chr <= 0x009F) || // U+007F to U+009F
                (chr >= 0xFDD0 && chr <= 0xFDEF) || // U+FDD0 to U+FDEF
                isInInvalidChars(chr);
    }

}
