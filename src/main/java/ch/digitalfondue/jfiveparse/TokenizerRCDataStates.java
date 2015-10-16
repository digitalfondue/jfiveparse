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

class TokenizerRCDataStates {

    static void handleRCDataState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.AMPERSAND:
            tokenizer.setState(TokenizerState.CHARACTER_REFERENCE_IN_RCDATA_STATE);
            break;
        case Characters.LESSTHAN_SIGN:
            tokenizer.setState(TokenizerState.RCDATA_LESS_THAN_SIGN_STATE);
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.emitEOF(); // does nothing
            break;
        default:
            tokenizer.emitCharacter(chr);
            break;
        }
    }

    static void handleCharacterReferenceInRCDataState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        tokenizer.setState(TokenizerState.RCDATA_STATE);
        char[] chars = TokenizerCharacterReference.consumeCharacterReference(-1, false, processedInputStream, tokenizer);
        if (chars == null) {
            tokenizer.emitCharacter(Characters.AMPERSAND);
        } else {
            for (int c : chars) {
                tokenizer.emitCharacter(c);
            }
        }
    }

    static void handleRCDataLessThanSignState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.SOLIDUS:
            tokenizer.createTemporaryBuffer();
            tokenizer.setState(TokenizerState.RCDATA_END_TAG_OPEN_STATE);
            break;
        default:
            tokenizer.setState(TokenizerState.RCDATA_STATE);
            tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
            processedInputStream.reconsume(chr);
            break;
        }
    }

    static void handleRCDataEndTagOpenState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        boolean isUpperCase = Common.isUpperCaseASCIILetter(chr);
        if (isUpperCase || Common.isLowerCaseASCIILetter(chr)) {
            tokenizer.newEndTokenTag();
            tokenizer.appendCurrentTagToken(chr, isUpperCase);
            tokenizer.appendToTemporaryBuffer(chr);
            tokenizer.setState(TokenizerState.RCDATA_END_TAG_NAME_STATE);
        } else {
            tokenizer.setState(TokenizerState.RCDATA_STATE);
            tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
            tokenizer.emitCharacter(Characters.SOLIDUS);
            processedInputStream.reconsume(chr);
        }
    }

    static void handleRCDataEndTagNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.TAB:
        case Characters.LF:
        case Characters.FF:
        case Characters.SPACE:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.BEFORE_ATTRIBUTE_NAME_STATE);
            } else {
                anythingElseRCDataEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        case Characters.SOLIDUS:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.SELF_CLOSING_START_TAG_STATE);
            } else {
                anythingElseRCDataEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        case Characters.GREATERTHAN_SIGN:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.DATA_STATE);
                // TODO: check attributes???
                tokenizer.emitTagToken();
            } else {
                anythingElseRCDataEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        default:
            boolean isUpperCase = Common.isUpperCaseASCIILetter(chr);
            if (isUpperCase || Common.isLowerCaseASCIILetter(chr)) {
                tokenizer.appendCurrentTagToken(chr, isUpperCase);
                tokenizer.appendToTemporaryBuffer(chr);
            } else {
                anythingElseRCDataEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        }
    }

    private static void anythingElseRCDataEndTagNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream, int chr) {
        tokenizer.setState(TokenizerState.RCDATA_STATE);
        tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
        tokenizer.emitCharacter(Characters.SOLIDUS);

        tokenizer.emitTemporaryBufferAsCharacters();

        processedInputStream.reconsume(chr);
    }
}
