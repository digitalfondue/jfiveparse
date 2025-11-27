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

import java.util.Locale;

/**
 * Implement the tokenization algorithm described in
 * 
 * https://html.spec.whatwg.org/multipage/syntax.html#tokenization .
 * 
 * Quite ugly, but it works.
 */
final class Tokenizer {

    private final TreeConstructor tokenHandler;

    private int state;

    //
    int additionalAllowedCharacter;
    private int previousState;

    // tag related
    private Attributes attributes;
    private final ResizableCharBuilder currentAttributeName = new ResizableCharBuilder();
    private ResizableCharBuilder currentAttributeValue;
    private int currentAttributeQuoteType;
    private boolean selfClosing;
    final ResizableCharBuilder tagName = new ResizableCharBuilder();
    private boolean isEndTagToken;
    char[] lastEmittedStartTagName;

    // doctype related
    private boolean doctypeForceQuirksFlag;
    private StringBuilder doctypeNameToken;
    private StringBuilder doctypePublicIdentifier;
    private StringBuilder doctypeSystemIdentifier;

    // comment related
    private ResizableCharBuilder commentToken;

    //
    private final ResizableCharBuilder temporaryBuffer = new ResizableCharBuilder();

    final boolean transformEntities;

    Tokenizer(TreeConstructor tokenHandler) {
        this(tokenHandler, true);
    }

    Tokenizer(TreeConstructor tokenHandler, boolean transformEntities) {
        this.tokenHandler = tokenHandler;
        state = TokenizerState.DATA_STATE;
        this.transformEntities = transformEntities;
    }

    //
    void appendCurrentAttributeName(int chr) {
        currentAttributeName.append((char) chr);
    }

    void appendCurrentAttributeValue(int chr) {
        currentAttributeValue.append((char) chr);
    }

    //

    //

    void createTemporaryBuffer() {
        temporaryBuffer.reset();
    }

    void appendToTemporaryBuffer(int chr) {
        temporaryBuffer.append((char) chr);
    }

    void emitTemporaryBufferAsCharacters() {
        int pos = temporaryBuffer.pos();
        for (int i = 0; i < pos; i++) {
            tokenHandler.emitCharacter(temporaryBuffer.at(i));
        }
    }

    boolean isTemporaryBufferEquals(char[] s) {
        return temporaryBuffer.equalsASCIICaseInsensitive(s);
    }

    //

    int getState() {
        return state;
    }

    void setState(int state) {
        this.state = state;
    }

    void emitParseError() {
        tokenHandler.emitParseError();
    }

    void setStateAndEmitCharacter(int state, int chr) {
        this.state = state;
        tokenHandler.emitCharacter((char) chr);
    }

    void emitCharacter(int chr) {
        tokenHandler.emitCharacter((char) chr);
    }

    void tokenize(ProcessedInputStream processedInputStream) {
        try {
            for (;;) {
                // most used states
                switch (state) {
                case TokenizerState.DATA_STATE:
                    TokenizerState.handleDataState(this, processedInputStream);
                    break;
                case TokenizerState.ATTRIBUTE_VALUE_DOUBLE_QUOTED_STATE:
                    TokenizerState.handleAttributeValueDoubleQuotedState(this, processedInputStream);
                    break;
                case TokenizerState.ATTRIBUTE_NAME_STATE:
                    TokenizerState.handleAttributeNameState(this, processedInputStream);
                    break;
                case TokenizerState.TAG_NAME_STATE:
                    TokenizerState.handleTagNameState(this, processedInputStream);
                    break;
                case TokenizerState.RAWTEXT_STATE:
                    TokenizerState.handleRawtextState(this, processedInputStream);
                    break;
                case TokenizerState.SCRIPT_DATA_STATE:
                    TokenizerState.handleScriptDataState(this, processedInputStream);
                    break;
                case TokenizerState.TAG_OPEN_STATE:
                    TokenizerState.handleTagOpenState(this, processedInputStream);
                    break;
                case TokenizerState.BEFORE_ATTRIBUTE_NAME_STATE:
                    TokenizerState.handleBeforeAttributeNameState(this, processedInputStream);
                    break;
                case TokenizerState.BEFORE_ATTRIBUTE_VALUE_STATE:
                    TokenizerState.handleBeforeAttributeValueState(this, processedInputStream);
                    break;
                case TokenizerState.AFTER_ATTRIBUTE_VALUE_QUOTED_STATE:
                    TokenizerState.handleAfterAttributeValueQuotedState(this, processedInputStream);
                    break;
                case TokenizerState.END_TAG_OPEN_STATE:
                    TokenizerState.handleEndTagOpenState(this, processedInputStream);
                    break;
                default:
                    allStates(processedInputStream);
                    break;
                }
                //
            }
        } catch (StopParse sp) {
        }
    }

