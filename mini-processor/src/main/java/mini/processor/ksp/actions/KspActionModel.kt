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

package mini.processor.ksp.actions

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName
import mini.processor.common.actions.ActionModel
import mini.processor.common.actions.ActionSuperType

class KspActionModel(declaration: KSClassDeclaration) : ActionModel {
    private val type = declaration.asStarProjectedType()

    override val typeName = type.toTypeName()
    private val superTypes = collectTypes(type)
        .sortedBy { it.depth }
        .map { it.typeName }

    override fun listOfSupertypesCodeBlock(): CodeBlock {
        val format = superTypes.joinToString(",\n") { "%T::class" }
        val args = superTypes.toTypedArray()
        return CodeBlock.of("listOf($format)", *args)
    }

    private fun collectTypes(type: KSType, depth: Int = 0): Set<ActionSuperType> {
        val rootSuperTypes = (type.declaration as? KSClassDeclaration)
            ?.superTypes
            ?.map { it.resolve() }
            ?.toSet()
            ?: emptySet()

        val superTypes = rootSuperTypes
            .map {
                collectTypes(it, depth + 1)
            }
            .flatten()

        return setOf(ActionSuperType(type.toTypeName(), depth)) + superTypes
    }
}