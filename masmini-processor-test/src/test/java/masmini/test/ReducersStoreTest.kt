package masmini.test

import kotlinx.coroutines.runBlocking
import masmini.Dispatcher
import masmini.Mini
import org.amshove.kluent.`should equal`
import org.junit.Test

internal class ReducersStoreTest {

    private val store = ReducersStore()
    private val dispatcher = Dispatcher().apply {
        Mini.link(this, listOf(store))
    }

    @Test
    fun `pure reducers are called`() {
        runBlocking {
            dispatcher.dispatch(AnyAction("changed"))
            store.state.value.`should equal`("changed")
        }
    }

    @Test
    fun `pure static reducers are called`() {
        runBlocking {
            dispatcher.dispatch(AnyAction("changed"))
            store.state.value.`should equal`("changed")
        }
    }
}