/*
 * Copyright 2021 HyperDevs
 *
 * Copyright 2020 BQ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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