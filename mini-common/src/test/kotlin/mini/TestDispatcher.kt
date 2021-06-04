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

package mini

import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

fun newTestDispatcher(): Dispatcher {
    return Dispatcher().apply {
        actionTypeMap = newReflectiveMap()
    }
}

private fun reflectActionTypes(type: KClass<*>, depth: Int = 0): List<ReflectedType> {
    return type.supertypes
        .asSequence()
        .map { (it.jvmErasure.java as Class<*>).kotlin }
        .map { reflectActionTypes(it, depth + 1) }
        .flatten()
        .plus(ReflectedType(type, depth))
        .toList()
}

private class ReflectedType(val clazz: KClass<*>, val depth: Int)

private fun newReflectiveMap(): Map<KClass<*>, List<KClass<*>>> {
    return object : Map<KClass<*>, List<KClass<*>>> {
        private val genericTypes = listOf(Object::class)
        private val map = HashMap<KClass<*>, List<KClass<*>>>()
        override val entries: Set<Map.Entry<KClass<*>, List<KClass<*>>>> = map.entries
        override val keys: Set<KClass<*>> = map.keys
        override val size: Int = map.size
        override val values: Collection<List<KClass<*>>> = map.values
        override fun containsKey(key: KClass<*>): Boolean = map.containsKey(key)
        override fun containsValue(value: List<KClass<*>>): Boolean = map.containsValue(value)
        override fun isEmpty(): Boolean = map.isEmpty()
        override fun get(key: KClass<*>): List<KClass<*>> {
            return map.getOrPut(key) {
                reflectActionTypes(key)
                    .asSequence()
                    .sortedBy { it.depth }
                    .map { it.clazz }
                    .filter { it !in genericTypes }
                    .toList()
            }
        }
    }
}