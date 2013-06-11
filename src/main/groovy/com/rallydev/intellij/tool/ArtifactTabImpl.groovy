package com.rallydev.intellij.tool

import com.intellij.ide.BrowserUtil
import com.rallydev.intellij.config.RallyConfig
import com.rallydev.intellij.wsapi.domain.Artifact

import javax.swing.border.EmptyBorder
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

/* Thoughts for future: Make another request to get complete information, request to actual endpoint */
class ArtifactTabImpl extends ArtifactTab {

    Artifact artifact

    public ArtifactTabImpl(Artifact artifact) {
        this.artifact = artifact

        populateFields()
        setupViewButton()
    }

    void populateFields() {
        header.text = "${artifact.formattedID} - ${artifact.name}"
        project.text = artifact.projectName
        lastUpdated.text = artifact.formattedLastUpdateDate

        description.text = "<html>${artifact.description ?: ''}</html>"
        description.border = new EmptyBorder(5, 5, 5, 5)

        notes.text = "<html>${artifact.notes ?: ''}</html>"
        notes.border = new EmptyBorder(5, 5, 5, 5)
    }

    void setupViewButton() {
        viewInRallyButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent actionEvent) {
                viewInRally()
            }
        })
    }

    void viewInRally() {
        String url = "${RallyConfig.instance.url}/#/search?keywords=${artifact.formattedID}"
        launchBrowser(url)
    }

    void launchBrowser(String url) {
        BrowserUtil.launchBrowser(url)
    }

}