    private void allStates(ProcessedInputStream processedInputStream) {
        switch (state) {
        case TokenizerState.DATA_STATE:
            TokenizerState.handleDataState(this, processedInputStream);
            break;
        case TokenizerState.CHARACTER_REFERENCE_IN_DATA_STATE:
            TokenizerState.handleCharacterReferenceInDataState(this, processedInputStream);
            break;
        case TokenizerState.RCDATA_STATE:
            TokenizerState.handleRCDataState(this, processedInputStream);
            break;
        case TokenizerState.CHARACTER_REFERENCE_IN_RCDATA_STATE:
            TokenizerState.handleCharacterReferenceInRCDataState(this, processedInputStream);
            break;
        case TokenizerState.RAWTEXT_STATE:
            TokenizerState.handleRawtextState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_STATE:
            TokenizerState.handleScriptDataState(this, processedInputStream);
            break;
        case TokenizerState.PLAINTEXT_STATE:
            TokenizerState.handlePlainTextState(this, processedInputStream);
            break;
        case TokenizerState.TAG_OPEN_STATE:
            TokenizerState.handleTagOpenState(this, processedInputStream);
            break;
        case TokenizerState.END_TAG_OPEN_STATE:
            TokenizerState.handleEndTagOpenState(this, processedInputStream);
            break;
        case TokenizerState.TAG_NAME_STATE:
            TokenizerState.handleTagNameState(this, processedInputStream);
            break;
        case TokenizerState.RCDATA_LESS_THAN_SIGN_STATE:
            TokenizerState.handleRCDataLessThanSignState(this, processedInputStream);
            break;
        case TokenizerState.RCDATA_END_TAG_OPEN_STATE:
            TokenizerState.handleRCDataEndTagOpenState(this, processedInputStream);
            break;
        case TokenizerState.RCDATA_END_TAG_NAME_STATE:
            TokenizerState.handleRCDataEndTagNameState(this, processedInputStream);
            break;
        case TokenizerState.RAWTEXT_LESS_THAN_SIGN_STATE:
            TokenizerState.handleRawTextLessThanSignState(this, processedInputStream);
            break;
        case TokenizerState.RAWTEXT_END_TAG_OPEN_STATE:
            TokenizerState.handleRawTextEndTagOpenState(this, processedInputStream);
            break;
        case TokenizerState.RAWTEXT_END_TAG_NAME_STATE:
            TokenizerState.handleRawTextEndTagNameState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_LESS_THAN_SIGN_STATE:
            TokenizerState.handleScriptDataLessThanSignState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_END_TAG_OPEN_STATE:
            TokenizerState.handleScriptDataEndTagOpenState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_END_TAG_NAME_STATE:
            TokenizerState.handleScriptDataEndTagNameState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_ESCAPE_START_STATE:
            TokenizerState.handleScriptDataEscapeStartState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_ESCAPE_START_DASH_STATE:
            TokenizerState.handleScriptDataEscapeStartDashState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_ESCAPED_STATE:
            TokenizerState.handleScriptDataEscapedState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_ESCAPED_DASH_STATE:
            TokenizerState.handleScriptDataEscapedDashState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_ESCAPED_DASH_DASH_STATE:
            TokenizerState.handleScriptDataEscapedDashDashState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_ESCAPED_LESS_THAN_SIGN_STATE:
            TokenizerState.handleScriptDataEscapedLessThanSignState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_ESCAPED_END_TAG_OPEN_STATE:
            TokenizerState.handleScriptDataEscapedEndTagOpenState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_ESCAPED_END_TAG_NAME_STATE:
            TokenizerState.handleScriptDataEscapedEndTagNameState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPE_START_STATE:
            TokenizerState.handleScriptDataDoubleEscapeStartState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_STATE:
            TokenizerState.handleScriptDataDoubleEscapedState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_DASH_STATE:
            TokenizerState.handleScriptDataDoubleEscapedDashState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_DASH_DASH_STATE:
            TokenizerState.handleScriptDataDoubleEscapedDashDashState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPED_LESS_THAN_SIGN_STATE:
            TokenizerState.handleScriptDataDoubleEscapedLessThanSignState(this, processedInputStream);
            break;
        case TokenizerState.SCRIPT_DATA_DOUBLE_ESCAPE_END_STATE:
            TokenizerState.handleScriptDataDoubleEscapedEndState(this, processedInputStream);
            break;
        case TokenizerState.BEFORE_ATTRIBUTE_NAME_STATE:
            TokenizerState.handleBeforeAttributeNameState(this, processedInputStream);
            break;
        case TokenizerState.ATTRIBUTE_NAME_STATE:
            TokenizerState.handleAttributeNameState(this, processedInputStream);
            break;
        case TokenizerState.AFTER_ATTRIBUTE_NAME_STATE:
            TokenizerState.handleAfterAttributeNameState(this, processedInputStream);
            break;
        case TokenizerState.BEFORE_ATTRIBUTE_VALUE_STATE:
            TokenizerState.handleBeforeAttributeValueState(this, processedInputStream);
            break;
        case TokenizerState.ATTRIBUTE_VALUE_DOUBLE_QUOTED_STATE:
            TokenizerState.handleAttributeValueDoubleQuotedState(this, processedInputStream);
            break;
        case TokenizerState.ATTRIBUTE_VALUE_SINGLE_QUOTED_STATE:
            TokenizerState.handleAttributeValueSingleQuotedState(this, processedInputStream);
            break;
        case TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE:
            TokenizerState.handleAttributeValueUnquotedState(this, processedInputStream);
            break;
        case TokenizerState.CHARACTER_REFERENCE_IN_ATTRIBUTE_VALUE_STATE:
            TokenizerState.handleCharacterReferenceInAttributeValueState(this, processedInputStream);
            break;
        case TokenizerState.AFTER_ATTRIBUTE_VALUE_QUOTED_STATE:
            TokenizerState.handleAfterAttributeValueQuotedState(this, processedInputStream);
            break;
        case TokenizerState.SELF_CLOSING_START_TAG_STATE:
            TokenizerState.handleSelfClosingStartTagState(this, processedInputStream);
            break;
        case TokenizerState.BOGUS_COMMENT_STATE:
            TokenizerState.handleBogusCommentState(this, processedInputStream);
            break;
        case TokenizerState.MARKUP_DECLARATION_OPEN_STATE:
            TokenizerState.handleMarkupDeclarationOpenState(this, processedInputStream);
            break;
        case TokenizerState.COMMENT_START_STATE:
            TokenizerState.handleCommentStartState(this, processedInputStream);
            break;
        case TokenizerState.COMMENT_START_DASH_STATE:
            TokenizerState.handleCommentStartDashState(this, processedInputStream);
            break;
        case TokenizerState.COMMENT_STATE:
            TokenizerState.handleCommentState(this, processedInputStream);
            break;
        case TokenizerState.COMMENT_END_DASH_STATE:
            TokenizerState.handleCommentEndDashState(this, processedInputStream);
            break;
        case TokenizerState.COMMENT_END_STATE:
            TokenizerState.handleCommentEndState(this, processedInputStream);
            break;
        case TokenizerState.COMMENT_END_BANG_STATE:
            TokenizerState.handleCommentEndBangState(this, processedInputStream);
            break;
        case TokenizerState.DOCTYPE_STATE:
            TokenizerState.handleDoctypeState(this, processedInputStream);
            break;
        case TokenizerState.BEFORE_DOCTYPE_NAME_STATE:
            TokenizerState.handleBeforeDoctypeNameState(this, processedInputStream);
            break;
        case TokenizerState.DOCTYPE_NAME_STATE:
            TokenizerState.handleDoctypeNameState(this, processedInputStream);
            break;
        case TokenizerState.AFTER_DOCTYPE_NAME_STATE:
            TokenizerState.handleAfterDoctypeNameState(this, processedInputStream);
            break;
        case TokenizerState.AFTER_DOCTYPE_PUBLIC_KEYWORD_STATE:
            TokenizerState.handleAfterDoctypePublicKeywordState(this, processedInputStream);
            break;
        case TokenizerState.BEFORE_DOCTYPE_PUBLIC_IDENTIFIER_STATE:
            TokenizerState.handleBeforeDoctypePublicIdentifierState(this, processedInputStream);
            break;
        case TokenizerState.DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED_STATE:
            TokenizerState.handleDoctypePublicIdentifierDoubleQuotedState(this, processedInputStream);
            break;
        case TokenizerState.DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED_STATE:
            TokenizerState.handleDoctypePublicIdentifierSingleQuotedState(this, processedInputStream);
            break;
        case TokenizerState.AFTER_DOCTYPE_PUBLIC_IDENTIFIER_STATE:
            TokenizerState.handleAfterDoctypePublicIdentifierState(this, processedInputStream);
            break;
        case TokenizerState.BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS_STATE:
            TokenizerState.handleBetweenDoctypePublicAndSystemIdentifiersState(this, processedInputStream);
            break;
        case TokenizerState.AFTER_DOCTYPE_SYSTEM_KEYWORD_STATE:
            TokenizerState.handleAfterDoctypeSystemKeywordState(this, processedInputStream);
            break;
        case TokenizerState.BEFORE_DOCTYPE_SYSTEM_IDENTIFIER_STATE:
            TokenizerState.handleBeforeDoctypeSystemIdentifierState(this, processedInputStream);
            break;
        case TokenizerState.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED_STATE:
            TokenizerState.handleDoctypeSystemIdentifierDoubleQuotedState(this, processedInputStream);
            break;
        case TokenizerState.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED_STATE:
            TokenizerState.handleDoctypeSystemIdentifierSingleQuotedState(this, processedInputStream);
            break;
        case TokenizerState.AFTER_DOCTYPE_SYSTEM_IDENTIFIER_STATE:
            TokenizerState.handleAfterDoctypeSystemIdentifierState(this, processedInputStream);
            break;
        case TokenizerState.BOGUS_DOCTYPE_STATE:
            TokenizerState.handleBogusDoctypeState(this, processedInputStream);
            break;
        case TokenizerState.CDATA_SECTION_STATE:
            TokenizerState.handleCDataSectionState(this, processedInputStream);
            break;
        }
    }

