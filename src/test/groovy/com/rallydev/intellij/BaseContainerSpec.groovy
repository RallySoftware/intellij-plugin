package com.rallydev.intellij

import com.google.common.util.concurrent.FutureCallback
import com.intellij.mock.MockApplication
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.testFramework.UsefulTestCase
import com.intellij.util.ui.UIUtil
import com.rallydev.intellij.config.RallyConfig
import com.rallydev.intellij.tool.OpenArtifacts
import com.rallydev.intellij.util.AsyncService
import com.rallydev.intellij.util.SwingService
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.GetRequest
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.cache.ProjectCache
import com.rallydev.intellij.wsapi.cache.ProjectCacheService
import com.rallydev.intellij.wsapi.domain.Project
import org.jetbrains.annotations.NotNull
import org.picocontainer.MutablePicoContainer
import spock.lang.Specification

abstract class BaseContainerSpec extends Specification {

    MutablePicoContainer picoContainer

    RallyClient recordingClient
    List<String> recordingClientRequests = []

    RallyConfig config
    List<Project> projects = [new Project(name: 'Project1'), new Project(name: 'Project1')]

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
        picoContainer.registerComponentInstance(RallyClient.name, recordingClient)

        ProjectCache projectCache = new ProjectCache(loadedOn: new Date(), projects: projects)
        registerComponentInstance(projectCache)

        ProjectCacheService projectCacheService = Spy(ProjectCacheService, constructorArgs: [recordingClient, projectCache])
        projectCacheService.getCachedProjects() >> { projects }
        registerComponentInstance(ProjectCacheService.name, projectCacheService)

        SwingService swingService = new SwingService()
        swingService.metaClass.doInUiThread = { Closure closure ->
            closure()
        }
        swingService.metaClass.queueForUiThread = { Closure closure ->
            closure()
        }
        registerComponentInstance(swingService)

        AsyncService asyncService = new AsyncService()
        asyncService.metaClass.schedule = { Closure callable, FutureCallback callback ->
            println 'Did mock implementation'
            def result = callable()
            if(callback) {
                callback.onSuccess(result)
            }
        }
        registerComponentInstance(AsyncService.name, asyncService)

        registerComponentImplementation(OpenArtifacts)
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

    protected setupProject() {
        //registerComponentImplementation(ProjectManager.name, ProjectManagerImpl)
        LightPlatformTestCase intellijTestCase = new LightPlatformTestCase() {
            String getName() {
                'hello'
            }

            @Override
            protected String getTestName(boolean lowercaseFirstLetter) {
                String name = getName();
                //assertTrue("Test name should start with 'test': " + name, name.startsWith("test"));
                name = name.substring("test".length());
//                if (name.length() > 0 && lowercaseFirstLetter && !UsefulTestCase.isAllUppercaseName(name)) {
//                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
//                }
                return name;
            }


        }
        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            void run() {
                intellijTestCase.setUp()
            }
        })
    }

    String getName() {
        'hello'
    }


}
