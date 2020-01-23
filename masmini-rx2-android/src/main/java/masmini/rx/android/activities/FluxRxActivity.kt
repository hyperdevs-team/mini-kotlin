package masmini.rx.android.activities

import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import masmini.rx.DefaultSubscriptionTracker
import masmini.rx.SubscriptionTracker

open class FluxRxActivity : AppCompatActivity(), SubscriptionTracker by DefaultSubscriptionTracker() {
    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        clearSubscriptions()
    }
}