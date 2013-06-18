package com.rallydev.intellij.config

import com.google.gson.JsonSyntaxException
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TestDialog
import com.rallydev.intellij.BaseContainerSpec
import com.rallydev.intellij.wsapi.ConnectionTest
import org.apache.commons.httpclient.auth.InvalidCredentialsException

class TestConnectionButtonListenerSpec extends BaseContainerSpec {

    List<String> messages
    RallyConfigForm form

    ConnectionTest connectionTest

    def setup() {
        messages = []
        form = new RallyConfigForm()
        form.with {
            url.text = 'http://google.com'
            userName.text = 'sbrin'
            password.text = 'glass'
        }

        Messages.testDialog = new TestDialog() {
            int show(String message) {
                messages << message
                return 0
            }
        }

        connectionTest = GroovySpy(ConnectionTest, global: true)
    }

    def "Message on success"() {
        given:
        connectionTest.doTest() >> {}

        and:
        TestConnectionButtonListener listener = new TestConnectionButtonListener(form)

        when:
        listener.actionPerformed(null)

        then:
        messages.first().toLowerCase().contains('success')
    }

    def "Message for auth failure"() {
        given:
        connectionTest.doTest() >> { throw new InvalidCredentialsException() }

        and:
        TestConnectionButtonListener listener = new TestConnectionButtonListener(form)

        when:
        listener.actionPerformed(null)

        then:
        messages.first().toLowerCase().contains('invalid credentials')
    }

    def "Message for bad response"() {
        given:
        connectionTest.doTest() >> { throw new JsonSyntaxException('Wrong') }

        and:
        TestConnectionButtonListener listener = new TestConnectionButtonListener(form)

        when:
        listener.actionPerformed(null)

        then:
        messages.first().toLowerCase().contains('url')
        messages.first().toLowerCase().contains('incorrect')
    }

}
