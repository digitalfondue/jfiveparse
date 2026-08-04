///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.google.guava:guava:33.4.0-jre
//DEPS commons-io:commons-io:2.18.0

import com.google.common.io.MoreFiles;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class update_external_resources {

    public static void main(String[] args) {
        Path tempDir = null;
        try {
            // 1. Create a temporary directory
            tempDir = Files.createTempDirectory("jbang-git-clone-");
            System.out.println("Created temporary directory: " + tempDir);

            List<String> commitLogs = new ArrayList<>();

            // 2. Clone repositories
            String repo1Url = "https://github.com/html5lib/html5lib-tests.git";
            Path repo1Path = tempDir.resolve("html5lib-tests");
            cloneRepo(tempDir, repo1Url);
            String repo1Hash = getCommitHash(repo1Path);
            commitLogs.add(repo1Url + " " + repo1Hash);

            String repo2Url = "https://github.com/web-platform-tests/wpt.git";
            Path repo2Path = tempDir.resolve("wpt");
            cloneRepo(tempDir, repo2Url);
            String repo2Hash = getCommitHash(repo2Path);
            commitLogs.add(repo2Url + " " + repo2Hash);

            MoreFiles.deleteRecursively(Path.of("external-resources"));

            // 3. Copy html5lib-tests/tokenizer
            Path sourceTokenizer = tempDir.resolve("html5lib-tests").resolve("tokenizer");
            Path targetTokenizer = Path.of("external-resources", "html5lib-tests", "tokenizer");
            copyDirectoryIfExists(sourceTokenizer, targetTokenizer);

            // 4. Copy wpt/html/syntax/parsing/resources
            Path sourceWptResources = tempDir.resolve("wpt").resolve("html").resolve("syntax").resolve("parsing").resolve("resources");
            Path targetWptResources = Path.of("external-resources", "wpt", "html", "syntax", "parsing", "resources");
            copyDirectoryIfExists(sourceWptResources, targetWptResources);

            // 5. Write external-resources/commit.md
            Path externalResourcesDir = Path.of("external-resources");
            Files.createDirectories(externalResourcesDir);
            Path commitFile = externalResourcesDir.resolve("commit.md");
            System.out.println("\nWriting commit info to " + commitFile.toAbsolutePath() + " ...");
            Files.write(commitFile, commitLogs, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Successfully wrote commit.md.");

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

    private static String getCommitHash(Path repoDir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "HEAD");
        pb.directory(repoDir.toFile());
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String hash = reader.readLine();
            int exitCode = process.waitFor();
            if (exitCode != 0 || hash == null) {
                throw new RuntimeException("Failed to get commit hash for " + repoDir);
            }
            return hash.trim();
        }
    }
}