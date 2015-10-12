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

import java.util.Arrays;

class TokenizerCharacterReference {

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
        Prefix currentPrefix = Entities.ENTITIES;
        ResizableCharBuilder tentativelyMatched = new ResizableCharBuilder();

        for (;;) {
            int next = processedInputStream.peekNextInputCharacter(matchedCount + 1);
            if (next != Characters.EOF) {
                tentativelyMatched.append((char) next);
            }
            Prefix tmpPrefix = currentPrefix.getNode((char) next);
            if (tmpPrefix != null) {
                currentPrefix = tmpPrefix;
                matchedCount++;
            } else {
                break;
            }
        }

        if (!currentPrefix.isComplete() && currentPrefix.hasParentComplete()) {
            currentPrefix = currentPrefix.getCompleteParent();
        }

        if (currentPrefix.isComplete()) {
            return handleCompleteEntity(inAttribute, processedInputStream, tokenHandler, currentPrefix);
        } else {
            handleUncompleteEntity(tokenHandler, chr, tentativelyMatched);
            return null;
        }
    }

    private static void handleUncompleteEntity(Tokenizer tokenHandler, int chr, ResizableCharBuilder tentativelyMatched) {
        // If no match can be made, then no characters are consumed, and
        // nothing is returned.
        // In this case, if the characters after the U+0026 AMPERSAND
        // character (&) consist of a sequence of one or more
        // alphanumeric ASCII characters
        // followed by a U+003B SEMICOLON character (;), then this is a
        // parse error.

        int tentativelyMatchedLength = tentativelyMatched.length();
        boolean emitParseError = tentativelyMatchedLength > 1 && tentativelyMatched.charAt(tentativelyMatchedLength - 1) == Characters.SEMICOLON;
        if (emitParseError) {
            for (int i = 0; emitParseError && i < tentativelyMatchedLength - 1; i++) {
                emitParseError = emitParseError && Common.isAlphaNumericASCII(chr);
            }
        }

        if (emitParseError) {
            tokenHandler.emitParseError();
        }
    }

    private static char[] handleCompleteEntity(boolean inAttribute, ProcessedInputStream processedInputStream, Tokenizer tokenHandler, Prefix currentPrefix) {
        String entityMatched = currentPrefix.getString();
        if (inAttribute) {
            return handleCompleteEntityInAttribute(processedInputStream, tokenHandler, currentPrefix, entityMatched);
        } else {
            return handleCompleteEntityNotInAttribute(processedInputStream, tokenHandler, currentPrefix, entityMatched);
        }
    }

    private static char[] handleCompleteEntityNotInAttribute(ProcessedInputStream processedInputStream, Tokenizer tokenHandler, Prefix currentPrefix, String entityMatched) {
        if ((currentPrefix.c) != Characters.SEMICOLON) {
            tokenHandler.emitParseError();
        }

        processedInputStream.consume(entityMatched.length() - 1);
        return currentPrefix.chars;
    }

    private static char[] handleCompleteEntityInAttribute(ProcessedInputStream processedInputStream, Tokenizer tokenHandler, Prefix currentPrefix, String entityMatched) {
        if ((currentPrefix.c) != Characters.SEMICOLON) {
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
                int parsedInt = Integer.parseInt(sb.asString(), 10);

                final int characterReferenceInSobstitutionTable = isCharacterReferenceSobstitutionTable(parsedInt);

                if (characterReferenceInSobstitutionTable != -1) {
                    tokenHandler.emitParseError();
                    return Character.toChars(characterReferenceInSobstitutionTable);
                } else if ((parsedInt >= 0xD800 && parsedInt <= 0xDFFF) || parsedInt > 0x10FFFF) {
                    tokenHandler.emitParseError();
                    return Character.toChars(Characters.REPLACEMENT_CHARACTER);
                } else {
                    if (isCharacterReferenceInvalid(parsedInt)) {
                        tokenHandler.emitParseError();
                    }
                    return Character.toChars(parsedInt);
                }
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
                int parsedInt = Integer.parseInt(sb.asString(), 16);

                final int characterReferenceInSobstitutionTable = isCharacterReferenceSobstitutionTable(parsedInt);

                if (characterReferenceInSobstitutionTable != -1) {
                    tokenHandler.emitParseError();
                    return Character.toChars(characterReferenceInSobstitutionTable);
                } else if ((parsedInt >= 0xD800 && parsedInt <= 0xDFFF) || parsedInt > 0x10FFFF) {
                    tokenHandler.emitParseError();
                    return Character.toChars(Characters.REPLACEMENT_CHARACTER);
                } else {
                    if (isCharacterReferenceInvalid(parsedInt)) {
                        tokenHandler.emitParseError();
                    }
                    return Character.toChars(parsedInt);
                }
            } catch (NumberFormatException nfe) {
                // greater than Int
                tokenHandler.emitParseError();
                return Character.toChars(Characters.REPLACEMENT_CHARACTER);
            }
        }
    }

    private static final int[] invalidCharacterReference = new int[] { 0x000B, 0xFFFE, 0xFFFF, 0x1FFFE, 0x1FFFF, 0x2FFFE, 0x2FFFF, 0x3FFFE, 0x3FFFF, 0x4FFFE, 0x4FFFF, 0x5FFFE,
            0x5FFFF, 0x6FFFE, 0x6FFFF, 0x7FFFE, 0x7FFFF, 0x8FFFE, 0x8FFFF, 0x9FFFE, 0x9FFFF, 0xAFFFE, 0xAFFFF, 0xBFFFE, 0xBFFFF, 0xCFFFE, 0xCFFFF, 0xDFFFE, 0xDFFFF, 0xEFFFE,
            0xEFFFF, 0xFFFFE, 0xFFFFF, 0x10FFFE, 0x10FFFF };

    static {
        Arrays.sort(invalidCharacterReference);
    }

    private static boolean isCharacterReferenceInvalid(int chr) {
        return (chr >= 0x0001 && chr <= 0x0008) || (chr >= 0x000D && chr <= 0x001F) || (chr >= 0x007F && chr <= 0x009F) || (chr >= 0xFDD0 && chr <= 0xFDEF)
                || Arrays.binarySearch(invalidCharacterReference, chr) > -1;
    }

    /*
     * Return -1 if it's not in the table, else the new value
     */
    private static int isCharacterReferenceSobstitutionTable(int chr) {

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
        switch (chr) {
        case 0x91:
            return Characters.LEFT_SINGLE_QUOTATION_MARK;
        case 0x92:
            return Characters.RIGHT_SINGLE_QUOTATION_MARK;
        case 0x93:
            return Characters.LEFT_DOUBLE_QUOTATION_MARK;
        case 0x94:
            return Characters.RIGHT_DOUBLE_QUOTATION_MARK;
        case 0x95:
            return Characters.BULLET;
        case 0x96:
            return Characters.EN_DASH;
        case 0x97:
            return Characters.EM_DASH;
        case 0x98:
            return Characters.SMALL_TILDE;
        case 0x99:
            return Characters.TRADE_MARK_SIGN;
        case 0x9A:
            return Characters.LATIN_SMALL_LETTER_S_WITH_CARON;
        case 0x9B:
            return Characters.SINGLE_RIGHT_POINTING_ANGLE_QUOTATION_MARK;
        case 0x9C:
            return Characters.LATIN_SMALL_LIGATURE_OE;
        case 0x9E:
            return Characters.LATIN_SMALL_LETTER_Z_WITH_CARON;
        case 0x9F:
            return Characters.LATIN_CAPITAL_LETTER_Y_WITH_DIAERESIS;
            // -----
        default:
            return -1;
        }
    }

    // TODO: use a support array!
    private static int handleRange8X(int chr) {
        switch (chr) {
        case 0x80:
            return Characters.EURO_SIGN;
        case 0x82:
            return Characters.SINGLE_LOW_9_QUOTATION_MARK;
        case 0x83:
            return Characters.LATIN_SMALL_LETTER_F_WITH_HOOK;
        case 0x84:
            return Characters.DOUBLE_LOW_9_QUOTATION_MARK;
        case 0x85:
            return Characters.HORIZONTAL_ELLIPSIS;
        case 0x86:
            return Characters.DAGGER;
        case 0x87:
            return Characters.DOUBLE_DAGGER;
        case 0x88:
            return Characters.MODIFIER_LETTER_CIRCUMFLEX_ACCENT;
        case 0x89:
            return Characters.PER_MILLE_SIGN;
        case 0x8A:
            return Characters.LATIN_CAPITAL_LETTER_S_WITH_CARON;
        case 0x8B:
            return Characters.SINGLE_LEFT_POINTING_ANGLE_QUOTATION_MARK;
        case 0x8C:
            return Characters.LATIN_CAPITAL_LIGATURE_OE;
        case 0x8E:
            return Characters.LATIN_CAPITAL_LETTER_Z_WITH_CARON;
        default:
            return -1;
        }
    }
}
