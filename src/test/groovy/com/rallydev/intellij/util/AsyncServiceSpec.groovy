package com.rallydev.intellij.util

import com.google.common.util.concurrent.FutureCallback
import com.rallydev.intellij.BaseContainerSpec
import spock.util.concurrent.BlockingVariable

class AsyncServiceSpec extends BaseContainerSpec {

    def "service adds callback and executes callable"() {
        BlockingVariable done = new BlockingVariable()

        given:
        AsyncService asyncService = new AsyncService()

        when:
        asyncService.schedule(
                {
                    done.set(true)
                    return ""
                },
                new FutureCallback<String>() {
                    @Override
                    void onSuccess(String v) {
                        done.set(true)
                    }

                    @Override
                    void onFailure(Throwable throwable) {}
                }
        )

        then:
        done.get()
    }

}