    // ------------

    // ------------

    boolean isAppropriateEndTagToken() {
        return tagName.pos() != 0 && lastEmittedStartTagName != null && tagName.equalsASCIICaseInsensitive(lastEmittedStartTagName);
    }

    // When the user agent leaves the attribute name state (and before emitting
    // the tag token, if appropriate), the complete attribute's name must be
    // compared to the other attributes on the same token; if there is already
    // an attribute on the token with the exact same name, then this is a parse
    // error and the new attribute must be removed from the token.
    //
    // TODO: maybe handle it better?
    void addCurrentAttributeInAttributes() {
        try {
            if (currentAttributeName.pos() != 0) { // not empty
                String curAttrName = currentAttributeName.toLowerCase();
                if (attributes.containsKey(curAttrName)) {
                    tokenHandler.emitParseError();
                } else {
                    attributes.put(new AttributeNode(
                            curAttrName,
                            currentAttributeName.containsUpperCase ? currentAttributeName.toString() : curAttrName,
                            currentAttributeValue,
                            currentAttributeQuoteType
                    ));
                }
            }
        } catch (NullPointerException npe) {
            // Failing on End Tag w/attribute -> the attributes map is not
            // initialized, will launch a npe
            tokenHandler.emitParseError();
        } finally {
            currentAttributeName.reset();
            currentAttributeValue = null;
        }
    }

