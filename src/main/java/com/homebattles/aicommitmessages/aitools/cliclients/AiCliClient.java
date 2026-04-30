package com.homebattles.aicommitmessages.aitools.cliclients;

import com.homebattles.aicommitmessages.settings.AiToolSettings;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * <p>Represents an abstract client for interacting with AI-powered command-line tools.</p>
 *
 * <p>This class provides a common interface for configuring the client settings
 * and building commands to interact with external AI tools via a CLI.</p>
 */
public abstract class AiCliClient {
    protected AiToolSettings settings;

    protected AiCliClient() {
    }

    public void setSettings(AiToolSettings settings) {
        this.settings = settings;
    }

    @NotNull
    public final String getExecutablePath() {
        return settings.getCliPath();
    }

    @NotNull
    public abstract List<String> buildCommand(@NotNull String prompt);

}
