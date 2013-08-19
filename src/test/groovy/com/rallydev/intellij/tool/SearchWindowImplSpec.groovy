package com.rallydev.intellij.tool

import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.cache.ProjectCacheService
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement
import com.rallydev.intellij.wsapi.domain.Task

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

    def "setupWindow shows/hides loading & toggles interactivity"() {
        given:
        SearchWindowImpl searchWindow = Spy(SearchWindowImpl)

        when:
        searchWindow.setupWindow()

        then:
        1 * searchWindow.showLoadingAnimation('Loading Rally configuration...')
        1 * searchWindow.toggleInteractiveComponents(false)

        and:
        1 * searchWindow.setStatus('Loaded Rally configuration')
        1 * searchWindow.toggleInteractiveComponents(true)
    }

    def "setupWindow populates labels from typedefs"() {
        given:
        SearchWindowImpl searchWindow = Spy(SearchWindowImpl)

        when:
        searchWindow.setupWindow()

        then:
        searchWindow.projectLabel.text == typeDefinitions[ApiEndpoint.PROJECT].displayName
    }

    def "setupWindow populates type choices"() {
        given:
        SearchWindowImpl searchWindow = Spy(SearchWindowImpl)

        when:
        searchWindow.setupWindow()

        then:
        searchWindow.typeChoices.model.objects.size() == 4

        and:
        searchWindow.typeChoices.model.objects.contains ''
        searchWindow.typeChoices.model.objects.contains typeDefinitions[ApiEndpoint.DEFECT].displayName
        searchWindow.typeChoices.model.objects.contains typeDefinitions[ApiEndpoint.TASK].displayName
        searchWindow.typeChoices.model.objects.contains typeDefinitions[ApiEndpoint.HIERARCHICAL_REQUIREMENT].displayName
    }

    def "setupWindow populates project choices when cache is primed"() {
        given:
        ProjectCacheService.instance.getIsPrimed() >> true

        and:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        searchWindow.setupWindow()

        expect:
        searchWindow.projectChoices.size() == projects.size() + 1

        and:
        searchWindow.projectChoices.getItemAt(0).toString() == ''
        searchWindow.projectChoices.getItemAt(1).toString() == projects[0].name
        searchWindow.projectChoices.getItemAt(2).toString() == projects[1].name
    }

    def "setupWindow populates project choices asynchronously when cache not primed"() {
        given:
        ProjectCacheService.instance.getIsPrimed() >> false

        and:
        SearchWindowImpl searchWindow = Spy(SearchWindowImpl)

        when:
        searchWindow.setupWindow()

        then:
        1 * searchWindow.showLoadingAnimation(_) >> {}
        1 * searchWindow.setStatus(_) >> {}
        2 * searchWindow.toggleInteractiveComponents(_ as Boolean) >> {}

        and:
        searchWindow.projectChoices.size() == projects.size() + 1

        and:
        searchWindow.projectChoices.getItemAt(0).toString() == ''
        searchWindow.projectChoices.getItemAt(1).toString() == projects[0].name
        searchWindow.projectChoices.getItemAt(2).toString() == projects[1].name

        and:
        searchWindow.searchBox.enabled
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

        and:
        ToolWindowManager toolWindowManager = Mock(ToolWindowManager)
        ToolWindow toolWindow = Mock(ToolWindow)
        1 * toolWindowManager.getToolWindow(_) >> toolWindow
        searchWindow.metaClass.getToolWindowManager = { toolWindowManager }

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

        and:
        1 * toolWindow.activate(_)
    }

    def "getType correctly determines type from drop-down"() {
        given:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        searchWindow.setupWindow()

        expect:
        searchWindow.selectedType == Artifact

        when:
        searchWindow.typeChoices.setSelectedItem typeDefinitions[ApiEndpoint.DEFECT].displayName

        then:
        searchWindow.selectedType == Defect

        when:
        searchWindow.typeChoices.setSelectedItem typeDefinitions[ApiEndpoint.HIERARCHICAL_REQUIREMENT].displayName

        then:
        searchWindow.selectedType == Requirement

        when:
        searchWindow.typeChoices.setSelectedItem typeDefinitions[ApiEndpoint.TASK].displayName

        then:
        searchWindow.selectedType == Task
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

    def "enable controls correctly changes state"() {
        when:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        searchWindow.enableControls(true)

        then:
        searchWindow.searchButton.enabled

        when:
        searchWindow.enableControls(false)

        then:
        !searchWindow.searchButton.enabled
    }

}
