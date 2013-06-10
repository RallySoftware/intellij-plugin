package com.rallydev.intellij.tool

import com.rallydev.intellij.wsapi.Search
import com.rallydev.intellij.wsapi.domain.Artifact

import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class SearchListener implements ActionListener, Runnable {
    DefaultTableModel tableModel
    List<Artifact> results
    RallyToolWindowImpl window
    Search search = new Search()

    @Override
    void actionPerformed(ActionEvent actionEvent) {
        Thread.start { doActionPerformed() }
    }

    void doActionPerformed() {
        search.with {
            term = window.searchTerm
            searchAttributes = window.searchAttributes
            domainClass = window.selectedType
            project = window.selectedProject
        }

        results = search.doSearch()
        SwingUtilities.invokeLater(this)
    }

    void run() {
        tableModel.rowCount = 0
        window.searchResults = new HashMap<>()
        results.each { Artifact result ->
            window.searchResults[result.formattedID] = result
            tableModel.addRow(
                    [result.formattedID, result.name, result.description, result._type, result.projectName].toArray()
            )
        }
    }
}
