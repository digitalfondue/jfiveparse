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

class TokenizerPlainAndRawTextStates {

    // ----

    static void handlePlainTextState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.emitEOF();
            break;
        default:
            tokenizer.emitCharacter(chr);
            break;
        }
    }

    // ----

    static void handleRawtextState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.LESSTHAN_SIGN:
            tokenizer.setState(TokenizerState.RAWTEXT_LESS_THAN_SIGN_STATE);
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.emitEOF();
            break;
        default:
            int previousInsertionMode = tokenizer.getTokenHandlerInsertionMode(); // optim
            tokenizer.emitCharacter(chr);
            // vvv optimization vvv
            int currentInsertionMode = tokenizer.getTokenHandlerInsertionMode();
            ResizableCharBuilder textNode = tokenizer.getTokenHandlerInsertCharacterPreviousTextNode();
            if (tokenizer.getState() == TokenizerState.RAWTEXT_STATE && previousInsertionMode == currentInsertionMode && textNode != null) {

                for (;;) {
                    int internalChr = processedInputStream.getNextInputCharacterAndConsume();
                    switch (internalChr) {
                        case Characters.LESSTHAN_SIGN:
                            tokenizer.resetTokenHandlerInsertCharacterPreviousTextNode();
                            tokenizer.setState(TokenizerState.RAWTEXT_LESS_THAN_SIGN_STATE);
                            return;
                        case Characters.NULL:
                            tokenizer.emitParseError();
                            tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
                            return;
                        case Characters.EOF:
                            tokenizer.resetTokenHandlerInsertCharacterPreviousTextNode();
                            tokenizer.emitEOF();
                            return;
                        default:
                            textNode.append((char) internalChr);
                    }
                }
            }
            break;
        }
    }

    static void handleRawTextLessThanSignState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        if (chr == Characters.SOLIDUS) {
            tokenizer.createTemporaryBuffer();
            tokenizer.setState(TokenizerState.RAWTEXT_END_TAG_OPEN_STATE);
        } else {
            tokenizer.setStateAndEmitCharacter(TokenizerState.RAWTEXT_STATE, Characters.LESSTHAN_SIGN);
            processedInputStream.reconsume(chr);
        }
    }

    static void handleRawTextEndTagOpenState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        if (Common.isUpperCaseASCIILetter(chr) || Common.isLowerCaseASCIILetter(chr)) {
            tokenizer.newEndTokenTag();
            tokenizer.appendCurrentTagToken(chr);
            tokenizer.appendToTemporaryBuffer(chr);
            tokenizer.setState(TokenizerState.RAWTEXT_END_TAG_NAME_STATE);
        } else {
            tokenizer.setStateAndEmitCharacter(TokenizerState.RAWTEXT_STATE, Characters.LESSTHAN_SIGN);
            tokenizer.emitCharacter(Characters.SOLIDUS);
            processedInputStream.reconsume(chr);
        }
    }

    static void handleRawTextEndTagNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.TAB:
        case Characters.LF:
        case Characters.FF:
        case Characters.SPACE:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.BEFORE_ATTRIBUTE_NAME_STATE);
            } else {
                anythingElseRawTextEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        case Characters.SOLIDUS:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.SELF_CLOSING_START_TAG_STATE);
            } else {
                anythingElseRawTextEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        case Characters.GREATERTHAN_SIGN:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.DATA_STATE);
                // TODO: check attributes???
                tokenizer.emitTagToken();
            } else {
                anythingElseRawTextEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        default:
            if (Common.isUpperCaseASCIILetter(chr) || Common.isLowerCaseASCIILetter(chr)) {
                tokenizer.appendCurrentTagToken(chr);
                tokenizer.appendToTemporaryBuffer(chr);
            } else {
                anythingElseRawTextEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        }
    }

    private static void anythingElseRawTextEndTagNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream, int chr) {
        tokenizer.setStateAndEmitCharacter(TokenizerState.RAWTEXT_STATE, Characters.LESSTHAN_SIGN);
        tokenizer.emitCharacter(Characters.SOLIDUS);

        tokenizer.emitTemporaryBufferAsCharacters();

        processedInputStream.reconsume(chr);
    }
}
