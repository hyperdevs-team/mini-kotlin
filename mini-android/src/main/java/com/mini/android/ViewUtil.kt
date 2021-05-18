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

package com.mini.android

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
    contentView: View,
    loadingView: View,
    errorView: View,
    invisibilityType: Int = View.INVISIBLE
) {
    val (content, loading, error) =
        when {
            resource.isSuccess -> Triple(View.VISIBLE, invisibilityType, invisibilityType)
            resource.isLoading -> Triple(invisibilityType, View.VISIBLE, invisibilityType)
            resource.isEmpty -> Triple(invisibilityType, invisibilityType, View.VISIBLE)
            else -> return
        }
    contentView.visibility = content
    loadingView.visibility = loading
    errorView.visibility = error
}

fun ViewGroup.inflateNoAttach(@LayoutRes layout: Int): View {
    return LayoutInflater.from(this.context).inflate(layout, this, false)
}

/** dp -> px */
val Number.dp: Int get() = (this.toFloat() * Resources.getSystem().displayMetrics.density).roundToInt()
