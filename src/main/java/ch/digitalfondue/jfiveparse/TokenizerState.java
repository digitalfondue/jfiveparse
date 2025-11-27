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

import java.util.Arrays;

class TokenizerState {
    //
    static final int DATA_STATE = 0;
    static final int ATTRIBUTE_VALUE_DOUBLE_QUOTED_STATE = 1;
    static final int ATTRIBUTE_NAME_STATE = 2;
    static final int TAG_NAME_STATE = 3;
    static final int RAWTEXT_STATE = 4;
    static final int SCRIPT_DATA_STATE = 5;
    static final int TAG_OPEN_STATE = 6;
    static final int BEFORE_ATTRIBUTE_NAME_STATE = 7;
    static final int BEFORE_ATTRIBUTE_VALUE_STATE = 8;
    static final int AFTER_ATTRIBUTE_VALUE_QUOTED_STATE = 9;
    static final int END_TAG_OPEN_STATE = 10;
    //

    //
    static final int CHARACTER_REFERENCE_IN_DATA_STATE = 11;
    static final int RCDATA_STATE = 12;
    static final int CHARACTER_REFERENCE_IN_RCDATA_STATE = 13;

    static final int PLAINTEXT_STATE = 14;
    static final int RCDATA_LESS_THAN_SIGN_STATE = 15;
    static final int RCDATA_END_TAG_OPEN_STATE = 16;
    static final int RCDATA_END_TAG_NAME_STATE = 17;
    static final int RAWTEXT_LESS_THAN_SIGN_STATE = 18;
    static final int RAWTEXT_END_TAG_OPEN_STATE = 19;
    static final int RAWTEXT_END_TAG_NAME_STATE = 20;
    static final int SCRIPT_DATA_LESS_THAN_SIGN_STATE = 21;
    static final int SCRIPT_DATA_END_TAG_OPEN_STATE = 22;
    static final int SCRIPT_DATA_END_TAG_NAME_STATE = 23;
    static final int SCRIPT_DATA_ESCAPE_START_STATE = 24;
    static final int SCRIPT_DATA_ESCAPE_START_DASH_STATE = 25;
    static final int SCRIPT_DATA_ESCAPED_STATE = 26;
    static final int SCRIPT_DATA_ESCAPED_DASH_STATE = 27;
    static final int SCRIPT_DATA_ESCAPED_DASH_DASH_STATE = 28;
    static final int SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN_STATE = 29;
    static final int SCRIPT_DATA_ESCAPED_END_TAG_OPEN_STATE = 30;
    static final int SCRIPT_DATA_ESCAPED_END_TAG_NAME_STATE = 31;
    static final int SCRIPT_DATA_DOUBLE_ESCAPE_START_STATE = 32;
    static final int SCRIPT_DATA_DOUBLE_ESCAPED_STATE = 33;
    static final int SCRIPT_DATA_DOUBLE_ESCAPED_DASH_STATE = 34;
    static final int SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH_STATE = 35;
    static final int SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN_STATE = 36;
    static final int SCRIPT_DATA_DOUBLE_ESCAPE_END_STATE = 37;
    static final int AFTER_ATTRIBUTE_NAME_STATE = 38;
    static final int ATTRIBUTE_VALUE_SINGLE_QUOTED_STATE = 39;
    static final int ATTRIBUTE_VALUE_UNQUOTED_STATE = 40;
    static final int CHARACTER_REFERENCE_IN_ATTRIBUTE_VALUE_STATE = 41;
    static final int SELF_CLOSING_START_TAG_STATE = 42;
    static final int BOGUS_COMMENT_STATE = 43;
    static final int MARKUP_DECLARATION_OPEN_STATE = 44;
    static final int COMMENT_START_STATE = 45;
    static final int COMMENT_START_DASH_STATE = 46;
    static final int COMMENT_STATE = 47;
    static final int COMMENT_END_DASH_STATE = 48;
    static final int COMMENT_END_STATE = 49;
    static final int COMMENT_END_BANG_STATE = 50;
    static final int DOCTYPE_STATE = 51;
    static final int BEFORE_DOCTYPE_NAME_STATE = 52;
    static final int DOCTYPE_NAME_STATE = 53;
    static final int AFTER_DOCTYPE_NAME_STATE = 54;
    static final int AFTER_DOCTYPE_PUBLIC_KEYWORD_STATE = 55;
    static final int BEFORE_DOCTYPE_PUBLIC_IDENTIFIER_STATE = 56;
    static final int DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED_STATE = 57;
    static final int DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED_STATE = 58;
    static final int AFTER_DOCTYPE_PUBLIC_IDENTIFIER_STATE = 59;
    static final int BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS_STATE = 60;
    static final int AFTER_DOCTYPE_SYSTEM_KEYWORD_STATE = 61;
    static final int BEFORE_DOCTYPE_SYSTEM_IDENTIFIER_STATE = 62;
    static final int DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED_STATE = 63;
    static final int DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED_STATE = 64;
    static final int AFTER_DOCTYPE_SYSTEM_IDENTIFIER_STATE = 65;
    static final int BOGUS_DOCTYPE_STATE = 66;
    static final int CDATA_SECTION_STATE = 67;


