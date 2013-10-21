package com.rallydev.intellij.tool;

import com.intellij.openapi.ui.SimpleToolWindowPanel;

import javax.swing.*;

public class ArtifactTab {

    protected JPanel contentPanel;

    protected JLabel header;

    protected JPanel dynamicFieldsPanel;

    protected JPanel buttonPanel;
    protected JButton viewInRallyButton;
    protected JButton openTaskContextButton;
    protected JButton saveButton;

    protected ArtifactTab() { }

    private void createUIComponents() {
        contentPanel = new SimpleToolWindowPanel(true);
    }

}
