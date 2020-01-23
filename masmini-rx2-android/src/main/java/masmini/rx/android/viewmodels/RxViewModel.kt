package masmini.rx.android.viewmodels

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import masmini.rx.DefaultSubscriptionTracker
import masmini.rx.SubscriptionTracker

abstract class RxViewModel : ViewModel(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        clearSubscriptions()
    }
}