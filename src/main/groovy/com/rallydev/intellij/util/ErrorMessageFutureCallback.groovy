package com.rallydev.intellij.util

import com.google.common.util.concurrent.FutureCallback
import com.intellij.ide.DataManager
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.AsyncResult

abstract class ErrorMessageFutureCallback<T> implements FutureCallback<T> {

    @Lazy
    private static final NotificationGroup RALLY_NOTIFICATION = NotificationGroup.balloonGroup("Rally")

    @Override
    void onFailure(Throwable throwable) {
        switch (throwable.class) {
            default:
                showError(
                        'Error communicating with Rally',
                        "Querying Rally failed; check the  ${throwable.message}"
                )
        }
    }

    private void showError(String title, String content) {
        DataManager.getInstance().getDataContextFromFocus().doWhenDone(new AsyncResult.Handler<DataContext>() {
            @Override
            void run(DataContext dataContext) {
                Project project = DataKeys.PROJECT.getData(dataContext);

                def thing = RALLY_NOTIFICATION.createNotification(title, content, NotificationType.ERROR, null)
                thing.notify(project)
            }
        })
    }

}
