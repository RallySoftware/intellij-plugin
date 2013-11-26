package com.rallydev.intellij.tool

import javax.swing.JEditorPane
import java.awt.KeyboardFocusManager
import java.awt.event.FocusEvent

/**
 * Hack to get around fact that I couldn't find a hook into a post-init in the API
 * Probably something in the API that would work to call "KeyboardFocusManager.currentKeyboardFocusManager.clearGlobalFocusOwner()",
 * but setup isn't the place.
 */
class PlainTextEditFocusListenerIgnoreFirst extends PlanTextEditFocusListener {

    Boolean firstFocus = true

    @Override
    void focusGained(FocusEvent focusEvent) {
        if(firstFocus) {
            firstFocus = false
            JEditorPane component = ((JEditorPane)focusEvent.component)

            contentTypeBeforeFocusEdit = component.contentType
            KeyboardFocusManager.currentKeyboardFocusManager.clearGlobalFocusOwner()
        } else {
            super.focusGained(focusEvent)
        }
    }

}
