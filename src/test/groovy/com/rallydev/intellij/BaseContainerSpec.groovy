package com.rallydev.intellij

import com.google.common.util.concurrent.FutureCallback
import com.intellij.mock.MockApplication
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.testFramework.UsefulTestCase
import com.rallydev.intellij.config.RallyConfig
import com.rallydev.intellij.tool.OpenArtifacts
import com.rallydev.intellij.util.AsyncService
import com.rallydev.intellij.util.SwingService
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.client.GetRequest
import com.rallydev.intellij.wsapi.client.RallyClient
import com.rallydev.intellij.wsapi.cache.ProjectCache
import com.rallydev.intellij.wsapi.cache.ProjectCacheService
import com.rallydev.intellij.wsapi.cache.TypeDefinitionCache
import com.rallydev.intellij.wsapi.cache.TypeDefinitionCacheService
import com.rallydev.intellij.wsapi.cache.WorkspaceCache
import com.rallydev.intellij.wsapi.cache.WorkspaceCacheService
import com.rallydev.intellij.wsapi.domain.AttributeDefinition
import com.rallydev.intellij.wsapi.domain.Project
import com.rallydev.intellij.wsapi.domain.TypeDefinition
import com.rallydev.intellij.wsapi.domain.Workspace
import org.jetbrains.annotations.NotNull
import org.picocontainer.MutablePicoContainer
import spock.lang.Specification

import static com.rallydev.intellij.wsapi.ApiEndpoint.DEFECT
import static com.rallydev.intellij.wsapi.ApiEndpoint.HIERARCHICAL_REQUIREMENT
import static com.rallydev.intellij.wsapi.ApiEndpoint.PROJECT
import static com.rallydev.intellij.wsapi.ApiEndpoint.TASK

abstract class BaseContainerSpec extends Specification {

    MutablePicoContainer picoContainer

    RallyClient recordingClient
    List<String> recordingClientRequests = []
    String workspaceRef = "http://rally1.rallydev.com"

    RallyConfig config
    List<Project> projects = [new Project(name: 'Project1'), new Project(name: 'Project1')]
    Map<ApiEndpoint, TypeDefinition> typeDefinitions = [
            (DEFECT): new TypeDefinition(displayName: 'Defect!', objectID: 1),
            (HIERARCHICAL_REQUIREMENT): new TypeDefinition(
                    displayName: 'User Story!', objectID: 2,
                    attributeDefinitions:[
                            new AttributeDefinition(
                                    elementName: "ScheduleState",
                                    name: "Schedule State",
                                    allowedValues: ["Defined", "In-Progress", "Completed", "Accepted"]
                            )
                    ]
            ),
            (PROJECT): new TypeDefinition(displayName: 'Project!', objectID: 3),
            (TASK): new TypeDefinition(displayName: 'Task!', objectID: 4)
    ]

    def setup() {
        //Application application = new MockApplication(myTestRootDisposable), myTestRootDisposable
        Application application = Spy(MockApplication, constructorArgs: [myTestRootDisposable])
        ApplicationManager.setApplication(application, myTestRootDisposable)
        picoContainer = ((MutablePicoContainer) ApplicationManager.application.getPicoContainer())

        config = Spy(TestConfig)
        config.userName = 'matt'
        config.url = 'http://google.com'
        config.rememberPassword = true
        registerComponentInstance(RallyConfig.name, config)

        recordingClient = Mock(RallyClient)
        recordingClient.makeRequest(_ as GetRequest) >> { GetRequest request ->
            recordingClientRequests << request.getUrl(config.url.toURL())
            return new ApiResponse(SpecUtils.minimalResponseJson)
        }
        registerComponentInstance(RallyClient.name, recordingClient)

        setupWorkspaces(recordingClient)
        setupProjects(recordingClient)
        setupTypeDefinitions(recordingClient)

        SwingService swingService = Spy(SwingService)
        swingService.doInUiThread(_ as Closure) >> { Closure closure ->
            closure()
        }
        swingService.queueForUiThread(_ as Closure) >> { Closure closure ->
            closure()
        }
        registerComponentInstance(SwingService.name, swingService)

        AsyncService asyncService = new AsyncService()
        asyncService.metaClass.schedule = { Closure callable, FutureCallback callback ->
            def result = callable()
            if (callback) {
                callback.onSuccess(result)
            }
        }
        registerComponentInstance(AsyncService.name, asyncService)

        registerComponentImplementation(OpenArtifacts)
    }

    protected void setupWorkspaces(RallyClient recordingClient) {
        WorkspaceCache workspaceCache = new WorkspaceCache(loadedOn: new Date(), workspaces: [
                new Workspace(_ref: workspaceRef, name: 'TestWorkspace', objectID: -1)
        ])

        WorkspaceCacheService workspaceCacheService = new WorkspaceCacheService(recordingClient, workspaceCache)

        registerComponentInstance(WorkspaceCacheService.name, workspaceCacheService)
    }

    protected void setupProjects(RallyClient recordingClient) {
        ProjectCache projectCache = new ProjectCache(projectsByWorkspace: [workspaceRef: projects])
        registerComponentInstance(projectCache)

        ProjectCacheService projectCacheService = Spy(ProjectCacheService, constructorArgs: [recordingClient, projectCache])
        projectCacheService.getCachedProjects(_) >> { projects }
        registerComponentInstance(ProjectCacheService.name, projectCacheService)
    }

    protected void setupTypeDefinitions(RallyClient recordingClient) {
        TypeDefinitionCache.TypeDefinitionEntry typeDefinitionEntry = new TypeDefinitionCache.TypeDefinitionEntry()
        TypeDefinitionCache typeDefinitionCache = new TypeDefinitionCache()
        typeDefinitionCache.typeDefinitionsByWorkspace.put(workspaceRef, typeDefinitionEntry)
        typeDefinitions.each { key, value ->
            key.typeDefinitionElementName
            typeDefinitionEntry.typeDefinitions[key.typeDefinitionElementName] = value
        }
        registerComponentInstance(typeDefinitionCache)

        TypeDefinitionCacheService typeDefinitionCacheService = Spy(TypeDefinitionCacheService, constructorArgs: [recordingClient, typeDefinitionCache])
        registerComponentInstance(TypeDefinitionCacheService.name, typeDefinitionCacheService)
    }

    protected registerComponentInstance(Object instance) {
        registerComponentInstance(instance.class.name, instance)
    }

    protected registerComponentInstance(String key, Object instance) {
        picoContainer.unregisterComponent(key)
        picoContainer.registerComponentInstance(key, instance)
    }

    protected registerComponentImplementation(Class serviceImplementation) {
        registerComponentImplementation(serviceImplementation.name, serviceImplementation)
    }

    protected registerComponentImplementation(String key, Class serviceImplementation) {
        picoContainer.unregisterComponent(key)
        picoContainer.registerComponentImplementation(serviceImplementation.name, serviceImplementation)
    }

    //From com.intellij.testFramework.UsefulTestCase
    protected final Disposable myTestRootDisposable = new Disposable() {
        @Override
        public void dispose() {}

        @Override
        public String toString() {
            String testName = UsefulTestCase.getTestName(this.class.name, false)
            return BaseContainerSpec + (StringUtil.isEmpty(testName) ? "" : ".test" + testName)
        }
    }

    protected class TestConfig extends RallyConfig {
        String password = 'monkey'

        @Override
        @NotNull
        String getPassword() {
            password
        }

        @Override
        void setPassword(String password) {
            this.password = password
        }

        @Override
        void clearCachedPassword() {
            password = null
        }

    }

    String getName() {
        'hello'
    }

}
