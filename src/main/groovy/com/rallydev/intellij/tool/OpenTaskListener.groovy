package com.rallydev.intellij.tool

import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskManager
import com.intellij.tasks.impl.TaskManagerImpl
import com.rallydev.intellij.task.RallyTask
import com.rallydev.intellij.wsapi.domain.Artifact
import org.jetbrains.annotations.NotNull

import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class OpenTaskListener implements ActionListener {

    Project project
    Artifact artifact

    OpenTaskListener(@NotNull Project project, @NotNull Artifact artifact) {
        this.project = project
        this.artifact = artifact
    }

    TaskManagerImpl getTaskManager() {
        (TaskManagerImpl) TaskManager.getManager(project)
    }

    @Override
    void actionPerformed(ActionEvent actionEvent) {
        RallyTask task = new RallyTask(artifact)
        taskManager.activateTask(task, true, false);
    }

}
