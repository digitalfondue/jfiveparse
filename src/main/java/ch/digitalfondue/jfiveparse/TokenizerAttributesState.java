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

class TokenizerAttributesState {

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
            if (Common.isUpperCaseASCIILetter(chr)) {
                chr += 0x0020;
            }
            tokenizer.startNewAttributeAndAppendToName(chr);
            tokenizer.setState(TokenizerState.ATTRIBUTE_NAME_STATE);
            break;
        }
    }

    /*
     * FIXME: When the user agent leaves the attribute name state (and before
     * emitting the tag token, if appropriate), the complete attribute's name
     * must be compared to the other attributes on the same token; if there is
     * already an attribute on the token with the exact same name, then this is
     * a parse error and the new attribute must be removed from the token.
     */
    static void handleAttributeNameState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.TAB:
        case Characters.LF:
        case Characters.FF:
        case Characters.SPACE:
            tokenizer.setState(TokenizerState.AFTER_ATTRIBUTE_NAME_STATE);
            break;
        case Characters.SOLIDUS:
            tokenizer.setState(TokenizerState.SELF_CLOSING_START_TAG_STATE);
            break;
        case Characters.EQUALS_SIGN:
            tokenizer.setState(TokenizerState.BEFORE_ATTRIBUTE_VALUE_STATE);
            break;
        case Characters.GREATERTHAN_SIGN:
            tokenizer.setState(TokenizerState.DATA_STATE);
            tokenizer.addCurrentAttributeAndEmitToken();
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.appendCurrentAttributeName(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.QUOTATION_MARK:
        case Characters.APOSTROPHE:
        case Characters.LESSTHAN_SIGN:
            tokenizer.emitParseError();
            tokenizer.appendCurrentAttributeName(chr);
            break;
        case Characters.EOF:
            tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
            processedInputStream.reconsume(chr);
            break;
        default:
            if (Common.isUpperCaseASCIILetter(chr)) {
                chr += 0x0020;
            }
            tokenizer.appendCurrentAttributeName(chr);
            break;
        }
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
            if (Common.isUpperCaseASCIILetter(chr)) {
                chr += 0x0020;
            }
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
            tokenizer.setAttributeQuoteType(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
            processedInputStream.reconsume(chr);
            break;
        case Characters.APOSTROPHE:
            tokenizer.setState(TokenizerState.ATTRIBUTE_VALUE_SINGLE_QUOTED_STATE);
            tokenizer.setAttributeQuoteType(TokenizerState.ATTRIBUTE_VALUE_SINGLE_QUOTED_STATE);
            break;
        case Characters.NULL:
            tokenizer.emitParseErrorAndSetState(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
            tokenizer.setAttributeQuoteType(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
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
            tokenizer.setAttributeQuoteType(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
            tokenizer.appendCurrentAttributeValue(chr);
            break;
        case Characters.EOF:
            tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.setState(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
            tokenizer.setAttributeQuoteType(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
            tokenizer.appendCurrentAttributeValue(chr);
            break;
        }
    }

    static void handleAttributeValueDoubleQuotedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.QUOTATION_MARK:
            tokenizer.setState(TokenizerState.AFTER_ATTRIBUTE_VALUE_QUOTED_STATE);
            break;
        case Characters.AMPERSAND:
            // save current state
            tokenizer.setPreviousState(TokenizerState.ATTRIBUTE_VALUE_DOUBLE_QUOTED_STATE);
            //
            tokenizer.setState(TokenizerState.CHARACTER_REFERENCE_IN_ATTRIBUTE_VALUE_STATE);
            tokenizer.setAdditionalAllowedCharacter(Characters.QUOTATION_MARK);
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.appendCurrentAttributeValue(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.appendCurrentAttributeValue(chr);
            break;
        }

    }

    static void handleAttributeValueSingleQuotedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.APOSTROPHE:
            tokenizer.setState(TokenizerState.AFTER_ATTRIBUTE_VALUE_QUOTED_STATE);
            break;
        case Characters.AMPERSAND:
            // save current state
            tokenizer.setPreviousState(TokenizerState.ATTRIBUTE_VALUE_SINGLE_QUOTED_STATE);
            //
            tokenizer.setState(TokenizerState.CHARACTER_REFERENCE_IN_ATTRIBUTE_VALUE_STATE);
            tokenizer.setAdditionalAllowedCharacter(Characters.APOSTROPHE);
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.appendCurrentAttributeValue(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.appendCurrentAttributeValue(chr);
            break;
        }
    }

    static void handleAttributeValueUnquotedState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.TAB:
        case Characters.LF:
        case Characters.FF:
        case Characters.SPACE:
            tokenizer.setState(TokenizerState.BEFORE_ATTRIBUTE_NAME_STATE);
            break;
        case Characters.AMPERSAND:
            tokenizer.setPreviousState(TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE);
            tokenizer.setState(TokenizerState.CHARACTER_REFERENCE_IN_ATTRIBUTE_VALUE_STATE);
            tokenizer.setAdditionalAllowedCharacter(Characters.GREATERTHAN_SIGN);
            break;
        case Characters.GREATERTHAN_SIGN:
            tokenizer.setState(TokenizerState.DATA_STATE);
            tokenizer.addCurrentAttributeAndEmitToken();
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.appendCurrentAttributeValue(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.QUOTATION_MARK:
        case Characters.APOSTROPHE:
        case Characters.LESSTHAN_SIGN:
        case Characters.EQUALS_SIGN:
        case Characters.GRAVE_ACCENT:
            tokenizer.emitParseError();
            tokenizer.appendCurrentAttributeValue(chr);
            break;
        case Characters.EOF:
            tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.appendCurrentAttributeValue(chr);
            break;
        }
    }

    static void handleCharacterReferenceInAttributeValueState(Tokenizer tokenizer, ProcessedInputStream processedInputStream) {
        char[] res = TokenizerCharacterReference.consumeCharacterReference(tokenizer.getAdditionalAllowedCharacter(), true, processedInputStream, tokenizer);
        if (res == null) {
            tokenizer.appendCurrentAttributeValue(Characters.AMPERSAND);
        } else {
            for (int c : res) {
                tokenizer.appendCurrentAttributeValue(c);
            }
        }
        tokenizer.setState(tokenizer.getPreviousState());

        // cleanup, maybe useless
        tokenizer.setPreviousState((byte) -1);
        tokenizer.setAdditionalAllowedCharacter(-1);
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
            break;
        }
    }
}
