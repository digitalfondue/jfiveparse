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

/**
 * Configuration options for the {@link Parser} and when calling the
 * serialization methods: {@link Element#getInnerHTML(java.util.Set)} and
 * {@link Element#getOuterHTML(java.util.Set)}.
 */
public enum Option {
    /**
     * Parser parameter. Disable scripting: the "script" elements will be
     * ignored, the "noscript" elements will be interpreted
     */
    SCRIPTING_DISABLED,
    // ---
    /**
     * <p>
     * Parser and serialization parameter. Disable the interpretation of the
     * entities.
     * </p>
     * /!\ this will break the conformance of the parsing and serialization
     * 
     */
    DONT_TRANSFORM_ENTITIES,
    // ---
    /**
     * <p>
     * Serialization parameter. If an attribute has an empty value, instead of
     * serializing as my-attribute="" it will be serialized as my-attribute
     * </p>
     * /!\ this will break the conformance of the serialization
     */
    HIDE_EMPTY_ATTRIBUTE_VALUE,
    /**
     * <p>
     * Serialization parameter. Print the original case of an attribute
     * name. By default, the attribute names are converted in lowerCase, when
     * passing this parameter, the original case will be printed.
     * </p>
     * /!\ this will break the conformance of the serialization
     */
    PRINT_ORIGINAL_ATTRIBUTES_CASE,
    /**
     * <p>
     * Serialization parameter. Print the original case of the tag name (only
     * the opening tag name will be considered, if you have
     * &lt;DIV&gt;&lt;/div&gt; it will be rendered as &lt;DIV&gt;&lt;/DIV&gt;).
     * By default, the tag name are converted in lowerCase, when passing this
     * parameter, the original case will be printed.
     * </p>
     * <p>
     * Note: it's possible that not all cases are covered.
     * </p>
     * /!\ this will break the conformance of the serialization
     */
    PRINT_ORIGINAL_TAG_CASE,
    /**
     * <p>
     * Serialization parameter. When serializing the document, the original
     * attribute quoting character (if any) will be used. e.g.: test='test' by
     * default is serialized as test="test", when passing this parameter, it
     * will serialized like the original value.
     * </p>
     * /!\ this will break the conformance of the serialization
     */
    PRINT_ORIGINAL_ATTRIBUTE_QUOTE,

    /**
     * <p>
     * Don't ignore start tag: "caption", "col", "colgroup", "frame", "head", "tbody", "td", "tfoot", "th", "thead", "tr"
     * when the parser is in the 'IN BODY' insertion mode.
     *
     * This allows to have for example "tr" tag without the containing table/tbody.
     * </p>
     */
    DISABLE_IGNORE_TOKEN_IN_BODY_START_TAG,

    /**
     * When encountering unknown self-closing tag, will interpret as self-closing tag instead of ignoring.
     */
    INTERPRET_SELF_CLOSING_ANYTHING_ELSE,

    DISABLE_IN_TABLE_TEXT_FOSTER_PARENTING
}