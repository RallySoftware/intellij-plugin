package com.rallydev.intellij.tool

import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.SpecUtils
import com.rallydev.intellij.facade.ActionToolbarFacade
import com.rallydev.intellij.util.SwingService
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.cache.CacheManager
import com.rallydev.intellij.wsapi.client.GetRequest
import com.rallydev.intellij.wsapi.client.RallyClient
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement
import com.rallydev.intellij.wsapi.domain.Task

import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.table.DefaultTableModel
import java.awt.event.MouseEvent

class SearchWindowImplSpec extends BaseContainerSpec {

    def setup() {
        registerComponentImplementation(CacheManager)
    }

    void setupActionToolbarMock() {
        ActionToolbar actionToolbar = Mock(ActionToolbar)
        actionToolbar.getComponent() >> Mock(JComponent)

        ActionToolbarFacade actionToolbarFacade = Mock(ActionToolbarFacade)

        registerComponentInstance(ActionToolbarFacade.name, actionToolbarFacade)
        1 * actionToolbarFacade.createActionToolbar(_) >> actionToolbar
    }

    def 'createToolWindowContent adds content'() {
        given: 'Mock out the IntelliJ pieces'
        setupActionToolbarMock()

        ContentFactory contentFactory = Mock(ContentFactory)
        contentFactory.createContent(_, _, _) >> { Mock(Content) }

        and:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        searchWindow.metaClass.getContentFactory = { contentFactory }

        and:
        ContentManager contentManager = Mock(ContentManager)
        ToolWindow toolWindow = Mock(ToolWindow)

        when: 'ToolWindowFactory interface createToolWindowContent invoked'
        searchWindow.createToolWindowContent(null, toolWindow)

        then: 'Content manager add content is called'
        1 * toolWindow.getContentManager() >> contentManager
        1 * contentManager.addContent(_ as Content) >> {}
    }

    def 'setupWindow shows/hides loading & toggles interactivity'() {
        given:
        setupActionToolbarMock()
        SearchWindowImpl searchWindow = Spy(SearchWindowImpl)

        when:
        searchWindow.setupWindow()

        then:
        1 * searchWindow.showLoadingAnimation('Loading Rally workspaces...')
        (1.._) * SwingService.instance.disableComponents(_)

        and:
        1 * searchWindow.setStatus('Loaded Rally configuration')
        (1.._) * SwingService.instance.enableComponents(_)
    }

    def 'setupWindow populates labels from typedefs'() {
        given:
        setupActionToolbarMock()
        SearchWindowImpl searchWindow = Spy(SearchWindowImpl)

        when:
        searchWindow.setupWindow()

        then:
        searchWindow.projectLabel.text == typeDefinitions[ApiEndpoint.PROJECT].displayName
    }

    def 'setupWindow populates type choices'() {
        given:
        setupActionToolbarMock()
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

    def 'setupWindow populates project choices asynchronously'() {
        given:
        setupActionToolbarMock()
        SearchWindowImpl searchWindow = Spy(SearchWindowImpl)

        when:
        searchWindow.setupWindow()

        then:
        1 * searchWindow.showLoadingAnimation('Loading workspace data...') >> {}
        1 * searchWindow.showLoadingAnimation('Loading Rally workspaces...') >> {}
        (1.._) * SwingService.instance.enableComponents(_)

        and:
        searchWindow.projectChoices.size() == projects.size() + 1

        and:
        searchWindow.projectChoices.getItemAt(0).toString() == ''
        searchWindow.projectChoices.getItemAt(1).toString() == projects[0].name
        searchWindow.projectChoices.getItemAt(2).toString() == projects[1].name

        and:
        searchWindow.searchBox.enabled
    }

    def 'handleTableClick adds artifact open artifact'() {
        given:
        setupActionToolbarMock()
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        searchWindow.setupWindow()

        and:
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

    def 'getType correctly determines type from drop-down'() {
        given:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        setupActionToolbarMock()
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

    def 'results table is not editable'() {
        given:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        setupActionToolbarMock()
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

    def 'searchAttributes returns list based on checkbox selection'() {
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

    def 'enable controls correctly changes state'() {
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

    def 'clear empties results table'() {
        given:
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        setupActionToolbarMock()
        searchWindow.setupWindow()

        and:
        ((DefaultTableModel) searchWindow.resultsTable.model).addRow(
                'id', 'name', '', 'Defect'
        )

        expect:
        searchWindow.resultsTable.model.size()

        when:
        searchWindow.clear()

        then:
        !searchWindow.resultsTable.model.size()
    }

    def 'workspaces are loaded on java.util.Observer update'() {
        given:
        recordingClient = Mock(RallyClient)
        recordingClient.makeRequest(_) >> { GetRequest request ->
            recordingClientRequests << request.getUrl(config.url.toURL())
            return new ApiResponse(SpecUtils.getTypedMinimalResponseJson('Workspace'))
        }
        registerComponentInstance(RallyClient.name, recordingClient)

        and: 'a setup toolbar window'
        SearchWindowImpl searchWindow = new SearchWindowImpl()
        setupActionToolbarMock()
        searchWindow.setupWindow()

        and: 'clear out workspace choices so we can check that something loads them'
        searchWindow.workspaceChoices.model = new DefaultComboBoxModel()

        expect: 'workspace choices are empty'
        !searchWindow.workspaceChoices.model.size

        when: 'trigger a cache clear (the observable)'
        CacheManager.instance.clearAllCaches()

        then: 'workspace choices were loaded (the observer)'
        searchWindow.workspaceChoices.model.size
    }

}
