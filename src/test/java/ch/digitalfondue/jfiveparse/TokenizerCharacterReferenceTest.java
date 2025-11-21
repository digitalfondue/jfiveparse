package ch.digitalfondue.jfiveparse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static ch.digitalfondue.jfiveparse.TokenizerCharacterReference.invalidCharacterReference;

class TokenizerCharacterReferenceTest {

    @Test
    void ensureIsOrdered() {
        var c = invalidCharacterReference.clone();
        Arrays.sort(c);
        Assertions.assertArrayEquals(invalidCharacterReference, c);
    }
}
