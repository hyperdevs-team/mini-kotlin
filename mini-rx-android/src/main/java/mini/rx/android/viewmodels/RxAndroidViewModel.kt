package mini.rx.android.viewmodels

import android.app.Application
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import mini.rx.DefaultSubscriptionTracker
import mini.rx.SubscriptionTracker

abstract class RxAndroidViewModel(app: Application) : AndroidViewModel(app),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        clearSubscriptions()
    }
}