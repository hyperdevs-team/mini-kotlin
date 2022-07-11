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

package mini.processor.common.actions

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import mini.Mini
import kotlin.reflect.KClass

class ActionTypesGenerator(private val delegate: ActionTypesGeneratorDelegate) {
    fun generate(container: TypeSpec.Builder) {
        val actionModels = delegate.provideModels()
        container.apply {
            val anyClassTypeName = KClass::class.asTypeName().parameterizedBy(STAR)
            val listTypeName = List::class.asTypeName().parameterizedBy(anyClassTypeName)
            val mapType = Map::class
                .asClassName()
                .parameterizedBy(anyClassTypeName, listTypeName)

            val prop = PropertySpec.builder(Mini::actionTypes.name, mapType)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(
                    CodeBlock.builder()
                        .add("mapOf(\n⇥")
                        .apply {
                            actionModels.forEach { actionModel ->
                                val comma = if (actionModel != actionModels.last()) "," else ""
                                add("«")
                                add("%T::class to ", actionModel.typeName)
                                add(actionModel.listOfSupertypesCodeBlock())
                                add(comma)
                                add("\n»")
                            }
                        }
                        .add("⇤)")
                        .build())
            addProperty(prop.build())
        }.build()
    }
}