package com.rallydev.intellij.tool;

import com.intellij.openapi.ui.SimpleToolWindowPanel;

import javax.swing.*;

public class ArtifactTab2 {

    protected JPanel contentPanel;

    protected JLabel header;

    protected JPanel dynamicFieldsPanel;

    protected JButton viewInRallyButton;
    protected JPanel buttonPanel;
    protected JButton openTaskContextButton;

    protected ArtifactTab2() { }

    private void createUIComponents() {
        contentPanel = new SimpleToolWindowPanel(true);
    }

}
