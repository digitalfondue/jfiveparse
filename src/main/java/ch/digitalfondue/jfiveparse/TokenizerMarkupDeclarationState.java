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

class TokenizerMarkupDeclarationState {

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

            if (Common.matchCharsCaseInsensitiveDoctype(chars)) {
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
}
