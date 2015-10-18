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

/**
 * Configuration options for the {@link Parser}, when calling
 * {@link Node#getInnerHTML(java.util.Set)} and
 * {@link Node#getOuterHTML(java.util.Set)}.
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
     * Not implemented
     */
    TRACK_CASE_FOR_ATTRIBUTES_AND_TAG, //
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
     * Serialization parameter. Print the '/' on self closing tag.
     * <p>
     * /!\ this will break the conformance of the serialization
     * </p>
     */
    PRINT_SELF_CLOSING_SOLIDUS;
}