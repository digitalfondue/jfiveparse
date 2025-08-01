/**
 * Copyright © 2025 digitalfondue (info@digitalfondue.ch)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.digitalfondue.jfiveparse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// based on https://github.com/fb55/css-what/blob/master/src/parse.ts
// under the license (BSD 2-Clause "Simplified" License): ( https://github.com/fb55/css-what/blob/master/LICENSE )
// Copyright (c) Felix Böhm
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//
// Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//
// Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//
// THIS IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS,
// EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
class CSS {

    private static final Pattern reName = Pattern.compile("^[^#\\\\]?(?:\\\\(?:[\\da-f]{1,6}\\s?|.)|[\\w\\u00B0-\\uFFFF-])+");
    private static final Pattern reEscape = Pattern.compile("\\\\([\\da-f]{1,6}\\s?|(\\s)|.)", Pattern.CASE_INSENSITIVE);


    // expose it on the JFiveParse class
    static List<List<CssSelector>> parseSelector(String selector) {
        List<List<CssSelector>> subselects = new ArrayList<>();
        int endIndex = new ParseSelector(subselects, selector, 0).parse();
        if (endIndex < selector.length()) {
            throw new IllegalStateException("Unmatched selector: " + selector.substring(endIndex));
        }
        return subselects;
    }

    sealed interface CssSelector {}

    // child '>', parent '<', sibling '~', adjacent '+', column '||', descendant 'a b'
    record Combinator(CombinatorType type) implements CssSelector {}

    record TagSelector(String name, String namespace) implements CssSelector {}

    record AttributeSelector(String name, AttributeAction action, String value, String ignoreCase, String namespace) implements CssSelector {}

    record PseudoElement(String name, String data) implements CssSelector {}

    sealed interface DataPseudo {}
    record DataString(String value) implements DataPseudo {}
    record DataSelectors(List<List<CssSelector>> value) implements DataPseudo {}
    record PseudoSelector(String name, DataPseudo data) implements CssSelector {}

    record UniversalSelector(String namespace) implements CssSelector {}

    enum CombinatorType {
        CHILD, PARENT, SIBLING, ADJACENT, COLUMN_COMBINATOR, DESCENDANT
    }

    enum AttributeAction {
        EQUALS, EXISTS, START, END, ANY, NOT, HYPHEN, ELEMENT
    }

    private static String unescapeCSS(String cssString) {
        return reEscape.matcher(cssString).replaceAll((r) -> {
            String escaped = r.group(1);
            boolean isHexNumber = true;
            int high = 0;
            try {
                high = Integer.parseInt(r.group(1).trim(), 16) - 0x1_00_00;
            } catch (NumberFormatException nfe) {
                isHexNumber = false;
            }
            boolean isWhiteSpace = r.group(2) != null;

            return !isHexNumber || isWhiteSpace ? Matcher.quoteReplacement(escaped) : high < 0 ? Character.toString(high + 0x1_00_00) : Character.toString(Character.toCodePoint((char) ((high >> 10) | 0xd8_00), (char) ((high & 0x3_ff) | 0xdc_00)));
        });
    }

    private static AttributeAction getActionTypes(char c) {
        return switch (c) {
            case '~' /*Tilde*/ -> AttributeAction.ELEMENT;
            case '^' /*Circumflex*/ -> AttributeAction.START;
            case '$' /*Dollar*/ -> AttributeAction.END;
            case '*' /*Asterisk*/ -> AttributeAction.ANY;
            case '!' /*ExclamationMark*/ -> AttributeAction.NOT;
            case '|' /* Pipe */ -> AttributeAction.HYPHEN;
            default -> null;
        };

    }

    private static boolean isWhitespace(char c) {
        return switch (c) {
            // tab
            // newline
            // formfeed
            // carriage return
            // space
            case 9, 10, 12, 13, 32 -> true;
            default -> false;
        };
    }

    private static boolean isQuote(char c) {
        return c == '\'' || c == '"';
    }


    private static boolean isPseudosToPseudoElements(String name) {
        return switch (name) {
            case "before", "after", "first-line", "first-letter" -> true;
            default -> false;
        };
    }

    private static boolean isUnpackPseudos(String name) {
        return switch (name) {
            case "has", "not", "matches", "is", "where", "host", "host-context" -> true;
            default -> false;
        };
    }



    private static final class ParseSelector {

        private final List<List<CssSelector>> subselects;
        private final String selector;
        private final int selectorLength;
        private List<CssSelector> tokens = new ArrayList<>();
        private int selectorIndex;

        ParseSelector(List<List<CssSelector>> subselects, String selector, int selectorIndex) {
            this.subselects = subselects;
            this.selector = selector;
            this.selectorLength = selector.length();
            this.selectorIndex = selectorIndex;
        }


        String getName(int offset) {
            Matcher matcher = reName.matcher(selector.substring(selectorIndex + offset));
            if (!matcher.find()) {
                throw new IllegalStateException("Expected name, found " + selector.substring(selectorIndex));
            }
            String name = matcher.group();
            selectorIndex += offset + name.length();
            return unescapeCSS(name);
        }

        void stripWhitespace(int offset) {
            selectorIndex += offset;

            while (selectorIndex < selectorLength && isWhitespace(selector.charAt(selectorIndex))) {
                selectorIndex++;
            }
        }

        String readValueWithParenthesis() {
            selectorIndex += 1;
            int start = selectorIndex;
            for (int counter = 1; selectorIndex < selectorLength; selectorIndex++) {
                switch (selector.charAt(selectorIndex)) {
                    case '\\': {
                        // Skip next character
                        selectorIndex += 1;
                        break;
                    }
                    case '(': {
                        counter += 1;
                        break;
                    }
                    case ')': {
                        counter -= 1;

                        if (counter == 0) {
                            return unescapeCSS(selector.substring(start, selectorIndex++));
                        }
                        break;
                    }
                }
            }
            throw new IllegalStateException("Parenthesis not matched");
        }

        void ensureNotCombinator() {
            if (!tokens.isEmpty() && tokens.get(tokens.size() - 1) instanceof Combinator) {
                throw new IllegalStateException("Did not expect successive combinators.");
            }
        }

        void addTraversal(CombinatorType type) {
            if (!tokens.isEmpty() && tokens.get(tokens.size() - 1) instanceof Combinator ct && ct.type() == CombinatorType.DESCENDANT
            ) {
                tokens.set(tokens.size() - 1, new Combinator(type));
                return;
            }

            ensureNotCombinator();

            tokens.add(new Combinator(type));
        }

        void addSpecialAttribute(String name, AttributeAction action) {
            tokens.add(new AttributeSelector(
                    name,
                    action,
                    getName(1),
                    "quirks",
                    null)
            );
        }

        void finalizeSubselector() {
            if (!tokens.isEmpty() && tokens.get(tokens.size() - 1) instanceof Combinator ct && ct.type() == CombinatorType.DESCENDANT) {
                tokens.remove(tokens.size()-1);
            }

            if (tokens.isEmpty()) {
                throw new IllegalStateException("Empty sub-selector");
            }

            subselects.add(tokens);
        }

        int parse() {
            stripWhitespace(0);

            if (selectorLength == selectorIndex) {
                return selectorIndex;
            }

            loop:
            while (selectorIndex < selectorLength) {
                var firstChar = selector.charAt(selectorIndex);
                switch (firstChar) {
                    // whitespace
                    case 9: // tab
                    case 10: // newline
                    case 12: // formfeed
                    case 13: // carriage return
                    case 32: // space
                    {
                        // check the first token is not DESCENDANT
                        if (tokens.isEmpty() || (!(tokens.get(0) instanceof Combinator ct) || ct.type() != CombinatorType.DESCENDANT)) {
                            ensureNotCombinator();
                            tokens.add(new Combinator(CombinatorType.DESCENDANT));
                        }
                        stripWhitespace(1);
                        break;
                    }
                    // Traversals
                    case '>': // GreaterThan
                    {
                        addTraversal(CombinatorType.CHILD);
                        stripWhitespace(1);
                        break;
                    }
                    case '<': // LessThan
                    {
                        addTraversal(CombinatorType.PARENT);
                        stripWhitespace(1);
                        break;
                    }
                    case '~': //Tilde
                    {
                        addTraversal(CombinatorType.SIBLING);
                        stripWhitespace(1);
                        break;
                    }
                    case '+': //Plus
                    {
                        addTraversal(CombinatorType.ADJACENT);
                        stripWhitespace(1);
                        break;
                    }
                    // Special attribute selectors: .class, #id
                    case '.': //Period
                    {
                        addSpecialAttribute("class", AttributeAction.ELEMENT);
                        break;
                    }
                    case '#': //Hash
                    {
                        addSpecialAttribute("id", AttributeAction.EQUALS);
                        break;
                    }
                    case '[': // LeftSquareBracket
                    {
                        stripWhitespace(1);
                        String name;
                        String namespace = null;

                        if (charAtIsEqual(selectorIndex, '|')) { // Pipe
                            // Equivalent to no namespace
                            name = getName(1);
                        } else if (selector.startsWith("*|", selectorIndex)) {
                            namespace = "*";
                            name = getName(2);
                        } else {
                            name = getName(0);

                            if (charAtIsEqual(selectorIndex, '|') /* Pipe */ && !charAtIsEqual(selectorIndex + 1, '=') /* Equal */) {
                                namespace = name;
                                name = getName(1);
                            }
                        }
                        stripWhitespace(0);
                        // Determine comparison operation
                        AttributeAction action = AttributeAction.EXISTS;
                        AttributeAction possibleAction = getActionTypes(selector.charAt(selectorIndex));
                        if (possibleAction != null) {
                            action = possibleAction;
                            if (!charAtIsEqual(selectorIndex + 1, '=')) {
                                throw new IllegalStateException("Expected '='");
                            }

                            stripWhitespace(2);
                        } else if (charAtIsEqual(selectorIndex, '=')) {
                            action = AttributeAction.EQUALS;
                            stripWhitespace(1);
                        }

                        String value = "";
                        String ignoreCase = null;

                        if (action != AttributeAction.EXISTS) {
                            if (isQuote(selector.charAt(selectorIndex))) {
                                char quote = selector.charAt(selectorIndex);
                                selectorIndex += 1;
                                int sectionStart = selectorIndex;
                                while (selectorIndex < selectorLength && selector.charAt(selectorIndex) != quote) {
                                    selectorIndex +=
                                            // Skip next character if it is escaped
                                            selector.charAt(selectorIndex) == '\\' ? 2 : 1;
                                }
                                if (selector.charAt(selectorIndex) != quote) {
                                    throw new IllegalStateException("Attribute value didn't end");
                                }
                                value = unescapeCSS(selector.substring(sectionStart, selectorIndex));
                                selectorIndex += 1;
                            } else {
                                int valueStart = selectorIndex;
                                while (selectorIndex < selectorLength && !isWhitespace(selector.charAt(selectorIndex)) && selector.charAt(selectorIndex) != ']') {
                                    selectorIndex +=
                                            // Skip next character if it is escaped
                                            selector.charAt(selectorIndex) == '\\' ? 2 : 1;
                                }

                                value = unescapeCSS(selector.substring(valueStart, selectorIndex));
                            }

                            stripWhitespace(0);
                            switch (selector.charAt(selectorIndex) | 0x20) {
                                // If the forceIgnore flag is set (either 'i' or 's'), use that value
                                case 'i': {
                                    ignoreCase = "true";
                                    stripWhitespace(1);
                                    break;
                                }
                                case 's': {
                                    ignoreCase = "false";
                                    stripWhitespace(1);
                                    break;
                                }
                            }
                        }

                        if (!charAtIsEqual(selectorIndex, ']')) {
                            throw new IllegalStateException("Attribute selector didn't terminate");
                        }

                        selectorIndex += 1;
                        tokens.add(new AttributeSelector(name, action, value, ignoreCase, namespace));
                        break;
                    }
                    case ':': {
                        if (charAtIsEqual(selectorIndex + 1, ':')) {
                            String name = getName(2).toLowerCase(Locale.ROOT);
                            String data = charAtIsEqual(selectorIndex, '(') ? readValueWithParenthesis() : null;
                            tokens.add(new PseudoElement(name, data));
                            break;
                        }

                        String name = getName(1).toLowerCase(Locale.ROOT);
                        if (isPseudosToPseudoElements(name)) {
                            tokens.add(new PseudoElement(name, null));
                            break;
                        }

                        DataPseudo data = null;
                        if (charAtIsEqual(selectorIndex, '(')) {
                            if (isUnpackPseudos(name)) {
                                if (canCharAt(selectorIndex + 1) && isQuote(selector.charAt(selectorIndex + 1))) {
                                    throw new IllegalStateException("Pseudo-selector " + name + " cannot be quoted");
                                }

                                List<List<CssSelector>> subselects = new ArrayList<>();
                                data = new DataSelectors(subselects);
                                selectorIndex = new ParseSelector(subselects, selector, selectorIndex + 1).parse();
                                if (!charAtIsEqual(selectorIndex, ')')) {
                                    throw new IllegalStateException("Missing closing parenthesis in :" + name + " (" + selector + ")");
                                }
                                selectorIndex += 1;
                            } else {
                                String value = readValueWithParenthesis();
                                if ("contains".equals(name) || "icontains".equals(name)) {
                                    char quot = value.charAt(0);
                                    if (quot == value.charAt(value.length() - 1) && isQuote(quot)) {
                                        value = value.substring(1, value.length() - 1);
                                    }
                                }
                                data = new DataString(unescapeCSS(value));
                            }
                        }
                        tokens.add(new PseudoSelector(name, data));
                        break;
                    }
                    case ',': {
                        finalizeSubselector();
                        tokens = new ArrayList<>();
                        stripWhitespace(1);
                        break;
                    }
                    default: {
                        if (selector.startsWith("/*", selectorIndex)) {
                            int endIndex = selector.indexOf("*/", selectorIndex + 2);
                            if (endIndex < 0) {
                                throw new IllegalStateException("Comment was not terminated");
                            }
                            selectorIndex = endIndex + 2;
                            // Remove leading whitespace
                            if (tokens.isEmpty()) {
                                stripWhitespace(0);
                            }

                            break;
                        }
                        String namespace = null;
                        String name;
                        if (firstChar == '*') {
                            selectorIndex += 1;
                            name = "*";
                        } else if (firstChar == '|') {
                            name = "";
                            if (charAtIsEqual(selectorIndex + 1, '|')) {
                                addTraversal(CombinatorType.COLUMN_COMBINATOR);
                                stripWhitespace(2);
                                break;
                            }
                        } else if (reName.matcher(selector.substring(selectorIndex)).find()) {
                            name = getName(0);
                        } else {
                            break loop;
                        }
                        if (charAtIsEqual(selectorIndex, '|') && !charAtIsEqual(selectorIndex + 1, '|')) {
                            namespace = name;
                            if (selector.charAt(selectorIndex + 1) == '*') {
                                name = "*";
                                selectorIndex += 2;
                            } else {
                                name = getName(1);
                            }
                        }
                        tokens.add("*".equals(name)  ? new UniversalSelector(namespace) : new TagSelector(name, namespace));
                    }
                }
            }

            finalizeSubselector();
            return selectorIndex;
        }


        private boolean charAtIsEqual(int i, char toCompare) {
            return i < selectorLength && selector.charAt(i) == toCompare;
        }

        private boolean canCharAt(int i) {
            return i < selectorLength;
        }
    }
}
