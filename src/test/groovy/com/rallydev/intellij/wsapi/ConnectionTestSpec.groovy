package com.rallydev.intellij.wsapi

import com.google.common.util.concurrent.ListenableFuture
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.SpecUtils

class ConnectionTestSpec extends BaseContainerSpec {

    def "Ensure doTest tries to make connection"() {
        given:
        RallyClient rallyClient = Mock(RallyClient)
        registerComponentInstance(RallyClient.name, rallyClient)

        when:
        new ConnectionTest().doTest()

        then:
        1 * rallyClient.makeRequest(_) >> {
            Mock(ListenableFuture) {
                get() >> { new ApiResponse(SpecUtils.minimalResponseJson) }
            }
        }
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
