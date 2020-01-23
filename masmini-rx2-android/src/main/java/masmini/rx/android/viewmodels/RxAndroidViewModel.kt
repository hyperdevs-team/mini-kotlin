package masmini.rx.android.viewmodels

import android.app.Application
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import masmini.rx.DefaultSubscriptionTracker
import masmini.rx.SubscriptionTracker

abstract class RxAndroidViewModel(app: Application) : AndroidViewModel(app),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        clearSubscriptions()
    }
}