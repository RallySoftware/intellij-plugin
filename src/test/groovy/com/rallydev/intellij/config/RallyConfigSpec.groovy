package com.rallydev.intellij.config

import com.rallydev.intellij.PlatformSpecification
import org.jdom.Element
import spock.lang.Ignore
import spock.lang.Specification

//TODO: Problems with base Intellij test case, commented out test requiring password safe

//class RallyConfigSpec extends PlatformSpecification {
class RallyConfigSpec extends Specification {

    def "loadState correctly copies values"() {
        given:
        def userName = 'matt'
        def url = 'http://localhost:70001/'

        and:
        Element element = new Element(RallyConfig.RALLY_CONFIG_TAG)
        element.setAttribute(RallyConfig.URL, url)
        element.setAttribute(RallyConfig.USER_NAME, userName)
        element.setAttribute(RallyConfig.REMEMBER_PASSWORD, String.valueOf(true))

        when:
        RallyConfig loadedConfig = new RallyConfig()
        loadedConfig.loadState(element)

        then:
        loadedConfig.userName == userName
        loadedConfig.url == url
        loadedConfig.rememberPassword
    }

    def "getState returns element with config data "() {
        given:
        RallyConfig rallyConfig = new RallyConfig(url: 'http://yahoo.com', userName: 'merissa', rememberPassword: true)

        when:
        Element xmlConfig = rallyConfig.getState()

        then:
        xmlConfig.getAttributeValue(RallyConfig.URL) == rallyConfig.url
        xmlConfig.getAttributeValue(RallyConfig.USER_NAME) == rallyConfig.userName
        Boolean.valueOf(xmlConfig.getAttributeValue(RallyConfig.REMEMBER_PASSWORD)) == rallyConfig.rememberPassword
    }

    @Ignore
    def "password set and get succeeds"() {
        given:
        RallyConfig rallyConfig = new RallyConfig(url: 'http://yahoo.com', userName: 'merissa', rememberPassword: true)

        expect:
        !rallyConfig.getPassword()

        when:
        rallyConfig.password = 'abc123'

        then:
        rallyConfig.getPassword() == 'abc123'
    }

    @Ignore
    def "clear password removes password from config"() {
        given:
        RallyConfig rallyConfig = new RallyConfig(url: 'http://yahoo.com', userName: 'merissa', rememberPassword: true)
        rallyConfig.password = 'abc123'

        expect:
        rallyConfig.password

        when:
        rallyConfig.clearPassword()

        then:
        !rallyConfig.password
    }

    @Ignore
    def "store password saved to master password database"() {
        given:
        RallyConfig rallyConfig = new RallyConfig(url: 'http://yahoo.com', userName: 'merissa', rememberPassword: true)

        expect:
        !rallyConfig.getStoredPassword()

        when:
        rallyConfig.setPassword('abc123')
        rallyConfig.storePassword()

        then:
        rallyConfig.getStoredPassword() == 'abc123'
    }

}
