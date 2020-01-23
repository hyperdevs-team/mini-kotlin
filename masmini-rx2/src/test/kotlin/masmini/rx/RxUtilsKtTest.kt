package masmini.rx

import masmini.SampleStore
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be equal to`
import org.junit.Test

class RxUtilsKtTest {
    @Test
    fun `flowable sends initial state`() {
        val store = SampleStore()
        store.updateState("abc") //Set before subscribe
        var sentState = ""
        store.flowable().subscribe {
            sentState = it
        }
        sentState `should be equal to` "abc"
    }

    @Test
    fun `flowable sends updates`() {
        val store = SampleStore()
        var sentState = ""
        store.flowable().subscribe {
            sentState = it
        }
        store.updateState("abc") //Set before subscribe
        sentState `should be equal to` "abc"
    }

    @Test
    fun `flowable completes`() {
        val store = SampleStore()
        var sentState = ""
        val disposable = store.flowable(hotStart = false).subscribe {
            sentState = it
        }
        disposable.dispose() //Clear it
        store.updateState("abc")
        sentState `should be equal to` "" //No change should be made
    }

    @Test
    fun `flowable disposes correctly`() {
        val store = SampleStore()
        val disposable = store.flowable(hotStart = false).subscribe()
        disposable.dispose() //Clear it

        store.storeSubscriptions.`should be empty`()
    }

    @Test
    fun `observable sends initial state`() {
        val store = SampleStore()
        store.updateState("abc") //Set before subscribe
        var sentState = ""
        store.observable().subscribe {
            sentState = it
        }
        sentState `should be equal to` "abc"
    }

    @Test
    fun `observable sends updates`() {
        val store = SampleStore()
        var sentState = ""
        store.observable().subscribe {
            sentState = it
        }
        store.updateState("abc") //Set before subscribe
        sentState `should be equal to` "abc"
    }

    @Test
    fun `observable completes`() {
        val store = SampleStore()
        var sentState = ""
        val disposable = store.observable(hotStart = false).subscribe {
            sentState = it
        }
        disposable.dispose() //Clear it
        store.updateState("abc")
        sentState `should be equal to` "" //No change should be made
    }

    @Test
    fun `observable disposes correctly`() {
        val store = SampleStore()
        val disposable = store.observable(hotStart = false).subscribe()
        disposable.dispose() //Clear it

        store.storeSubscriptions.`should be empty`()
    }
}