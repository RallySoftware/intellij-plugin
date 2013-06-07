package com.rallydev.intellij.task

import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.QueryBuilder
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.dao.GenericDao
import com.rallydev.intellij.wsapi.domain.Artifact
import com.rallydev.intellij.wsapi.domain.Defect
import com.rallydev.intellij.wsapi.domain.Requirement

import java.text.SimpleDateFormat

import static com.rallydev.intellij.wsapi.QueryBuilder.Operator.gt

class WsapiQuery {

    RallyClient client

    GenericDao<Artifact> artifactDao
    GenericDao<Defect> defectDao
    GenericDao<Requirement> requirementDao

    public WsapiQuery(RallyClient client) {
        this.client = client
        artifactDao = new GenericDao<Artifact>(Artifact)
        defectDao = new GenericDao<Defect>(Defect)
        requirementDao = new GenericDao<Requirement>(Requirement)
    }

    Collection<RallyTask> findTasks(String keyword, int max, long since) {
        QueryBuilder query = new QueryBuilder()
        if (keyword) {
            query.withKeyword(keyword)
        }
        if (since) {
            String date = new SimpleDateFormat(ApiResponse.RALLY_DATE_FORMAT).format(new Date(since))
            query.withConjunction('LastUpdateDate', gt, date)
        }

        List<RallyTask> rallyTasks = []
        defectDao.find(query, max).each {
            rallyTasks << new RallyTask(it)
        }
        requirementDao.find(query, max).each {
            rallyTasks << new RallyTask(it)
        }

        return rallyTasks
    }

    RallyTask findTask(String id) {
        new RallyTask(artifactDao.findById(id))
    }

}
