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

package mini.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

object ActionTypesGenerator {
    fun generate(container: TypeSpec.Builder, elements: Set<Element>) {

        val actionModels = elements.filter { Modifier.ABSTRACT !in it.modifiers }
            .map { ActionModel(it) }

        container.apply {
            val anyClassTypeName = KClass::class.asTypeName().parameterizedBy(STAR)
            val listTypeName = List::class.asTypeName().parameterizedBy(anyClassTypeName)
            val mapType = Map::class
                .asClassName()
                .parameterizedBy(anyClassTypeName, listTypeName)

            val prop = PropertySpec.builder("actionTypes", mapType)
                .addModifiers(KModifier.PRIVATE)
                .initializer(CodeBlock.builder()
                    .addStatement("mapOf(")
                    .indent()
                    .apply {
                        actionModels.forEach { actionModel ->
                            val comma = if (actionModel != actionModels.last()) "," else ""
                            add("«") // Starts statement
                            add("%T::class to ", actionModel.typeName)
                            add(actionModel.listOfSupertypesCodeBlock())
                            add(comma)
                            add("\n")
                            add("»") // Ends statement
                        }
                    }
                    .unindent()
                    .add(")")
                    .build())
            addProperty(prop.build())
        }.build()
    }
}

private class ActionModel(element: Element) {
    private val type = element.asType()
    private val superTypes = collectTypes(type)
        .sortedBy { it.depth }
        // Ignore base types
        .filter { it.mirror.qualifiedName() !in listOf("java.lang.Object", "mini.BaseAction") }
    val typeName = type.asTypeName()


    fun listOfSupertypesCodeBlock(): CodeBlock {
        val format = superTypes.joinToString(",\n") { "%T::class" }
        val args = superTypes.map { it.mirror.asTypeName() }.toTypedArray()
        return CodeBlock.of("listOf($format)", *args)
    }

    private fun collectTypes(mirror: TypeMirror, depth: Int = 0): Set<ActionSuperType> {
        // We want to add by depth
        val superTypes = typeUtils.directSupertypes(mirror).toSet()
            .map { collectTypes(it, depth + 1) }
            .flatten()
        return setOf(ActionSuperType(mirror, depth)) + superTypes
    }

    class ActionSuperType(val mirror: TypeMirror, val depth: Int) {
        val qualifiedName = mirror.asElement().qualifiedName()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ActionSuperType
            if (qualifiedName != other.qualifiedName) return false
            return true
        }

        override fun hashCode(): Int = qualifiedName.hashCode()
    }
}