    //region TokenizerTagStates
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
                    //handleTagOpenStateAnythingElse
                    tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                    tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
                    processedInputStream.reconsume(chr);
                }
                break;
        }
    }


    static void handleEndTagOpenState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                break;
            case Characters.EOF:
                // handleEndTagOpenStateEOF(tokenizer, processedInputStream, chr);
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.emitCharacter(Characters.LESSTHAN_SIGN);
                tokenizer.emitCharacter(Characters.SOLIDUS);
                processedInputStream.reconsume(chr);
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
    //endregion

    //region TokenizerRCDataAndScriptStates
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
        char[] chars = consumeCharacterReference(-1, false, processedInputStream, tokenizer);
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
        if (chr == Characters.SOLIDUS) {
            tokenizer.createTemporaryBuffer();
            tokenizer.setState(TokenizerState.RCDATA_END_TAG_OPEN_STATE);
        } else {
            tokenizer.setStateAndEmitCharacter(TokenizerState.RCDATA_STATE, Characters.LESSTHAN_SIGN);
            processedInputStream.reconsume(chr);
        }
    }

    static void handleRCDataEndTagOpenState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
            tokenizer.newEndTokenTag();
            tokenizer.appendCurrentTagToken(chr);
            tokenizer.appendToTemporaryBuffer(chr);
            tokenizer.setState(TokenizerState.RCDATA_END_TAG_NAME_STATE);
        } else {
            tokenizer.setStateAndEmitCharacter(TokenizerState.RCDATA_STATE, Characters.LESSTHAN_SIGN);
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
                if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
                    tokenizer.appendCurrentTagToken(chr);
                    tokenizer.appendToTemporaryBuffer(chr);
                } else {
                    anythingElseRCDataEndTagNameState(tokenizer, processedInputStream, chr);
                }
                break;
        }
    }

    private static void anythingElseRCDataEndTagNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream, int chr) {
        tokenizer.setStateAndEmitCharacter(TokenizerState.RCDATA_STATE, Characters.LESSTHAN_SIGN);
        tokenizer.emitCharacter(Characters.SOLIDUS);

        tokenizer.emitTemporaryBufferAsCharacters();

        processedInputStream.reconsume(chr);
    }

    // --------- script states

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
                ResizableCharBuilder textNode = tokenizer.getTokenHandlerInsertCharacterPreviousTextNode();

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

        if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
            tokenizer.newEndTokenTag();
            tokenizer.appendCurrentTagToken(chr);
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
                if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
                    tokenizer.appendCurrentTagToken(chr);
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
                tokenizer.emitParseErrorAndSetState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
                tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
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
                tokenizer.emitParseErrorAndSetState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
                tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
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

        if (chr == Characters.SOLIDUS) {
            tokenizer.createTemporaryBuffer();
            tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_END_TAG_OPEN_STATE);
        } else if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
            tokenizer.createTemporaryBuffer();
            tokenizer.appendToTemporaryBuffer(chr);
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

        if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
            tokenizer.newEndTokenTag();
            tokenizer.appendCurrentTagToken(chr);
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
                if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
                    tokenizer.appendCurrentTagToken(chr);
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

    private static final char[] SCRIPT = "script".toCharArray();

    static void handleScriptDataDoubleEscapeStartState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
            case Characters.SOLIDUS:
            case Characters.GREATERTHAN_SIGN:
                if (tokenizer.isTemporaryBufferEquals(SCRIPT)) {
                    tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
                } else {
                    tokenizer.setState(TokenizerState.SCRIPT_DATA_ESCAPED_STATE);
                }
                tokenizer.emitCharacter(chr);
                break;
            default:
                if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
                    tokenizer.appendToTemporaryBuffer(chr);
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
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
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
                tokenizer.emitParseErrorAndSetState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
                tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
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
                tokenizer.emitParseErrorAndSetState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
                tokenizer.emitCharacter(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
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
                var state = tokenizer.isTemporaryBufferEquals(SCRIPT) ? TokenizerState.SCRIPT_DATA_ESCAPED_STATE : TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE;
                tokenizer.setStateAndEmitCharacter(state, chr);
                break;
            default:
                if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
                    tokenizer.appendToTemporaryBuffer(chr);
                    tokenizer.emitCharacter(chr);
                } else {
                    tokenizer.setState(TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE);
                    processedInputStream.reconsume(chr);
                }
                break;
        }
    }
    //endregion

    //region TokenizerPlainAndRawTextStates

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
        if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
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
                if (Common.isUpperOrLowerCaseASCIILetter(chr)) {
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

    //endregion

    //region TokenizerMarkupDeclarationState
    static void handleMarkupDeclarationOpenState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int[] chars = new int[7];

        chars[0] = processedInputStream.peekNextInputCharacter(1);
        chars[1] = processedInputStream.peekNextInputCharacter(2);

        if (chars[0] == Characters.HYPHEN_MINUS && chars[1] == Characters.HYPHEN_MINUS) {
            processedInputStream.consume(2);
            tokenizer.createNewCommentToken();
            tokenizer.setState(TokenizerState.COMMENT_START_STATE);
        } else {
            chars[2] = processedInputStream.peekNextInputCharacter(3);
            chars[3] = processedInputStream.peekNextInputCharacter(4);
            chars[4] = processedInputStream.peekNextInputCharacter(5);
            chars[5] = processedInputStream.peekNextInputCharacter(6);
            chars[6] = processedInputStream.peekNextInputCharacter(7);

            if (Common.matchCharsCaseInsensitive(Common.DOCTYPE, chars)) {
                processedInputStream.consume(7);
                tokenizer.setState(TokenizerState.DOCTYPE_STATE);

            } else if (tokenizer.getAdjustedCurrentNode() != null && //
                    Node.NAMESPACE_HTML_ID != tokenizer.getAdjustedCurrentNode().namespaceID && //
                    Arrays.equals(CDATA, chars)) {

                processedInputStream.consume(7);
                tokenizer.setState(TokenizerState.CDATA_SECTION_STATE);

            } else {
                tokenizer.emitParseErrorAndSetState(TokenizerState.BOGUS_COMMENT_STATE);
            }
        }
    }

    private static final int[] CDATA = new int[] {'[', 'C', 'D', 'A', 'T', 'A', '['}; //"[CDATA[".toCharArray();

    //endregion

    //region TokenizerDoctypeStates
    static void handleDoctypeState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                tokenizer.setState(TokenizerState.BEFORE_DOCTYPE_NAME_STATE);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.createNewDoctypeToken();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(null, null, null);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.emitParseErrorAndSetState(TokenizerState.BEFORE_DOCTYPE_NAME_STATE);
                processedInputStream.reconsume(chr);
                break;
        }
    }

    static void handleBeforeDoctypeNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                // ignore
                break;
            case Characters.NULL:
                tokenizer.emitParseError();
                tokenizer.createNewDoctypeToken();
                tokenizer.appendDoctypeNameCharacter(Characters.REPLACEMENT_CHARACTER);
                tokenizer.setState(TokenizerState.DOCTYPE_NAME_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseError();
                tokenizer.createNewDoctypeToken();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(null, null, null);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.createNewDoctypeToken();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(null, null, null);
                processedInputStream.reconsume(chr);
                break;
            default:
                if (Common.isUpperCaseASCIILetter(chr)) {
                    tokenizer.createNewDoctypeToken();
                    tokenizer.appendDoctypeNameCharacter(chr + 0x0020);
                    tokenizer.setState(TokenizerState.DOCTYPE_NAME_STATE);
                } else {
                    tokenizer.createNewDoctypeToken();
                    tokenizer.appendDoctypeNameCharacter(chr);
                    tokenizer.setState(TokenizerState.DOCTYPE_NAME_STATE);
                }
                break;
        }
    }

    static void handleDoctypeNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                tokenizer.setState(TokenizerState.AFTER_DOCTYPE_NAME_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), null, null);
                break;
            case Characters.NULL:
                tokenizer.emitParseError();
                tokenizer.appendDoctypeNameCharacter(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), null, null);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.appendDoctypeNameCharacter(chr);
                break;
        }
    }

    static void handleAfterDoctypeNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                // ignore
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), null, null);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), null, null);
                processedInputStream.reconsume(chr);
                break;
            default:
                int[] sixChars = new int[]{
                        chr,
                        processedInputStream.peekNextInputCharacter(1),
                        processedInputStream.peekNextInputCharacter(2),
                        processedInputStream.peekNextInputCharacter(3),
                        processedInputStream.peekNextInputCharacter(4),
                        processedInputStream.peekNextInputCharacter(5)
                };
                if (Common.matchCharsCaseInsensitive(Common.PUBLIC, sixChars)) {
                    processedInputStream.consume(5);
                    tokenizer.setState(TokenizerState.AFTER_DOCTYPE_PUBLIC_KEYWORD_STATE);
                } else if (Common.matchCharsCaseInsensitive(Common.SYSTEM, sixChars)) {
                    processedInputStream.consume(5);
                    tokenizer.setState(TokenizerState.AFTER_DOCTYPE_SYSTEM_KEYWORD_STATE);
                } else {
                    tokenizer.emitParseError();
                    tokenizer.setDoctypeForceQuirksFlag(true);
                    tokenizer.setState(TokenizerState.BOGUS_DOCTYPE_STATE);
                }
                break;
        }
    }

    static void handleAfterDoctypePublicKeywordState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                tokenizer.setState(TokenizerState.BEFORE_DOCTYPE_PUBLIC_IDENTIFIER_STATE);
                break;
            case Characters.QUOTATION_MARK:
                tokenizer.emitParseError();
                tokenizer.createDoctypePublicIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED_STATE);
                break;
            case Characters.APOSTROPHE:
                tokenizer.emitParseError();
                tokenizer.createDoctypePublicIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), null, null);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), null, null);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.BOGUS_DOCTYPE_STATE);
                break;
        }
    }

    static void handleBeforeDoctypePublicIdentifierState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                // ignore
                break;
            case Characters.QUOTATION_MARK:
                tokenizer.createDoctypePublicIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED_STATE);
                break;
            case Characters.APOSTROPHE:
                tokenizer.createDoctypePublicIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), null, null);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), null, null);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.BOGUS_DOCTYPE_STATE);
                break;
        }
    }

    static void handleDoctypePublicIdentifierDoubleQuotedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.QUOTATION_MARK:
                tokenizer.setState(TokenizerState.AFTER_DOCTYPE_PUBLIC_IDENTIFIER_STATE);
                break;
            case Characters.NULL:
                tokenizer.emitParseError();
                tokenizer.appendDoctypePublicIdentifier(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.appendDoctypePublicIdentifier(chr);
                break;
        }
    }

    static void handleDoctypePublicIdentifierSingleQuotedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.APOSTROPHE:
                tokenizer.setState(TokenizerState.AFTER_DOCTYPE_PUBLIC_IDENTIFIER_STATE);
                break;
            case Characters.NULL:
                tokenizer.emitParseError();
                tokenizer.appendDoctypePublicIdentifier(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.appendDoctypePublicIdentifier(chr);
                break;
        }
    }

    static void handleAfterDoctypePublicIdentifierState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                tokenizer.setState(TokenizerState.BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                break;
            case Characters.QUOTATION_MARK:
                tokenizer.emitParseError();
                tokenizer.createDoctypeSystemIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED_STATE);
                break;
            case Characters.APOSTROPHE:
                tokenizer.emitParseError();
                tokenizer.createDoctypeSystemIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED_STATE);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.BOGUS_DOCTYPE_STATE);
                break;
        }
    }

    static void handleBetweenDoctypePublicAndSystemIdentifiersState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                // ignore
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                break;
            case Characters.QUOTATION_MARK:
                tokenizer.createDoctypeSystemIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED_STATE);
                break;
            case Characters.APOSTROPHE:
                tokenizer.createDoctypeSystemIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED_STATE);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.BOGUS_DOCTYPE_STATE);
                break;
        }
    }

    static void handleAfterDoctypeSystemKeywordState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                tokenizer.setState(TokenizerState.BEFORE_DOCTYPE_SYSTEM_IDENTIFIER_STATE);
                break;
            case Characters.QUOTATION_MARK:
                tokenizer.emitParseError();
                tokenizer.createDoctypeSystemIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED_STATE);
                break;
            case Characters.APOSTROPHE:
                tokenizer.emitParseError();
                tokenizer.createDoctypeSystemIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.BOGUS_DOCTYPE_STATE);
                break;
        }
    }

    static void handleBeforeDoctypeSystemIdentifierState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                // ignore
                break;

            case Characters.QUOTATION_MARK:
                tokenizer.createDoctypeSystemIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED_STATE);
                break;
            case Characters.APOSTROPHE:
                tokenizer.createDoctypeSystemIdentifier();
                tokenizer.setState(TokenizerState.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), null);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.BOGUS_DOCTYPE_STATE);
                break;
        }
    }

    static void handleDoctypeSystemIdentifierDoubleQuotedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.QUOTATION_MARK:
                tokenizer.setState(TokenizerState.AFTER_DOCTYPE_SYSTEM_IDENTIFIER_STATE);
                break;
            case Characters.NULL:
                tokenizer.emitParseError();
                tokenizer.appendDoctypeSystemIdentifierCharacter(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), tokenizer.getDoctypeSystemIdentifier());
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), tokenizer.getDoctypeSystemIdentifier());
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.appendDoctypeSystemIdentifierCharacter(chr);
                break;
        }
    }

    static void handleDoctypeSystemIdentifierSingleQuotedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.APOSTROPHE:
                tokenizer.setState(TokenizerState.AFTER_DOCTYPE_SYSTEM_IDENTIFIER_STATE);
                break;
            case Characters.NULL:
                tokenizer.emitParseError();
                tokenizer.appendDoctypeSystemIdentifierCharacter(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseError();
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), tokenizer.getDoctypeSystemIdentifier());
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), tokenizer.getDoctypeSystemIdentifier());
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.appendDoctypeSystemIdentifierCharacter(chr);
                break;
        }
    }

    static void handleAfterDoctypeSystemIdentifierState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                // ignore
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), tokenizer.getDoctypeSystemIdentifier());
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.setDoctypeForceQuirksFlag(true);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), tokenizer.getDoctypeSystemIdentifier());
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.emitParseErrorAndSetState(TokenizerState.BOGUS_DOCTYPE_STATE);
                break;
        }
    }

    static void handleBogusDoctypeState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.GREATERTHAN_SIGN:
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), tokenizer.getDoctypeSystemIdentifier());
                break;
            case Characters.EOF:
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitDoctypeToken(tokenizer.getDoctypeNameToken(), tokenizer.getDoctypePublicIdentifier(), tokenizer.getDoctypeSystemIdentifier());
                processedInputStream.reconsume(chr);
                break;
            default:
                // ignore
                break;
        }
    }
    //endregion

    //region TokenizerCommentStates
    static void handleCommentStartState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.HYPHEN_MINUS:
                tokenizer.setState(TokenizerState.COMMENT_START_DASH_STATE);
                break;
            case Characters.NULL:
                tokenizer.emitParseError();
                tokenizer.appendCommentCharacter(Characters.REPLACEMENT_CHARACTER);
                tokenizer.setState(TokenizerState.COMMENT_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.emitComment();
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.emitComment();
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.appendCommentCharacter(chr);
                tokenizer.setState(TokenizerState.COMMENT_STATE);
                break;
        }
    }

    static void handleCommentStartDashState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.HYPHEN_MINUS:
                tokenizer.setState(TokenizerState.COMMENT_END_STATE);
                break;
            case Characters.NULL:
                tokenizer.emitParseError();
                tokenizer.appendCommentCharacter(Characters.HYPHEN_MINUS, Characters.REPLACEMENT_CHARACTER);
                tokenizer.setState(TokenizerState.COMMENT_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.emitComment();
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.emitComment();
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.appendCommentCharacter(Characters.HYPHEN_MINUS, chr);
                tokenizer.setState(TokenizerState.COMMENT_STATE);
                break;
        }
    }

    static void handleCommentState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        do {
            int chr = processedInputStream.getNextInputCharacterAndConsume();
            switch (chr) {
                case Characters.HYPHEN_MINUS:
                    tokenizer.setState(TokenizerState.COMMENT_END_DASH_STATE);
                    return;
                case Characters.NULL:
                    tokenizer.emitParseError();
                    tokenizer.appendCommentCharacter(Characters.REPLACEMENT_CHARACTER);
                    return;
                case Characters.EOF:
                    tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                    tokenizer.emitComment();
                    processedInputStream.reconsume(chr);
                    return;
                default:
                    tokenizer.appendCommentCharacter(chr);
            }
        } while (true);
    }

    static void handleCommentEndDashState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.HYPHEN_MINUS:
                tokenizer.setState(TokenizerState.COMMENT_END_STATE);
                break;
            case Characters.NULL:
                tokenizer.emitParseError();
                tokenizer.appendCommentCharacter(Characters.HYPHEN_MINUS, Characters.REPLACEMENT_CHARACTER);
                tokenizer.setState(TokenizerState.COMMENT_STATE);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.emitComment();
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.appendCommentCharacter(Characters.HYPHEN_MINUS, chr);
                tokenizer.setState(TokenizerState.COMMENT_STATE);
                break;
        }
    }

    static void handleCommentEndState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.GREATERTHAN_SIGN:
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitComment();
                break;
            case Characters.NULL:
                tokenizer.emitParseError();
                tokenizer.appendCommentCharacter(Characters.HYPHEN_MINUS, Characters.HYPHEN_MINUS);
                tokenizer.appendCommentCharacter(Characters.REPLACEMENT_CHARACTER);
                tokenizer.setState(TokenizerState.COMMENT_STATE);
                break;
            case Characters.EXCLAMATION_MARK:
                tokenizer.emitParseErrorAndSetState(TokenizerState.COMMENT_END_BANG_STATE);
                break;
            case Characters.HYPHEN_MINUS:
                tokenizer.emitParseError();
                tokenizer.appendCommentCharacter(Characters.HYPHEN_MINUS);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.emitComment();
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.emitParseError();
                tokenizer.appendCommentCharacter(Characters.HYPHEN_MINUS, Characters.HYPHEN_MINUS);
                tokenizer.appendCommentCharacter(chr);
                tokenizer.setState(TokenizerState.COMMENT_STATE);
                break;
        }

    }

    static void handleCommentEndBangState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.HYPHEN_MINUS:
                tokenizer.appendCommentCharacter(Characters.HYPHEN_MINUS, Characters.HYPHEN_MINUS);
                tokenizer.appendCommentCharacter(Characters.EXCLAMATION_MARK);
                tokenizer.setState(TokenizerState.COMMENT_END_DASH_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.emitComment();
                break;
            case Characters.NULL:
                tokenizer.emitParseError();
                tokenizer.appendCommentCharacter(Characters.HYPHEN_MINUS, Characters.HYPHEN_MINUS);
                tokenizer.appendCommentCharacter(Characters.EXCLAMATION_MARK, Characters.REPLACEMENT_CHARACTER);
                tokenizer.setState(TokenizerState.COMMENT_STATE);
                break;
            case Characters.EOF:
                tokenizer.emitParseError();
                tokenizer.emitComment();
                tokenizer.setState(TokenizerState.DATA_STATE);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.appendCommentCharacter(Characters.HYPHEN_MINUS, Characters.HYPHEN_MINUS);
                tokenizer.appendCommentCharacter(Characters.EXCLAMATION_MARK, chr);
                tokenizer.setState(TokenizerState.COMMENT_STATE);
                break;
        }
    }

    static void handleBogusCommentState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        ResizableCharBuilder sb = new ResizableCharBuilder();

        boolean continueProcess = true;
        while (continueProcess) {
            int chr = processedInputStream.getNextInputCharacterAndConsume();
            switch (chr) {
                case Characters.GREATERTHAN_SIGN:
                    continueProcess = false;
                    break;
                case Characters.EOF:
                    continueProcess = false;
                    processedInputStream.reconsume(chr);
                    break;
                case Characters.NULL:
                    sb.append(Characters.REPLACEMENT_CHARACTER);
                    break;
                default:
                    sb.append((char) chr);
                    break;
            }
        }
        tokenizer.emitComment(sb);

        tokenizer.setState(TokenizerState.DATA_STATE);
    }
    //endregion

    //region TokenizerCharacterReference
    static char[] consumeCharacterReference(int additionalCharacter, boolean inAttribute, ProcessedInputStream processedInputStream, Tokenizer tokenHandler) {
        //
        if(!tokenHandler.transformEntities) {
            return null;
        }
        //

        int chr = processedInputStream.getNextInputCharacter();

        if (additionalCharacter != -1 && additionalCharacter == chr) {
            return null;
        }

        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.SPACE:
            case Characters.LESSTHAN_SIGN:
            case Characters.AMPERSAND:
            case Characters.EOF:
                return null;
            case Characters.NUMBER_SIGN: {
                return parseNumberSign(processedInputStream, tokenHandler);
            }
            default:
                return parseEntity(inAttribute, processedInputStream, tokenHandler, chr);
        }
    }

    private static char[] parseNumberSign(ProcessedInputStream processedInputStream, Tokenizer tokenHandler) {
        processedInputStream.consume();
        int nextChar = processedInputStream.getNextInputCharacter();
        if (nextChar == Characters.LATIN_SMALL_LETTER_X || nextChar == Characters.LATIN_CAPITAL_LETTER_X) {
            return parseHexSection(processedInputStream, tokenHandler, nextChar);
        } else {
            return parseDecSection(processedInputStream, tokenHandler);
        }
    }

    private static char[] parseEntity(boolean inAttribute, ProcessedInputStream processedInputStream, Tokenizer tokenHandler, int chr) {
        int matchedCount = 0;
        var currentPrefix = EntitiesPrefix.ENTITIES;
        ResizableCharBuilder tentativelyMatched = new ResizableCharBuilder();

        for (;;) {
            int next = processedInputStream.peekNextInputCharacter(matchedCount + 1);
            if (next != Characters.EOF) {
                tentativelyMatched.append((char) next);
            }
            var tmpPrefix = currentPrefix.getNode((char) next);
            if (tmpPrefix != null) {
                currentPrefix = tmpPrefix;
                matchedCount++;
            } else {
                break;
            }
        }

        if (!currentPrefix.isComplete()) {
            var maybeCompleteParent = currentPrefix.getMaybeCompleteParent();
            if (maybeCompleteParent != null) {
                currentPrefix = maybeCompleteParent;
            }
        }

        if (currentPrefix.isComplete()) {
            String entityMatched = currentPrefix.getString();
            if (inAttribute) {
                return handleCompleteEntityInAttribute(processedInputStream, tokenHandler, currentPrefix, entityMatched);
            } else {
                return handleCompleteEntityNotInAttribute(processedInputStream, tokenHandler, currentPrefix, entityMatched);
            }
        } else {
            // handleUncompleteEntity
            // If no match can be made, then no characters are consumed, and
            // nothing is returned.
            // In this case, if the characters after the U+0026 AMPERSAND
            // character (&) consist of a sequence of one or more
            // alphanumeric ASCII characters
            // followed by a U+003B SEMICOLON character (;), then this is a
            // parse error.

            int tentativelyMatchedLength = tentativelyMatched.pos();
            boolean emitParseError = tentativelyMatchedLength > 1 && tentativelyMatched.at(tentativelyMatchedLength - 1) == Characters.SEMICOLON;
            if (emitParseError) {
                for (int i = 0; emitParseError && i < tentativelyMatchedLength - 1; i++) {
                    emitParseError = Common.isAlphaNumericASCII(chr);
                }
            }

            if (emitParseError) {
                tokenHandler.emitParseError();
            }
            return null;
        }
    }



    private static char[] handleCompleteEntityNotInAttribute(ProcessedInputStream processedInputStream, Tokenizer tokenHandler, EntitiesPrefix currentPrefix, String entityMatched) {
        if ((currentPrefix.c) != Characters.SEMICOLON) {
            tokenHandler.emitParseError();
        }

        processedInputStream.consume(entityMatched.length() - 1);
        return currentPrefix.chars;
    }

    private static char[] handleCompleteEntityInAttribute(ProcessedInputStream processedInputStream, Tokenizer tokenHandler, EntitiesPrefix currentPrefix, String entityMatched) {
        if (currentPrefix.c != Characters.SEMICOLON) {
            int nextCharacterAfterMatchedEntity = processedInputStream.peekNextInputCharacter(entityMatched.length());
            if (Common.isAlphaNumericASCII(nextCharacterAfterMatchedEntity)) {
                return null;
            } else if (Characters.EQUALS_SIGN == nextCharacterAfterMatchedEntity) {
                tokenHandler.emitParseError();
                return null;
            } else {
                return handleCompleteEntityNotInAttribute(processedInputStream, tokenHandler, currentPrefix, entityMatched);
            }

        } else {
            processedInputStream.consume(entityMatched.length() - 1);
            return currentPrefix.chars;
        }
    }

    private static char[] parseDecSection(ProcessedInputStream processedInputStream, Tokenizer tokenHandler) {

        int matchedCount = 0;
        ResizableCharBuilder sb = new ResizableCharBuilder();

        for (;;) {
            int nextPossibleHexDigit = processedInputStream.peekNextInputCharacter(matchedCount + 1);
            if (Common.isASCIIDigit(nextPossibleHexDigit)) {
                sb.append((char) nextPossibleHexDigit);
                matchedCount++;
            } else {
                break;
            }
        }

        if (matchedCount == 0) {
            // this handle the EOF too it seems
            processedInputStream.reconsume('#'); // #
            tokenHandler.emitParseError();
            return null;
        } else {
            processedInputStream.consume(matchedCount);
            if (Characters.SEMICOLON == processedInputStream.getNextInputCharacter()) {
                processedInputStream.consume();
            } else {
                tokenHandler.emitParseError();
            }
            try {
                return numberToChars(tokenHandler, sb, 10);
            } catch (NumberFormatException nfe) {
                // greater than Int
                tokenHandler.emitParseError();
                return Character.toChars(Characters.REPLACEMENT_CHARACTER);
            }
        }
    }

    private static char[] parseHexSection(ProcessedInputStream processedInputStream, Tokenizer tokenHandler, int prevChar) {

        processedInputStream.consume();

        int matchedCount = 0;
        ResizableCharBuilder sb = new ResizableCharBuilder();

        for (;;) {
            int nextPossibleHexDigit = processedInputStream.peekNextInputCharacter(matchedCount + 1);
            if (Common.isASCIIHexDigit(nextPossibleHexDigit)) {
                sb.append((char) nextPossibleHexDigit);
                matchedCount++;
            } else {
                break;
            }
        }

        if (matchedCount == 0) {
            processedInputStream.reconsume(prevChar);
            processedInputStream.reconsume('#');// # and x|X
            tokenHandler.emitParseError();
            return null;
        } else {
            processedInputStream.consume(matchedCount);
            if (Characters.SEMICOLON == processedInputStream.getNextInputCharacter()) {
                processedInputStream.consume();
            } else {
                tokenHandler.emitParseError();
            }
            try {
                return numberToChars(tokenHandler, sb, 16);
            } catch (NumberFormatException nfe) {
                // greater than Int
                tokenHandler.emitParseError();
                return Character.toChars(Characters.REPLACEMENT_CHARACTER);
            }
        }
    }

    private static char[] numberToChars(Tokenizer tokenHandler, ResizableCharBuilder sb, int radix) {
        int parsedInt = Integer.parseInt(sb.toString(), radix);

        final int characterReferenceInSubstitutionTable = isCharacterReferenceSubstitutionTable(parsedInt);

        if (characterReferenceInSubstitutionTable != -1) {
            tokenHandler.emitParseError();
            return Character.toChars(characterReferenceInSubstitutionTable);
        } else if ((parsedInt >= 0xD800 && parsedInt <= 0xDFFF) || parsedInt > 0x10FFFF) {
            tokenHandler.emitParseError();
            return Character.toChars(Characters.REPLACEMENT_CHARACTER);
        } else {
            if (isCharacterReferenceInvalid(parsedInt)) {
                tokenHandler.emitParseError();
            }
            return Character.toChars(parsedInt);
        }
    }

    // already sorted, can use binarySearch on it
    static final int[] invalidCharacterReference = new int[] { 0x000B, 0xFFFE, 0xFFFF, 0x1FFFE, 0x1FFFF, 0x2FFFE, 0x2FFFF, 0x3FFFE, 0x3FFFF, 0x4FFFE, 0x4FFFF, 0x5FFFE,
            0x5FFFF, 0x6FFFE, 0x6FFFF, 0x7FFFE, 0x7FFFF, 0x8FFFE, 0x8FFFF, 0x9FFFE, 0x9FFFF, 0xAFFFE, 0xAFFFF, 0xBFFFE, 0xBFFFF, 0xCFFFE, 0xCFFFF, 0xDFFFE, 0xDFFFF, 0xEFFFE,
            0xEFFFF, 0xFFFFE, 0xFFFFF, 0x10FFFE, 0x10FFFF };

    private static boolean isCharacterReferenceInvalid(int chr) {
        return (chr >= 0x0001 && chr <= 0x0008) || (chr >= 0x000D && chr <= 0x001F) || (chr >= 0x007F && chr <= 0x009F) || (chr >= 0xFDD0 && chr <= 0xFDEF)
                || Arrays.binarySearch(invalidCharacterReference, chr) > -1;
    }

    /*
     * Return -1 if it's not in the table, else the new value
     */
    private static int isCharacterReferenceSubstitutionTable(int chr) {

        if (chr == 0x00) {
            return Characters.REPLACEMENT_CHARACTER;
        } else if (chr >= 0x80 && chr <= 0x8E) {
            return handleRange8X(chr);
        } else if (chr >= 0x91 && chr <= 0x9F) {
            return handleRange9x(chr);
        } else {
            return -1;
        }
    }

    // TODO: use a support array!
    private static int handleRange9x(int chr) {
        return switch (chr) {
            case 0x91 -> Characters.LEFT_SINGLE_QUOTATION_MARK;
            case 0x92 -> Characters.RIGHT_SINGLE_QUOTATION_MARK;
            case 0x93 -> Characters.LEFT_DOUBLE_QUOTATION_MARK;
            case 0x94 -> Characters.RIGHT_DOUBLE_QUOTATION_MARK;
            case 0x95 -> Characters.BULLET;
            case 0x96 -> Characters.EN_DASH;
            case 0x97 -> Characters.EM_DASH;
            case 0x98 -> Characters.SMALL_TILDE;
            case 0x99 -> Characters.TRADE_MARK_SIGN;
            case 0x9A -> Characters.LATIN_SMALL_LETTER_S_WITH_CARON;
            case 0x9B -> Characters.SINGLE_RIGHT_POINTING_ANGLE_QUOTATION_MARK;
            case 0x9C -> Characters.LATIN_SMALL_LIGATURE_OE;
            case 0x9E -> Characters.LATIN_SMALL_LETTER_Z_WITH_CARON;
            case 0x9F -> Characters.LATIN_CAPITAL_LETTER_Y_WITH_DIAERESIS;
            // -----
            default -> -1;
        };
    }

    // TODO: use a support array!
    private static int handleRange8X(int chr) {
        return switch (chr) {
            case 0x80 -> Characters.EURO_SIGN;
            case 0x82 -> Characters.SINGLE_LOW_9_QUOTATION_MARK;
            case 0x83 -> Characters.LATIN_SMALL_LETTER_F_WITH_HOOK;
            case 0x84 -> Characters.DOUBLE_LOW_9_QUOTATION_MARK;
            case 0x85 -> Characters.HORIZONTAL_ELLIPSIS;
            case 0x86 -> Characters.DAGGER;
            case 0x87 -> Characters.DOUBLE_DAGGER;
            case 0x88 -> Characters.MODIFIER_LETTER_CIRCUMFLEX_ACCENT;
            case 0x89 -> Characters.PER_MILLE_SIGN;
            case 0x8A -> Characters.LATIN_CAPITAL_LETTER_S_WITH_CARON;
            case 0x8B -> Characters.SINGLE_LEFT_POINTING_ANGLE_QUOTATION_MARK;
            case 0x8C -> Characters.LATIN_CAPITAL_LIGATURE_OE;
            case 0x8E -> Characters.LATIN_CAPITAL_LETTER_Z_WITH_CARON;
            default -> -1;
        };
    }
    //endregion

    //region TokenizerCDataSectionAndDataState
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
                        && (currentInsertionMode == TreeConstructor.IM_IN_BODY || currentInsertionMode == TreeConstructor.IM_IN_CELL)
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
        char[] chars = TokenizerState.consumeCharacterReference(-1, false, processedInputStream, tokenizer);
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
    //endregion

    //region TokenizerAttributesState
    static void handleBeforeAttributeNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                // ignore
                break;
            case Characters.SOLIDUS:
                tokenizer.setState(TokenizerState.SELF_CLOSING_START_TAG_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.setState(TokenizerState.DATA_STATE);
                tokenizer.addCurrentAttributeAndEmitToken();
                break;
            case Characters.NULL:
                tokenizer.emitParseErrorAndSetState(TokenizerState.ATTRIBUTE_NAME_STATE);
                tokenizer.startNewAttributeAndAppendToName(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.QUOTATION_MARK:
            case Characters.APOSTROPHE:
            case Characters.LESSTHAN_SIGN:
            case Characters.EQUALS_SIGN:
                tokenizer.emitParseErrorAndSetState(TokenizerState.ATTRIBUTE_NAME_STATE);
                tokenizer.startNewAttributeAndAppendToName(chr);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.startNewAttributeAndAppendToName(chr);
                tokenizer.setState(TokenizerState.ATTRIBUTE_NAME_STATE);
                break;
        }
    }

    static void handleAttributeNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        // vvv optimization vvv, we try to bypass as much as possible for the case "appendCurrentAttributeName"
        do {
            int chr = processedInputStream.getNextInputCharacterAndConsume();
            switch (chr) {
                case Characters.TAB:
                case Characters.LF:
                case Characters.FF:
                case Characters.SPACE:
                    tokenizer.setState(TokenizerState.AFTER_ATTRIBUTE_NAME_STATE);
                    return;
                case Characters.SOLIDUS:
                    tokenizer.setState(TokenizerState.SELF_CLOSING_START_TAG_STATE);
                    return;
                case Characters.EQUALS_SIGN:
                    tokenizer.setState(TokenizerState.BEFORE_ATTRIBUTE_VALUE_STATE);
                    return;
                case Characters.GREATERTHAN_SIGN:
                    tokenizer.setState(TokenizerState.DATA_STATE);
                    tokenizer.addCurrentAttributeAndEmitToken();
                    return;
                case Characters.NULL:
                    tokenizer.emitParseError();
                    tokenizer.appendCurrentAttributeName(Characters.REPLACEMENT_CHARACTER);
                    return;
                case Characters.QUOTATION_MARK:
                case Characters.APOSTROPHE:
                case Characters.LESSTHAN_SIGN:
                    tokenizer.emitParseError();
                    tokenizer.appendCurrentAttributeName(chr);
                    return;
                case Characters.EOF:
                    tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                    processedInputStream.reconsume(chr);
                    return;
                default:
                    tokenizer.appendCurrentAttributeName(chr);
                    break;
            }
        } while (true);
    }

    static void handleAfterAttributeNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                // ignore
                break;
            case Characters.SOLIDUS:
                tokenizer.setState(TokenizerState.SELF_CLOSING_START_TAG_STATE);
                break;
            case Characters.EQUALS_SIGN:
                tokenizer.setState(TokenizerState.BEFORE_ATTRIBUTE_VALUE_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.setState(TokenizerState.DATA_STATE);
                //
                tokenizer.addCurrentAttributeAndEmitToken();
                //
                break;
            case Characters.NULL:
                tokenizer.emitParseErrorAndSetState(TokenizerState.ATTRIBUTE_NAME_STATE);
                tokenizer.addCurrentAttributeInAttributes();
                tokenizer.startNewAttributeAndAppendToName(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.QUOTATION_MARK:
            case Characters.APOSTROPHE:
            case Characters.LESSTHAN_SIGN:
                tokenizer.emitParseErrorAndSetState(TokenizerState.ATTRIBUTE_NAME_STATE);
                tokenizer.addCurrentAttributeInAttributes();
                tokenizer.startNewAttributeAndAppendToName(chr);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                processedInputStream.reconsume(chr);
                break;
            default:
                try {
                    tokenizer.addCurrentAttributeInAttributes();
                } catch (NullPointerException npe) {
                    // Duplicate close tag attributes
                    tokenizer.emitParseError();
                }
                tokenizer.startNewAttributeAndAppendToName(chr);
                tokenizer.setState(TokenizerState.ATTRIBUTE_NAME_STATE);
                break;
        }
    }

    static void handleBeforeAttributeValueState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                // ignore
                break;
            case Characters.QUOTATION_MARK:
                tokenizer.setState(TokenizerState.ATTRIBUTE_VALUE_DOUBLE_QUOTED_STATE);
                tokenizer.setAttributeQuoteType(TokenizerState.ATTRIBUTE_VALUE_DOUBLE_QUOTED_STATE);
                break;
            case Characters.AMPERSAND:
                tokenizer.setState(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
                processedInputStream.reconsume(chr);
                break;
            case Characters.APOSTROPHE:
                tokenizer.setState(TokenizerState.ATTRIBUTE_VALUE_SINGLE_QUOTED_STATE);
                tokenizer.setAttributeQuoteType(TokenizerState.ATTRIBUTE_VALUE_SINGLE_QUOTED_STATE);
                break;
            case Characters.NULL:
                tokenizer.emitParseErrorAndSetState(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
                tokenizer.appendCurrentAttributeValue(Characters.REPLACEMENT_CHARACTER);
                break;
            case Characters.GREATERTHAN_SIGN:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                tokenizer.addCurrentAttributeAndEmitToken();
                break;
            case Characters.LESSTHAN_SIGN:
            case Characters.EQUALS_SIGN:
            case Characters.GRAVE_ACCENT:
                tokenizer.emitParseErrorAndSetState(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
                tokenizer.appendCurrentAttributeValue(chr);
                break;
            case Characters.EOF:
                tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                processedInputStream.reconsume(chr);
                break;
            default:
                tokenizer.setState(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
                tokenizer.appendCurrentAttributeValue(chr);
        }
    }

    static void handleAttributeValueDoubleQuotedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        // vvv optimization vvv, we try to append as much as possible
        do {
            int chr = processedInputStream.getNextInputCharacterAndConsume();
            switch (chr) {
                case Characters.QUOTATION_MARK:
                    tokenizer.setState(TokenizerState.AFTER_ATTRIBUTE_VALUE_QUOTED_STATE);
                    return;
                case Characters.AMPERSAND:
                    // save current state
                    tokenizer.setPreviousState(TokenizerState.ATTRIBUTE_VALUE_DOUBLE_QUOTED_STATE);
                    //
                    tokenizer.setState(TokenizerState.CHARACTER_REFERENCE_IN_ATTRIBUTE_VALUE_STATE);
                    tokenizer.additionalAllowedCharacter = Characters.QUOTATION_MARK;
                    return;
                case Characters.NULL:
                    tokenizer.emitParseError();
                    tokenizer.appendCurrentAttributeValue(Characters.REPLACEMENT_CHARACTER);
                    return;
                case Characters.EOF:
                    tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                    processedInputStream.reconsume(chr);
                    return;
                default:
                    tokenizer.appendCurrentAttributeValue(chr);
            }
        } while (true);
    }

    static void handleAttributeValueSingleQuotedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        do {
            int chr = processedInputStream.getNextInputCharacterAndConsume();
            switch (chr) {
                case Characters.APOSTROPHE:
                    tokenizer.setState(TokenizerState.AFTER_ATTRIBUTE_VALUE_QUOTED_STATE);
                    return;
                case Characters.AMPERSAND:
                    // save current state
                    tokenizer.setPreviousState(TokenizerState.ATTRIBUTE_VALUE_SINGLE_QUOTED_STATE);
                    //
                    tokenizer.setState(TokenizerState.CHARACTER_REFERENCE_IN_ATTRIBUTE_VALUE_STATE);
                    tokenizer.additionalAllowedCharacter = Characters.APOSTROPHE;
                    return;
                case Characters.NULL:
                    tokenizer.emitParseError();
                    tokenizer.appendCurrentAttributeValue(Characters.REPLACEMENT_CHARACTER);
                    return;
                case Characters.EOF:
                    tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                    processedInputStream.reconsume(chr);
                    return;
                default:
                    tokenizer.appendCurrentAttributeValue(chr);
            }
        } while (true);
    }

    static void handleAttributeValueUnquotedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        do {
            int chr = processedInputStream.getNextInputCharacterAndConsume();
            switch (chr) {
                case Characters.TAB:
                case Characters.LF:
                case Characters.FF:
                case Characters.SPACE:
                    tokenizer.setState(TokenizerState.BEFORE_ATTRIBUTE_NAME_STATE);
                    return;
                case Characters.AMPERSAND:
                    tokenizer.setPreviousState(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
                    tokenizer.setState(TokenizerState.CHARACTER_REFERENCE_IN_ATTRIBUTE_VALUE_STATE);
                    tokenizer.additionalAllowedCharacter = Characters.GREATERTHAN_SIGN;
                    return;
                case Characters.GREATERTHAN_SIGN:
                    tokenizer.setState(TokenizerState.DATA_STATE);
                    tokenizer.addCurrentAttributeAndEmitToken();
                    return;
                case Characters.NULL:
                    tokenizer.emitParseError();
                    tokenizer.appendCurrentAttributeValue(Characters.REPLACEMENT_CHARACTER);
                    return;
                case Characters.QUOTATION_MARK:
                case Characters.APOSTROPHE:
                case Characters.LESSTHAN_SIGN:
                case Characters.EQUALS_SIGN:
                case Characters.GRAVE_ACCENT:
                    tokenizer.emitParseError();
                    tokenizer.appendCurrentAttributeValue(chr);
                    return;
                case Characters.EOF:
                    tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
                    processedInputStream.reconsume(chr);
                    return;
                default:
                    tokenizer.appendCurrentAttributeValue(chr);
            }
        } while (true);
    }

    static void handleCharacterReferenceInAttributeValueState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        char[] res = TokenizerState.consumeCharacterReference(tokenizer.additionalAllowedCharacter, true, processedInputStream, tokenizer);
        if (res == null) {
            tokenizer.appendCurrentAttributeValue(Characters.AMPERSAND);
        } else {
            for (int c : res) {
                tokenizer.appendCurrentAttributeValue(c);
            }
        }
        tokenizer.setState(tokenizer.getPreviousState());

        // cleanup, maybe useless
        tokenizer.setPreviousState(-1);
        tokenizer.additionalAllowedCharacter = -1;
        //
    }

    static void handleAfterAttributeValueQuotedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
            case Characters.TAB:
            case Characters.LF:
            case Characters.FF:
            case Characters.SPACE:
                tokenizer.setState(TokenizerState.BEFORE_ATTRIBUTE_NAME_STATE);
                break;
            case Characters.SOLIDUS:
                tokenizer.setState(TokenizerState.SELF_CLOSING_START_TAG_STATE);
                break;
            case Characters.GREATERTHAN_SIGN:
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
        }
    }
    //endregion
}