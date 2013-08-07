package com.rallydev.intellij.tool

import com.google.common.util.concurrent.FutureCallback
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.AsyncProcessIcon
import com.rallydev.intellij.util.AsyncService
import com.rallydev.intellij.util.ErrorMessageFutureCallback
import com.rallydev.intellij.util.SwingService
import com.rallydev.intellij.wsapi.ApiEndpoint
import com.rallydev.intellij.wsapi.cache.ProjectCacheService
import com.rallydev.intellij.wsapi.cache.TypeDefinitionCacheService
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Project
import com.rallydev.intellij.wsapi.domain.Requirement
import com.rallydev.intellij.wsapi.domain.Task
import com.rallydev.intellij.wsapi.domain.TypeDefinition

import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

//todo: store checkbox state
class SearchWindowImpl extends SearchWindow implements ToolWindowFactory {
    static final Logger log = Logger.getInstance(SearchWindowImpl)

    ToolWindow myToolWindow
    Map<String, Artifact> searchResults = new HashMap<>()
    com.intellij.openapi.project.Project project

    public void createToolWindowContent(com.intellij.openapi.project.Project project, ToolWindow toolWindow) {
        this.project = project
        myToolWindow = toolWindow
        Content content = getContentFactory().createContent(myToolWindowContent, "", false)
        setupWindow()
        toolWindow.getContentManager().addContent(content)
    }

    void setupWindow() {
        setupTable()
        installSearchListener()
        setupTypeChoices()
        setupProjectChoices()

        //todo: temp, remove before (merge)
        button1.addActionListener(
                new ActionListener() {
                    @Override
                    void actionPerformed(ActionEvent actionEvent) {
                        TypeDefinition typedef = TypeDefinitionCacheService.instance.getTypeDefinition('HierarchicalRequirement')
                        typedef
                    }
                }
        )
    }

