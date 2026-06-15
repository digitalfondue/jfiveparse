package ch.digitalfondue.jfiveparse;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import com.google.monitoring.runtime.instrumentation.Sampler;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

public class AllocationTest {

    // ./mvnw test -Dtest=AllocationTest
    @Test
    public void testParserAllocations() throws IOException {
        String file = Files.readString(Paths.get("src/test/resources/test.html"));
        Map<String, LongAdder> counts = new HashMap<>();
        Map<String, LongAdder> sizes = new HashMap<>();
        Sampler sampler = (count, desc, newObj, size) -> {
            counts.computeIfAbsent(desc, k -> new LongAdder()).increment();
            sizes.computeIfAbsent(desc, k -> new LongAdder()).add(size);
        };
        AllocationRecorder.addSampler(sampler);
        try {
            JFiveParse.parse(file);
        } finally {
            AllocationRecorder.removeSampler(sampler);
        }
        System.out.println("=== Allocation Results ===");
        System.out.printf("%-90s %10s %15s%n", "Class Descriptor", "Count", "Total Size (B)");
        System.out.println("-".repeat(117));
        
        counts.entrySet().stream()
                .filter(e -> e.getKey().startsWith("ch/") || e.getKey().startsWith("java/"))
                .sorted((e1, e2) -> Long.compare(e2.getValue().sum(), e1.getValue().sum()))
                .forEach(e -> {
                    String desc = e.getKey();
                    long countVal = e.getValue().sum();
                    long sizeVal = sizes.get(desc).sum();
                    System.out.printf("%-90s %10d %15d%n", desc, countVal, sizeVal);
                });
    }
}
