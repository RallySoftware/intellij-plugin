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

    @Override
    void onFailure(Throwable throwable) {
        switch (throwable.class) {
            default:
                IdeNotification.showError(
                        'Error communicating with Rally',
                        "Querying Rally failed; check the  ${throwable.message}"
                )
        }
    }

}
