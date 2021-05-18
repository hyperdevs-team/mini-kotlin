package masmini.testing

import masmini.Action
import masmini.Chain
import masmini.Middleware
import java.util.*

/**
 * [Middleware] class for testing purposes which mute all the received actions.
 */
internal class TestDispatcherMiddleware : Middleware {
    private val mutedActions = LinkedList<Any>()

    val actions: List<Any> get() = mutedActions
    override suspend fun intercept(action: Any, chain: Chain): Any {
        println("Muted: $action")
        mutedActions.add(action)
        return TestOnlyAction
    }
}

/**
 * Action for testing purposes.
 */
@Action
internal object TestOnlyAction