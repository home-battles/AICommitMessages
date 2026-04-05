package com.homebattles.aicommitmessages;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Action to generate a commit message using AI (Cursor or Copilot CLI).
 * This action is available in the commit message area of the IDE.
 */
public class GenerateCommitMessageAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(GenerateCommitMessageAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // Get Selected Files
        var selectedFiles = getSelectedFiles(e);


        if (selectedFiles.isEmpty()) {
            Messages.showErrorDialog(project, "No files selected for commit. Please select at least one file.", "Error");
            return;
        }

        // Get CLI from user
        CLIExecutor.CLIType cliType = getSelectedCLI(project);
        if (cliType == null) { // Cancel or close
            return;
        }

        // Check if CLI is available
        CLIExecutor cliExecutor = new CLIExecutor(project);
        if (!cliExecutor.isCliAvailable(cliType)) {
            Messages.showErrorDialog(
                    project,
                    cliType.getDisplayName() + " CLI is not available. Check installation and the configured CLI path in Settings > Tools > AI Commit Messages.",
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

                    String generatedMessage = extractMessageBetweenDelimiters(cliExecutor.generateCommitMessage(cliType, diff));

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

    private static @NotNull Set<VirtualFile> getSelectedFiles(@NotNull AnActionEvent e) {
        var selectedChanges = Objects.requireNonNull(e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI)).getIncludedChanges();

        var selectedUnversionedChanges = Objects.requireNonNull(e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI)).getIncludedUnversionedFiles();


        Set<VirtualFile> selectedFiles = new HashSet<>();

        for (Change change : selectedChanges) {
            VirtualFile file = change.getVirtualFile();
            if (file != null) {
                selectedFiles.add(file);
            }
        }

        for (FilePath unversionedFile : selectedUnversionedChanges) {
            VirtualFile file = unversionedFile.getVirtualFile();

            if (file != null) {
                selectedFiles.add(file);
            }
        }

        return selectedFiles;
    }

    private static CLIExecutor.CLIType getSelectedCLI(Project project) {
        AICommitMessagesSettings settings = AICommitMessagesSettings.getInstance();
        AICommitMessagesSettings.CliSelectionMode mode = settings.getCliSelectionMode();
        if (mode == AICommitMessagesSettings.CliSelectionMode.CURSOR) {
            return CLIExecutor.CLIType.CURSOR;
        }
        if (mode == AICommitMessagesSettings.CliSelectionMode.VSCODE) {
            return CLIExecutor.CLIType.COPILOT;
        }

        String[] options = {"Cursor", "VSCode", "Cancel"};
        int selectedOption = Messages.showDialog(
                project,
                "Choose which AI CLI to use for generating the commit message:",
                "Select AI CLI",
                options,
                0,
                Messages.getQuestionIcon()
        );

        if (selectedOption == 2 || selectedOption == -1) {
            return null;
        }

        return selectedOption == 0 ? CLIExecutor.CLIType.CURSOR : CLIExecutor.CLIType.COPILOT;
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
