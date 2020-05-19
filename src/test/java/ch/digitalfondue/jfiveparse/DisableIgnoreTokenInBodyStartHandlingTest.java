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

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class DisableIgnoreTokenInBodyStartHandlingTest {


    @Test
    public void testRawTableHanding() {
        List<Node> l = JFiveParse.parseFragment("<tr><td>a</td></tr>", Collections.singleton(Option.DISABLE_IGNORE_TOKEN_IN_BODY_START_TAG));
        Assert.assertEquals("<tr><td>a</td></tr>", JFiveParse.serialize(l.get(0)));
    }
}