    void startNewAttributeAndAppendToName(int chr) {
        if (attributes == null) {
            attributes = new Attributes();
        }
        addCurrentAttributeInAttributes();
        currentAttributeName.reset();
        currentAttributeValue = new ResizableCharBuilder();
        currentAttributeQuoteType = TokenizerState.ATTRIBUTE_VALUE_UNQUOTED_STATE;
        appendCurrentAttributeName(chr);
    }

    void setAttributeQuoteType(int currentAttributeQuoteType) {
        this.currentAttributeQuoteType = currentAttributeQuoteType;
    }

    void newEndTokenTag() {
        tagName.reset();
        isEndTagToken = true;
        attributes = null;
        currentAttributeName.reset();
        currentAttributeValue = null;
    }


    void appendCurrentTagToken(int chr) {
        tagName.append((char) chr);
    }

    void createNewStartTagToken(int chr) {
        tagName.reset();
        appendCurrentTagToken(chr);
        isEndTagToken = false;
        attributes = null;
        currentAttributeName.reset();
        currentAttributeValue = null;
        selfClosing = false;
    }

    void addCurrentAttributeAndEmitToken() {

        addCurrentAttributeInAttributes();

        /*
         * FIXME check if we need to add selfClosing to the condition: edit:
         * seems nope
         */
        if (!isEndTagToken) {
            lastEmittedStartTagName = tagName.copyBackingCharArray();
        }

        emitTagToken();
    }

