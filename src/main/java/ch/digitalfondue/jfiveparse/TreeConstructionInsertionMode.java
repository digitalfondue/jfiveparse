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

//
class TreeConstructionInsertionMode {

    static final int TEXT = 0;
    static final int IN_BODY = 1;
    static final int IN_CELL = 2;

    static final int INITIAL = 3;
    static final int BEFORE_HTML = 4;
    static final int BEFORE_HEAD = 5;
    static final int IN_HEAD = 6;
    static final int IN_HEAD_NOSCRIPT = 7;
    static final int AFTER_HEAD = 8;

    static final int IN_TABLE = 9;
    static final int IN_TABLE_TEXT = 10;
    static final int IN_CAPTION = 11;
    static final int IN_COLUMN_GROUP = 12;
    static final int IN_TABLE_BODY = 13;
    static final int IN_ROW = 14;

    static final int IN_SELECT = 15;
    static final int IN_SELECT_IN_TABLE = 16;
    static final int IN_TEMPLATE = 17;
    static final int AFTER_BODY = 18;
    static final int IN_FRAMESET = 19;
    static final int AFTER_FRAMESET = 20;
    static final int AFTER_AFTER_BODY = 21;
    static final int AFTER_AFTER_FRAMESET = 22;
}
