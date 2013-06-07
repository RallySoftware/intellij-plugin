package com.rallydev.intellij.task

import com.rallydev.intellij.BaseContainerSpec

class RallyRepositorySpec extends BaseContainerSpec {

    def "Check that clone returns identical repo"() {
        given:
        String workdspaceId = '1122'

        and:
        RallyRepository sourceRepository = new RallyRepository(
                workspaceId: workdspaceId
        )

        when:
        def clonedRepository = sourceRepository.clone()

        then:
        clonedRepository instanceof RallyRepository

        and:
        clonedRepository.workspaceId == workdspaceId
    }

}
