package com.homebattles.aicommitmessages;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Executes CLI commands for Cursor and Copilot to generate commit messages.
 */
public class CLIExecutor {
    private static final Logger LOG = Logger.getInstance(CLIExecutor.class);

    public enum CLIType {
        CURSOR("cursor"),
        COPILOT("vscode");

        private final String displayName;

        CLIType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Project project;

    public CLIExecutor(@NotNull Project project) {
        this.project = project;
    }

    /**
     * Generates a commit message using the specified CLI and diff content.
     *
     * @param cliType The CLI to use (CURSOR or COPILOT)
     * @param diff    The git diff content
     * @return The generated commit message
     * @throws Exception if the CLI execution fails
     */
    public String generateCommitMessage(@NotNull CLIType cliType, @NotNull String diff) throws Exception {
        if (diff.trim().isEmpty()) {
            throw new IllegalArgumentException("Diff content is empty");
        }

        String prompt = buildPrompt(diff);
        return executeCLI(cliType, prompt);
    }

    /**
     * Builds the prompt for the AI to generate a commit message.
     *
     * @param diff The git diff content
     * @return The formatted prompt
     */
    private String buildPrompt(@NotNull String diff) {
        return "Generate a concise and descriptive git commit message based on the following diff. " +
                "The commit message must be surrounded by triple $ signs (e.g. $$$ Commit message $$$)." +
                "Return only the commit message, without any explanation or additional text.\n\n" +
                "Diff:\n" + diff;
    }

    /**
     * Executes the CLI command to generate a commit message.
     *
     * @param cliType The CLI to use (CURSOR or COPILOT)
     * @param prompt  The prompt to send to the AI
     * @return The generated commit message
     * @throws Exception if the CLI execution fails
     */
    private String executeCLI(@NotNull CLIType cliType, @NotNull String prompt) throws Exception {
        List<String> command = buildCommand(cliType, prompt);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(project.getBasePath() != null ? project.getBasePath() : System.getProperty("user.dir")));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Read the output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                LOG.warn("CLI execution failed with exit code: " + exitCode + ", output: " + output);
                throw new Exception("Failed to get response from CLI. Exit code: " + exitCode + ", Output: " + output);
            }

            String result = output.toString().trim();
            if (result.isEmpty()) {
                throw new Exception("No output received from " + cliType.getDisplayName());
            }

            return result;
        } catch (Exception e) {
            LOG.error("Error executing " + cliType.getDisplayName() + " CLI", e);
            throw e;
        }
    }

    /**
     * Builds the command to execute based on the CLI type.
     *
     * @param cliType The CLI to use (CURSOR or COPILOT)
     * @param prompt  The prompt to send to the AI
     * @return The command as a list of strings
     */
    private List<String> buildCommand(@NotNull CLIType cliType, @NotNull String prompt) {
        return List.of(getCliExecutablePath(cliType), "-p", prompt);
    }

    /**
     * Checks if a CLI is available in the system PATH.
     *
     * @param cliType The CLI to check
     * @return true if the CLI is available, false otherwise
     */
    public boolean isCliAvailable(@NotNull CLIType cliType) {
        try {
            ProcessBuilder pb = new ProcessBuilder(getCliExecutablePath(cliType), "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            LOG.debug(cliType.getDisplayName() + " CLI not available: " + e.getMessage());
            return false;
        }
    }

    private @NotNull String getCliExecutablePath(@NotNull CLIType cliType) {
        AICommitMessagesSettings settings = AICommitMessagesSettings.getInstance();
        return cliType == CLIType.CURSOR ? settings.getCursorCliPath() : settings.getVscodeCliPath();
    }
}
