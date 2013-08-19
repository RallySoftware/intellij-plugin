package com.rallydev.intellij.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager

import javax.swing.*

class SwingService {

    static SwingService getInstance() {
        return ServiceManager.getService(SwingService.class)
    }

    void doInUiThread(Closure later) {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            later()
        } else {
            queueForUiThread(later)
        }
    }

    void queueForUiThread(Closure later) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            void run() {
                later()
            }
        })
    }

    void insertChoiceAlphabetically(String choice, JComboBox comboBox) {
        int position = 0
        boolean foundPosition = false
        while (position <= comboBox.itemCount && !foundPosition) {
            ++position
            foundPosition = position == comboBox.itemCount ||
                    choice.compareToIgnoreCase((String) comboBox.getItemAt(position)) < 0
        }
        comboBox.insertItemAt(choice, position)
    }

}
