package com.rallydev.intellij.tool

import com.google.common.util.concurrent.FutureCallback
import com.intellij.openapi.application.ApplicationManager
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.ResultListMock
import com.rallydev.intellij.wsapi.Search
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement
import spock.lang.Ignore
import spock.util.concurrent.BlockingVariable

class SearchListenerSpec extends BaseContainerSpec {

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
            1 * doSearch(_) >> {}
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
            1 * doSearch(_ as FutureCallback) >> { FutureCallback callback ->
                callback.onSuccess(new ResultListMock<Artifact>())
            }
        }

        and:
        SearchListener searchListener = new SearchListener(window: window, search: search)

        when:
        searchListener.doActionPerformed()

        then:
        1 * window.enableControls(false)
        1 * window.startLoadingAnimation()

        then:
        1 * window.enableControls(true)
    }

    @Ignore
    def "enables controls & sets message on search failure"() {
        given:
        Search search = Spy(Search) {
            1 * doSearch(_ as FutureCallback) >> { FutureCallback callback ->
                callback.onFailure(new RuntimeException())
            }
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
        BlockingVariable done = new BlockingVariable()

        and:
        Map<String, Artifact> searchResults = new HashMap<>()
        SearchWindowImpl window = Mock(SearchWindowImpl)
        window.searchResults >> searchResults

        and:
        ResultListMock<Artifact> results = [
                new Artifact(formattedID: 'S1', name: 'Story1', description: 'Some story', _type: Requirement.TYPE, projectName: 'P1'),
                new Artifact(formattedID: 'D2', name: 'Defect2', description: 'Some defect', _type: Defect.TYPE, projectName: 'P1')
        ]

        and:
        SearchListener searchListener = new SearchListener(window: window, search: Mock(Search))

        when:
        ApplicationManager.application.invokeLater(_ as Runnable) >> { Runnable runnable ->
            done.set(true)
            runnable.run()
        }
        searchListener.getCallback().onSuccess(results)

        and: 'the ui thread finishes'
        done.get()

        then:
        1 * window.addResult(results[0])
        1 * window.addResult(results[1])
    }

}
