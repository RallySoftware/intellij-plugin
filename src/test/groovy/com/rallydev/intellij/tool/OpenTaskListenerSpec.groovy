package com.rallydev.intellij.tool

import com.intellij.openapi.project.Project
import com.intellij.tasks.Task
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.domain.Artifact

import java.awt.event.ActionEvent

class OpenTaskListenerSpec extends BaseContainerSpec {

    def "actionPerformed opens task"() {
        given:
        OpenTaskListener listener = new OpenTaskListener(Mock(Project), new Artifact())

        and:
        def taskManager = GroovySpy(Object)
        listener.metaClass.getTaskManager = { taskManager }

        when:
        listener.actionPerformed(Mock(ActionEvent, constructorArgs: [this, -1, '']))

        then:
        1 * taskManager.activateTask(_ as Task, _, _) >> { }
    }

}
