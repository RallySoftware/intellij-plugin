package com.rallydev.intellij.wsapi

import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Artifact

import static com.rallydev.intellij.wsapi.QueryBuilder.Operator.contains

//todo: I don't like this class, refactor into some other way
class Search {
    String term
    RallyClient rallyClient
    List<String> searchAttributes = []
    Class domainClass

    List<Artifact> doSearch(int pageSize = 30) {
        QueryBuilder queryBuilder = new QueryBuilder()
        searchAttributes.each { attribute ->
            queryBuilder.withDisjunction(attribute, contains, term)
        }

        GenericDao<Artifact> dao = new GenericDao<>(rallyClient, domainClass)
        dao.find(queryBuilder, pageSize)
    }

}
