package com.rallydev.intellij.wsapi

import com.rallydev.intellij.BaseContainerSpec
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.auth.InvalidCredentialsException
import org.apache.commons.httpclient.methods.GetMethod

class RallyClientSpec extends BaseContainerSpec {

    def "properties come from config instance"() {
        given:
        RallyClient client = new RallyClient()

        expect:
        client.server.toString() == config.url
        client.username == config.userName
        client.password == config.password
    }

    def "makeRequests stores password prompt results"() {
        given:
        RallyClient client = Spy(RallyClient)
        client.executeMethod(_) >> { HttpStatus.SC_OK }
        client.promptForPassword() >> { 'queue' }
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> { '{}'}
            }
        }

        and:
        config.password = null

        when:
        client.makeRequest(Mock(GetRequest))

        then:
        config.password == 'queue'
    }

    def "makeRequests does not prompt for password when set in config"() {
        given:
        RallyClient client = Spy(RallyClient)
        client.executeMethod(_) >> { HttpStatus.SC_OK }
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> { '{}'}
            }
        }

        and:
        config.password = 'monkey'

        when:
        client.makeRequest(Mock(GetRequest))

        then:
        0 * client.promptForPassword() >> { }
    }

    def "auth failures clear the cached password"() {
        given:
        RallyClient client = Spy(RallyClient)
        client.executeMethod(_) >> { HttpStatus.SC_UNAUTHORIZED }
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> { '{}'}
            }
        }

        when:
        client.makeRequest(Mock(GetRequest))

        then:
        1 * config.clearCachedPassword() >> { }

        and:
        thrown(InvalidCredentialsException)
    }

}
