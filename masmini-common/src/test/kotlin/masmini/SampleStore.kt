package masmini

class SampleStore : Store<String>() {
    override fun initialState(): String = "initial"

    fun updateState(s: String) {
        newState = s
    }

    val storeSubscriptions = listeners
}