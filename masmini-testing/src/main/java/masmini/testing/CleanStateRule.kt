package masmini.testing

import masmini.Store
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * [TestRule] that resets the state of each Store (retrieved via function) after an evaluation.
 */
class CleanStateRule(val storesFn: () -> List<Store<*>>) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            fun reset() {
                val stores = storesFn()
                stores.forEach { it.resetState() }
            }

            override fun evaluate() {
                reset()
                base.evaluate() //Execute the test
                reset()
            }
        }
    }
}