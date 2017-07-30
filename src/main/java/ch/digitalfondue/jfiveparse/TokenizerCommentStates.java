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

class TokenizerCommentStates {

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
        int chr = processedInputStream.getNextInputCharacterAndConsume();
        switch (chr) {
        case Characters.HYPHEN_MINUS:
            tokenizer.setState(TokenizerState.COMMENT_END_DASH_STATE);
            break;
        case Characters.NULL:
            tokenizer.emitParseError();
            tokenizer.appendCommentCharacter(Characters.REPLACEMENT_CHARACTER);
            break;
        case Characters.EOF:
            tokenizer.emitParseErrorAndSetState(TokenizerState.DATA_STATE);
            tokenizer.emitComment();
            processedInputStream.reconsume(chr);
            break;
        default:
            tokenizer.appendCommentCharacter(chr);
            break;
        }
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
}
