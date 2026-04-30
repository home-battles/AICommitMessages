package com.homebattles.aicommitmessages;

import com.homebattles.aicommitmessages.aitools.AiTool;
import com.homebattles.aicommitmessages.aitools.cliclients.AiCliClient;
import com.homebattles.aicommitmessages.settings.AiToolSettingsService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Executes AI CLI providers to generate commit messages.
 */
public class CLIExecutor {
    private static final Logger LOG = Logger.getInstance(CLIExecutor.class);

    private final Project project;
    private final AiTool aiTool;
    private final AiCliClient aiCliClient;

    public CLIExecutor(Project project, AiTool aiTool) {
        this.aiTool = aiTool;
        this.aiCliClient = aiTool.getAiCliClient();
        this.project = project;

        AiToolSettingsService settings = AiToolSettingsService.getInstance();
        this.aiCliClient.setSettings(settings.getState().tools.get(aiTool.getDisplayName()));

        LOG.info("CLIExecutor initialized with provider: " + aiTool);
    }

    public boolean isCliAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(aiCliClient.getExecutablePath(), "--version");
            processBuilder.directory(new File(project.getBasePath() != null ? project.getBasePath() : System.getProperty("user.dir")));
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            LOG.debug(aiTool.getDisplayName() + " CLI not available: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generates a commit message using the specified provider and diff content.
     *
     * @param diff         The git diff content
     * @return The generated commit message
     * @throws Exception if the CLI execution fails
     */
    public String generateCommitMessage(@NotNull String diff) throws Exception {
        if (diff.trim().isEmpty()) {
            throw new IllegalArgumentException("Diff content is empty");
        }

        String prompt = buildPrompt(diff);
        return this.execute(prompt);
    }

    private String buildPrompt(@NotNull String diff) {
        return "Generate a concise and descriptive git commit message based on the following diff. " +
                "The commit message must be surrounded by triple $ signs (e.g. $$$ Commit message $$$)." +
                "Return only the commit message, without any explanation or additional text.\n\n" +
                "Diff:\n" + diff;
    }



    public @NotNull String execute(@NotNull String prompt) throws Exception {
        List<String> command = aiCliClient.buildCommand(prompt);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(project.getBasePath() != null ? project.getBasePath() : System.getProperty("user.dir")));
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            LOG.warn("CLI execution failed with exit code: " + exitCode + ", output: " + output);
            throw new Exception("Failed to get response from CLI. Exit code: " + exitCode + ", Output: " + output);
        }

        String result = output.toString().trim();
        if (result.isEmpty()) {
            throw new Exception("No output received from " + aiTool.getDisplayName());
        }

        return result;
    }
}
