package com.rallydev.intellij.tool

import com.rallydev.intellij.wsapi.Search
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement
import spock.lang.Specification

class SearchListenerSpec extends Specification {

    def "performs search based on window selections"() {
        given:
        SearchWindowImpl window = Mock(SearchWindowImpl) {
            1 * getSearchAttributes() >> { ['TestAttribute'] }
            1 * getSelectedType() >> { Defect }
            1 * getSearchTerm() >> { 'The search' }
        }
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
