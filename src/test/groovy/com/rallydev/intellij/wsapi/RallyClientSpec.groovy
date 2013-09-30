package com.rallydev.intellij.wsapi

import com.google.common.util.concurrent.FutureCallback
import com.intellij.util.net.HttpConfigurable
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.util.AsyncService
import org.apache.commons.httpclient.HostConfiguration
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpState
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.auth.AuthScope
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
        client.httpClient.getState() >> Mock(HttpState)
        client.httpClient.executeMethod(_) >> HttpStatus.SC_OK
        client.promptForPassword() >> { password }
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> '{}'
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
        client.promptForPassword() >> password

        and:
        client.makeRequest(Mock(GetRequest), Mock(FutureCallback))

        then:
        config.password == password
    }

    def "makeRequests does not prompt for password when set in config"() {
        given:
        RallyClient client = Spy(RallyClient, constructorArgs: [new AsyncService()])
        client.httpClient = Mock(HttpClient)
        client.httpClient.getState() >> Mock(HttpState)

        and: 'executing client fakes return with empty json'
        client.httpClient.executeMethod(_) >> HttpStatus.SC_OK
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> '{}'
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

    def "unhandled status codes include code in exception message"() {
        given:
        RallyClient client = Spy(RallyClient, constructorArgs: [new AsyncService()])
        client.httpClient = Mock(HttpClient)
        client.httpClient.getState() >> { Mock(HttpState) }

        and: 'executing client fakes return with empty json'
        client.httpClient.executeMethod(_) >> 409
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> '{}'
            }
        }

        when:
        client.makeRequest(Mock(GetRequest))

        then:
        def e = thrown(RuntimeException)
        e.message.contains('409')
    }

    def "auth failure clears the cached password"() {
        BlockingVariable done = new BlockingVariable()

        given:
        RallyClient client = Spy(RallyClient, constructorArgs: [new AsyncService()])
        client.httpClient = Mock(HttpClient)
        client.httpClient.getState() >> { Mock(HttpState) }

        and: 'executing client fakes return with empty json'
        client.httpClient.executeMethod(_) >> HttpStatus.SC_UNAUTHORIZED
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> '{}'
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

    def "proxy is used when globally configured"() {
        given:
        String proxyHost = 'localhost'
        Integer proxyPort = 8080

        and: 'global proxy is configured'
        registerComponentImplementation(HttpConfigurable)
        HttpConfigurable.instance.USE_HTTP_PROXY = true
        HttpConfigurable.instance.PROXY_HOST = proxyHost
        HttpConfigurable.instance.PROXY_PORT = proxyPort

        and:
        RallyClient client = Spy(RallyClient, constructorArgs: [new AsyncService()])
        client.httpClient = Mock(HttpClient)
        client.httpClient.getState() >> Mock(HttpState)

        and: 'executing client fakes return with empty json'
        client.httpClient.executeMethod(_) >> HttpStatus.SC_OK
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> '{}'
            }
        }

        and:
        HostConfiguration hostConfiguration = Mock(HostConfiguration)
        client.httpClient.getHostConfiguration() >> hostConfiguration

        when:
        client.makeRequest(Mock(GetRequest))

        then: 'http client has proxy set'
        1 * hostConfiguration.setProxy(proxyHost, proxyPort)
    }

    def "authenticated proxy is used when globally configured"() {
        given:
        String proxyPassword = 'monkey'
        String proxyUserName = 'bob'

        and: 'global proxy is configured with auth'
        registerComponentInstance(HttpConfigurable.class.name, Mock(HttpConfigurable))
        HttpConfigurable.instance.USE_HTTP_PROXY = true
        HttpConfigurable.instance.PROXY_HOST = 'localhost'
        HttpConfigurable.instance.PROXY_PORT = 8080

        HttpConfigurable.instance.PROXY_AUTHENTICATION = true
        HttpConfigurable.instance.getPlainProxyPassword() >> proxyPassword
        HttpConfigurable.instance.PROXY_LOGIN = proxyUserName

        and:
        RallyClient client = Spy(RallyClient, constructorArgs: [new AsyncService()])
        client.httpClient = Mock(HttpClient)
        client.httpClient.getState() >> Mock(HttpState)

        UsernamePasswordCredentials proxyCredentials = null
        client.httpClient.getState().setProxyCredentials(_, _) >> {AuthScope authscope, UsernamePasswordCredentials credentials ->
            proxyCredentials = credentials
        }

        and: 'executing client fakes return with empty json'
        client.httpClient.executeMethod(_) >> HttpStatus.SC_OK
        client.buildMethod(_ as GetRequest) >> {
            Mock(GetMethod) {
                getResponseBodyAsString() >> '{}'
            }
        }

        and:
        HostConfiguration hostConfiguration = Mock(HostConfiguration)
        client.httpClient.getHostConfiguration() >> hostConfiguration

        when:
        client.makeRequest(Mock(GetRequest))

        then: 'credentials set on client match config'
        proxyCredentials.userName == proxyUserName
        proxyCredentials.password == proxyPassword
    }

}
