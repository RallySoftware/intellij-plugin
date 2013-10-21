package com.rallydev.intellij.tool

import com.google.common.util.concurrent.FutureCallback
import com.intellij.openapi.application.ApplicationManager
import com.rallydev.intellij.util.ErrorMessageFutureCallback
import com.rallydev.intellij.wsapi.client.RallyClient
import com.rallydev.intellij.wsapi.ResultList
import com.rallydev.intellij.wsapi.Search
import com.rallydev.intellij.wsapi.domain.Artifact

import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class SearchListener implements ActionListener {
    SearchWindowImpl window
    Search search = new Search()

    @Override
    void actionPerformed(ActionEvent actionEvent) {
        RallyClient.instance.ensurePasswordLoaded()
        Thread.start { doActionPerformed() }
    }

    void doActionPerformed() {
        window.showLoadingAnimation()
        search.with {
            term = window.searchTerm
            searchAttributes = window.searchAttributes
            domainClass = window.selectedType
            project = window.selectedProject
            workspaceRef = window.selectedWorkspaceRef
        }

        window.enableControls(false)

        search.doSearch(callback)
    }

    FutureCallback<ResultList<Artifact>> getCallback() {
        new ErrorMessageFutureCallback<ResultList<Artifact>>() {
            @Override
            void onSuccess(ResultList<Artifact> results) {
                window.setStatus("Loaded ${results.size()} artifacts")
                window.enableControls(true)
                ApplicationManager.application.invokeLater({
                    window.clear()
                    results.each {
                        window.addResult(it)
                    }
                })
            }

            @Override
            void onFailure(Throwable throwable) {
                super.onFailure(throwable)
                window.setStatus('Error communicating with Rally')
                window.enableControls(true)
            }
        }
    }

}
