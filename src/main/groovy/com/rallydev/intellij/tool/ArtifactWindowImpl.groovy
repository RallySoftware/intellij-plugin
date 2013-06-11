package com.rallydev.intellij.tool

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory

class ArtifactWindowImpl extends ArtifactWindow implements ToolWindowFactory {

    ToolWindow myToolWindow

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        myToolWindow = toolWindow
        Content content = getContentFactory().createContent(myToolWindowContent, "", false)

        toolWindow.getContentManager().addContent(content)
    }

    ContentFactory getContentFactory() {
        ContentFactory.SERVICE.getInstance()
    }

}
