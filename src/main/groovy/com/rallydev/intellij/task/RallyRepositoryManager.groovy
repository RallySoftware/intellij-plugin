package com.rallydev.intellij.task

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskManager
import com.intellij.tasks.TaskRepository
import com.intellij.tasks.impl.TaskManagerImpl

//todo: Hard-coded right now, move to project based when config in place
class RallyRepositoryManager {

    Project project

    public static RallyRepositoryManager getInstance() {
        return ServiceManager.getService(RallyRepositoryManager.class);
    }

    RallyRepositoryManager(Project project) {
        this.project = project
    }

    TaskManagerImpl getTaskManager() {
        (TaskManagerImpl) TaskManager.getManager(project)
    }

    RallyRepository getRepository(String projectRef) {
        RallyRepository repository = null
        taskManager.allRepositories.each {
            if (it.class == RallyRepository) {
                repository = (RallyRepository) it
            }
        }

        repository ?: createRepository(projectRef)
    }

    RallyRepository createRepository(String projectRef) {
        RallyRepository repository = new RallyRepository(workspaceId: -100)
        List<TaskRepository> repositories = new ArrayList<TaskRepository>()
        repositories.addAll(taskManager.allRepositories)
        repositories.add(repository)
        taskManager.setRepositories(repositories)

        repository
    }

}
