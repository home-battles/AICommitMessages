package com.homebattles.aicommitmessages.aitools.cliclients;

import org.jetbrains.annotations.NotNull;
import java.util.List;

public class CursorAiCliClient extends AiCliClient {

    @Override
    public @NotNull List<String> buildCommand(@NotNull String prompt) {
        String cursorModel = settings.getModelName();

        if (cursorModel.isBlank()) {
            return List.of(getExecutablePath(), "instructions", "-p", prompt);
        }
        return List.of(getExecutablePath(), "--model", cursorModel, "instructions", "-p", prompt);
    }
}
