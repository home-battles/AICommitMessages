package com.homebattles.aicommitmessages.aitools.cliclients;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CopilotAiCliClient extends AiCliClient {

    @Override
    public @NotNull List<String> buildCommand(@NotNull String prompt) {
        return List.of(getExecutablePath(), "-p", prompt);
    }
}
