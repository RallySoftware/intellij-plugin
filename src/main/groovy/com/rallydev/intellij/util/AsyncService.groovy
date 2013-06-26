package com.rallydev.intellij.util

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListenableFutureTask
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class AsyncService {

    public static AsyncService getInstance() {
        ServiceManager.getService(AsyncService)
    }

    public <K> ListenableFuture<K> schedule(@NotNull Closure<K> callable, @Nullable FutureCallback<K> callback = null) {
        ListenableFutureTask<K> task = ListenableFutureTask.create(callable)
        if (callback) {
            Futures.addCallback(task, callback)
        }
        ApplicationManager.application.executeOnPooledThread(task)

        task
    }

    public <A, B> ListenableFuture<B> chain(@NotNull ListenableFuture<A> future,
                                            @NotNull Closure<B> callable,
                                            @Nullable FutureCallback<B> callback) {
        return schedule({ callable.call(future.get()) }, callback)
    }

}
