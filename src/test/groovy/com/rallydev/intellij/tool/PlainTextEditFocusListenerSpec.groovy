package com.rallydev.intellij.tool

import spock.lang.Specification

import javax.swing.JEditorPane
import java.awt.event.FocusEvent


class PlainTextEditFocusListenerSpec extends Specification {

    def 'should switch content type to text/plain on focus gain and restore on loss'() {
        given:
        PlanTextEditFocusListener listener = new PlanTextEditFocusListener()
        JEditorPane pane = new JEditorPane(text: '', contentType: 'text/html')

        and:
        FocusEvent event = Mock(FocusEvent)
        event.component >> pane

        when:
        listener.focusGained(event)

        then:
        pane.contentType == 'text/plain'

        when:
        listener.focusLost(event)

        then:
        pane.contentType == 'text/html'
    }

}
