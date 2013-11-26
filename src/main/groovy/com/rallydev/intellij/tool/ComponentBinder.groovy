package com.rallydev.intellij.tool

import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class ComponentBinder {

    def bean

    ComponentBinder(def bean) {
        this.bean = bean
    }

    void bind(JLabel component, String property) {
        component.addPropertyChangeListener('text', new PropertyChangeListener() {
            @Override
            void propertyChange(PropertyChangeEvent event) {
                bean[property] = event.newValue
            }
        })
    }

    void bind(JComboBox component, String property) {
        component.addItemListener(new ItemListener() {
            @Override
            void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    bean[property] = event.item
                }
            }
        })
    }

    void bind(CustomTextPane component, String property) {
        component.addDocumentListener(new DocumentListener() {
            @Override
            void insertUpdate(DocumentEvent documentEvent) {
                onChange()
            }

            @Override
            void removeUpdate(DocumentEvent documentEvent) {
                onChange()
            }

            @Override
            void changedUpdate(DocumentEvent documentEvent) {
                onChange()
            }

            void onChange() {
                bean[property] = component.text
            }

        })
    }

}
