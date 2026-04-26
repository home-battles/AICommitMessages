package com.homebattles.aicommitmessages.settings;

import com.homebattles.aicommitmessages.aitools.AiTool;
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
    private JBTextField cursorModelField;
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
        cursorModelField = new JBTextField();
        vscodeCliPathField = new JBTextField();

        String[] providerOptions = new String[AiTool.values().length + 1];
        providerOptions[0] = "Ask Every Time";
        for (int i = 0; i < AiTool.values().length; i++) {
            providerOptions[i + 1] = AiTool.values()[i].getDisplayName();
        }
        defaultCliCombo = new ComboBox<>(providerOptions);

        panel = new JBPanel<>(new BorderLayout());
        JPanel form = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Cursor CLI path"), cursorCliPathField, 1, false)
                .addLabeledComponent(new JBLabel("Cursor model"), cursorModelField, 1, false)
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
                || !settings.getCursorModel().equals(cursorModelField.getText().trim())
                || !settings.getVscodeCliPath().equals(vscodeCliPathField.getText().trim())
                || providerTypeFromUi() != settings.getDefaultProviderType();
    }

    @Override
    public void apply() {
        AICommitMessagesSettings settings = AICommitMessagesSettings.getInstance();
        settings.setCursorCliPath(cursorCliPathField.getText());
        settings.setCursorModel(cursorModelField.getText());
        settings.setVscodeCliPath(vscodeCliPathField.getText());
        AiTool providerType = providerTypeFromUi();
        settings.setDefaultCliType(providerType == null ? AICommitMessagesSettings.DEFAULT_CLI_SELECTION_MODE : providerType.name());
    }

    @Override
    public void reset() {
        AICommitMessagesSettings settings = AICommitMessagesSettings.getInstance();
        if (cursorCliPathField != null) {
            cursorCliPathField.setText(settings.getCursorCliPath());
        }
        if (cursorModelField != null) {
            cursorModelField.setText(settings.getCursorModel());
        }
        if (vscodeCliPathField != null) {
            vscodeCliPathField.setText(settings.getVscodeCliPath());
        }
        if (defaultCliCombo != null) {
            defaultCliCombo.setSelectedIndex(indexFromProviderType(settings.getDefaultProviderType()));
        }
    }

    @Override
    public void disposeUIResources() {
        panel = null;
        cursorCliPathField = null;
        cursorModelField = null;
        vscodeCliPathField = null;
        defaultCliCombo = null;
    }

    private AiTool providerTypeFromUi() {
        if (defaultCliCombo == null) {
            return null;
        }

        int selectedIndex = defaultCliCombo.getSelectedIndex();
        if (selectedIndex <= 0 || selectedIndex > AiTool.values().length) {
            return null;
        }

        return AiTool.values()[selectedIndex - 1];
    }

    private int indexFromProviderType(@Nullable AiTool providerType) {
        if (providerType == null) {
            return 0;
        }

        for (int i = 0; i < AiTool.values().length; i++) {
            if (AiTool.values()[i] == providerType) {
                return i + 1;
            }
        }
        return 0;
    }
}
