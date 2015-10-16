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

public enum Option {
    // Parser and serialization parameter
    SCRIPTING_DISABLED,
    // ---
    // Parser and serialization parameter
    // /!\ this will break the conformance of the parsing and serialization
    DONT_TRANSFORM_ENTITIES,
    // ---
    // Serialization parameter
    // /!\ this will break the conformance of the serialization
    // If an attribute has an empty value, instead of serializing as
    // my-attribute="" it will be serialized as my-attribute
    HIDE_EMPTY_ATTRIBUTE_VALUE,
    //
    TRACK_CASE_FOR_ATTRIBUTES_AND_TAG,//
    PRINT_ORIGINAL_ATTRIBUTE_QUOTE
}