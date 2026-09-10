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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AttributeNode}.
 */
class AttributeNodeTest {

    @Test
    void equalsWithBothValuesNull() {
        AttributeNode a = new AttributeNode("src", null);
        AttributeNode b = new AttributeNode("src", null);
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void equalsWithOneValueNull() {
        AttributeNode a = new AttributeNode("src", null);
        AttributeNode b = new AttributeNode("src", "image.png");
        assertNotEquals(a, b);
        assertNotEquals(b, a);
    }
}
