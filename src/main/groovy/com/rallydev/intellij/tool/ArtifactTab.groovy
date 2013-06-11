package com.rallydev.intellij.tool

import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.rallydev.intellij.wsapi.domain.Artifact

import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.*

class ArtifactTab extends SimpleToolWindowPanel {

    Artifact artifact
    JPanel content

    ArtifactTab(Artifact artifact) {
        super(false)
        this.artifact = artifact

        content = new JPanel(new GridBagLayout())
//        add(new JScrollPane(viewportView: content))
        add(content)

        setupWindow()
    }

    void setupWindow() {
        Integer row = 0
        addTitle(row++)
        addProject(row++)
        addLastUpdate(row++)
        addDescription(row++)
        addNotes(row++)

        //addSpacer(row)
    }

    private void addTitle(Integer gridY) {
        JLabel title = new JLabel("${artifact.formattedID} - ${artifact.name}")
        title.font = title.font.deriveFont(Font.BOLD)
        content.add(title, new GridBagConstraints(
                gridwidth: 2, gridy: gridY, fill: GridBagConstraints.HORIZONTAL
        ))
    }

    private void addProject(Integer gridY) {
        addLabel("Project", gridY)
        addField(artifact.projectName, gridY)
    }

    private void addLastUpdate(Integer gridY) {
        addLabel("Last Updated", gridY)
        addField(artifact.formattedLastUpdateDate, gridY)
    }

    private void addDescription(Integer gridY) {
        addLabel("Description", gridY)
        addHtmlField(artifact.description, gridY)
    }

    private void addNotes(Integer gridY) {
        addLabel("Notes", gridY)
        addHtmlField(artifact.notes, gridY)
    }

    //push everything up by adding something that expands vertically
    private void addSpacer(Integer gridY) {
        content.add(new JPanel(), new GridBagConstraints(
                gridy: gridY, weighty: 1, fill: GridBagConstraints.VERTICAL
        ))

    }

    private void addLabel(String text, Integer gridY) {
        content.add(new JLabel(text), new GridBagConstraints(
                anchor: GridBagConstraints.NORTHWEST,
                insets: defaultInsets, gridy: gridY
        ))
    }

    private void addField(String text, Integer gridY) {
        content.add(new JLabel(text?.trim() ?: ''), new GridBagConstraints(
                fill: GridBagConstraints.HORIZONTAL,
                gridy: gridY, insets: defaultInsets, weightx: 1,
        ))
    }

    private void addHtmlField(String text, Integer gridY) {
        JComponent description = new JLabel(
                text: "<html>${text ?: ''}</html>",
                opaque: true, background: Color.WHITE,
                border: new EmptyBorder(5, 5, 5, 5),
        )
//        description.setPreferredSize(new Dimension(
//                0, description.preferredSize.height as Integer,
//        ))


        def scroll = new JScrollPane(description)

        content.add(scroll, new GridBagConstraints(
                fill: GridBagConstraints.BOTH,
                gridy: gridY, insets: defaultInsets, weightx: 1, weighty: 1
        ))
    }

    private static Insets getDefaultInsets() {
        new Insets(10, 10, 10, 10)
    }

}
