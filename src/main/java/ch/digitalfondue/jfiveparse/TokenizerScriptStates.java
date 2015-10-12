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


class TokenizerScriptStates {

    static void handleScriptDataState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.EOF:
            tokenizer.emitEOF();
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.LESSTHAN_SIGN:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_LESS_THAN_SIGN_STATE);
            break;
        default:
            // tokenizer.emitCharacter(chr);
            int previousInsertionMode = tokenizer.getTokenHandlerInsertionMode();
            tokenizer.emitCharacter(chr);
            int currentInsertionMode = tokenizer.getTokenHandlerInsertionMode();
            Text textNode = tokenizer.getTokenHandlerInsertCharacterPreviousTextNode();

            // optimization: bypass if possible
            if (tokenizer.getState() == TokenizerState.SCRIPT_DATA_STATE && previousInsertionMode == currentInsertionMode && textNode != null) {
                for (;;) {
                    int internalChr = processedInputStream.getNextInputCharacterAndConsume();
                    switch (internalChr) {
                    case Characters.EOF:
                        tokenizer.resetTokenHandlerInsertCharacterPreviousTextNode();
                        tokenizer.emitEOF();
                        return;
                    case Characters.NULL:
                        tokenizer.emitParseError();
                        tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
                        return;
                    case Characters.LESSTHAN_SIGN:
                        tokenizer.setState(TokenizerState.SCRIPT_DATA_LESS_THAN_SIGN_STATE);
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

    static void handleScriptDataLessThanSignState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.SOLIDUS:
            tokenizer.createTemporaryBuffer();
            tokenizer.setState(TokenizerState.SCRIPT_DATA_END_TAG_OPEN_STATE);
            break;
        case Characters.EXCLAMATION_MARK:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPE_START_STATE);
            tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
            tokenizer.emitCharacter(Characters.EXCLAMATION_MARK);
            break;
        default:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_STATE);
            tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
            processedInputStream.reconsume(chr);
            break;
        }
    }

    static void handleScriptDataEndTagOpenState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();

        final boolean isUpperCase = Common.isUpperCaseASCIILetter(chr);

        if (isUpperCase || Common.isLowerCaseASCIILetter(chr)) {
            tokenizer.newEndTokenTag();
            tokenizer.appendCurrentTagToken((chr + (isUpperCase ? 0x0020 : 0)));
            tokenizer.appendToTemporaryBuffer(chr);
            tokenizer.setState(TokenizerState.SCRIPT_DATA_END_TAG_NAME_STATE);
        } else {
            tokenizer.setState(TokenizerState.SCRIPT_DATA_STATE);
            tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
            tokenizer.emitCharacter(Characters.SOLIDUS);
            processedInputStream.reconsume(chr);
        }

    }

    static void handleScriptDataEndTagNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.TAB:
        case Characters.LF:
        case Characters.FF:
        case Characters.SPACE:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.BEFORE_ATTRIBUTE_NAME_STATE);
            } else {
                anythingElseScriptDataEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        case Characters.SOLIDUS:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.SELF_CLOSING_START_TAG_STATE);
            } else {
                anythingElseScriptDataEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        case Characters.GREATERTHAN_SIGN:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitTagToken();
            } else {
                anythingElseScriptDataEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        default:
            final boolean isUpperCase = Common.isUpperCaseASCIILetter(chr);
            if (isUpperCase || Common.isLowerCaseASCIILetter(chr)) {
                tokenizer.appendCurrentTagToken(chr + (isUpperCase ? 0x0020 : 0));
                tokenizer.appendToTemporaryBuffer(chr);
            } else {
                anythingElseScriptDataEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        }
    }

    private static void anythingElseScriptDataEndTagNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream, int chr) {
        tokenizer.setState(TokenizerState.SCRIPT_DATA_STATE);
        tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
        tokenizer.emitCharacter(Characters.SOLIDUS);

        tokenizer.emitTemporaryBufferAsCharacters();

        processedInputStream.reconsume(chr);
    }

    static void handleScriptDataEscapeStartState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        if (chr == Characters.HYPHEN_MINUS) {
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPE_START_DASH_STATE);
            tokenizer.emitCharacter(Characters.HYPHEN_MINUS);
        } else {
            tokenizer.setState(TokenizerState.SCRIPT_DATA_STATE);
            processedInputStream.reconsume(chr);
        }
    }

    static void handleScriptDataEscapeStartDashState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        if (chr == Characters.HYPHEN_MINUS) {
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_DASH_DASH_STATE);
            tokenizer.emitCharacter(Characters.HYPHEN_MINUS);
        } else {
            tokenizer.setState(TokenizerState.SCRIPT_DATA_STATE);
            processedInputStream.reconsume(chr);
        }
    }

    static void handleScriptDataEscapedDashDashState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.HYPHEN_MINUS:
            tokenizer.emitCharacter(Characters.HYPHEN_MINUS);
            break;
        case Characters.LESSTHAN_SIGN:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN_STATE);
            break;
        case Characters.GREATERTHAN_SIGN:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_STATE);
            tokenizer.emitCharacter(Characters.GREATERTHAN_SIGN);
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
            tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.emitParseError();
            tokenizer.setState(TokenizerState.DATA_STATE);
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
            tokenizer.emitCharacter(chr);
            break;
        }
    }

    static void handleScriptDataEscapedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.HYPHEN_MINUS:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_DASH_STATE);
            tokenizer.emitCharacter(Characters.HYPHEN_MINUS);
            break;
        case Characters.LESSTHAN_SIGN:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN_STATE);
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.setState(TokenizerState.DATA_STATE);
            tokenizer.emitParseError();
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.emitCharacter(chr);
            break;
        }
    }

    static void handleScriptDataEscapedDashState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.HYPHEN_MINUS:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_DASH_DASH_STATE);
            tokenizer.emitCharacter(Characters.HYPHEN_MINUS);
            break;
        case Characters.LESSTHAN_SIGN:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN_STATE);
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
            tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.emitParseError();
            tokenizer.setState(TokenizerState.DATA_STATE);
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
            tokenizer.emitCharacter(chr);
            break;
        }
    }

    static void handleScriptDataEscapedLessThanSignState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();

        final boolean isUpperCase = Common.isUpperCaseASCIILetter(chr);

        if (chr == Characters.SOLIDUS) {
            tokenizer.createTemporaryBuffer();
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_END_TAG_OPEN_STATE);
        } else if (isUpperCase || Common.isLowerCaseASCIILetter(chr)) {
            final int upperCaseCompensation = isUpperCase ? 0x0020 : 0;
            tokenizer.createTemporaryBuffer();
            tokenizer.appendToTemporaryBuffer(chr + upperCaseCompensation);
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPE_START_STATE);
            tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
            tokenizer.emitCharacter(chr);
        } else {
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
            tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
            processedInputStream.reconsume(chr);
        }
    }

    static void handleScriptDataEscapedEndTagOpenState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();

        final boolean isUpperCase = Common.isUpperCaseASCIILetter(chr);

        if (isUpperCase || Common.isLowerCaseASCIILetter(chr)) {
            tokenizer.newEndTokenTag();
            tokenizer.appendCurrentTagToken(chr + (isUpperCase ? 0x0020 : 0));
            tokenizer.appendToTemporaryBuffer(chr);
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_END_TAG_NAME_STATE);
        } else {
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
            tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
            tokenizer.emitCharacter(Characters.SOLIDUS);
            processedInputStream.reconsume(chr);
        }
    }

    static void handleScriptDataEscapedEndTagNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.TAB:
        case Characters.LF:
        case Characters.FF:
        case Characters.SPACE:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.BEFORE_ATTRIBUTE_NAME_STATE);
            } else {
                anythingElseScriptDataEscapedEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        case Characters.SOLIDUS:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.SELF_CLOSING_START_TAG_STATE);
            } else {
                anythingElseScriptDataEscapedEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        case Characters.GREATERTHAN_SIGN:
            if (tokenizer.isAppropriateEndTagToken()) {
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitTagToken();
            } else {
                anythingElseScriptDataEscapedEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        default:
            final boolean isUpperCase = Common.isUpperCaseASCIILetter(chr);
            if (isUpperCase || Common.isLowerCaseASCIILetter(chr)) {
                tokenizer.appendCurrentTagToken(chr + (isUpperCase ? 0x0020 : 0));
                tokenizer.appendToTemporaryBuffer(chr);
            } else {
                anythingElseScriptDataEscapedEndTagNameState(tokenizer, processedInputStream, chr);
            }
            break;
        }
    }

    private static void anythingElseScriptDataEscapedEndTagNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream, int chr) {
        tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
        tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
        tokenizer.emitCharacter(Characters.SOLIDUS);

        tokenizer.emitTemporaryBufferAsCharacters();

        processedInputStream.reconsume(chr);
    }

    static void handleScriptDataDoubleEscapeStartState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.TAB:
        case Characters.LF:
        case Characters.FF:
        case Characters.SPACE:
        case Characters.SOLIDUS:
        case Characters.GREATERTHAN_SIGN:
            if (tokenizer.isTemporaryBufferEquals("script")) {
                tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
            } else {
                tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
            }
            tokenizer.emitCharacter(chr);
            break;
        default:
            final boolean isUpperCase = Common.isUpperCaseASCIILetter(chr);
            if (isUpperCase || Common.isLowerCaseASCIILetter(chr)) {
                tokenizer.appendToTemporaryBuffer(chr + (isUpperCase ? 0x0020 : 0));
                tokenizer.emitCharacter(chr);
            } else {
                tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
                processedInputStream.reconsume(chr);
            }
            break;
        }
    }

    static void handleScriptDataDoubleEscapedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.HYPHEN_MINUS:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_DASH_STATE);
            tokenizer.emitCharacter(Characters.HYPHEN_MINUS);
            break;
        case Characters.LESSTHAN_SIGN:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN_STATE);
            tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.emitParseError();
            tokenizer.setState(TokenizerState.DATA_STATE);
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.emitCharacter(chr);
            break;
        }
    }

    static void handleScriptDataDoubleEscapedDashState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.HYPHEN_MINUS:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH_STATE);
            tokenizer.emitCharacter(Characters.HYPHEN_MINUS);
            break;
        case Characters.LESSTHAN_SIGN:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN_STATE);
            tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
            tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.emitParseError();
            tokenizer.setState(TokenizerState.DATA_STATE);
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
            tokenizer.emitCharacter(chr);
            break;
        }
    }

    static void handleScriptDataDoubleEscapedDashDashState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.HYPHEN_MINUS:
            tokenizer.emitCharacter(Characters.HYPHEN_MINUS);
            break;
        case Characters.LESSTHAN_SIGN:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN_STATE);
            tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
            break;
        case Characters.GREATERTHAN_SIGN:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_STATE);
            tokenizer.emitCharacter(Characters.GREATERTHAN_SIGN);
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
            tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.emitParseError();
            tokenizer.setState(TokenizerState.DATA_STATE);
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
            tokenizer.emitCharacter(chr);
            break;
        }
    }

    static void handleScriptDataDoubleEscapedLessThanSignState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        if (chr == Characters.SOLIDUS) {
            tokenizer.createTemporaryBuffer();
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPE_END_STATE);
            tokenizer.emitCharacter(Characters.SOLIDUS);
        } else {
            tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
            processedInputStream.reconsume(chr);
        }
    }

    static void handleScriptDataDoubleEscapedEndState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.TAB:
        case Characters.LF:
        case Characters.FF:
        case Characters.SPACE:
        case Characters.SOLIDUS:
        case Characters.GREATERTHAN_SIGN:
            if (tokenizer.isTemporaryBufferEquals("script")) {
                tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
            } else {
                tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
            }
            tokenizer.emitCharacter(chr);
            break;
        default:
            final boolean isUpperCase = Common.isUpperCaseASCIILetter(chr);
            if (isUpperCase || Common.isLowerCaseASCIILetter(chr)) {
                tokenizer.appendToTemporaryBuffer(chr + (isUpperCase ? 0x0020 : 0));
                tokenizer.emitCharacter(chr);
            } else {
                tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
                processedInputStream.reconsume(chr);
            }
            break;
        }
    }
}
