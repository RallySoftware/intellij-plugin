package com.rallydev.intellij.tool

import com.rallydev.intellij.wsapi.Search
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement
import spock.lang.Specification

class SearchListenerSpec extends Specification {

    SearchWindowImpl window

    def setup() {
        window = Mock(SearchWindowImpl) {
            getSearchAttributes() >> { ['TestAttribute'] }
            getSelectedType() >> { Defect }
            getSearchTerm() >> { 'The search' }
        }
    }

    def "performs search based on window selections"() {
        given:
        Search search = Spy(Search) {
            1 * doSearch() >> { [] }
        }

        and:
        SearchListener searchListener = new SearchListener(window: window, search: search)

        when:
        searchListener.doActionPerformed()

        then:
        search.term == 'The search'
        search.searchAttributes == ['TestAttribute']
        search.domainClass == Defect
    }

    def "sets status and enables/disables controls on ActionPerformed"() {
        given:
        Search search = Spy(Search) {
            1 * doSearch() >> { [] }
        }

        and:
        SearchListener searchListener = new SearchListener(window: window, search: search)

        when:
        searchListener.doActionPerformed()

        then:
        search.term == 'The search'
        search.searchAttributes == ['TestAttribute']
        search.domainClass == Defect

        and:
        1 * window.enableControls(false)
        1 * window.startLoadingAnimation()

        then:
        1 * window.enableControls(true)
    }

    def "enables controls & sets message on search failure"() {
        given:
        Search search = Spy(Search) {
            1 * doSearch() >> { throw new Exception() }
        }

        and:
        SearchListener searchListener = new SearchListener(window: window, search: search)

        when:
        searchListener.doActionPerformed()

        then:
        1 * window.enableControls(true)
        1 * window.setStatus('Error communicating with Rally')
    }

    def "updates window with results"() {
        given:
        Map<String, Artifact> searchResults = new HashMap<>()
        SearchWindowImpl window = Mock(SearchWindowImpl)
        window.searchResults >> searchResults

        and:
        Artifact artifact1 = new Artifact(formattedID: 'S1', name: 'Story1', description: 'Some story', _type: Requirement.TYPE, projectName: 'P1')
        Artifact artifact2 = new Artifact(formattedID: 'D2', name: 'Defect2', description: 'Some defect', _type: Defect.TYPE, projectName: 'P1')

        and:
        SearchListener searchListener = new SearchListener(
                window: window, search: Mock(Search), results: [artifact1, artifact2]
        )

        when:
        searchListener.run()

        then:
        1 * window.addResult(searchListener.results[0])
        1 * window.addResult(searchListener.results[1])
    }

}
