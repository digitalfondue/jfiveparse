package ch.digitalfondue.jfiveparse;

import com.google.gson.JsonParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Test the imported test cases from https://github.com/fb55/css-what/blob/25396c36bfc08bb4839aec690a7c6625b57165de/src/__fixtures__/out.json .
 * The data is under the same license as https://github.com/fb55/css-what/blob/25396c36bfc08bb4839aec690a7c6625b57165de/LICENSE .
 */
class CSSImportedTest {


    @MethodSource("data")
    @ParameterizedTest(name = "selector: \"{0}\"")
    public void check(String selector,  List<List<CSS.CssSelector>> expected) {
        var parsed = CSS.parseSelector(selector);
    }

    public static List<Object[]> data() throws IOException {
        var res = new ArrayList<Object[]>();

        var parsed = JsonParser.parseString(Files.readString(Path.of("src/test/resources/css/css-what-out.json"))).getAsJsonObject();
        for (var selector : parsed.keySet()) {
            var parsedSelector = parsed.get(selector).getAsJsonArray();
            // FIXME add conversion in List<List<CSS.CssSelector>>
            res.add(new Object[] {selector, List.of()});
        }
        return res;
    }
}
