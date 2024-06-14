package ch.digitalfondue.jfiveparse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CheckRead {

    public static void main(String[] args) throws IOException {
        var content = Files.readString(Path.of("wikipedia.html"), StandardCharsets.UTF_8);

        for(int i = 0; i < 100_000;i ++) {
            Document doc = JFiveParse.parse(content);

            Element e = doc.getElementById("mp-dyk-h2");
            String c = e.getOuterHTML();
        }
    }
}
