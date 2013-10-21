package com.rallydev.intellij.wsapi.client

import spock.lang.Specification

class RallyClientConfigurableSpec extends Specification {

    def "configurable client overrides properties"() {
        given:
        String server = 'http://yahoo.com'
        String username = 'merissa'
        String password = 'purple'

        when:
        RallyClient client = new RallyClientConfigurable(server.toURL(), username, password, false)

        then:
        client.server.toString() == server
        client.username == username
        client.password == password
    }

}
