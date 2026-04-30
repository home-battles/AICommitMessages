package com.homebattles.aicommitmessages.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AICommitMessagesSettingsUI implements SearchableConfigurable, Configurable.NoScroll {
    private JPanel panel;
    private CollectionListModel<String> listModel;
    private JBList<String> toolList;
    private JPanel detailsPanel;
    private CardLayout cardLayout;
    private JComboBox<String> defaultCliCombo;
    private final Map<String, AiToolSettings> uiTools = new LinkedHashMap<>();
    private final Map<String, ToolDetailEditor> editors = new LinkedHashMap<>();

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
        Map<String, AiToolSettings> aiTools = AiToolSettingsService
                .getInstance()
                .getState()
                .tools;

        uiTools.clear();
        for (Map.Entry<String, AiToolSettings> entry : aiTools.entrySet()) {
            AiToolSettings uiSettings = new AiToolSettings(entry.getValue().getName(), entry.getValue().getCliPath());
            uiSettings.setModelName(entry.getValue().getModelName());
            uiTools.put(entry.getKey(), uiSettings);
        }

        listModel = new CollectionListModel<>(new ArrayList<>(uiTools.keySet()));
        toolList = new JBList<>(listModel);
        toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolList.setCellRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends String> list,
                                                 String value,
                                                 int index,
                                                 boolean selected,
                                                 boolean hasFocus) {
                append(value);
            }
        });

        cardLayout = new CardLayout();
        detailsPanel = new JPanel(cardLayout);

        toolList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = toolList.getSelectedValue();
                if (selected != null) {
                    showEditorFor(selected);
                }
            }
        });

        JPanel leftHeader = new JBPanel<>(new BorderLayout());
        leftHeader.add(new JBLabel("<html><b>AI tool</b></html>"), BorderLayout.NORTH);
        leftHeader.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.SOUTH);

        JPanel leftPanel = new JBPanel<>(new BorderLayout());
        leftPanel.add(leftHeader, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(toolList), BorderLayout.CENTER);

        JPanel rightPanel = new JBPanel<>(new BorderLayout());
        rightPanel.add(detailsPanel, BorderLayout.CENTER);

        Splitter splitter = new Splitter(false, 0.3f);
        splitter.setFirstComponent(leftPanel);
        splitter.setSecondComponent(rightPanel);

        panel = new JBPanel<>(new BorderLayout());
        panel.add(splitter, BorderLayout.CENTER);

        defaultCliCombo = new ComboBox<>(createProviderOptions(aiTools));
        JPanel topPanel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JBLabel("Default CLI"));
        topPanel.add(defaultCliCombo);
        panel.add(topPanel, BorderLayout.NORTH);

        reset();
        return panel;
    }

    private String[] createProviderOptions(Map<String, AiToolSettings> tools) {
        String[] providerOptions = new String[tools.size() + 1];
        providerOptions[0] = "Ask Every Time";
        int i = 1;
        for (String name : tools.keySet()) {
            providerOptions[i++] = name;
        }
        return providerOptions;
    }

    private void showEditorFor(String toolName) {
        if (!editors.containsKey(toolName)) {
            ToolDetailEditor editor = new ToolDetailEditor(uiTools.get(toolName));
            editors.put(toolName, editor);
            detailsPanel.add(editor.getPanel(), toolName);
        }
        cardLayout.show(detailsPanel, toolName);
    }

    @Override
    public boolean isModified() {
        AiToolSettingsService settings = AiToolSettingsService.getInstance();
        Map<String, AiToolSettings> tools = settings.getState().tools;

        syncEditorsToUiTools();

        for (Map.Entry<String, AiToolSettings> entry : uiTools.entrySet()) {
            AiToolSettings uiSettings = entry.getValue();
            AiToolSettings stateSettings = tools.get(entry.getKey());
            if (!Objects.equals(uiSettings.getCliPath(), stateSettings.getCliPath()) ||
                !Objects.equals(uiSettings.getModelName(), stateSettings.getModelName())) {
                return true;
            }
        }

        String selectedItem = (String) defaultCliCombo.getSelectedItem();
        String currentDefault = "Ask Every Time".equals(selectedItem) ? AiToolSettingsService.DEFAULT_AI_TOOL : selectedItem;
        return !settings.getDefaultToolName().equals(currentDefault);
    }

    private void syncEditorsToUiTools() {
        editors.forEach((name, editor) -> editor.applyTo(uiTools.get(name)));
    }

    @Override
    public void apply() {
        AiToolSettingsService settings = AiToolSettingsService.getInstance();
        editors.forEach((name, editor) -> editor.applyTo(uiTools.get(name)));

        settings.getState().tools.clear();
        for (Map.Entry<String, AiToolSettings> entry : uiTools.entrySet()) {
            AiToolSettings stateSettings = new AiToolSettings(entry.getValue().getName(), entry.getValue().getCliPath());
            stateSettings.setModelName(entry.getValue().getModelName());
            settings.getState().tools.put(entry.getKey(), stateSettings);
        }

        String selectedItem = (String) defaultCliCombo.getSelectedItem();
        String defaultTool = "Ask Every Time".equals(selectedItem) ? AiToolSettingsService.DEFAULT_AI_TOOL : selectedItem;
        settings.setDefaultAiTool(defaultTool);
    }

    @Override
    public void reset() {
        AiToolSettingsService settings = AiToolSettingsService.getInstance();
        Map<String, AiToolSettings> tools = settings.getState().tools;

        uiTools.clear();
        editors.clear();
        detailsPanel.removeAll();

        for (Map.Entry<String, AiToolSettings> entry : tools.entrySet()) {
            AiToolSettings uiSettings = new AiToolSettings(entry.getValue().getName(), entry.getValue().getCliPath());
            uiSettings.setModelName(entry.getValue().getModelName());
            uiTools.put(entry.getKey(), uiSettings);
        }

        listModel.replaceAll(new ArrayList<>(uiTools.keySet()));
        if (!uiTools.isEmpty()) {
            String firstTool = uiTools.keySet().iterator().next();
            toolList.setSelectedValue(firstTool, true);
            showEditorFor(firstTool);
        }

        String defaultTool = settings.getDefaultToolName();
        if (AiToolSettingsService.DEFAULT_AI_TOOL.equals(defaultTool)) {
            defaultCliCombo.setSelectedIndex(0);
        } else {
            defaultCliCombo.setSelectedItem(defaultTool);
        }
    }

    @Override
    public void disposeUIResources() {
        panel = null;
        toolList = null;
        detailsPanel = null;
        defaultCliCombo = null;
        uiTools.clear();
        editors.clear();
        cardLayout = null;
        listModel = null;
    }

    private static final class ToolDetailEditor {
        private final JPanel panel;
        private final JBTextField cliPathField = new JBTextField();
        private final JBTextField modelField = new JBTextField();

        private ToolDetailEditor(AiToolSettings tool) {
            cliPathField.setText(tool.getCliPath());
            modelField.setText(tool.getModelName() != null ? tool.getModelName() : "");

            panel = FormBuilder.createFormBuilder()
                    .addLabeledComponent("CLI executable path:", cliPathField)
                    .addLabeledComponent("Model name:", modelField)
                    .addComponentFillVertically(new JPanel(), 0)
                    .getPanel();
            panel.setBorder(JBUI.Borders.empty(10, 12));
        }

        public JPanel getPanel() {
            return panel;
        }

        public void applyTo(AiToolSettings tool) {
            tool.setCliPath(cliPathField.getText().trim());
            String modelText = modelField.getText().trim();
            tool.setModelName(modelText.isEmpty() ? null : modelText);
        }
    }
}
