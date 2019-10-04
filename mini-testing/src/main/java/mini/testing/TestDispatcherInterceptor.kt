package mini.testing

import mini.BaseAction
import mini.Chain
import mini.Interceptor
import java.util.*

/**
 * [Interceptor] class for testing purposes which mute all the received actions.
 */
class TestDispatcherInterceptor : Interceptor {
    override fun invoke(action: Any, chain: Chain): Any {
        mutedActions.add(action)
        return TestOnlyAction
    }

    private val mutedActions = LinkedList<Any>()

    val actions: List<Any> get() = mutedActions
}

/**
 * Action for testing purposes.
 */
object TestOnlyAction : BaseAction()