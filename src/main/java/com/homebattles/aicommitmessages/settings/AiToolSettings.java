package com.homebattles.aicommitmessages.settings;

/**
 * POJO class representing the settings configuration for AI tools.
 */
public class AiToolSettings {
    private String name;
    private String cliPath;
    private String modelName;

    @SuppressWarnings("unused")
    public AiToolSettings() {
        // Default constructor for serialization
    }

    public AiToolSettings(String name, String cliPath) {
        this.name = name;
        this.cliPath = cliPath;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public void setName(String name) {
        this.name = name;
    }

    public String getCliPath() {
        return cliPath;
    }

    public void setCliPath(String cliPath) {
        this.cliPath = cliPath;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
