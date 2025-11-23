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

class TokenizerDoctypeStates {

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
            int[] sixChars = new int[6];
            sixChars[0] = chr;
            sixChars[1] = processedInputStream.peekNextInputCharacter(1);
            sixChars[2] = processedInputStream.peekNextInputCharacter(2);
            sixChars[3] = processedInputStream.peekNextInputCharacter(3);
            sixChars[4] = processedInputStream.peekNextInputCharacter(4);
            sixChars[5] = processedInputStream.peekNextInputCharacter(5);

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
}
