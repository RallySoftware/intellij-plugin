package com.rallydev.intellij.wsapi

import com.google.common.util.concurrent.FutureCallback
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.util.AsyncService
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpState
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.auth.InvalidCredentialsException
import org.apache.commons.httpclient.methods.GetMethod
import spock.util.concurrent.BlockingVariable

class RallyClientSpec extends BaseContainerSpec {

    def "properties come from config instance"() {
        given:
        RallyClient client = new RallyClient(Mock(AsyncService))

        expect:
        client.server.toString() == config.url
        client.username == config.userName
        client.password == config.password
    }

    def "makeRequest stores password prompt results"() {
        given:
        String password = 'first'

        RallyClient client = Spy(RallyClient, constructorArgs: [new AsyncService()])
        client.httpClient = Mock(HttpClient)
        client.httpClient.getState() >> { Mock(HttpState) }
        client.httpClient.executeMethod(_) >> { HttpStatus.SC_OK }
        client.promptForPassword() >> { password }
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> { '{}' }
            }
        }

        and:
        config.password = null

        when:
        client.makeRequest(Mock(GetRequest))

        then:
        config.password == password

        when:
        config.password = null
        password = 'second'
        client.promptForPassword() >> { password }

        and:
        client.makeRequest(Mock(GetRequest), Mock(FutureCallback))

        then:
        config.password == password
    }

    def "makeRequests does not prompt for password when set in config"() {
        given:
        RallyClient client = Spy(RallyClient, constructorArgs: [new AsyncService()])
        client.httpClient = Mock(HttpClient)
        client.httpClient.getState() >> { Mock(HttpState) }
        client.httpClient.executeMethod(_) >> { HttpStatus.SC_OK }
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> { '{}' }
            }
        }

        and: 'password is configured'
        config.password = 'monkey'

        when: 'making a request synchronously'
        client.makeRequest(Mock(GetRequest))

        then: 'user is not prompted for password'
        0 * client.promptForPassword() >> {}

        when: 'making a request asynchronously'
        client.makeRequest(Mock(GetRequest), Mock(FutureCallback))

        then: 'user is not prompted for password'
        0 * client.promptForPassword() >> {}
    }

    def "auth failure clears the cached password"() {
        BlockingVariable done = new BlockingVariable()

        given:
        RallyClient client = Spy(RallyClient, constructorArgs: [new AsyncService()])
        client.httpClient = Mock(HttpClient)
        client.httpClient.getState() >> { Mock(HttpState) }
        client.httpClient.executeMethod(_) >> { HttpStatus.SC_UNAUTHORIZED }
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> { '{}' }
            }
        }

        when: 'an async request is made'
        client.makeRequest(Mock(GetRequest), new FutureCallback<ApiResponse>() {
            @Override
            void onSuccess(ApiResponse v) {}

            @Override
            void onFailure(Throwable throwable) { done.set(true) }
        })

        then: 'the failure callback is called and the password is cleared'
        done.get()
        !config.password

        when: 'a synchronous request is made'
        config.password = 'monkey'
        client.makeRequest(Mock(GetRequest))

        then: 'the password is cleared'
        !config.password

        and: 'an exception is thrown'
        thrown(InvalidCredentialsException)
    }

}
