package com.rallydev.intellij.tool

import com.intellij.openapi.project.Project
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Requirement

import javax.swing.JComboBox
import javax.swing.JLabel

class ArtifactTableImplSpec extends BaseContainerSpec {

    def "labels setup from typeDefinition"() {
        given:
        Artifact artifact = new Artifact(formattedID: 'S1', workspaceRef: workspaceRef)
        ArtifactTabImpl artifactTab = new ArtifactTabImpl(artifact, Mock(Project))
        artifactTab.setup()

        expect:
        artifactTab.projectLabel.text == typeDefinitions[ApiEndpoint.PROJECT].displayName
    }

    def "given a story, schedule state is populated"() {
        given:
        Requirement requirement = new Requirement(formattedID: 'S1', workspaceRef: workspaceRef, scheduleState: 'Completed')
        ArtifactTabImpl artifactTab = new ArtifactTabImpl(requirement, Mock(Project))
        artifactTab.setup()

        expect:
        artifactTab.getFieldValue("scheduleState") == "Completed"
    }

    def "given a story, the schedule state combo box is added to the form"() {
        given:
        Requirement requirement = new Requirement(formattedID: 'S1', workspaceRef: workspaceRef, scheduleState: 'Accepted')
        ArtifactTabImpl artifactTab = new ArtifactTabImpl(requirement, Mock(Project))
        artifactTab.setup()

        expect:
        artifactTab.dynamicFieldPanel.componentCount == 2
        artifactTab.dynamicFieldPanel.components.find { it instanceof JLabel }?.text == typeDefinitions.get(ApiEndpoint.HIERARCHICAL_REQUIREMENT).findAttributeDefinition("ScheduleState").name
        artifactTab.dynamicFieldPanel.components.find { it instanceof JComboBox }?.selectedItem == "Accepted"
    }

    def "viewInRally opens correct url"() {
        given:
        String launchedUrl = null

        Artifact artifact = new Artifact(formattedID: 'S1')
        ArtifactTabImpl artifactTab = Spy(ArtifactTabImpl, constructorArgs: [artifact, Mock(Project)]) {
            launchBrowser(_ as String) >> { List args ->
                launchedUrl = args[0]
            }
        }

        when:
        artifactTab.viewInRally()

        then:
        launchedUrl == "${config.url}/#/search?keywords=${artifact.formattedID}"
    }

}
