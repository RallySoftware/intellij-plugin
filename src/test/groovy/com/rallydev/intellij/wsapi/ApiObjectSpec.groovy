package com.rallydev.intellij.wsapi

import spock.lang.Specification

class ApiObjectSpec extends Specification {

    def "Check toString implementation"() {
        expect:
        (ApiEndpoint.WORKSPACE).toString() == 'workspace'

        and:
        (ApiEndpoint.HIERARCHICAL_REQUIREMENT).toString() == 'hierarchicalrequirement'
    }

}