    // ------------

    void createNewDoctypeToken() {
        doctypeForceQuirksFlag = false;
        doctypeNameToken = new StringBuilder();
    }

    void createDoctypeSystemIdentifier() {
        doctypeSystemIdentifier = new StringBuilder();
    }

    void createDoctypePublicIdentifier() {
        doctypePublicIdentifier = new StringBuilder();
    }

    void appendDoctypeNameCharacter(int chr) {
        doctypeNameToken.append((char) chr);
    }

    void appendDoctypePublicIdentifier(int chr) {
        doctypePublicIdentifier.append((char) chr);
    }

    void appendDoctypeSystemIdentifierCharacter(int chr) {
        doctypeSystemIdentifier.append((char) chr);
    }

    // -------

    void createNewCommentToken() {
        commentToken = new ResizableCharBuilder();
    }

    void appendCommentCharacter(int chr) {
        commentToken.append((char) chr);
    }

    void appendCommentCharacter(int chr, int chr2) {
        commentToken.append((char) chr);
        commentToken.append((char) chr2);
    }

    //

    void emitEOF() {
        tokenHandler.emitEOF();
    }

    //

    void emitDoctypeToken(StringBuilder doctypeName, StringBuilder doctypePublicId, StringBuilder doctypeSystemId) {
        if (doctypeName != null) {
            // FIXME to check, in the tests the name is always lowercase
            doctypeName = new StringBuilder(doctypeName.toString().toLowerCase(Locale.ROOT));
        }
        tokenHandler.emitDoctypeToken(doctypeName, doctypePublicId, doctypeSystemId, !doctypeForceQuirksFlag);
    }

    void emitComment() {
        tokenHandler.emitComment(commentToken);
    }

    void emitComment(ResizableCharBuilder cb) {
        tokenHandler.emitComment(cb);
    }

    void emitTagToken() {

        // Permitted slash but in close tag
        // When an end tag token is emitted with attributes, that is a parse
        // error. FIXME: maybe handle here the issue? instead of catching NPE
        // ERRYWHERE?
        // When an end tag token is emitted with its self-closing flag set, that
        // is a parse error.
        //
        if (selfClosing && isEndTagToken) {
            tokenHandler.emitParseError();
        }
        if (!isEndTagToken) {
            tokenHandler.emitStartTagToken(tagName, attributes, selfClosing);
        } else {
            tokenHandler.emitEndTagToken(tagName);
        }
    }

    int getPreviousState() {
        return previousState;
    }

    void setPreviousState(int previousState) {
        this.previousState = previousState;
    }

    Element getAdjustedCurrentNode() {
        return tokenHandler.getAdjustedCurrentNode();
    }

    // optimizations related getters and methods
    int getTokenHandlerInsertionMode() {
        return tokenHandler.getInsertionMode();
    }

    boolean isTokenHandlerInHtmlContent() {
        return tokenHandler.isInHtmlContent();
    }

    ResizableCharBuilder getTokenHandlerInsertCharacterPreviousTextNode() {
        return tokenHandler.getInsertCharacterPreviousTextNode();
    }

    void resetTokenHandlerInsertCharacterPreviousTextNode() {
        tokenHandler.resetInsertCharacterPreviousTextNode();
    }

    void emitParseErrorAndSetState(int state) {
        tokenHandler.emitParseError();
        this.state = state;
    }

    //

    //
    void setDoctypeForceQuirksFlag(boolean doctypeForceQuirksFlag) {
        this.doctypeForceQuirksFlag = doctypeForceQuirksFlag;
    }

    StringBuilder getDoctypeNameToken() {
        return doctypeNameToken;
    }

    StringBuilder getDoctypePublicIdentifier() {
        return doctypePublicIdentifier;
    }

    StringBuilder getDoctypeSystemIdentifier() {
        return doctypeSystemIdentifier;
    }

    void setSelfClosing(boolean selfClosing) {
        this.selfClosing = selfClosing;
    }

    void setStartToken(String tagName, Attributes attributes) {
        this.tagName.set(tagName);
        this.attributes = attributes;
        this.isEndTagToken = false;
    }
}
