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

package mini.android

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import mini.Resource
import kotlin.math.roundToInt

/**
 * Toggle two views between content / loading / error based on [Resource] state.
 *
 * Has no effect when resource is idle.
 */
fun toggleViewsVisibility(
    resource: Resource<*>,
    contentView: View? = null,
    loadingView: View? = null,
    errorView: View? = null,
    idleView: View? = null,
    invisibilityType: Int = View.INVISIBLE
) {
    val newVisibilities = arrayOf(invisibilityType, invisibilityType, invisibilityType, invisibilityType)
    val indexToMakeVisible =
        when {
            resource.isSuccess -> 0
            resource.isLoading -> 1
            resource.isFailure -> 2
            resource.isEmpty -> 3
            else -> throw UnsupportedOperationException()
        }
    newVisibilities[indexToMakeVisible] = View.VISIBLE
    contentView?.visibility = newVisibilities[0]
    loadingView?.visibility = newVisibilities[1]
    errorView?.visibility = newVisibilities[2]
    idleView?.visibility = newVisibilities[3]
}

fun ViewGroup.inflateNoAttach(@LayoutRes layout: Int): View {
    return LayoutInflater.from(this.context).inflate(layout, this, false)
}

/**
 * 8.dp -> 8dp in value in pixels
 */
val Number.dp: Int get() = (this.toFloat() * Resources.getSystem().displayMetrics.density).roundToInt()
