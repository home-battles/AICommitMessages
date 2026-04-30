package com.homebattles.aicommitmessages.settings;

import com.homebattles.aicommitmessages.aitools.AiTool;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
@State(name = "AICommitMessagesSettings", storages = @Storage("AICommitMessages.xml"))
public final class AiToolSettingsService implements PersistentStateComponent<AiToolSettingsService.State> {

    public static final String DEFAULT_AI_TOOL = "";

    public static class State {
        public String defaultAiTool = DEFAULT_AI_TOOL;

        public Map<String, AiToolSettings> tools = new LinkedHashMap<>(Map.of(
                "Cursor", new AiToolSettings("Cursor", "agent"),
                "GitHub Copilot", new AiToolSettings("GitHub Copilot", "copilot")
        ));
    }

    public static @NotNull AiToolSettingsService getInstance() {
        return ApplicationManager.getApplication().getService(AiToolSettingsService.class);
    }

    private State state = new State();

    @Override
    @NotNull
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public @Nullable AiTool getDefaultAiTool() {
        if (Objects.isNull(state.defaultAiTool) || state.defaultAiTool.trim().isEmpty()) {
            return null;
        }

        return AiTool.fromString(state.defaultAiTool);
    }

    public void setDefaultAiTool(@Nullable String typeName) {
        state.defaultAiTool = normalize(typeName);
    }

    public @NotNull String getDefaultToolName() {
        return state.defaultAiTool;
    }

    private static @NotNull String normalize(@Nullable String value) {
        if (value == null) {
            return AiToolSettingsService.DEFAULT_AI_TOOL;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? AiToolSettingsService.DEFAULT_AI_TOOL : trimmed;
    }
}
