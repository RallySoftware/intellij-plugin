package com.rallydev.intellij.tool

import com.google.common.util.concurrent.FutureCallback
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.rallydev.intellij.config.RallyConfig
import com.rallydev.intellij.util.AsyncService
import com.rallydev.intellij.util.ErrorMessageFutureCallback
import com.rallydev.intellij.util.SwingService
import com.rallydev.intellij.wsapi.cache.TypeDefinitionCacheService
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.TypeDefinition
import org.jetbrains.annotations.NotNull

import javax.swing.border.EmptyBorder
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static com.rallydev.intellij.wsapi.ApiEndpoint.PROJECT

/* Thoughts for future: Make another request to get complete information, request to actual endpoint */
class ArtifactTabImpl extends ArtifactTab {

    Project project
    Artifact artifact

    public ArtifactTabImpl(@NotNull Artifact artifact, @NotNull Project project) {
        this.project = project
        this.artifact = artifact

        populateFields()
        setupLabels()
        setupViewButton()
        openTaskContextButton.addActionListener(new OpenTaskListener(project, artifact))
    }

    void populateFields() {
        header.text = "${artifact.formattedID} - ${artifact.name}"
        projectValue.text = artifact.projectName
        lastUpdatedLabel.text = artifact.formattedLastUpdateDate

        description.text = "<html>${artifact.description ?: ''}</html>"
        description.border = new EmptyBorder(5, 5, 5, 5)

        notes.text = "<html>${artifact.notes ?: ''}</html>"
        notes.border = new EmptyBorder(5, 5, 5, 5)
    }

    private void setupLabels() {
        Closure<TypeDefinition> call = {
            TypeDefinitionCacheService.instance.getTypeDefinition(PROJECT.typeDefinitionElementName, artifact.workspaceRef)
        }

        FutureCallback<TypeDefinition> callback = new ErrorMessageFutureCallback<TypeDefinition>() {
            void onSuccess(TypeDefinition typeDefinition) {
                SwingService.instance.queueForUiThread {
                    projectLabel.setText(typeDefinition.displayName)
                }
            }

            void onFailure(Throwable error) {
                super.onFailure(error)
            }
        }

        AsyncService.instance.schedule(call, callback)
    }


    private void setupViewButton() {
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
