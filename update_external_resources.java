///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.google.guava:guava:33.4.0-jre
//DEPS commons-io:commons-io:2.18.0

import com.google.common.io.MoreFiles;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class update_external_resources {

    public static void main(String[] args) {
        Path tempDir = null;
        try {
            // 1. Create a temporary directory
            tempDir = Files.createTempDirectory("jbang-git-clone-");
            System.out.println("Created temporary directory: " + tempDir);

            // 2. Clone repositories
            cloneRepo(tempDir, "https://github.com/html5lib/html5lib-tests.git");
            cloneRepo(tempDir, "https://github.com/web-platform-tests/wpt.git");

            MoreFiles.deleteRecursively(Path.of("external-resources2"));

            // 3. Copy html5lib-tests/tokenizer
            Path sourceTokenizer = tempDir.resolve("html5lib-tests").resolve("tokenizer");
            Path targetTokenizer = Path.of("external-resources2", "html5lib-tests", "tokenizer");
            copyDirectoryIfExists(sourceTokenizer, targetTokenizer);

            // 4. Copy wpt/html/syntax/parsing/resources
            Path sourceWptResources = tempDir.resolve("wpt").resolve("html").resolve("syntax").resolve("parsing").resolve("resources");
            Path targetWptResources = Path.of("external-resources2", "wpt", "html", "syntax", "parsing", "resources");
            copyDirectoryIfExists(sourceWptResources, targetWptResources);

        } catch (Exception e) {
            System.err.println("An error occurred during execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. Clean up temporary directory using Guava
            if (tempDir != null && Files.exists(tempDir)) {
                System.out.println("\nCleaning up temporary directory: " + tempDir);
                try {
                    MoreFiles.deleteRecursively(tempDir);
                    System.out.println("Temporary directory deleted successfully.");
                } catch (IOException e) {
                    System.err.println("Failed to delete temp directory: " + e.getMessage());
                }
            }
        }
    }

    private static void cloneRepo(Path workDir, String repoUrl) throws IOException, InterruptedException {
        System.out.println("\nCloning: " + repoUrl + " ...");
        ProcessBuilder pb = new ProcessBuilder("git", "clone", "--depth", "1", repoUrl);
        pb.directory(workDir.toFile());
        pb.inheritIO();

        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to clone " + repoUrl + " (Exit code: " + exitCode + ")");
        }
    }

    private static void copyDirectoryIfExists(Path source, Path target) throws IOException {
        System.out.println("\nCopying directory: " + source + " -> " + target.toAbsolutePath() + " ...");
        if (Files.exists(source)) {
            FileUtils.copyDirectory(source.toFile(), target.toFile());
            System.out.println("Successfully copied.");
        } else {
            System.err.println("Source path does not exist: " + source);
        }
    }
}