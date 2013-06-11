package com.rallydev.intellij.tool

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.rallydev.intellij.util.SwingService
import com.rallydev.intellij.wsapi.cache.ProjectCacheService
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Project
import com.rallydev.intellij.wsapi.domain.Requirement

import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableModel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

//todo: store checkbox state
class SearchWindowImpl extends SearchWindow implements ToolWindowFactory {

    ToolWindow myToolWindow
    Map<String, Artifact> searchResults = new HashMap<>()
    com.intellij.openapi.project.Project project

    volatile Boolean showLoadingAnimation

    public void createToolWindowContent(com.intellij.openapi.project.Project project, ToolWindow toolWindow) {
        this.project = project
        myToolWindow = toolWindow
        Content content = getContentFactory().createContent(myToolWindowContent, "", false)
        setupWindow()
        toolWindow.getContentManager().addContent(content)
    }

    void setupWindow() {
        status.text = ''
        status.border = new EmptyBorder(0, 10, 10, 0)

        setupTypeChoices()
        setupProjectChoices()
        setupTable()
        installSearchListener()
    }

    void setupTypeChoices() {
        typeChoices.setModel(new DefaultComboBoxModel(
                ['', 'Defect', 'Requirement'].toArray()
        ))
    }

    void setupProjectChoices() {
        projectChoices.addItem(new ProjectItem(project: new Project(name: '')))
        ServiceManager.getService(ProjectCacheService.class).cachedProjects.each {
            projectChoices.addItem(new ProjectItem(project: it))
        }
    }

    void setupTable() {
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

    void installSearchListener() {
        searchButton.addActionListener(new SearchListener(window: this))
        // ?. for test - explore IntelliJ test framework to better handle
        searchPane.rootPane?.setDefaultButton(searchButton)
    }

    void handleTableClick(MouseEvent mouseEvent) {
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

    void setStatus(String statusText, boolean haltLoadingAnimation = true) {
        if(haltLoadingAnimation) {
            showLoadingAnimation = false
        }
        SwingService.instance.doInUiThread {
            status.text = statusText
        }
    }

    void startLoadingAnimation() {
        setStatus('Loading', false)
        showLoadingAnimation = true
        Thread.start {
            while (showLoadingAnimation) {
                Thread.sleep(250)
                if (showLoadingAnimation) {
                    if(status.text.endsWith('......')) {
                        setStatus('Loading.', false)
                    } else {
                        setStatus(status.text + '.', false)
                    }
                }
            }
        }
    }

    Class getSelectedType() {
        switch (typeChoices.selectedItem) {
            case 'Defect':
                return Defect
            case 'Requirement':
                return Requirement
            default:
                return Artifact
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
