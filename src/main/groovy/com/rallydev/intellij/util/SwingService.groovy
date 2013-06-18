package com.rallydev.intellij.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager

import javax.swing.*

class SwingService {

    static SwingService getInstance() {
        return ServiceManager.getService(SwingService.class)
    }

    void doInUiThread(Closure change) {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            change()
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                void run() {
                    change()
                }
            })
        }
    }

}
