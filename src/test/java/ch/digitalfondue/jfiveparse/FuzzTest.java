package ch.digitalfondue.jfiveparse;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.DictionaryFile;
import com.code_intelligence.jazzer.junit.DictionaryFiles;
import org.junit.jupiter.api.Disabled;

// set env variable JAZZER_FUZZ=1 to run them
class FuzzTest {


    // dictionaries are taken from https://github.com/google/fuzzing/tree/master/dictionaries
    @Disabled
    @DictionaryFiles({
            @DictionaryFile(resourcePath = "html.dict"),
            @DictionaryFile(resourcePath = "mathml.dict"),
            @DictionaryFile(resourcePath = "svg.dict"),
            @DictionaryFile(resourcePath = "xml.dict"),
    })
    @com.code_intelligence.jazzer.junit.FuzzTest
    void checkParse(FuzzedDataProvider data) {
        var input = data.consumeRemainingAsString();
        if (input == null) {
            return;
        }
        JFiveParse.parse(input);
    }

    @Disabled
    @DictionaryFile(resourcePath = "css.dict")
    @com.code_intelligence.jazzer.junit.FuzzTest
    void checkCSS(FuzzedDataProvider data) {
        var input = data.consumeRemainingAsString();
        if (input == null) {
            return;
        }
        try {
            CSS.parseSelector(input);
        } catch (ParserException e) {
            // acceptable, all ParserException are contemplated cases
        }
    }
}
