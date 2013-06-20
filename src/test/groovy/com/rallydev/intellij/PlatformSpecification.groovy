package com.rallydev.intellij

import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.util.ui.UIUtil
import org.junit.Rule
import org.junit.rules.TestName
import spock.lang.Specification

/**
 * todo: Something isn't quite right with this spec; only a single
 * todo: class can extend it due to something around indexes and index shutdown
 */
abstract class PlatformSpecification extends Specification {

    @Rule TestName testName = new TestName()

    @Delegate
    LightPlatformTestCase intellijTestCase

    def setup() {
        String name = testName.methodName
        intellijTestCase = new LightPlatformTestCase() {
            @Override
            String getName() {
                name
            }

            @Override
            protected String getTestName(boolean lowercaseFirstLetter) {
                getName()
            }
        }
        def intellijTestCase = this.intellijTestCase
        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            void run() {
                intellijTestCase.setUp()
            }
        })
    }

    def cleanup() {
        def intellijTestCase = this.intellijTestCase
        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            void run() {
                intellijTestCase.tearDown()
            }
        })
    }

}
