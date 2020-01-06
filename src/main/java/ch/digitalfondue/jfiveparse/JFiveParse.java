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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Expose convenient static methods for parsing String or Reader as Html document and serializing nodes back to String.
 */
public class JFiveParse {

    /**
     * Parse a full html document with the default options.
     *
     * @param input
     * @return a {@link Document}
     */
    public static Document parse(String input) {
        return parse(input, EnumSet.noneOf(Option.class));
    }

    /**
     * Parse a full html document with the given options
     *
     * @param input
     * @param options
     * @return a {@link Document}
     */
    public static Document parse(String input, Set<Option> options) {
        return new Parser(options).parse(input);
    }

    /**
     * Parse a full html document using a {@link Reader} as a input with the default options.
     *
     * @param input
     * @return
     */
    public static Document parse(Reader input) {
        return parse(input, EnumSet.noneOf(Option.class));
    }

    public static Document parse(Reader input, Set<Option> options) {
        return new Parser(options).parse(input);
    }

    /**
     * Parse a html fragment, with a "div" element as a parent node.
     *
     * @param input
     * @return
     */
    public static List<Node> parseFragment(String input) {
        return parseFragment(input, EnumSet.noneOf(Option.class));
    }

    /**
     * Parse a html fragment, with a "div" element as a parent node.
     *
     * @param input
     * @param options
     * @return
     */
    public static List<Node> parseFragment(String input, Set<Option> options) {
        return parseFragment(new Element("div", Node.NAMESPACE_HTML), input, options);
    }

    public static List<Node> parseFragment(Element parent, String input, Set<Option> options) {
        return new Parser(options).parseFragment(parent, input);
    }

    public static String serialize(Node node) {
        return serialize(node, EnumSet.noneOf(Option.class));
    }

    public static String serialize(Node node, Set<Option> options) {
        return HtmlSerializer.serialize(node, options);
    }

    public static void serialize(Node node, Writer writer) throws IOException {
        serialize(node, EnumSet.noneOf(Option.class), writer);
    }

    public static void serialize(Node node, Set<Option> options, Writer writer) throws IOException {
        HtmlSerializer.serialize(node, options, writer);
    }
}
