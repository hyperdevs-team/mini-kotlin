package mini.rx.android

import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import mini.rx.DefaultSubscriptionTracker
import mini.rx.SubscriptionTracker

open class FluxRxActivity : AppCompatActivity(), SubscriptionTracker by DefaultSubscriptionTracker() {
    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        clearSubscriptions()
    }
}