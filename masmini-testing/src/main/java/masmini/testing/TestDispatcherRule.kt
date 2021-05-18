package masmini.testing

import masmini.Dispatcher
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
                dispatcher.removeInterceptor(testMiddleware)
            }
        }
    }

}