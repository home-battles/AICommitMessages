package com.homebattles.aicommitmessages.settings;

import com.homebattles.aicommitmessages.aitools.AiTool;
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
    public static final String DEFAULT_CURSOR_MODEL = "";
    public static final String DEFAULT_CLI_SELECTION_MODE = "ASK_EVERY_TIME";

    public static final class State {
        public String cursorCliPath = DEFAULT_CURSOR_CLI_PATH;
        public String vscodeCliPath = DEFAULT_VSCODE_CLI_PATH;
        public String cursorModel = DEFAULT_CURSOR_MODEL;
        public String defaultCliType = DEFAULT_CLI_SELECTION_MODE;
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

    public @NotNull String getCursorModel() {
        return normalize(state.cursorModel, DEFAULT_CURSOR_MODEL);
    }

    public void setCursorCliPath(@Nullable String path) {
        state.cursorCliPath = normalize(path, DEFAULT_CURSOR_CLI_PATH);
    }

    public void setVscodeCliPath(@Nullable String path) {
        state.vscodeCliPath = normalize(path, DEFAULT_VSCODE_CLI_PATH);
    }

    public void setCursorModel(@Nullable String model) {
        state.cursorModel = normalize(model, DEFAULT_CURSOR_MODEL);
    }

    public @Nullable AiTool getDefaultProviderType() {
        return AiTool.fromString(state.defaultCliType);
    }

    public void setDefaultCliType(@Nullable String typeName) {
        state.defaultCliType = normalize(typeName, DEFAULT_CLI_SELECTION_MODE);
    }

    private static @NotNull String normalize(@Nullable String value, @NotNull String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
