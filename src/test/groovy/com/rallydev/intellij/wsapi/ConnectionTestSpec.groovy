package com.rallydev.intellij.wsapi

import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.SpecUtils
import com.rallydev.intellij.wsapi.client.RallyClient

class ConnectionTestSpec extends BaseContainerSpec {

    def "Ensure doTest tries to make connection"() {
        given:
        RallyClient rallyClient = Mock(RallyClient)
        registerComponentInstance(RallyClient.name, rallyClient)

        when:
        new ConnectionTest().doTest()

        then:
        1 * rallyClient.makeRequest(_) >> { new ApiResponse(SpecUtils.minimalResponseJson) }
    }

    def "Ensure exception when no response results"() {
        given:
        RallyClient rallyClient = Mock(RallyClient) {
            1 * makeRequest(_) >> { null }
        }
        registerComponentInstance(RallyClient.name, rallyClient)

        when:
        new ConnectionTest().doTest()

        then:
        thrown(Exception)
    }

}
