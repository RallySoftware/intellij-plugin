package com.rallydev.intellij.tool

import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.Search
import com.rallydev.intellij.wsapi.domain.Artifact

import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class SearchListener implements ActionListener, Runnable {
    List<Artifact> results
    SearchWindowImpl window
    Search search = new Search()

    @Override
    void actionPerformed(ActionEvent actionEvent) {
        RallyClient.instance.ensurePasswordLoaded()
        Thread.start { doActionPerformed() }
    }

    void doActionPerformed() {
        search.with {
            term = window.searchTerm
            searchAttributes = window.searchAttributes
            domainClass = window.selectedType
            project = window.selectedProject
        }

        window.startLoadingAnimation()
        window.enableControls(false)
        try {
            results = search.doSearch()
            window.setStatus("Loaded ${results.size()} artifacts")
        } catch (Exception e) {
            window.setStatus('Error communicating with Rally')
        } finally {
            window.enableControls(true)
        }

        SwingUtilities.invokeLater(this)
    }

    void run() {
        window.clear()
        results.each {
            window.addResult(it)
        }
    }

}
