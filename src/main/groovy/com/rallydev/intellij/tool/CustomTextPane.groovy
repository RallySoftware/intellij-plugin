package com.rallydev.intellij.tool

import javax.swing.JTextPane
import javax.swing.event.DocumentListener
import javax.swing.text.Document

/**
 * Store document listeners so that when new documents are generated due to content type changes
 * the listener can be added to the same document
 */
class CustomTextPane extends JTextPane {

    List<DocumentListener> listeners = []

    void addDocumentListener(DocumentListener listener) {
        if (document) {
            document.addDocumentListener(listener)
        }
        listeners << listener
    }

    @Override
    void setDocument(Document document) {
        if (document) {
            listeners.each { listener ->
                document.addDocumentListener(listener)
            }
        }
        super.setDocument(document)
    }

}
