/*
 * Copyright 2022 HyperDevs
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

package mini.processor.kapt.actions

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asTypeName
import mini.processor.common.actions.ActionModel
import mini.processor.common.actions.ActionSuperType
import mini.processor.kapt.typeUtils
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

class KaptActionModel(element: Element) : ActionModel {
    private val type = element.asType()
    private val javaObject = ClassName.bestGuess("java.lang.Object")

    override val typeName = type.asTypeName()
    private val superTypes = collectTypes(type)
        .sortedBy { it.depth }
        .filter { it.typeName != javaObject }
        .map { it.typeName }
        .plus(ANY)

    override fun listOfSupertypesCodeBlock(): CodeBlock {
        val format = superTypes.joinToString(",\n") { "%T::class" }
        val args = superTypes.toTypedArray()
        return CodeBlock.of("listOf($format)", *args)
    }

    private fun collectTypes(mirror: TypeMirror, depth: Int = 0): Set<ActionSuperType> {
        //We want to add by depth
        val superTypes = typeUtils.directSupertypes(mirror).toSet()
            .map { collectTypes(it, depth + 1) }
            .flatten()
        return setOf(ActionSuperType(mirror.asTypeName(), depth)) + superTypes
    }
}