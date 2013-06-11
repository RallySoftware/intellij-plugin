package com.rallydev.intellij.tool

import com.rallydev.intellij.wsapi.Search
import com.rallydev.intellij.wsapi.domain.Artifact

import javax.swing.SwingUtilities
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class SearchListener implements ActionListener, Runnable {
    List<Artifact> results
    SearchWindowImpl window
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
        window.clear()
        results.each {
            window.addResult(it)
        }
    }
}
