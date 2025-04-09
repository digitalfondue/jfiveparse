package ch.digitalfondue.jfiveparse;

//import one.profiler.AsyncProfiler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CheckRead {

    // test class for gathering profiling stats & co...
    public static void main(String[] args) throws IOException {
        var content = Files.readString(Path.of("src/test/resources/wikipedia.html"), StandardCharsets.UTF_8);
        //AsyncProfiler profiler = AsyncProfiler.getInstance();
        //profiler.execute("start,event=cpu,file=test.html");
        var start = System.currentTimeMillis();
        for (int i = 0; i < 100_000; i++) {
            Document doc = JFiveParse.parse(content);

            Element e = doc.getElementById("mp-dyk-h2");
            String c = e.getOuterHTML();
        }
        var end = System.currentTimeMillis();
        //profiler.execute("stop");
        System.out.println("total :" + ((end - start) / 1000.0) + "s");
    }
}
