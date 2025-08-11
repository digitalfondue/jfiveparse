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

class TokenizerTagStates {

    static void handleTagOpenState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.EXCLAMATION_MARK:
            tokenizer.setState(TokenizerState.MARKUP_DECLARATION_OPEN_STATE);
            break;
        case Characters.SOLIDUS:
            tokenizer.setState(TokenizerState.END_TAG_OPEN_STATE);
            break;
        case Characters.QUESTION_MARK:
            tokenizer.emitParseError();
            // FIXME CHECK only in some case the bogus comment state
            // will use the character that caused the transition.
            // This seems the (only) one
            processedInputStream.reconsume(chr);
            //
            tokenizer.setState(TokenizerState.BOGUS_COMMENT_STATE);
            break;
        default:
            if (Common.isUpperOrLowerCaseASCIILetter(chr)) { //
                tokenizer.createNewStartTagToken(chr);
                tokenizer.setState(TokenizerState.TAG_NAME_STATE);
            } else { // default
                handleTagOpenStateAnythingElse(tokenizer, processedInputStream, chr);
            }
            break;
        }
    }

    private static void handleTagOpenStateAnythingElse(Tokenizer tokenizer, ProcessedInputStream processedInputStream, int chr) {
        tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
        tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
        processedInputStream.reconsume(chr);
    }

    static void handleEndTagOpenState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.GREATERTHAN_SIGN:
            tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
            break;
        case Characters.EOF:
            handleEndTagOpenStateEOF(tokenizer, processedInputStream, chr);
            break;
        default:
            if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
                tokenizer.newEndTokenTag();
                tokenizer.appendCurrentTagToken(chr);
                tokenizer.setState(TokenizerState.TAG_NAME_STATE);
            } else {
                tokenizer.emitParseError();
                // FIXME CHECK only in some case the bogus comment state
                // will use the character that caused the transition.
                // This seems the (only) one
                processedInputStream.reconsume(chr);
                //
                tokenizer.setState(TokenizerState.BOGUS_COMMENT_STATE);
            }
            break;
        }
    }

    private static void handleEndTagOpenStateEOF(Tokenizer tokenizer, ProcessedInputStream processedInputStream, int chr) {
        tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
        tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
        tokenizer.emitCharacter(Characters.SOLIDUS);
        processedInputStream.reconsume(chr);
    }

    static void handleTagNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        // bypass and optimization, as we are accumulating the tag name, we can do it here
        // in a single loop, avoiding method calls
        do {
            int chr = processedInputStream.getNextInputCharacterAndConsume();
            switch (chr) {
                case Characters.TAB:
                case Characters.LF:
                case Characters.FF:
                case Characters.SPACE:
                    tokenizer.setState(TokenizerState.BEFORE_ATTRIBUTE_NAME_STATE);
                    return;
                case Characters.SOLIDUS:
                    tokenizer.setState(TokenizerState.SELF_CLOSING_START_TAG_STATE);
                    return;
                case Characters.GREATERTHAN_SIGN:
                    tokenizer.setState(TokenizerState.DATA_STATE);
                    tokenizer.addCurrentAttributeAndEmitToken();
                    return;
                case Characters.NULL:
                    tokenizer.emitParseError();
                    tokenizer.appendCurrentTagToken(Characters.REPLACEMENT_CHARACTER);
                    return;
                case Characters.EOF:
                    tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                    processedInputStream.reconsume(chr);
                    return;
                default:
                    tokenizer.tagName.append((char) chr);
            }
        } while (true);
    }

    static void handleSelfClosingStartTagState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.GREATERTHAN_SIGN:
            tokenizer.setSelfClosing(true);
            tokenizer.setState(TokenizerState.DATA_STATE);
            tokenizer.addCurrentAttributeAndEmitToken();
            break;
        case Characters.EOF:
            tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.emitParseErrorAndSetState(TokenizerState.BEFORE_ATTRIBUTE_NAME_STATE);
            processedInputStream.reconsume(chr);
            break;
        }
    }
}
