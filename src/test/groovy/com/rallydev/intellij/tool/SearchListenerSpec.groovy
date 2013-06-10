package com.rallydev.intellij.tool

import com.rallydev.intellij.wsapi.Search
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement
import spock.lang.Specification

import javax.swing.table.DefaultTableModel

class SearchListenerSpec extends Specification {

    def "performs search based on window selections"() {
        given:
        RallyToolWindowImpl window = Mock(RallyToolWindowImpl) {
            1 * getSearchAttributes() >> { ['TestAttribute'] }
            1 * getSelectedType() >> { Defect }
            1 * getSearchTerm() >> { 'The search' }
        }
        Search search = Spy(Search) {
            1 * doSearch() >> { [] }
        }

        and:
        SearchListener searchListener = new SearchListener(window: window, tableModel: Mock(DefaultTableModel), search: search)

        when:
        searchListener.doActionPerformed()

        then:
        search.term == 'The search'
        search.searchAttributes == ['TestAttribute']
        search.domainClass == Defect
    }

    def "updates table from results"() {
        given:
        DefaultTableModel tableModel = Mock(DefaultTableModel)

        SearchListener searchListener = new SearchListener(
                window: Mock(RallyToolWindowImpl), tableModel: tableModel, search: Mock(Search),
                results: [
                        new Artifact(formattedID: 'S1', name: 'Story1', description: 'Some story', _type: Requirement.TYPE, projectName: 'P1'),
                        new Artifact(formattedID: 'D2', name: 'Defect2', description: 'Some defect', _type: Defect.TYPE, projectName: 'P1')
                ]
        )

        when:
        searchListener.run()

        then:
        1 * tableModel.addRow('S1', 'Story1', 'Some story', Requirement.TYPE, 'P1')
        1 * tableModel.addRow('D2', 'Defect2', 'Some defect', Defect.TYPE, 'P1')
    }

}
