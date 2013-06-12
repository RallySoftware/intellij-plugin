package com.rallydev.intellij.tool

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.impl.ContentImpl
import com.rallydev.intellij.wsapi.domain.Artifact
import org.jetbrains.annotations.NotNull

//todo: remove from open artifacts on close
class ArtifactWindowImpl implements ToolWindowFactory, Observer {

    ToolWindow myToolWindow
    Project project

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project
        myToolWindow = toolWindow

        ServiceManager.getService(OpenArtifacts.class).addObserver(this)
        ServiceManager.getService(OpenArtifacts.class).artifacts.each {
            addTab(it)
        }
    }

    //todo: Cache by id & refresh if already there
    void addTab(Artifact artifact) {
        ContentImpl content = new ContentImpl(new ArtifactTabImpl(artifact, project).contentPanel, artifact.formattedID, true)
        myToolWindow.getContentManager().addContent(content)
        myToolWindow.getContentManager().setSelectedContent(content, true)
    }

    @Override
    void update(Observable observable, Object artifact) {
        addTab((Artifact) artifact)
    }

}
