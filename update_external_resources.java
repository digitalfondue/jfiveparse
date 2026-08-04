///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.google.guava:guava:33.4.0-jre

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class update_external_resources {

    public static void main(String[] args) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("jbang-git-clone-");
            System.out.println("Created temporary directory: " + tempDir);

            List<String> repos = List.of(
                    "https://github.com/html5lib/html5lib-tests.git",
                    "https://github.com/web-platform-tests/wpt.git"
            );

            for (String repoUrl : repos) {
                System.out.println("\nCloning: " + repoUrl + " ...");
                ProcessBuilder pb = new ProcessBuilder("git", "clone", repoUrl);
                pb.directory(tempDir.toFile());
                pb.inheritIO();
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    String msg = "Failed to clone " + repoUrl + " (Exit code: " + exitCode + ")";
                    System.err.println(msg);
                    throw new IllegalStateException(msg);
                } else {
                    System.out.println("Successfully cloned " + repoUrl);
                }
            }

            // TODO: git checkout to specific commit
            // TODO: copy files / directories that we need for our test

        } catch (Exception e) {
            System.err.println("An error occurred during execution: " + e.getMessage());
        } finally {
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
}