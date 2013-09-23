package com.rallydev.intellij.wsapi

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.ListenableFuture
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Artifact

import static com.rallydev.intellij.wsapi.QueryBuilder.Operator.contains
import static com.rallydev.intellij.wsapi.QueryBuilder.Operator.eq

//todo: I don't like this class, refactor into some other way
class Search {
    String term
    List<String> searchAttributes = []
    String project
    Class domainClass
    String workspaceRef = null

    ListenableFuture<List> doSearch(FutureCallback<ResultList> callback , int pageSize = 30) {
        QueryBuilder queryBuilder = new QueryBuilder()

        if (term) {
            searchAttributes.each { attribute ->
                queryBuilder.withDisjunction(attribute, contains, term)
            }
        }
        if (project) {
            queryBuilder.withConjunction('Project', eq, project)
        }

        GenericDao<Artifact> dao = new GenericDao<>(domainClass, workspaceRef)
        dao.findAsync(callback, queryBuilder, 'LastUpdateDate&dir=DESC', pageSize)
    }

}
