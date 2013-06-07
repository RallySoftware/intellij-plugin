package com.rallydev.intellij

import com.intellij.mock.MockApplication
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.testFramework.UsefulTestCase
import com.rallydev.intellij.config.RallyConfig
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.GetRequest
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.cache.ProjectCache
import com.rallydev.intellij.wsapi.cache.ProjectCacheService
import com.rallydev.intellij.wsapi.domain.Project
import org.picocontainer.MutablePicoContainer
import spock.lang.Specification

abstract class BaseContainerSpec extends Specification {

    static String server = 'http://asdf'

    MutablePicoContainer picoContainer

    RallyConfig config
    RallyClient recordingClient
    List<String> recordingClientRequests = []

    List<Project> projects = [new Project(name: 'Project1'), new Project(name: 'Project1')]

    def setup() {
        ApplicationManager.setApplication(new MockApplication(myTestRootDisposable), myTestRootDisposable)
        picoContainer = ((MutablePicoContainer) ApplicationManager.application.getPicoContainer())

        config = new RallyConfig(url: 'http://google.com', userName: 'matt', password: 'monkey')
        registerComponentInstance(config)

        recordingClient = Mock(RallyClient)
        recordingClient.makeRequest(_ as GetRequest) >> { GetRequest request ->
            recordingClientRequests << request.getUrl(server.toURL())
            return new ApiResponse(SpecUtils.minimalResponseJson)
        }
        picoContainer.registerComponentInstance(RallyClient.name, recordingClient)

        registerComponentInstance(new ProjectCache(loaded: new Date(), projects: projects))
        registerComponentImplementation(ProjectCacheService.class)
    }

    protected registerComponentInstance(Object instance) {
        registerComponentInstance(instance.class.name, instance)
    }

    protected registerComponentInstance(String key, Object instance) {
        picoContainer.unregisterComponent(key)
        picoContainer.registerComponentInstance(key, instance)
    }

    protected registerComponentImplementation(Class serviceImplementation) {
        picoContainer.unregisterComponent(serviceImplementation.name)
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

}
