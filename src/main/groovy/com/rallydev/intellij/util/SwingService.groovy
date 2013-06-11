package com.rallydev.intellij.util

import com.intellij.openapi.components.ServiceManager

import javax.swing.SwingUtilities

class SwingService {

    static SwingService getInstance() {
        return ServiceManager.getService(SwingService.class);
    }

    void doInUiThread(Closure change) {
        if (SwingUtilities.isEventDispatchThread()) {
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
