package com.rallydev.intellij.util

import com.rallydev.intellij.BaseContainerSpec

import javax.swing.*

class SwingServiceSpec extends BaseContainerSpec {

    def setup() {
        registerComponentImplementation(SwingService)
    }

    def "disableComponents sets enabled false"() {
        given:
        def components = [Mock(JComponent), Mock(JComponent)]

        when:
        SwingService.instance.disableComponents(components)

        then:
        1 * components[0].setEnabled(false)
        1 * components[1].setEnabled(false)
    }

    def "enableComponents sets enabled true"() {
        given:
        def components = [Mock(JComponent), Mock(JComponent)]

        when:
        SwingService.instance.enableComponents(components)

        then:
        1 * components[0].setEnabled(true)
        1 * components[1].setEnabled(true)
    }

}
