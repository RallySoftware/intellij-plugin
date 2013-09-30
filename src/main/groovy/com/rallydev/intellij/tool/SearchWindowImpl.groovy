package com.rallydev.intellij.tool

import com.google.common.util.concurrent.FutureCallback
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.AsyncProcessIcon
import com.rallydev.intellij.facade.ActionToolbarFacade
import com.rallydev.intellij.util.AsyncService
import com.rallydev.intellij.util.ErrorMessageFutureCallback
import com.rallydev.intellij.util.SwingService
import com.rallydev.intellij.wsapi.cache.CacheManager
import com.rallydev.intellij.wsapi.cache.ProjectCacheService
import com.rallydev.intellij.wsapi.cache.TypeDefinitionCacheService
import com.rallydev.intellij.wsapi.cache.WorkspaceCacheService
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Project
import com.rallydev.intellij.wsapi.domain.TypeDefinition
import com.rallydev.intellij.wsapi.domain.Workspace
import org.jetbrains.annotations.Nullable

import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.awt.*
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.List
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

import static com.rallydev.intellij.wsapi.ApiEndpoint.DEFECT
import static com.rallydev.intellij.wsapi.ApiEndpoint.HIERARCHICAL_REQUIREMENT
import static com.rallydev.intellij.wsapi.ApiEndpoint.PROJECT
import static com.rallydev.intellij.wsapi.ApiEndpoint.TASK

class SearchWindowImpl extends SearchWindow implements ToolWindowFactory, Observer {
    static final Logger log = Logger.getInstance(SearchWindowImpl)

    ToolWindow myToolWindow
    Map<String, Artifact> searchResults = new HashMap<>()
    ConcurrentMap<String, Boolean> asynchronousLoadingStates = new ConcurrentHashMap<>()
    ConcurrentMap<String, Class> typeChoicesToDomainClass = new ConcurrentHashMap<>()
    com.intellij.openapi.project.Project project

    private final ArrayList<JComponent> componentListGeneric = [
            searchBox, workspaceChoices
    ]
    private final ArrayList<JComponent> componentListWorkspace = [
            typeChoices, projectChoices, formattedIDCheckBox, nameCheckBox,
            descriptionCheckBox, searchButton, resultsTable
    ]
    private final ArrayList<JComponent> componentListInteractive = componentListGeneric + componentListWorkspace

    SearchWindowImpl() {
        super()
        CacheManager.instance.addObserver(this)
    }

    public void createToolWindowContent(com.intellij.openapi.project.Project project, ToolWindow toolWindow) {
        this.project = project
        myToolWindow = toolWindow
        Content content = getContentFactory().createContent(myToolWindowContent, "", false)
        setupWindow()
        toolWindow.getContentManager().addContent(content)
    }

    void setupWindow() {
        setupToolbar()
        setupTable()
        installSearchListener()
        installWorkspaceListener()

        //now kick off asynchronous loading of Rally data
        loadWorkspaces()
    }

    private void loadWorkspaces() {
        SwingService.instance.disableComponents(componentListInteractive)
        showLoadingAnimation('Loading Rally workspaces...')
        setupWorkspaceChoices()
    }

