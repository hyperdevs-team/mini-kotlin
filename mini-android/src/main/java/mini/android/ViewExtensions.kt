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

import android.view.View
import androidx.annotation.IntRange

/**
 * Makes the [View] visible.
 */
fun View.makeVisible() = run { visibility = View.VISIBLE }

/**
 * Makes the [View] invisible.
 */
fun View.makeInvisible() = run { visibility = View.INVISIBLE }

/**
 * Makes the [View] gone.
 */
fun View.makeGone() = run { visibility = View.GONE }

/**
 * Returns true if the [View] is visible, false otherwise.
 */
fun View.isVisible() = visibility == View.VISIBLE

/**
 * Returns true if the [View] is invisible, false otherwise.
 */
fun View.isInvisible() = visibility == View.INVISIBLE

/**
 * Returns true if the [View] is gone, false otherwise.
 */
fun View.isGone() = visibility == View.GONE

/**
 * Returns true if the [View] is either in invisible or gone status.
 */
fun View.isNotVisible() = isInvisible() || isGone()

/**
 * Toggles visibility of a [View] between [View.VISIBLE] and the input [notVisibleState].
 */
fun View.toggleVisibility(@IntRange(from = View.INVISIBLE.toLong(), to = View.GONE.toLong()) notVisibleState: Int = View.GONE) {
    visibility = if (isVisible()) notVisibleState else View.VISIBLE
}

/**
 * Toggles the [View] enabled status: enabled if disabled or disabled if enabled.
 */
fun View.toggleEnabled() {
    isEnabled = !isEnabled
}

/**
 * Makes the [View] list visible.
 */
fun List<View>.makeVisible() = forEach { it.makeVisible() }

/**
 * Makes the [View] list invisible.
 */
fun List<View>.makeInvisible() = forEach { it.makeInvisible() }

/**
 * Makes the [View] list gone.
 */
fun List<View>.makeGone() = forEach { it.makeGone() }

/**
 * Returns true if any [View] in the list is visible.
 */
fun List<View>.anyVisible() = any { it.isVisible() }

/**
 * Returns true if any [View] in the list is invisible.
 */
fun List<View>.anyInvisible() = any { it.isInvisible() }

/**
 * Returns true if any [View] in the list is gone.
 */
fun List<View>.anyGone() = any { it.isGone() }

/**
 * Returns true if any [View] in the list is gone or invisible.
 */
fun List<View>.anyNotVisible() = any { it.isNotVisible() }

/**
 * Returns true if all [View]s in the list are visible.
 */
fun List<View>.allVisible() = all { it.isVisible() }

/**
 * Returns true if all [View]s in the list are invisible.
 */
fun List<View>.allInvisible() = all { it.isInvisible() }

/**
 * Returns true if all [View]s in the list are gone.
 */
fun List<View>.allGone() = all { it.isGone() }

/**
 * Returns true if all [View]s in the list are either gone or invisible.
 */
fun List<View>.allNotVisible() = all { it.isNotVisible() }

/**
 * Toggles visibility of a [View] list between [View.VISIBLE] and the input [notVisibleState].
 */
fun List<View>.toggleVisibility(@IntRange(from = View.INVISIBLE.toLong(), to = View.GONE.toLong()) notVisibleState: Int = View.GONE) {
    forEach { it.visibility = if (it.isVisible()) notVisibleState else View.VISIBLE }
}

/**
 * Toggles the [View] list enabled status: enabled if disabled or disabled if enabled.
 */
fun List<View>.toggleEnabled() = forEach { it.toggleEnabled() }