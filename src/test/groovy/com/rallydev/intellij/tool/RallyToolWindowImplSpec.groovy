package com.rallydev.intellij.tool

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import com.rallydev.intellij.BaseContainerSpec
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
        rallyToolWindow.projectChoices.size() == projects.size() + 1

        and:
        rallyToolWindow.projectChoices.getItemAt(0).toString() == ''
        rallyToolWindow.projectChoices.getItemAt(1).toString() == projects[0].name
        rallyToolWindow.projectChoices.getItemAt(2).toString() == projects[1].name
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

    def "results table is not editable"() {
        given:
        RallyToolWindowImpl rallyToolWindow = new RallyToolWindowImpl()
        rallyToolWindow.setupWindow()

        rallyToolWindow.resultsTable.isCellEditable(0, 0)

        when:
        ((DefaultTableModel)rallyToolWindow.resultsTable.model).addRow(
                'id', 'name', '', 'Defect'
        )

        then:
        'id' == rallyToolWindow.resultsTable.getValueAt(0, 0)
        !rallyToolWindow.resultsTable.isCellEditable(0, 0)
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

}
