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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@RunWith(Parameterized.class)
public class TokenizerTest {

    TokenizerTestDescriptor desc;
    TokenizerStateForTest state;

    public TokenizerTest(String path, TokenizerTestDescriptor test, TokenizerStateForTest state) {
        this.desc = test;
        this.state = state;
    }

    @Parameters(name = "{0}:''{1}'':{2}")
    public static List<Object[]> data() throws IOException {
        List<Object[]> data = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("src/test/resources/html5lib-tests/tokenizer"), "*.test")) {
            for (Path p : ds) {
                byte[] file = Files.readAllBytes(p);
                String json = new String(file, StandardCharsets.UTF_8);
                TokenizerTestSuite suite = GSON.fromJson(json, TokenizerTestSuite.class);
                if (suite.tests != null) {
                    for (TokenizerTestDescriptor test : suite.tests) {

                        List<TokenizerStateForTest> states = Arrays.asList(TokenizerStateForTest.DATA_STATE);
                        if (test.initialStates != null) {
                            states = new ArrayList<>();
                            for (String state : test.initialStates) {
                                states.add(TokenizerStateForTest.valueOf(state.replace(" ", "_").toUpperCase(Locale.ENGLISH)));
                            }
                        }

                        for (TokenizerStateForTest state : states) {
                            data.add(new Object[] { p.getFileName().toString(), test, state });
                        }
                    }
                }
            }
        }
        return data;
    }

    private static final Gson GSON = new GsonBuilder().create();

    @SuppressWarnings("unchecked")
    @Test
    public void test() {

        // unescape
        if (desc.doubleEscaped != null && desc.doubleEscaped) {

            Replacer doubleEscapedUnicode = new Replacer() {
                @Override
                public String apply(MatchResult result) {
                    int val = Integer.parseInt(result.group(1), 16);
                    return new String(new char[] { ((char) val) });
                }
            };
            String newInput = replaceAll(desc.input, "\\\\u([\\d\\w]{4})", doubleEscapedUnicode);
            desc.input = newInput;
            for (Object o : desc.output) {
                if (isCharacterToken(o) || isCommentToken(o)) {
                    List<String> token = (List<String>) o;
                    token.set(1, replaceAll(token.get(1), "\\\\u([\\d\\w]{4})", doubleEscapedUnicode));
                }
            }
        }

        checkSingleTest(desc, state);
    }

    private void checkSingleTest(TokenizerTestDescriptor desc, TokenizerStateForTest initialState) {
        TokenSaver tokenSaver = new TokenSaver();
        Tokenizer tokenizer = new Tokenizer(tokenSaver);
        tokenizer.setState((byte) initialState.ordinal());

        if (desc.lastStartTag != null) {
            tokenizer.setLastStartTag(new ResizableCharBuilder(desc.lastStartTag));
        }

        ProcessedInputStream is = new ProcessedInputStreamWithParseError(desc.input, tokenSaver);

        tokenizer.tokenize(is);

        List<Token> tokens = mergeCharacter(tokenSaver.getTokens());

        Assert.assertEquals(desc.output.toString(), tokens.toString());
    }

    @SuppressWarnings("unchecked")
    private static boolean isCharacterToken(Object t) {
        if (t == null) {
            return false;
        }
        if (t instanceof List) {
            List<Object> token = (List<Object>) t;
            if (token.size() == 2 && token.get(0).equals(TokenType.character)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static boolean isCommentToken(Object t) {
        if (t == null) {
            return false;
        }
        if (t instanceof List) {
            List<Object> token = (List<Object>) t;
            if (token.size() == 2 && token.get(0).equals(TokenType.comment)) {
                return true;
            }
        }
        return false;
    }

    private static List<Token> mergeCharacter(List<Token> tokens) {

        List<Token> res = new ArrayList<>();

        for (Token t : tokens) {
            Token lastRes = res.isEmpty() ? null : res.get(res.size() - 1);
            if (t != null && lastRes != null && t instanceof Token.CharacterToken && lastRes instanceof Token.CharacterToken) {
                Token.CharacterToken lastResChar = (Token.CharacterToken) lastRes;
                Token.CharacterToken currentChar = (Token.CharacterToken) t;
                for (int i = 0; i < currentChar.chr.pos; i++) {
                    lastResChar.chr.append(currentChar.chr.buff[i]);
                }
            } else {
                res.add(t);
            }
        }

        return res;
    }

    private static boolean isCharacter(Object o) {
        if(o!= null && o instanceof List) {
            List<Object> l = (List<Object>) o;
            return !l.isEmpty() && l.get(0).equals("Character");
        }
        return false;
    }

    public static class TokenizerTestSuite {
        List<TokenizerTestDescriptor> tests;
        List<TokenizerTestDescriptor> xmlViolationTests;
    }

    public static class TokenizerTestDescriptor {
        String description;
        String input;
        List<Object> output;
        List<String> initialStates;
        String lastStartTag;
        String ignoreErrorOrder;
        Boolean doubleEscaped;

        @Override
        public String toString() {
            return description;
        }
    }

    private interface Replacer {
        String apply(MatchResult result);
    }

    // https://code.google.com/p/guava-libraries/issues/detail?id=651
    static String replaceAll(String target, String regex, Replacer operation) {
        StringBuffer result = new StringBuffer(target.length() * 3 / 2);
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(target);
        while (matcher.find()) {
            MatchResult match = matcher.toMatchResult();
            String replacement = operation.apply(match);
            if (!replacement.equals(match.group()))
                matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    //
}
