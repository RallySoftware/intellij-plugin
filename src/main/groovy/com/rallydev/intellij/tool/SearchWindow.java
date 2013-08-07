package com.rallydev.intellij.tool;

import javax.swing.*;

public class SearchWindow {

    protected JPanel myToolWindowContent;

    protected JTabbedPane searchPane;
    protected JTextField searchBox;
    protected JComboBox typeChoices;

    protected JLabel projectLabel;
    protected JComboBox projectChoices;

    protected JCheckBox formattedIDCheckBox;
    protected JCheckBox nameCheckBox;
    protected JCheckBox descriptionCheckBox;

    protected JButton searchButton;

    protected JTable resultsTable;
    protected JPanel statusPanel;

    //todo: temp, delete before merge (also from form)
    protected JButton button1;

}
