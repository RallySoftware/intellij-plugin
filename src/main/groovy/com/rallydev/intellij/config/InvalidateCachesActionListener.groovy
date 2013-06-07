package com.rallydev.intellij.config

import com.rallydev.intellij.wsapi.cache.ProjectCacheService

import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class InvalidateCachesActionListener implements ActionListener {

    @Override
    void actionPerformed(ActionEvent actionEvent) {
        ProjectCacheService.getInstance().clear()
    }

}
