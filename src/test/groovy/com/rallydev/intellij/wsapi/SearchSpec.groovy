package com.rallydev.intellij.wsapi

import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.domain.Defect

//todo: I dislike this spec; it retests QueryBuilder rather than just Search
class SearchSpec extends BaseContainerSpec {

    def "search builds query and searches from dao"() {
        given:
        Search search = new Search<>(
                term: 'hello', searchAttributes: ['Name', 'Description'], domainClass: Defect
        )

        when:
        search.doSearch()

        then:
        recordingClientRequests
        recordingClientRequests[0].contains('query=((Name contains "hello") OR (Description contains "hello"))')
    }

    def "search has no restrictions when term is empty"() {
        given:
        Search search = new Search<>(
                searchAttributes: ['Name', 'Description'], domainClass: Defect
        )

        when:
        search.doSearch()

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
        search.doSearch()

        then:
        recordingClientRequests
        recordingClientRequests[0].contains('query=(Project = "http://some.ref.to.project")')
    }

}