    private void setupToolbar() {
        ActionToolbar toolbar = ActionToolbarFacade.instance.createActionToolbar([
                new AnAction("Refresh Rally configuration", "Reload Rally workspaces and projects", AllIcons.Actions.Refresh) {
                    @Override
                    public void actionPerformed(AnActionEvent e) {
                        CacheManager.instance.clearAllCaches()
                    }
                }
        ])

        toolbarPanel.add(toolbar.getComponent())
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

    @Override
    void update(Observable observable, @Nullable Object o) {
        loadWorkspaces()
    }

    private void installSearchListener() {
        searchButton.addActionListener(new SearchListener(window: this))
        //todo: ?. for test - explore IntelliJ test framework to better handle
        searchPane.rootPane?.setDefaultButton(searchButton)
    }

    private void installWorkspaceListener() {
        workspaceChoices.addItemListener(new ItemListener() {
            @Override
            void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.stateChange == ItemEvent.SELECTED) {
                    SwingService.instance.disableComponents(componentListWorkspace)
                    showLoadingAnimation('Loading workspace data...')
                    asynchronousLoadingStates['setup'] = false

                    String workspaceRef = getSelectedWorkspaceRef()
                    if (workspaceRef) {
                        setupLabels(workspaceRef)
                        setupTypeChoices(workspaceRef)
                        setupProjectChoices(workspaceRef)
                    }

                    flagAsynchronousLoadCompleted('setup')
                }
            }
        })
    }

    private void setupWorkspaceChoices() {
        workspaceChoices.model = new DefaultComboBoxModel()

        Closure<List<Workspace>> call = {
            return WorkspaceCacheService.instance.cachedWorkspaces
        }

        FutureCallback<List<Workspace>> callback = new ErrorMessageFutureCallback<List<Workspace>>() {
            void onSuccess(List<Workspace> workspaces) {
                SwingService.instance.queueForUiThread {
                    workspaces.each { workspace ->
                        workspaceChoices.addItem(new WorkspaceItem(workspace: workspace))
                    }
                    SwingService.instance.enableComponents(componentListGeneric)
                    setStatus('Loaded workspaces')
                }
            }

            void onFailure(Throwable error) {
                super.onFailure(error)
                setStatus('Failed to load workspaces')
                //todo: need a refresh/try again button
                SwingService.instance.enableComponents(componentListGeneric)
            }
        }

        AsyncService.instance.schedule(call, callback)
    }

    private void setupLabels(String workspaceRef) {
        asynchronousLoadingStates['labels'] = false
        Closure<TypeDefinition> call = {
            TypeDefinitionCacheService.instance.getTypeDefinition(PROJECT.typeDefinitionElementName, workspaceRef)
        }

        FutureCallback<TypeDefinition> callback = new ErrorMessageFutureCallback<TypeDefinition>() {
            void onSuccess(TypeDefinition typeDefinition) {
                SwingService.instance.queueForUiThread {
                    projectLabel.text = typeDefinition.displayName
                    ((DefaultTableModel) resultsTable.model).columnIdentifiers = ['Formatted ID', 'Name', 'Last Updated', 'Type', typeDefinition.displayName]
                    //((DefaultTableModel) resultsTable.model).columnIdentifiers = ['Formatted ID', 'Name', 'Last Updated', 'Type', "Debugging ${typeDefinition.objectID}"]
                }
                flagAsynchronousLoadCompleted('labels')
            }

            void onFailure(Throwable error) {
                super.onFailure(error)
                flagAsynchronousLoadCompleted('labels')
            }
        }

        AsyncService.instance.schedule(call, callback)
    }

    private void setupTypeChoices(String workspaceRef) {
        typeChoices.model = new DefaultComboBoxModel([''].toArray())

        [DEFECT, TASK, HIERARCHICAL_REQUIREMENT].each { endpoint ->
            asynchronousLoadingStates["typeChoice_${endpoint}"] = false
            Closure<TypeDefinition> call = {
                TypeDefinitionCacheService.instance.getTypeDefinition((String) endpoint['typeDefinitionElementName'], workspaceRef)
            }

            FutureCallback<TypeDefinition> callback = new ErrorMessageFutureCallback<TypeDefinition>() {
                void onSuccess(TypeDefinition typeDefinition) {
                    SwingService.instance.queueForUiThread {
                        asynchronousLoadingStates["typeChoice_${endpoint}"] = true
                        typeChoicesToDomainClass[typeDefinition.displayName] = endpoint.domainClass
                        SwingService.instance.insertChoiceAlphabetically(typeDefinition.displayName, typeChoices)
                    }

                    flagAsynchronousLoadCompleted("typeChoice_${endpoint}")
                }

                void onFailure(Throwable error) {
                    super.onFailure(error)
                    flagAsynchronousLoadCompleted("typeChoice_${endpoint}")
                }
            }

            AsyncService.instance.schedule(call, callback)
        }
    }

    private void setupProjectChoices(String workspaceRef) {
        asynchronousLoadingStates['projectChoices'] = false
        Closure<List<Project>> call = {
            return ProjectCacheService.instance.getCachedProjects(workspaceRef)
        }

        FutureCallback<List<Project>> callback = new ErrorMessageFutureCallback<List<Project>>() {
            void onSuccess(List<Project> projects) {
                SwingService.instance.queueForUiThread {
                    projectChoices.model = new DefaultComboBoxModel()
                    projectChoices.addItem(new ProjectItem(project: new Project(name: '')))
                    projects.each {
                        projectChoices.addItem(new ProjectItem(project: it))
                    }

                    flagAsynchronousLoadCompleted('projectChoices')
                }
            }

            void onFailure(Throwable error) {
                super.onFailure(error)
                flagAsynchronousLoadCompleted('projectChoices')
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
        typeChoicesToDomainClass[typeChoices.selectedItem as String] ?: Artifact
    }

    void flagAsynchronousLoadCompleted(String key) {
        asynchronousLoadingStates[key] = true
        boolean ready = asynchronousLoadingStates.inject(true, { acc, k, value ->
            acc && value
        })
        if (ready) {
            setStatus('Loaded Rally configuration')
            SwingService.instance.enableComponents(componentListInteractive)
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

    String getSelectedWorkspaceRef() {
        ((WorkspaceItem) workspaceChoices.selectedItem).workspace._ref
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

    static class WorkspaceItem {
        @Delegate
        Workspace workspace

        @Override
        String toString() {
            workspace.name
        }
    }

}
