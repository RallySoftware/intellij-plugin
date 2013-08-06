package com.rallydev.intellij.util

import com.google.common.util.concurrent.FutureCallback

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
