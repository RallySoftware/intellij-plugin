package com.rallydev.intellij.task

import com.intellij.tasks.Comment
import com.intellij.tasks.Task
import com.intellij.tasks.TaskType
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement

import javax.swing.*

class RallyTask extends Task {

    String id
    String summary
    String description
    Date created
    Date updated
    //todo: closed is unimplemented
    boolean closed
    String issueUrl
    TaskType type = TaskType.OTHER

    RallyTask() {}

    RallyTask(Artifact artifact) {
        id = artifact.objectID
        summary = artifact.formattedID
        description = artifact.description
        created = artifact.creationDate
        updated = artifact.lastUpdateDate
        issueUrl = artifact._ref
        switch (artifact._type) {
            case Requirement.TYPE:
                type = TaskType.FEATURE
                break
            case Defect.TYPE:
                type = TaskType.BUG
                break
            default:
                type = TaskType.OTHER
        }
    }

    @Override
    Comment[] getComments() {
        return new Comment[0]
    }

    @Override
    Icon getIcon() {
        return null
    }

    boolean isIssue() {
        return true
    }

}
