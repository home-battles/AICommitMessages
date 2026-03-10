package com.homebattles.aicommitmessages;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AICommitMessagesConfigurable implements SearchableConfigurable, Configurable.NoScroll {
    private JPanel panel;
    private JBTextField cursorCliPathField;
    private JBTextField vscodeCliPathField;
    private JComboBox<String> defaultCliCombo;

    @Override
    public @NotNull String getId() {
        return "com.homebattles.aicommitmessages.settings";
    }

    @Override
    public @Nls String getDisplayName() {
        return "AI Commit Messages";
    }

    @Override
    public @Nullable JComponent createComponent() {
        cursorCliPathField = new JBTextField();
        vscodeCliPathField = new JBTextField();
        defaultCliCombo = new ComboBox<>(new String[]{"Ask Every Time", "Cursor", "VSCode"});

        panel = new JBPanel<>(new BorderLayout());
        JPanel form = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Cursor CLI path"), cursorCliPathField, 1, false)
                .addLabeledComponent(new JBLabel("VSCode CLI path"), vscodeCliPathField, 1, false)
                .addLabeledComponent(new JBLabel("Default CLI"), defaultCliCombo, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        panel.add(form, BorderLayout.CENTER);
        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        AICommitMessagesSettings settings = AICommitMessagesSettings.getInstance();
        return !settings.getCursorCliPath().equals(cursorCliPathField.getText().trim())
                || !settings.getVscodeCliPath().equals(vscodeCliPathField.getText().trim())
                || cliSelectionModeFromUi() != settings.getCliSelectionMode();
    }

    @Override
    public void apply() {
        AICommitMessagesSettings settings = AICommitMessagesSettings.getInstance();
        settings.setCursorCliPath(cursorCliPathField.getText());
        settings.setVscodeCliPath(vscodeCliPathField.getText());
        settings.setCliSelectionMode(cliSelectionModeFromUi());
    }

    @Override
    public void reset() {
        AICommitMessagesSettings settings = AICommitMessagesSettings.getInstance();
        if (cursorCliPathField != null) {
            cursorCliPathField.setText(settings.getCursorCliPath());
        }
        if (vscodeCliPathField != null) {
            vscodeCliPathField.setText(settings.getVscodeCliPath());
        }
        if (defaultCliCombo != null) {
            defaultCliCombo.setSelectedIndex(indexFromCliSelectionMode(settings.getCliSelectionMode()));
        }
    }

    @Override
    public void disposeUIResources() {
        panel = null;
        cursorCliPathField = null;
        vscodeCliPathField = null;
        defaultCliCombo = null;
    }

    private AICommitMessagesSettings.CliSelectionMode cliSelectionModeFromUi() {
        if (defaultCliCombo == null) {
            return AICommitMessagesSettings.DEFAULT_CLI_SELECTION_MODE;
        }
        return switch (defaultCliCombo.getSelectedIndex()) {
            case 1 -> AICommitMessagesSettings.CliSelectionMode.CURSOR;
            case 2 -> AICommitMessagesSettings.CliSelectionMode.VSCODE;
            default -> AICommitMessagesSettings.CliSelectionMode.ASK_EVERY_TIME;
        };
    }

    private int indexFromCliSelectionMode(@NotNull AICommitMessagesSettings.CliSelectionMode mode) {
        return switch (mode) {
            case ASK_EVERY_TIME -> 0;
            case CURSOR -> 1;
            case VSCODE -> 2;
        };
    }
}
