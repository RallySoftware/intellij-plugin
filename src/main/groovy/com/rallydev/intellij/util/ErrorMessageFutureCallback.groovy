package com.rallydev.intellij.util

import com.google.common.util.concurrent.FutureCallback

abstract class ErrorMessageFutureCallback<T> implements FutureCallback<T> {

    @Override
    void onFailure(Throwable throwable) {
        switch (throwable.class) {
            default:
                IdeNotification.showError(
                        'Error communicating with Rally', """
<dl style="margin: 0">
    <dt>Querying Rally failed. Error:</dt>
    <dd>${throwable.message}</dd>
</dl>
"""
                )
        }
    }

}