    private void setupTable() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            boolean isCellEditable(int row, int column) {
                false
            }
        }
        resultsTable.model = model

        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            void mouseClicked(MouseEvent mouseEvent) {
                handleTableClick(mouseEvent)
            }
        })

        model.addColumn('Formatted ID')
        model.addColumn('Name')
        model.addColumn('Last Updated')
        model.addColumn('Type')
        model.addColumn('Project')
    }

    private void installSearchListener() {
        searchButton.addActionListener(new SearchListener(window: this))
        //todo: ?. for test - explore IntelliJ test framework to better handle
        searchPane.rootPane?.setDefaultButton(searchButton)
    }

    private void setupLabels() {
        //todo: changes isPrimed & toggleInteractiveComponents in loadProjectChoices() to check all async callbacks
        Closure<TypeDefinition> call = {
            TypeDefinitionCacheService.instance.getTypeDefinition(ApiEndpoint.HIERARCHICAL_REQUIREMENT.typeDefinitionElementName)
        }

        FutureCallback<TypeDefinition> callback = new ErrorMessageFutureCallback<TypeDefinition>() {
            void onSuccess(TypeDefinition typeDefinition) {
                SwingService.instance.queueForUiThread {
                    log.info "Project typedef display name: ${typeDefinition.displayName}"
                    projectLabel.setText(typeDefinition.displayName)
                }
            }
        }

        AsyncService.instance.schedule(call, callback)
    }

    private void setupTypeChoices() {
        //todo: some kind of sorting on for list
        typeChoices.setModel(new DefaultComboBoxModel(
                [''].toArray()
        ))

        FutureCallback<TypeDefinition> callback = new ErrorMessageFutureCallback<TypeDefinition>() {
            void onSuccess(TypeDefinition typeDefinition) {
                SwingService.instance.queueForUiThread {
//                    int position = 1
//                    for(int i=position; i<typeChoices.itemCount; ++i) {
//                        if(typeChoices.getItemAt(i).toLowerCase() < typeDefinition.displayName.toLowerCase()) {
//                            position = i
//                        } else {
//                            break;
//                        }
//                    }
//                    typeChoices.insertItemAt(typeDefinition.displayName, position)
                    typeChoices.addItem typeDefinition.displayName
                }
            }
        }

        Closure<TypeDefinition> call = {
            TypeDefinitionCacheService.instance.getTypeDefinition(ApiEndpoint.HIERARCHICAL_REQUIREMENT.typeDefinitionElementName)
        }
        AsyncService.instance.schedule(call, callback)

        call = {
            TypeDefinitionCacheService.instance.getTypeDefinition(ApiEndpoint.DEFECT.typeDefinitionElementName)
        }
        AsyncService.instance.schedule(call, callback)

        call = {
            TypeDefinitionCacheService.instance.getTypeDefinition(ApiEndpoint.TASK.typeDefinitionElementName)
        }
        AsyncService.instance.schedule(call, callback)
    }


    private void setupProjectChoices() {
        if (ProjectCacheService.instance.isPrimed) {
            projectChoices.addItem(new ProjectItem(project: new Project(name: '')))
            ProjectCacheService.instance.cachedProjects.each {
                projectChoices.addItem(new ProjectItem(project: it))
            }
        } else {
            loadProjectChoices()
        }
    }

    private void loadProjectChoices() {
        showLoadingAnimation('Loading project list...')
        toggleInteractiveComponents(false)

        Closure<List<Project>> call = {
            return ProjectCacheService.instance.cachedProjects
        }
        FutureCallback<List<Project>> callback = new ErrorMessageFutureCallback<List<Project>>() {
            void onSuccess(List<Project> projects) {
                SwingService.instance.queueForUiThread {
                    projectChoices.addItem(new ProjectItem(project: new Project(name: '')))
                    projects.each {
                        projectChoices.addItem(new ProjectItem(project: it))
                    }
                    toggleInteractiveComponents(true)
                    setStatus('Loaded project list')
                }
            }
        }

        AsyncService.instance.schedule(call, callback)
    }

    private void handleTableClick(MouseEvent mouseEvent) {
        if (mouseEvent.clickCount == 2) {
            Artifact artifact = searchResults[(String) resultsTable.getValueAt(resultsTable.selectedRow, 0)]
            toolWindowManager.getToolWindow("Rally Artifacts").activate(null)
            ServiceManager.getService(OpenArtifacts.class) << artifact
        }
    }

    void clear() {
        ((DefaultTableModel) resultsTable.model).rowCount = 0
        searchResults = new HashMap<>()
    }

    void addResult(Artifact result) {
        searchResults[result.formattedID] = result
        ((DefaultTableModel) resultsTable.model).addRow(
                [result.formattedID, result.name, result.formattedLastUpdateDate, result._type, result.projectName].toArray()
        )
    }

    void enableControls(Boolean enabled) {
        SwingService.instance.doInUiThread {
            [searchBox, typeChoices, projectChoices, formattedIDCheckBox, nameCheckBox,
                    descriptionCheckBox, searchButton].each { control ->
                control.enabled = enabled
            }
        }
    }

    void setStatus(String statusText) {
        SwingService.instance.doInUiThread {
            statusPanel.removeAll()
            statusPanel.add(new JLabel(statusText))
            statusPanel.revalidate()
            statusPanel.repaint()
        }
    }

    void showLoadingAnimation(String text = "Loading...") {
        SwingService.instance.doInUiThread {
            statusPanel.removeAll()
            statusPanel.add(new AsyncProcessIcon("loading"))
            statusPanel.add(new JLabel(text))
            statusPanel.revalidate()
            statusPanel.repaint()
        }
    }

    Class getSelectedType() {
        switch (typeChoices.selectedItem) {
            case 'Defect':
                return Defect
            case 'Requirement':
                return Requirement
            case 'Task':
                return Task
            default:
                return Artifact
        }
    }

    void toggleInteractiveComponents(Boolean enabled) {
        [
                searchBox, typeChoices, projectChoices, formattedIDCheckBox, nameCheckBox,
                descriptionCheckBox, searchButton, resultsTable, statusPanel
        ].each { JComponent component ->
            component.enabled = enabled
        }
    }

    List<String> getSearchAttributes() {
        List<String> searchAttributes = []
        if (formattedIDCheckBox.selected) {
            searchAttributes << 'FormattedID'
        }
        if (nameCheckBox.selected) {
            searchAttributes << 'Name'
        }
        if (descriptionCheckBox.selected) {
            searchAttributes << 'Description'
        }
        searchAttributes
    }

    String getSearchTerm() {
        searchBox.text
    }

    String getSelectedProject() {
        projectChoices.getSelectedItem()?.project?._ref
    }

    ContentFactory getContentFactory() {
        ContentFactory.SERVICE.getInstance()
    }

    ToolWindowManager getToolWindowManager() {
        ToolWindowManager.getInstance(project)
    }


    static class ProjectItem {
        @Delegate
        Project project

        @Override
        String toString() {
            project.name
        }
    }

}
