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


class TokenizerCDataSectionAndDataState {

    static void handleDataState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.EOF:
            tokenizer.emitEOF();
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.emitCharacter(chr);
            break;
        case Characters.AMPERSAND:
            tokenizer.setState(TokenizerState.CHARACTER_REFERENCE_IN_DATA_STATE);
            break;
        case Characters.LESSTHAN_SIGN:
            tokenizer.setState(TokenizerState.TAG_OPEN_STATE);
            break;
        default:
            int previousInsertionMode = tokenizer.getTokenHandlerInsertionMode(); // optim
            tokenizer.emitCharacter(chr);
            // vvv optimization vvv
            int currentInsertionMode = tokenizer.getTokenHandlerInsertionMode();
            ResizableCharBuilder textNode = tokenizer.getTokenHandlerInsertCharacterPreviousTextNode();
            if (tokenizer.getState() == TokenizerState.DATA_STATE && previousInsertionMode == currentInsertionMode
                    && (currentInsertionMode == TreeConstructionInsertionMode.IN_BODY || currentInsertionMode == TreeConstructionInsertionMode.IN_CELL)
                    && tokenizer.isTokenHandlerInHtmlContent() && textNode != null) {

                for (;;) {
                    int internalChr = processedInputStream.getNextInputCharacterAndConsume();
                    switch (internalChr) {
                    case Characters.EOF:
                        tokenizer.resetTokenHandlerInsertCharacterPreviousTextNode();
                        tokenizer.emitEOF();
                        return;
                    case Characters.NULL:
                        tokenizer.emitParseError();
                        tokenizer.emitCharacter(internalChr);
                        return;
                    case Characters.AMPERSAND:
                        tokenizer.resetTokenHandlerInsertCharacterPreviousTextNode();
                        tokenizer.setState(TokenizerState.CHARACTER_REFERENCE_IN_DATA_STATE);
                        return;
                    case Characters.LESSTHAN_SIGN:
                        tokenizer.resetTokenHandlerInsertCharacterPreviousTextNode();
                        tokenizer.setState(TokenizerState.TAG_OPEN_STATE);
                        return;
                    default:
                        textNode.append((char) internalChr);
                        break;
                    }
                }

            }
            break;
        }
    }

    static void handleCharacterReferenceInDataState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        tokenizer.setState(TokenizerState.DATA_STATE);
        char[] chars = TokenizerCharacterReference.consumeCharacterReference(-1, false, processedInputStream, tokenizer);
        if (chars == null) {
            tokenizer.emitCharacter(Characters.AMPERSAND);
        } else {
            for (int c : chars) {
                tokenizer.emitCharacter(c);
            }
        }
    }

    // cdata

    static void handleCDataSectionState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        tokenizer.setState(TokenizerState.DATA_STATE);

        while (true) {

            if (processedInputStream.peekNextInputCharacter(1) == Characters.RIGHT_SQUARE_BRACKET
                    && processedInputStream.peekNextInputCharacter(2) == Characters.RIGHT_SQUARE_BRACKET
                    && processedInputStream.peekNextInputCharacter(3) == Characters.GREATERTHAN_SIGN) {
                processedInputStream.consume(3);
                return;
            } else {
                int curr = processedInputStream.getNextInputCharacter();

                if (curr == Characters.EOF) {
                    return;
                } else {
                    processedInputStream.consume();
                    tokenizer.emitCharacter(curr);
                }
            }
        }
    }
}
