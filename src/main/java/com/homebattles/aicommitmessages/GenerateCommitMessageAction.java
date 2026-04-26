package com.homebattles.aicommitmessages;

import com.homebattles.aicommitmessages.aitools.AiTool;
import com.homebattles.aicommitmessages.providers.AiToolProvider;
import com.homebattles.aicommitmessages.providers.FilesProvider;
import com.homebattles.aicommitmessages.providers.GitDiffProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Action to generate a commit message using AI (Cursor or Copilot CLI).
 * This action is available in the commit message area of the IDE.
 */
public class GenerateCommitMessageAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(GenerateCommitMessageAction.class);

    /**
     * Action entry point.
     * @param e Action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // Get Selected Files
        var selectedFiles = FilesProvider.getSelectedFiles(e);
        if (selectedFiles.isEmpty()) {
            Messages.showErrorDialog(project, "No files selected for commit. Please select at least one file.", "Error");
            return;
        }

        // Get CLI from user
        AiToolProvider aiToolProvider = new AiToolProvider(project);
        AiTool aiTool = aiToolProvider.getSelectedAiTool();
        if (aiTool == null) { // Cancel or close
            return;
        }

        // Check if CLI is available
        CLIExecutor cliExecutor = new CLIExecutor(project, aiTool);
        if (!cliExecutor.isCliAvailable()) {
            Messages.showErrorDialog(
                    project,
                    aiTool.getDisplayName() + " CLI is not available. Check installation and the configured CLI path in Settings > Tools > AI Commit Messages.",
                    "CLI Not Available"
            );
            return;
        }

        // Run the generation in a background task
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating commit message...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);

                try {
                    GitDiffProvider diffProvider = new GitDiffProvider(project);
                    String diff = diffProvider.getDiff(selectedFiles); // Pass selected files
                    if (diff.trim().isEmpty()) {
                        throw new Exception("No diff content found for selected files.");
                    }

                    String generatedMessage = extractMessageBetweenDelimiters(cliExecutor.generateCommitMessage(diff));

                    // Update commit message on EDT
                    ApplicationManager.getApplication().invokeLater(() ->
                            Objects.requireNonNull(e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL)).setCommitMessage(generatedMessage));
                } catch (Exception ex) {
                    LOG.error("Error generating commit message", ex);
                    ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(
                            project,
                            "Failed to generate commit message: " + ex.getMessage(),
                            "Error"
                    ));
                }
            }
        });
    }

    /**
     * Extracts the message between $$$ delimiters.
     * Finds the first $$$ and the second $$$, and extracts everything between them.
     * Ignores any text after the closing $$$.
     *
     * @param message The message that may contain $$$ delimiters
     * @return The extracted message, or the original message if delimiters are not found
     */
    private static String extractMessageBetweenDelimiters(String message) {
        if (message == null) {
            return "";
        }

        String trimmed = message.trim();
        String delimiter = "$$$";

        int firstIndex = trimmed.indexOf(delimiter);
        if (firstIndex == -1) {
            // No opening delimiter found
            return trimmed;
        }

        int secondIndex = trimmed.indexOf(delimiter, firstIndex + delimiter.length());
        if (secondIndex == -1) {
            // No closing delimiter found
            return trimmed;
        }

        // Extract content between the two delimiters
        return trimmed.substring(firstIndex + delimiter.length(), secondIndex).trim();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean enabled = false;

        if (project != null) {
            // Enable if there are changes in the default changelist
            ChangeListManager changeListManager = ChangeListManager.getInstance(project);
            enabled = !changeListManager.getDefaultChangeList().getChanges().isEmpty();
        }

        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(enabled);
    }
}
