package com.homebattles.aicommitmessages.aitools;

import com.homebattles.aicommitmessages.aitools.cliclients.AiCliClient;
import com.homebattles.aicommitmessages.aitools.cliclients.CopilotAiCliClient;
import com.homebattles.aicommitmessages.aitools.cliclients.CursorAiCliClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum AiTool {
    CURSOR("Cursor", new CursorAiCliClient()),
    GITHUB_COPILOT("GitHub Copilot", new CopilotAiCliClient());

    private final String displayName;

    private final AiCliClient aiCliClient;

    AiTool(String displayName, AiCliClient aiCliClient) {
        this.displayName = displayName;
        this.aiCliClient = aiCliClient;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    public @NotNull AiCliClient getAiCliClient() {
        return aiCliClient;
    }

    public static @Nullable AiTool fromString(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.trim().toUpperCase();
        for (AiTool providerType : values()) {
            if (providerType.name().equalsIgnoreCase(normalized)
                    || providerType.getDisplayName().equalsIgnoreCase(normalized)) {
                return providerType;
            }
        }
        return null;
    }
}
