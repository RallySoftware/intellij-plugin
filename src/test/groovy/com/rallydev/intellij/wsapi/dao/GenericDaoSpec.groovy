package com.rallydev.intellij.wsapi.dao

import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.GetRequest
import com.rallydev.intellij.wsapi.QueryBuilder
import com.rallydev.intellij.wsapi.RallyClient
import com.rallydev.intellij.wsapi.domain.Requirement

class GenericDaoSpec extends BaseContainerSpec {

    def "findById makes request and parses response"() {
        given:
        RallyClient rallyClient = Mock(RallyClient)
        rallyClient.makeRequest(_ as GetRequest) >> {
            new ApiResponse(GenericDaoSpec.classLoader.getResourceAsStream('single_requirement.json').text)
        }
        registerComponentInstance(RallyClient.name, rallyClient)

        when:
        GenericDao requirementDao = new GenericDao<Requirement>(Requirement)
        Requirement requirement = requirementDao.findById('14345')

        then:
        requirement.objectID == '14345'
    }

    def "find with NO query makes request and parses response"() {
        RallyClient rallyClient = Mock(RallyClient)
        GetRequest madeRequest = null
        rallyClient.makeRequest(_ as GetRequest) >> { GetRequest request ->
            madeRequest = request
            new ApiResponse(GenericDaoSpec.classLoader.getResourceAsStream('multiple_requirements.json').text)
        }
        registerComponentInstance(RallyClient.name, rallyClient)

        when:
        GenericDao requirementDao = new GenericDao<Requirement>(Requirement)
        List<Requirement> requirements = requirementDao.find()

        then:
        !madeRequest.getUrl('http://www.test.com'.toURL()).contains('query=')

        and:
        requirements.size() == 3

        and:
        requirements[0].objectID == '14345'
        requirements[1].objectID == '14389'
        requirements[2].objectID == '14579'
    }

    def "find with query makes request and parses response"() {
        given:
        RallyClient rallyClient = Mock(RallyClient)
        GetRequest madeRequest = null
        rallyClient.makeRequest(_ as GetRequest) >> { GetRequest request ->
            madeRequest = request
            new ApiResponse(GenericDaoSpec.classLoader.getResourceAsStream('multiple_requirements.json').text)
        }
        registerComponentInstance(RallyClient.name, rallyClient)

        QueryBuilder queryBuilder = new QueryBuilder()
        queryBuilder.withKeyword("hello")

        when:
        GenericDao requirementDao = new GenericDao<Requirement>(Requirement)
        List<Requirement> requirements = requirementDao.find(queryBuilder)

        then:
        madeRequest.getUrl('http://www.test.com'.toURL()).contains('query=')

        and:
        requirements.size() == 3

        and:
        requirements[0].objectID == '14345'
        requirements[1].objectID == '14389'
        requirements[2].objectID == '14579'
    }

}
