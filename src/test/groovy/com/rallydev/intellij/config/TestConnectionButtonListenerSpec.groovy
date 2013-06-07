package com.rallydev.intellij.config

import com.google.gson.JsonSyntaxException
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TestDialog
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.SpecUtils
import com.rallydev.intellij.wsapi.ApiResponse
import com.rallydev.intellij.wsapi.RallyClient
import org.apache.commons.httpclient.auth.InvalidCredentialsException

class TestConnectionButtonListenerSpec extends BaseContainerSpec {

    List<String> messages
    RallyConfigForm mockForm
    RallyClient mockClient

    def setup() {
        messages = []
        mockForm = Mock(RallyConfigForm)
        Messages.testDialog = new TestDialog() {
            int show(String message) {
                messages << message
                return 0
            }
        }
        mockClient = Mock(RallyClient)

        registerComponentInstance(RallyClient.name, mockClient)
    }

    def "Message on success"() {
        given:
        1 * mockClient.makeRequest(_) >> { new ApiResponse(SpecUtils.minimalResponseJson) }

        and:
        TestConnectionButtonListener listener = new TestConnectionButtonListener(mockForm)

        when:
        listener.actionPerformed(null)

        then:
        messages.first().toLowerCase().contains('success')
    }

    def "Message for auth failure"() {
        given:
        1 * mockClient.makeRequest(_) >> { throw new InvalidCredentialsException() }

        and:
        TestConnectionButtonListener listener = new TestConnectionButtonListener(mockForm)

        when:
        listener.actionPerformed(null)

        then:
        messages.first().toLowerCase().contains('invalid credentials')
    }

    def "Message for bad response"() {
        given:
        1 * mockClient.makeRequest(_) >> { throw new JsonSyntaxException('Wrong') }

        and:
        TestConnectionButtonListener listener = new TestConnectionButtonListener(mockForm)

        when:
        listener.actionPerformed(null)

        then:
        messages.first().toLowerCase().contains('url')
        messages.first().toLowerCase().contains('incorrect')
    }

}
