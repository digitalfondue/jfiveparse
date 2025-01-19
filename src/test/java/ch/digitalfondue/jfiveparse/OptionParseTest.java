/**
 * Copyright © 2020 digitalfondue (info@digitalfondue.ch)
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.stream.Collectors;

class OptionParseTest {


    @Test
    void rawTableHandling() {
        // without option
        Document dw = JFiveParse.parse("<html><body><tr><td>a</td></tr></body>");
        assertEquals("<html><head></head><body>a</body></html>", JFiveParse.serialize(dw));

        // with option
        Document l = JFiveParse.parse("<html><body><tr><td>a</td></tr></body>", Collections.singleton(Option.DISABLE_IGNORE_TOKEN_IN_BODY_START_TAG));
        assertEquals("<html><head></head><body><tr><td>a</td></tr></body></html>", JFiveParse.serialize(l));
    }

    @Test
    void optionInterpretSelfClosing() {
        // without option
        var res = JFiveParse.parseFragment("<hr /><sj-test /><sj-a><sj-b /></mj-a>");
        var html = res.stream().map(s -> ((Element) s).getOuterHTML()).collect(Collectors.joining());
        assertEquals("<hr><sj-test><sj-a><sj-b></sj-b></sj-a></sj-test>", html);

        // with option
        var selfClosing = JFiveParse.parseFragment("<hr /><sj-test /><sj-a><sj-b /></mj-a>", Collections.singleton(Option.INTERPRET_SELF_CLOSING_ANYTHING_ELSE));
        var selfClosingHtml = selfClosing.stream().map(s -> ((Element) s).getOuterHTML()).collect(Collectors.joining());
        assertEquals("<hr><sj-test></sj-test><sj-a><sj-b></sj-b></sj-a>", selfClosingHtml);
    }
}
