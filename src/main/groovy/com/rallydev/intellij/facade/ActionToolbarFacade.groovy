package com.rallydev.intellij.facade

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ServiceManager

class ActionToolbarFacade {

    public static ActionToolbarFacade getInstance() {
        return ServiceManager.getService(this)
    }

    public ActionToolbar createActionToolbar(Collection<AnAction> actions) {
        final DefaultActionGroup group = new DefaultActionGroup()
        actions.each { action ->
            group.add(action)
        }
        final ActionToolbar toolbar = ActionManager.instance.createActionToolbar(ActionPlaces.UNKNOWN, group, false)
        return toolbar
    }

}
