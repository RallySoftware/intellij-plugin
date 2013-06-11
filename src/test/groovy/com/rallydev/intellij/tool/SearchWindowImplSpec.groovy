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
import java.awt.event.MouseEvent

class SearchWindowImplSpec extends BaseContainerSpec {

    def "createToolWindowContent adds content"() {
        given: 'Mock out the IntelliJ pieces'
        ContentFactory contentFactory = Mock(ContentFactory)
        contentFactory.createContent(_, _, _) >> { Mock(Content) }

        and:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        searchWindow.metaClass.getContentFactory = { contentFactory }

        and:
        ContentManager contentManager = Mock(ContentManager)
        ToolWindow toolWindow = Mock(ToolWindow)

        when:
        searchWindow.createToolWindowContent(null, toolWindow)

        then:
        1 * contentManager.addContent(_ as Content) >> {}
        1 * toolWindow.getContentManager() >> { contentManager }
    }

    def "setupWindow populates choices"() {
        given:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        searchWindow.setupWindow()

        expect:
        searchWindow.projectChoices.size() == projects.size() + 1

        and:
        searchWindow.projectChoices.getItemAt(0).toString() == ''
        searchWindow.projectChoices.getItemAt(1).toString() == projects[0].name
        searchWindow.projectChoices.getItemAt(2).toString() == projects[1].name
    }

    def "handleTableClick adds artifact open artifact"() {
        given:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        searchWindow.setupWindow()
        Artifact artifact = new Artifact(
                formattedID: 'D1', name: 'name', lastUpdateDate: new Date(), _type: 'Defect', projectName: 'P1'
        )

        and:
        searchWindow.addResult(artifact)
        searchWindow.resultsTable.setRowSelectionInterval(0, 0)

        expect:
        OpenArtifacts.instance.artifacts.size() == 0

        when:
        searchWindow.handleTableClick(new MouseEvent(
                searchWindow.resultsTable, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                10, 10, /* location 10, 10 */ 2 /* double click*/, false
        ))

        then:
        OpenArtifacts.instance.artifacts.size() == 1
        OpenArtifacts.instance.artifacts.contains(artifact)
    }

    def "getType correctly determines type from drop-down"() {
        given:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        searchWindow.setupWindow()

        expect:
        searchWindow.selectedType == Artifact

        when:
        searchWindow.typeChoices.setSelectedItem 'Defect'

        then:
        searchWindow.selectedType == Defect

        when:
        searchWindow.typeChoices.setSelectedItem 'Requirement'

        then:
        searchWindow.selectedType == Requirement
    }

    def "results table is not editable"() {
        given:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        searchWindow.setupWindow()

        searchWindow.resultsTable.isCellEditable(0, 0)

        when:
        ((DefaultTableModel) searchWindow.resultsTable.model).addRow(
                'id', 'name', '', 'Defect'
        )

        then:
        'id' == searchWindow.resultsTable.getValueAt(0, 0)
        !searchWindow.resultsTable.isCellEditable(0, 0)
    }

    def "searchAttributes returns list based on checkbox selection"() {
        when:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        searchWindow.formattedIDCheckBox.selected = false
        searchWindow.nameCheckBox.selected = false
        searchWindow.descriptionCheckBox.selected = false

        then:
        searchWindow.searchAttributes == []

        when:
        searchWindow.formattedIDCheckBox.selected = true

        then:
        searchWindow.searchAttributes == ['FormattedID']

        when:
        searchWindow.nameCheckBox.selected = true
        searchWindow.descriptionCheckBox.selected = true

        then:
        searchWindow.searchAttributes == ['FormattedID', 'Name', 'Description']
    }

}
