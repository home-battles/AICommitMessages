package com.homebattles.aicommitmessages.providers;

import com.homebattles.aicommitmessages.aitools.AiTool;
import com.homebattles.aicommitmessages.settings.AiToolSettingsService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides utility methods for selecting an AI CLI tool for generating commit messages.
 */
public class AiToolProvider {
    /**
     * Represents the IntelliJ IDEA project context in which the AI tools operate.
     * This variable is used to access project-specific data and services required
     * for interacting with the user interface components tied to the corresponding project.
     */
    private final Project project;

    public AiToolProvider(Project project) {
        this.project = project;
    }

    /**
     * <p>Retrieves the selected AI tool to be used for generating commit messages.</p>
     * <p>If a default AI tool is specified in the settings, it is returned.
     * Otherwise, it prompts the user to select an AI tool from a list of available options.</p>
     *
     * @return the selected {@code AIProviderType}, or {@code null} if no selection is made
     */
    public @Nullable AiTool getSelectedAiTool() {
        AiTool defaultTool = AiToolSettingsService
                .getInstance()
                .getDefaultAiTool();

        if (defaultTool != null) {
            return defaultTool;
        }

        return askAndGetSelectedTool();
    }

    /**
     * Prompts the user to select an AI tool from a list of available options and retrieves the selected tool.
     *
     * @return the selected {@code AITool}, or {@code null} if the user cancels the selection or makes no valid choice
     */
    private @Nullable AiTool askAndGetSelectedTool() {
        String[] options = getAllAiToolOptions();

        int selectedOption = Messages.showDialog(
                project,
                "Choose which AI CLI to use for generating the commit message:",
                "Select AI CLI",
                options,
                0,
                Messages.getQuestionIcon()
        );

        if (selectedOption <= 0 || selectedOption >= options.length) {
            return null;
        }

        return AiTool.values()[selectedOption - 1];
    }

    private static String @NotNull [] getAllAiToolOptions() {
        String[] options = new String[AiTool.values().length + 1];

        options[0] = "Cancel";

        for (int i = 0; i < AiTool.values().length; i++) {
            options[i + 1] = AiTool.values()[i].getDisplayName();
        }

        return options;
    }
}
