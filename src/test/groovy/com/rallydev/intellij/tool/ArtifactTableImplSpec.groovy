package com.rallydev.intellij.tool

import com.intellij.openapi.project.Project
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.domain.Artifact

class ArtifactTableImplSpec extends BaseContainerSpec {

    def "labels setup from typeDefinition"() {
        given:
        Artifact artifact = new Artifact(formattedID: 'S1', workspaceRef: workspaceRef)
        ArtifactTabImpl artifactTab = new ArtifactTabImpl(artifact, Mock(Project))

        expect:
        artifactTab.projectLabel.text == typeDefinitions[ApiEndpoint.PROJECT].displayName
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
