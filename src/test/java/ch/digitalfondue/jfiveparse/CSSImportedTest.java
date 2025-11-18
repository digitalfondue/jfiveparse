package ch.digitalfondue.jfiveparse;

import com.google.gson.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Test the imported test cases from https://github.com/fb55/css-what/blob/25396c36bfc08bb4839aec690a7c6625b57165de/src/__fixtures__/out.json .
 * The data is under the same license as https://github.com/fb55/css-what/blob/25396c36bfc08bb4839aec690a7c6625b57165de/LICENSE .
 */
class CSSImportedTest {

    @MethodSource("data")
    @ParameterizedTest(name = "selector: \"{0}\"")
    public void check(String selector, List<List<CSS.CssSelector>> expected) {
        var parsed = CSS.parseSelector(selector);
        Assertions.assertEquals(expected, parsed);
    }

    public static List<Object[]> data() throws IOException {
        var res = new ArrayList<Object[]>();

        var parsed = JsonParser.parseString(Files.readString(Path.of("src/test/resources/css/css-what-out.json"))).getAsJsonObject();
        for (var selector : parsed.keySet()) {
            var parsedSelector = parsed.get(selector).getAsJsonArray();
            res.add(new Object[]{selector, convertFromJson(parsedSelector)});
        }
        return res;
    }

    private static List<List<CSS.CssSelector>> convertFromJson(JsonArray json) {
        var res = new ArrayList<List<CSS.CssSelector>>();

        for (var list : json) {
            var selectorList = new ArrayList<CSS.CssSelector>();
            for (var elem : list.getAsJsonArray()) {
                var selector = elem.getAsJsonObject();
                var type = selector.get("type").getAsString();
                selectorList.add(from(type, selector));
            }
            res.add(selectorList);
        }
        return res;
    }

    private static final Map<String, CSS.AttributeAction> ATTRIBUTE_ACTION = Map.of(
            "any", CSS.AttributeAction.ANY,
            "element", CSS.AttributeAction.ELEMENT,
            "end", CSS.AttributeAction.END,
            "equals", CSS.AttributeAction.EQUALS,
            "exists", CSS.AttributeAction.EXISTS,
            "hyphen", CSS.AttributeAction.HYPHEN,
            "not", CSS.AttributeAction.NOT,
            "start", CSS.AttributeAction.START
    );


    private static String fromStringOrNull(JsonElement elem) {
        return elem.isJsonNull() ? null : elem.getAsString();
    }

    private static CSS.CssSelector from(String type, JsonObject elem) {
        return switch (type) {
            case "attribute" -> new CSS.AttributeSelector(
                    elem.get("name").getAsString(),
                    ATTRIBUTE_ACTION.get(elem.get("action").getAsString()),
                    elem.get("value").getAsString(),
                    Optional.ofNullable(fromStringOrNull(elem.get("ignoreCase"))).map(s -> switch (s) {
                        case "true" -> CSS.AttributeIgnoreCase.IGNORE_CASE_TRUE;
                        case "false" -> CSS.AttributeIgnoreCase.IGNORE_CASE_FALSE;
                        case "quirks" -> CSS.AttributeIgnoreCase.IGNORE_CASE_QUIRKS;
                        default -> throw new IllegalStateException("ignore case is not covered " + s);
                    }).orElse(null),
                    fromStringOrNull(elem.get("namespace"))
            );
            case "pseudo" -> {
                var pseudoData = elem.get("data");
                CSS.DataPseudo data = null;
                if (pseudoData instanceof JsonPrimitive p && p.isString()) {
                    data = new CSS.DataString(p.getAsString());
                } else if (pseudoData.isJsonArray() && pseudoData instanceof JsonArray subSelectors) {
                    data = new CSS.DataSelectors(convertFromJson(subSelectors));
                }
                yield new CSS.PseudoSelector(elem.get("name").getAsString(), data);
            }
            case "pseudo-element" ->
                    new CSS.PseudoElement(elem.get("name").getAsString(), fromStringOrNull(elem.get("data")));
            case "tag" -> new CSS.TagSelector(elem.get("name").getAsString(), fromStringOrNull(elem.get("namespace")));
            case "universal" -> new CSS.UniversalSelector(fromStringOrNull(elem.get("namespace")));
            case "adjacent" -> new CSS.Combinator(CSS.CombinatorType.ADJACENT);
            case "child" -> new CSS.Combinator(CSS.CombinatorType.CHILD);
            case "descendant" -> new CSS.Combinator(CSS.CombinatorType.DESCENDANT);
            case "parent" -> new CSS.Combinator(CSS.CombinatorType.PARENT);
            case "sibling" -> new CSS.Combinator(CSS.CombinatorType.SIBLING);
            case "column-combinator" -> new CSS.Combinator(CSS.CombinatorType.COLUMN_COMBINATOR);
            default -> throw new IllegalStateException(type);
        };
    }
}
