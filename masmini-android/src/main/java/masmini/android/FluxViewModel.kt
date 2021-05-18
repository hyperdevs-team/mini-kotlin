package masmini.android

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import masmini.CloseableTracker
import masmini.DefaultCloseableTracker

abstract class FluxViewModel : ViewModel(),
    CloseableTracker by DefaultCloseableTracker() {

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        close()
    }
}