package com.rallydev.intellij.tool

import com.rallydev.intellij.wsapi.domain.Artifact

import javax.swing.border.EmptyBorder

class ArtifactTabImpl extends ArtifactTab {

    public ArtifactTabImpl(Artifact artifact) {
        header.text = "${artifact.formattedID} - ${artifact.name}"
        project.text = artifact.projectName
        lastUpdated.text = artifact.formattedLastUpdateDate

        description.text = "<html>${artifact.description ?: ''}</html>"
        description.border = new EmptyBorder(5, 5, 5, 5)

        notes.text = "<html>${artifact.notes ?: ''}</html>"
        notes.border = new EmptyBorder(5, 5, 5, 5)
    }

}
