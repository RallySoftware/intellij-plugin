package com.rallydev.intellij.tool;

import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;

public class RallyToolWindow {

    protected ToolWindow myToolWindow;
    protected JPanel myToolWindowContent;

    protected JTabbedPane searchPane;
    protected JTextField searchBox;
    protected JComboBox typeChoices;
    protected JComboBox projectChoices;

    protected JCheckBox formattedIDCheckBox;
    protected JCheckBox nameCheckBox;
    protected JCheckBox descriptionCheckBox;

    protected JButton searchButton;

    protected JTable resultsTable;

}
