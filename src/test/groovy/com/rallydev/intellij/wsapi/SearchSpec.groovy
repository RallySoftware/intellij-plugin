package com.rallydev.intellij.wsapi

import com.google.common.util.concurrent.FutureCallback
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.domain.Defect
import spock.util.concurrent.BlockingVariable

//todo: I dislike this spec; it retests QueryBuilder rather than just Search
class SearchSpec extends BaseContainerSpec {

    BlockingVariable done = new BlockingVariable()
    FutureCallback<ResultList> callback

    def setup() {
        callback = new FutureCallback<ResultList>() {
            @Override
            void onSuccess(ResultList v) {
                done.set(true)
            }

            @Override
            void onFailure(Throwable throwable) {}
        }
    }

    def "search builds query and searches from dao"() {
        given:
        Search search = new Search<>(
                term: 'hello', searchAttributes: ['Name', 'Description'], domainClass: Defect
        )

        when:
        search.doSearch(callback)

        and:
        done.get()

        then:
        recordingClientRequests
        recordingClientRequests[0].contains('query=((Name contains "hello") OR (Description contains "hello"))')
    }

    def "search restricts by workspace"() {
        given:
        Search search = new Search<>(domainClass: Defect, workspaceRef: workspaceRef)

        when:
        search.doSearch(callback)

        and:
        done.get()

        then:
        recordingClientRequests
        recordingClientRequests[0].contains('workspace=http://rally1.rallydev.com')
    }

    def "search has no restrictions when term is empty"() {
        given:
        Search search = new Search<>(
                searchAttributes: ['Name', 'Description'], domainClass: Defect
        )

        when:
        search.doSearch(callback)

        and:
        done.get()

        then:
        recordingClientRequests
        !recordingClientRequests[0].contains('query')
    }

    def "search restricts by project when provided"() {
        given:
        Search search = new Search<>(
                searchAttributes: ['Name', 'Description'], domainClass: Defect, project: 'http://some.ref.to.project'
        )

        when:
        search.doSearch(callback)

        and:
        done.get()

        then:
        recordingClientRequests
        recordingClientRequests[0].contains('query=(Project = "http://some.ref.to.project")')
    }

}
