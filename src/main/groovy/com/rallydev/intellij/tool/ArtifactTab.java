package com.rallydev.intellij.tool;

import com.intellij.openapi.ui.SimpleToolWindowPanel;

import javax.swing.*;

public class ArtifactTab {

    protected JPanel contentPanel;

    protected JLabel header;
    protected JLabel project;
    protected JLabel lastUpdated;
    protected JTextPane description;
    protected JTextPane notes;

    protected JButton viewInRallyButton;
    protected JPanel buttonPanel;

    protected ArtifactTab() { }

    private void createUIComponents() {
        contentPanel = new SimpleToolWindowPanel(true);
    }

}
