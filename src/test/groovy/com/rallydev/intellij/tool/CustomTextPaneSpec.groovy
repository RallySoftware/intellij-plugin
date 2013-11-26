package com.rallydev.intellij.tool

import spock.lang.Specification

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class CustomTextPaneSpec extends Specification {

    def "when content type is changed then document listener is preserved"() {
        given:
        int eventFires = 0
        DocumentListener listener = new DocumentListener() {
            void insertUpdate(DocumentEvent documentEvent) {
                ++eventFires
            }

            @Override
            void removeUpdate(DocumentEvent documentEvent) {
                ++eventFires
            }

            @Override
            void changedUpdate(DocumentEvent documentEvent) {
                ++eventFires
            }
        }

        CustomTextPane textPane = new CustomTextPane()
        textPane.addDocumentListener(listener)

        expect:
        !eventFires

        when:
        textPane.document.insertString(0, 'hi', null)

        then:
        eventFires == 1

        when:
        textPane.contentType = 'text/html'
        textPane.document.insertString(0, 'hi', null)

        then:
        eventFires == 2

        when:
        textPane.contentType = 'text/plain'
        textPane.document.insertString(0, 'hi', null)

        then:
        eventFires == 3
    }

}
