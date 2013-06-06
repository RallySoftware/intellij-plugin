package com.rallydev.intellij.tool

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.tool.RallyToolWindowImpl.SearchListener
import com.rallydev.intellij.wsapi.Search
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement

import javax.swing.table.DefaultTableModel

class RallyToolWindowImplSpec extends BaseContainerSpec {

    def "createToolWindowContent adds content"() {
        given: 'Mock out the IntelliJ pieces'
        ContentFactory contentFactory = Mock(ContentFactory)
        contentFactory.createContent(_, _, _) >> { Mock(Content) }

        and:
        RallyToolWindowImpl rallyToolWindow = new RallyToolWindowImpl()
        rallyToolWindow.metaClass.getContentFactory = { contentFactory }

        and:
        ContentManager contentManager = Mock(ContentManager)
        ToolWindow toolWindow = Mock(ToolWindow)

        when:
        rallyToolWindow.createToolWindowContent(null, toolWindow)

        then:
        1 * contentManager.addContent(_ as Content) >> {}
        1 * toolWindow.getContentManager() >> { contentManager }
    }

    def "setupWindow populates choices"() {
        given:
        RallyToolWindowImpl rallyToolWindow = new RallyToolWindowImpl()
        rallyToolWindow.setupWindow()

        expect:
        rallyToolWindow.projectChoices.size() == projects.size()
        
        and:
        rallyToolWindow.projectChoices.getItemAt(0).toString() == projects[0].name
        rallyToolWindow.projectChoices.getItemAt(1).toString() == projects[1].name
    }

    def "getType correctly determines type from drop-down"() {
        given:
        RallyToolWindowImpl rallyToolWindow = new RallyToolWindowImpl()
        rallyToolWindow.setupWindow()

        expect:
        rallyToolWindow.selectedType == Artifact

        when:
        rallyToolWindow.typeChoices.setSelectedItem 'Defect'

        then:
        rallyToolWindow.selectedType == Defect

        when:
        rallyToolWindow.typeChoices.setSelectedItem 'Requirement'

        then:
        rallyToolWindow.selectedType == Requirement
    }

    def "searchAttributes returns list based on checkbox selection"() {
        when:
        RallyToolWindowImpl rallyToolWindow = new RallyToolWindowImpl()
        rallyToolWindow.formattedIDCheckBox.selected = false
        rallyToolWindow.nameCheckBox.selected = false
        rallyToolWindow.descriptionCheckBox.selected = false

        then:
        rallyToolWindow.searchAttributes == []

        when:
        rallyToolWindow.formattedIDCheckBox.selected = true

        then:
        rallyToolWindow.searchAttributes == ['FormattedID']

        when:
        rallyToolWindow.nameCheckBox.selected = true
        rallyToolWindow.descriptionCheckBox.selected = true

        then:
        rallyToolWindow.searchAttributes == ['FormattedID', 'Name', 'Description']
    }

    def "SearchListener performs search based on window selections"() {
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

    def "SearchListener updates table from results"() {
        given:
        DefaultTableModel tableModel = Mock(DefaultTableModel)

        SearchListener searchListener = new SearchListener(
                window: Mock(RallyToolWindowImpl), tableModel: tableModel, search: Mock(Search),
                results: [
                        new Artifact(formattedID: 'S1', name: 'Story1', description: 'Some story', _type: Requirement.TYPE),
                        new Artifact(formattedID: 'D2', name: 'Defect2', description: 'Some defect', _type: Defect.TYPE)
                ]
        )

        when:
        searchListener.run()

        then:
        1 * tableModel.addRow('S1', 'Story1', 'Some story', Requirement.TYPE)
        1 * tableModel.addRow('D2', 'Defect2', 'Some defect', Defect.TYPE)
    }

}
