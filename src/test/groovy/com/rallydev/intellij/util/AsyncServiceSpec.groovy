package com.rallydev.intellij.util

import com.google.common.util.concurrent.FutureCallback
import com.rallydev.intellij.BaseContainerSpec
import spock.util.concurrent.BlockingVariable

import java.util.concurrent.Future

class AsyncServiceSpec extends BaseContainerSpec {

    def "service adds callback and executes callable"() {
        given:
        BlockingVariable done = new BlockingVariable()
        AsyncService asyncService = new AsyncService()

        when:
        asyncService.schedule(
                {
                    done.set(true)
                    return "hello"
                },
                new FutureCallback<String>() {
                    @Override
                    void onSuccess(String result) { done.set(result == 'hello') }

                    @Override
                    void onFailure(Throwable throwable) {}
                }
        )

        then:
        done.get()
    }

    def "service chains futures"() {
        given:
        BlockingVariable done = new BlockingVariable()
        AsyncService asyncService = new AsyncService()

        and:
        Future<String> future = asyncService.schedule({
            return "hello"
        })
        when:
        asyncService.chain(future,
                {
                    it == 'hello'
                },
                new FutureCallback<Boolean>() {
                    @Override
                    void onSuccess(Boolean result) { done.set(result) }

                    @Override
                    void onFailure(Throwable throwable) {}
                }
        )

        then:
        done.get()
    }

}
