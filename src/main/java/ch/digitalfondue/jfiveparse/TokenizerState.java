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
}