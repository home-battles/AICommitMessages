package com.homebattles.aicommitmessages.aitools.cliclients;

import com.homebattles.aicommitmessages.settings.AICommitMessagesSettings;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * <p>Represents an abstract client for interacting with AI-powered command-line tools.</p>
 *
 * <p>This class provides a common interface for configuring the client settings
 * and building commands to interact with external AI tools via a CLI.</p>
 */
public abstract class AiCliClient {
    protected AICommitMessagesSettings settings;

    protected AiCliClient() {
    }

    public void setSettings(AICommitMessagesSettings settings) {
        this.settings = settings;
    }

    @NotNull
    public abstract String getExecutablePath();

    @NotNull
    public abstract List<String> buildCommand(@NotNull String prompt);

}
