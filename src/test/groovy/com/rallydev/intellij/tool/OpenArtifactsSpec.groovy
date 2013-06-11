package com.rallydev.intellij.tool

import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.domain.Artifact

class OpenArtifactsSpec extends BaseContainerSpec {

    def "observers are notified on add & left shift"() {
        given:
        OpenArtifacts artifacts = new OpenArtifacts()

        Observer observer = Mock(Observer)
        artifacts.addObserver(observer)

        when:
        artifacts.add(new Artifact(name: 'Some artifact'))

        then:
        1 * observer.update(_, _)

        when:
        artifacts << new Artifact(name: 'Some artifact')

        then:
        1 * observer.update(_, _)
    }

}
