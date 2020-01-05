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
import java.util.Set;

/**
 * Expose convenient static methods for parsing String or Reader as Html document and serializing nodes back to String.
 */
public class JFiveParse {

    public static Document parse(String input) {
        return parse(input, EnumSet.noneOf(Option.class));
    }

    public static Document parse(String input, Set<Option> options) {
        return new Parser(options).parse(input);
    }

    public static Document parse(Reader input) {
        return parse(input, EnumSet.noneOf(Option.class));
    }

    public static Document parse(Reader input, Set<Option> options) {
        return new Parser(options).parse(input);
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
