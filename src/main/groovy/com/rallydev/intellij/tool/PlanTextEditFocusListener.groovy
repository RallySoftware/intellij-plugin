package com.rallydev.intellij.tool

import javax.swing.JEditorPane
import java.awt.event.FocusEvent
import java.awt.event.FocusListener

class PlanTextEditFocusListener implements FocusListener {

    String contentTypeBeforeFocusEdit

    @Override
    void focusGained(FocusEvent focusEvent) {
        JEditorPane component = ((JEditorPane)focusEvent.component)
        contentTypeBeforeFocusEdit = component.contentType
        String currentText = component.text
        component.contentType = 'text/plain'
        component.text = currentText
    }

    @Override
    void focusLost(FocusEvent focusEvent) {
        JEditorPane component = ((JEditorPane)focusEvent.component)
        String currentText = component.text
        component.contentType = contentTypeBeforeFocusEdit
        component.text = currentText
    }

}
