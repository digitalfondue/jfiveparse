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
import java.util.Arrays;
import java.util.List;

public class update_external_resources {

    private record ExternalRepo(String url, String directory, String... sparsePaths) {
    }

    public static void main(String[] args) {
        Path tempDir = null;
        try {
            // 1. Create a temporary directory
            tempDir = Files.createTempDirectory("jbang-git-clone-");
            System.out.println("Created temporary directory: " + tempDir);

            List<String> commitLogs = new ArrayList<>();

            // 2. Repositories to fetch
            ExternalRepo html5libTests = new ExternalRepo(
                    "https://github.com/html5lib/html5lib-tests.git",
                    "html5lib-tests",
                    "/tokenizer/**");
            ExternalRepo wpt = new ExternalRepo(
                    "https://github.com/web-platform-tests/wpt.git",
                    "wpt",
                    "/html/syntax/parsing/resources/**");

            for (ExternalRepo repo : List.of(html5libTests, wpt)) {
                cloneRepo(tempDir, repo);
                String hash = getCommitHash(tempDir.resolve(repo.directory()));
                commitLogs.add(repo.url() + " " + hash);
            }

            Path externalResourcesDir = Path.of("external-resources");
            if (Files.exists(externalResourcesDir)) {
                MoreFiles.deleteRecursively(externalResourcesDir);
            }

            // 3. Copy html5lib-tests/tokenizer
            copyDirectoryIfExists(
                    tempDir.resolve("html5lib-tests").resolve("tokenizer"),
                    externalResourcesDir.resolve("html5lib-tests").resolve("tokenizer"));

            // 4. Copy wpt/html/syntax/parsing/resources
            copyDirectoryIfExists(
                    tempDir.resolve("wpt").resolve("html").resolve("syntax").resolve("parsing").resolve("resources"),
                    externalResourcesDir.resolve("wpt").resolve("html").resolve("syntax").resolve("parsing").resolve("resources"));

            // 5. Write external-resources/commit.md
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

    /**
     * Clones only the requested sparse paths using a partial clone.
     */
    private static void cloneRepo(Path workDir, ExternalRepo repo) throws IOException, InterruptedException {
        System.out.println("\nCloning: " + repo.url() + " -> " + repo.directory() + " ...");
        runCommand(workDir, List.of(
                "git", "clone",
                "--depth", "1",
                "--filter=blob:none",
                "--sparse",
                repo.url(),
                repo.directory()));

        if (repo.sparsePaths().length > 0) {
            System.out.println("Sparse-checkout paths: " + Arrays.toString(repo.sparsePaths()));
            List<String> command = new ArrayList<>(List.of("git", "sparse-checkout", "set", "--no-cone"));
            command.addAll(Arrays.asList(repo.sparsePaths()));
            runCommand(workDir.resolve(repo.directory()), command);
        }
    }

    private static void runCommand(Path directory, List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(directory.toFile());
        pb.inheritIO();

        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed (exit code " + exitCode + "): " + String.join(" ", command));
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
