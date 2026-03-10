package com.homebattles.aicommitmessages;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service
@State(name = "AICommitMessagesSettings", storages = @Storage("AICommitMessages.xml"))
public final class AICommitMessagesSettings implements PersistentStateComponent<AICommitMessagesSettings.State> {

    public static final String DEFAULT_CURSOR_CLI_PATH = "agent";
    public static final String DEFAULT_VSCODE_CLI_PATH = "copilot";
    public static final CliSelectionMode DEFAULT_CLI_SELECTION_MODE = CliSelectionMode.ASK_EVERY_TIME;

    public enum CliSelectionMode {
        ASK_EVERY_TIME,
        CURSOR,
        VSCODE
    }

    public static final class State {
        public String cursorCliPath = DEFAULT_CURSOR_CLI_PATH;
        public String vscodeCliPath = DEFAULT_VSCODE_CLI_PATH;
        public String defaultCliType = DEFAULT_CLI_SELECTION_MODE.name();
    }

    private State state = new State();

    public static @NotNull AICommitMessagesSettings getInstance() {
        return com.intellij.openapi.application.ApplicationManager.getApplication().getService(AICommitMessagesSettings.class);
    }

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public @NotNull String getCursorCliPath() {
        return normalize(state.cursorCliPath, DEFAULT_CURSOR_CLI_PATH);
    }

    public @NotNull String getVscodeCliPath() {
        return normalize(state.vscodeCliPath, DEFAULT_VSCODE_CLI_PATH);
    }

    public void setCursorCliPath(@Nullable String path) {
        state.cursorCliPath = normalize(path, DEFAULT_CURSOR_CLI_PATH);
    }

    public void setVscodeCliPath(@Nullable String path) {
        state.vscodeCliPath = normalize(path, DEFAULT_VSCODE_CLI_PATH);
    }

    public @NotNull CliSelectionMode getCliSelectionMode() {
        return parseCliSelectionMode(state.defaultCliType);
    }

    public void setCliSelectionMode(@Nullable CliSelectionMode mode) {
        state.defaultCliType = (mode == null ? DEFAULT_CLI_SELECTION_MODE : mode).name();
    }

    private static @NotNull String normalize(@Nullable String value, @NotNull String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static @NotNull CliSelectionMode parseCliSelectionMode(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return DEFAULT_CLI_SELECTION_MODE;
        }
        String normalized = value.trim().toUpperCase();
        switch (normalized) {
            case "CURSOR":
                return CliSelectionMode.CURSOR;
            case "COPILOT":
            case "VSCODE":
                return CliSelectionMode.VSCODE;
            case "ASK_EVERY_TIME":
                return CliSelectionMode.ASK_EVERY_TIME;
            default:
                return DEFAULT_CLI_SELECTION_MODE;
        }
    }
}
