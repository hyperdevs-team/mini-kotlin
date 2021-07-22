/*
 * Copyright 2021 HyperDevs
 *
 * Copyright 2020 BQ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mini.testing

import mini.Dispatcher
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * This [TestRule] evaluates every action received with the [TestDispatcherMiddleware] to
 * intercept all the actions dispatched during a test and block them, getting them not reaching the store.
 */
class TestDispatcherRule(val dispatcherFn: () -> Dispatcher) : TestRule {
    private val testMiddleware = TestDispatcherMiddleware()
    val actions: List<Any> get() = testMiddleware.actions

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val dispatcher = dispatcherFn()
                dispatcher.addMiddleware(testMiddleware)
                base.evaluate() //Execute the test
                dispatcher.removeMiddleware(testMiddleware)
            }
        }
    }

}