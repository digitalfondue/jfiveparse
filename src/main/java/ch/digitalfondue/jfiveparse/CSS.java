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

    // expose it on the JFiveParse class
    static List<List<CssSelector>> parseSelector(String selector) {
        List<List<CssSelector>> subselects = new ArrayList<>();
        int endIndex = new ParseSelector(subselects, selector, 0).parse();
        if (endIndex < selector.length()) {
            throw new IllegalStateException("Unmatched selector: ");
            //throw new Error(`Unmatched selector: ${selector.slice(endIndex)}`);
        }

        return subselects;
    }

    static class CssSelector {
        SelectorType type;

        CssSelector(SelectorType type) {
            this.type = type;
        }
    }

    static class AttributeSelector extends CssSelector {
        String name;
        AttributeAction action;
        String value;
        String ignoreCase;
        String nameSpace;

        AttributeSelector(String name, AttributeAction action, String value, String ignoreCase, String nameSpace) {
            super(SelectorType.Attribute);
            this.name = name;
            this.action = action;
            this.value = value;
            this.ignoreCase = ignoreCase;
            this.nameSpace = nameSpace;
        }
    }

    static class PseudoElement extends CssSelector {

        String name;
        String data;

        PseudoElement(String name, String data) {
            super(SelectorType.PseudoElement);
            this.name = name;
            this.data = data;
        }
    }

    enum SelectorType {
        Child, Parent, Sibling, Adjacent, Attribute, PseudoElement, ColumnCombinator, Descendant
    }

    enum AttributeAction {
        Equals, Exists, Start, End, Any, Not, Hyphen, Element

    }

    private static String unescapeCSS(String cssString) {
        return cssString; //.replace(reEscape, funescape);
    }

    private static AttributeAction getActionTypes(char c) {
        return switch (c) {
            case '~' /*Tilde*/ -> AttributeAction.Element;
            case '^' /*Circumflex*/ -> AttributeAction.Start;
            case '$' /*Dollar*/ -> AttributeAction.End;
            case '*' /*Asterisk*/ -> AttributeAction.Any;
            case '!' /*ExclamationMark*/ -> AttributeAction.Not;
            case '|' /* Pipe */ -> AttributeAction.Hyphen;
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

    private static boolean isTraversal(CssSelector selector) {
        return switch (selector.type) {
            case Adjacent, Child, Descendant, Parent, Sibling, ColumnCombinator -> true;
            default -> false;
        };
    }

    private static boolean isQuote(char c) {
        return c == '\'' || c == '"';
    }

    private static class ParseSelector {

        final List<List<CssSelector>> subselects;
        ArrayList<CssSelector> tokens = new ArrayList<>();
        final String selector;
        final int selectorLength;
        int selectorIndex;

        ParseSelector(List<List<CssSelector>> subselects, String selector, int selectorIndex) {
            this.subselects = subselects;
            this.selector = selector;
            this.selectorLength = selector.length();
            this.selectorIndex = selectorIndex;
        }


        String getName(int offset) {
            return ""; // FIXME
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

        void ensureNotTraversal() {
            if (!tokens.isEmpty() && isTraversal(tokens.get(tokens.size() - 1))) {
                throw new IllegalStateException("Did not expect successive traversals.");
            }
        }

        void addTraversal(SelectorType type) {
            if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).type == SelectorType.Descendant
            ) {
                tokens.get(tokens.size() - 1).type = type;
                return;
            }

            ensureNotTraversal();

            tokens.add(new CssSelector(type));
        }

        void addSpecialAttribute(String name, AttributeAction action) {
            tokens.add(new AttributeSelector(
                    name,
                    action,
                    getName(1),
                    null,
                    "quirks")
            );
        }

        void finalizeSubselector() {
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
                        if (tokens.isEmpty() || tokens.get(0).type != SelectorType.Descendant) {
                            ensureNotTraversal();
                            tokens.add(new CssSelector(SelectorType.Descendant));
                        }
                        stripWhitespace(1);
                        break;
                    }
                    // Traversals
                    case '>': // GreaterThan
                    {
                        addTraversal(SelectorType.Child);
                        stripWhitespace(1);
                        break;
                    }
                    case '<': // LessThan
                    {
                        addTraversal(SelectorType.Parent);
                        stripWhitespace(1);
                        break;
                    }
                    case '~': //Tilde
                    {
                        addTraversal(SelectorType.Sibling);
                        stripWhitespace(1);
                        break;
                    }
                    case '+': //Plus
                    {
                        addTraversal(SelectorType.Adjacent);
                        stripWhitespace(1);
                        break;
                    }
                    // Special attribute selectors: .class, #id
                    case '.': //Period
                    {
                        addSpecialAttribute("class", AttributeAction.Element);
                        break;
                    }
                    case '#': //Hash
                    {
                        addSpecialAttribute("id", AttributeAction.Equals);
                        break;
                    }
                    case '[': // LeftSquareBracket
                    {
                        stripWhitespace(1);
                        String name;
                        String namespace = null;

                        if (selector.charAt(selectorIndex) == '|') { // Pipe
                            // Equivalent to no namespace
                            name = getName(1);
                        } else if (selector.startsWith("*|", selectorIndex)) {
                            namespace = "*";
                            name = getName(2);
                        } else {
                            name = getName(0);

                            if (selector.charAt(selectorIndex) == '|' /* Pipe */ && selector.charAt(selectorIndex + 1) != '=' /* Equal */) {
                                namespace = name;
                                name = getName(1);
                            }
                        }
                        stripWhitespace(0);
                        // Determine comparison operation
                        AttributeAction action = AttributeAction.Exists;
                        AttributeAction possibleAction = getActionTypes(selector.charAt(selectorIndex));
                        if (possibleAction != null) {
                            action = possibleAction;
                            if (selector.charAt(selectorIndex + 1) != '='
                            ) {
                                throw new Error("Expected `=`");
                            }

                            stripWhitespace(2);
                        } else if (selector.charAt(selectorIndex) == '=') {
                            action = AttributeAction.Equals;
                            stripWhitespace(1);
                        }

                        String value = "";
                        String ignoreCase = null;

                        if (action != AttributeAction.Exists) {
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
                            // See if we have a force ignore flag
                        }

                        if (selector.charAt(selectorIndex) != ']') {
                            throw new IllegalStateException("Attribute selector didn't terminate");
                        }

                        selectorIndex += 1;
                        tokens.add(new AttributeSelector(name, action, value, ignoreCase, namespace));
                        break;
                    }
                    case ':': {
                        if (selector.charAt(selectorIndex + 1) == ':') {
                            String name = getName(2).toLowerCase(Locale.ROOT);
                            String data = selector.charAt(selectorIndex) == '(' ? readValueWithParenthesis() : null;
                            tokens.add(new PseudoElement(name, data));
                            break;
                        }

                        String name = getName(1).toLowerCase(Locale.ROOT);

                        //FIXME

                        break;
                    }
                    case ',': {
                        finalizeSubselector();
                        tokens = new ArrayList<>();
                        stripWhitespace(1);
                        break;
                    }
                    default: {
                        // FIXME
                    }
                }
            }

            finalizeSubselector();
            return selectorIndex;
        }
    }
}
