package com.rallydev.intellij.task

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.ui.DocumentAdapter
import com.intellij.util.Consumer
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Workspace

import javax.swing.*
import javax.swing.event.DocumentEvent
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class RepositoryEditorImpl extends RepositoryEditor {
    static final Logger log = Logger.getInstance(RepositoryEditorImpl)

    Project project
    RallyRepository repository
    Consumer<RallyRepository> changeListener

    private boolean applying
    private Document document

    public RepositoryEditorImpl(Project project, RallyRepository repository, Consumer<RallyRepository> changeListener) {
        this.project = project
        this.repository = repository
        this.changeListener = changeListener

        toggleErrorPanel(false)

        createDocumentWithListener(repository)
        loadRallyWorkspaces(repository)
        setComponentValues()

        installListener(workspaces)
        installListener(testField)
    }

    private void loadRallyWorkspaces(RallyRepository repository) {
        try {
            workspaces.addItem(new Workspace(name: 'Select Workspace', objectID: '-1'))
            new GenericDao<Workspace>(Workspace).find().each {
                workspaces.addItem(it)
            }
        } catch (Exception e) {
            log.error(e)
            toggleErrorPanel(true)
        }
    }

    private void createDocumentWithListener(RallyRepository repository) {
        document = EditorFactory.instance.createDocument(repository.commitMessageFormat)
        document.addDocumentListener(new com.intellij.openapi.editor.event.DocumentAdapter() {
            @Override
            public void documentChanged(com.intellij.openapi.editor.event.DocumentEvent e) {
                doApply()
            }
        })
    }

    @Override
    public JComponent createComponent() {
        return editorPanel
    }

    private void setComponentValues() {
        testField.text = repository.testField
        (0..(workspaces.itemCount - 1)).each { i ->
            if (repository.workspaceId == workspaces[i].objectID) {
                workspaces.selectedIndex = i
            }
        }
    }

    private void toggleErrorPanel(boolean showError) {
        errorPanel.visible = showError
        successPanel.visible = !showError
    }

    public void apply() {
        repository.testField = testField.text
        repository.workspaceId = workspaces.selectedItem.objectID

        changeListener.consume(repository)
    }

    //from BaseRepositoryEditor
    protected void installListener(JComboBox comboBox) {
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doApply()
            }
        })
    }

    //from BaseRepositoryEditor
    protected void installListener(JTextField textField) {
        textField.document.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                ApplicationManager.application.invokeLater(new Runnable() {
                    public void run() {
                        doApply()
                    }
                })
            }
        })
    }

    //from BaseRepositoryEditor
    private void doApply() {
        if (!applying) {
            try {
                applying = true
                apply()
            }
            finally {
                applying = false
            }
        }
    }

}
