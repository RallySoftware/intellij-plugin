package com.rallydev.intellij.util

import com.intellij.ide.DataManager
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.AsyncResult

//todo: service, not static
class IdeNotification {

    @Lazy
    private static final NotificationGroup RALLY_NOTIFICATION = NotificationGroup.balloonGroup("Rally")

    static void showError(String title, String content) {
        doShow(title, content, NotificationType.ERROR)
    }

    static void showWarning(String title, String content) {
        doShow(title, content, NotificationType.WARNING)
    }

    private static void doShow(String title, String content, NotificationType type) {
        NotificationGroup notificationGroup = RALLY_NOTIFICATION
        DataManager.getInstance().getDataContextFromFocus().doWhenDone(new AsyncResult.Handler<DataContext>() {
            @Override
            void run(DataContext dataContext) {
                Project project = DataKeys.PROJECT.getData(dataContext);

                def thing = notificationGroup.createNotification(title, content, type, null)
                thing.notify(project)
            }
        })
    }

}
