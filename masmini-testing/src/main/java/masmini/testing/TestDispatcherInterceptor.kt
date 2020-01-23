package masmini.testing

import masmini.BaseAction
import masmini.Chain
import masmini.Interceptor
import java.util.*

/**
 * [Interceptor] class for testing purposes which mute all the received actions.
 */
internal class TestDispatcherInterceptor : Interceptor {
    override fun invoke(action: Any, chain: Chain): Any {
        println("Muted: $action")
        mutedActions.add(action)
        return TestOnlyAction
    }

    private val mutedActions = LinkedList<Any>()

    val actions: List<Any> get() = mutedActions
}

/**
 * Action for testing purposes.
 */
internal object TestOnlyAction : BaseAction()