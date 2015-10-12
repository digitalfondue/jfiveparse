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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Class holding the trie of entities. The entities are stored in a gzipped file
 * in the classpath.
 * 
 * This file is generated by GenerateEntities in the test directory.
 */
class Entities {

    static final Prefix ENTITIES = new Prefix(null);

    static {

        // number of entities with only one codepoint
        final int length = 2138;

        // number of entities with 2 codepoints
        final int length2 = 93;

        try {

            DataInputStream dais = new DataInputStream(new GZIPInputStream(Entities.class.getResourceAsStream("/ch/digitalfondue/jfiveparse/entities-with-1-2-codepoint")));

            for (int i = 0; i < length; i++) {

                String name = dais.readUTF();
                int[] codePoints = new int[] { dais.readInt() };
                ENTITIES.addWord(name, codePoints);

            }

            for (int i = 0; i < length2; i++) {
                String name = dais.readUTF();
                int[] codePoints = new int[] { dais.readInt(), dais.readInt() };
                ENTITIES.addWord(name, codePoints);
            }

            ENTITIES.compact();

        } catch (IOException ioe) {
        }
    }
}
