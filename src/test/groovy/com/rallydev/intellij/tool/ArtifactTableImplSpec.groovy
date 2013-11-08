package com.rallydev.intellij.tool

import com.intellij.openapi.project.Project
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement

import javax.swing.JComboBox
import javax.swing.JLabel

class ArtifactTableImplSpec extends BaseContainerSpec {

    def 'labels setup from typeDefinition'() {
        given:
        Artifact artifact = new Artifact(formattedID: 'S1', workspaceRef: workspaceRef)
        ArtifactTabImpl artifactTab = new ArtifactTabImpl(artifact, Mock(Project))
        artifactTab.setup()

        expect:
        artifactTab.projectLabel.text == typeDefinitions[ApiEndpoint.PROJECT].displayName
    }

    def 'given a story, schedule state is populated'() {
        given:
        Requirement requirement = new Requirement(formattedID: 'S1', workspaceRef: workspaceRef, scheduleState: 'Completed')
        ArtifactTabImpl artifactTab = new ArtifactTabImpl(requirement, Mock(Project))
        artifactTab.setup()

        expect:
        artifactTab.getFieldValue('scheduleState') == 'Completed'
    }

    def 'given a story, the schedule state combo box & label is added to the form'() {
        given:
        Requirement requirement = new Requirement(formattedID: 'S1', workspaceRef: workspaceRef, scheduleState: 'Accepted')
        ArtifactTabImpl artifactTab = new ArtifactTabImpl(requirement, Mock(Project))
        artifactTab.setup()

        and:
        String scheduleStateName = typeDefinitions.get(ApiEndpoint.HIERARCHICAL_REQUIREMENT).findAttributeDefinition('ScheduleState').name

        expect:
        artifactTab.dynamicFieldsPanel.components.find {
            it instanceof JLabel && it.text == scheduleStateName
        }
        artifactTab.dynamicFieldsPanel.components.find {
            it instanceof JComboBox && it.selectedItem == 'Accepted'
        }
    }

    def 'default fields and labels are added'() {
        given:
        Requirement requirement = new Requirement(
                formattedID: 'S1', workspaceRef: workspaceRef, scheduleState: 'Accepted',
                description: 'Make things faster',
                projectName: 'Operation Cheetah Blood', lastUpdateDate: new Date(),
                notes: 'Do this awesomely'
        )
        ArtifactTabImpl artifactTab = new ArtifactTabImpl(requirement, Mock(Project))
        artifactTab.setup()

        expect:
        hasLabelWithValue(artifactTab, typeDefinitions[ApiEndpoint.PROJECT].displayName)
        hasLabelWithValue(artifactTab, requirement.projectName)

        and:
        hasLabelWithValue(artifactTab, 'Last Updated')
        hasLabelWithValue(artifactTab, requirement.formattedLastUpdateDate)

        and:
        //todo: failing due to Scroll pane around description
        hasLabelWithValue(artifactTab, 'Description')
        artifactTab.dynamicFieldsPanel.components.find {
            it.hasProperty('text') && it.text.contains(requirement.description)
        }

        and:
        //todo: failing due to Scroll pane around notes
        hasLabelWithValue(artifactTab, 'Notes')
        artifactTab.dynamicFieldsPanel.components.find {
            it.hasProperty('text') && it.text.contains(requirement.notes)
        }
    }

    private Boolean hasLabelWithValue(ArtifactTabImpl artifactTab, String labelValue) {
        artifactTab.dynamicFieldsPanel.components.find {
            it instanceof JLabel && it.text == labelValue
        }
    }

    def 'given a defect, no schedule state info is added to the form'() {
        given:
        Defect requirement = new Defect(formattedID: 'S1', workspaceRef: workspaceRef)
        ArtifactTabImpl artifactTab = new ArtifactTabImpl(requirement, Mock(Project))
        artifactTab.setup()

        and:
        String scheduleStateName = typeDefinitions.get(ApiEndpoint.HIERARCHICAL_REQUIREMENT).findAttributeDefinition('ScheduleState').name

        expect:
        !artifactTab.dynamicFieldsPanel.components.find {
            it instanceof JLabel && it.text == scheduleStateName
        }
    }

    def 'viewInRally opens correct url'() {
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
