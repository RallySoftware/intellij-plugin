package com.rallydev.intellij.config

import com.rallydev.intellij.BaseContainerSpec
import spock.lang.Unroll

class RallyConfigFormSpec extends BaseContainerSpec {

    def "Form correctly initialized after createComponent"() {
        given:
        RallyConfigForm form = new RallyConfigForm()

        when:
        form.createComponent()

        then:
        form.url.text == config.url
        form.userName.text == config.userName
        form.password.text == RallyConfigForm.PASSWORD_PLACEHOLDER
        form.rememberPassword.selected == config.rememberPassword
    }

    def "isModified false when initially loaded"() {
        given:
        RallyConfigForm form = new RallyConfigForm()
        form.createComponent()

        expect:
        !form.modified
    }

    @Unroll
    def "isModified changes with field values"() {
        given:
        RallyConfigForm form = new RallyConfigForm()
        form.createComponent()

        when:
        form.rememberPassword.selected = !form.rememberPassword.selected

        then:
        form.isModified()

        when:
        form[field].text = newValue

        then:
        form.isModified()

        where:
        field << ['url', 'userName', 'password']
        newValue << ['http://yahoo.com', 'bob', 'newPassword']
    }

    def "Apply changes config values"() {
        given:
        String url = 'http://yahoo'
        String userName = 'newUserName'
        String password = 'newPassword'
        Boolean rememberPassword = true

        and:
        RallyConfigForm form = new RallyConfigForm()
        form.createComponent()
        form.url.text = url
        form.userName.text = userName
        form.password.text = password
        form.rememberPassword.selected = rememberPassword

        when:
        form.apply()

        then:
        config.url == url
        config.userName == userName
        config.password == password
        config.rememberPassword == rememberPassword
    }

    def "Reset sets to initial config values"() {
        RallyConfigForm form = new RallyConfigForm()
        form.createComponent()
        form.url.text = 'http://yahoo.com'
        form.userName.text = 'newUserName'
        form.password.text = 'newPassword'
        form.rememberPassword.selected = false

        when:
        form.reset()

        then:
        form.url.text == config.url
        form.userName.text == config.userName
        form.rememberPassword.selected == config.rememberPassword

        form.password.text == RallyConfigForm.PASSWORD_PLACEHOLDER
        !form.passwordChanged
    }

    def "Configed password is cleared when applying without remember"() {
        given:
        RallyConfigForm form = new RallyConfigForm()
        form.createComponent()
        form.rememberPassword.selected = false

        expect:
        config.password

        when:
        form.apply()

        then:
        !config.password
    }

    def "toggle password sets password field's enabled state based on remember checkbox"() {
        given:
        RallyConfigForm form = new RallyConfigForm()
        form.createComponent()

        when:
        form.rememberPassword.selected = true
        form.togglePassword()

        then:
        form.password.enabled

        when:
        form.rememberPassword.selected = false
        form.togglePassword()

        then:
        !form.password.enabled
    }

}
