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

package mini.processor.kapt.reducers

import com.squareup.kotlinpoet.*
import mini.Reducer
import mini.StateContainer
import mini.processor.common.reducers.ContainerModel
import mini.processor.common.reducers.ReducerModel
import mini.processor.kapt.getAllSuperTypes
import mini.processor.kapt.isSuspending
import mini.processor.kapt.kaptCompilePrecondition
import mini.processor.kapt.safeAnyTypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType

class KaptReducerModel(private val function: ExecutableElement) : ReducerModel {
    override val isPure: Boolean
    override val isSuspending: Boolean

    override val container: KaptContainerModel
    override val priority = function.getAnnotation(Reducer::class.java).priority

    override val actionTypeName: TypeName
    override val returnTypeName: TypeName

    init {
        kaptCompilePrecondition(
            check = function.modifiers.contains(Modifier.PUBLIC),
            message = "Reducer functions must be public.",
            element = function
        )

        isSuspending = function.isSuspending()
        container = KaptContainerModel(function.enclosingElement)
        val parameters: List<TypeName>

        if (isSuspending) {
            //Hacky check to get return type of a kotlin continuation
            val continuationTypeParameter = (function.parameters.last().asType() as DeclaredType)
                .typeArguments[0].asTypeName() as WildcardTypeName
            returnTypeName = continuationTypeParameter.inTypes.first()
            parameters = function.parameters.dropLast(1).map { it.asType().asTypeName() }
        } else {
            returnTypeName = function.returnType.asTypeName()
            parameters = function.parameters.map { it.asType().asTypeName() }
        }

        if (returnTypeName == UNIT) {
            isPure = false
            actionTypeName = function.parameters[0].asType().asTypeName().safeAnyTypeName()
            kaptCompilePrecondition(
                check = parameters.size == 1,
                message = "Expected exactly one action parameter",
                element = function
            )
        } else {
            isPure = true
            kaptCompilePrecondition(
                check = parameters.size == 2,
                message = "Expected exactly two parameters, ${container.stateTypeName} and action",
                element = function
            )
            val stateTypeName = parameters[0]
            actionTypeName = parameters[1].safeAnyTypeName()

            kaptCompilePrecondition(
                check = stateTypeName == container.stateTypeName,
                message = "Expected ${container.stateTypeName} as first state parameter",
                element = function
            )

            kaptCompilePrecondition(
                check = returnTypeName == container.stateTypeName,
                message = "Expected ${container.stateTypeName} as return value",
                element = function
            )
        }

    }

    override fun generateCallBlock(containerParam: String, actionParam: String): CodeBlock {

        val receiver = if (container.isStatic) {
            CodeBlock.of("%T.${function.simpleName}", container.typeName)
        } else {
            CodeBlock.of("${containerParam}.${function.simpleName}")
        }

        val call = if (isPure) {
            CodeBlock.of("(${containerParam}.state, $actionParam)")
        } else {
            CodeBlock.of("($actionParam)")
        }

        return if (isPure) {
            CodeBlock.builder()
                .add("${containerParam}.setState(")
                .add(receiver)
                .add(call)
                .add(")")
                .build()
        } else {
            CodeBlock.builder()
                .add(receiver)
                .add(call)
                .build()
        }
    }
}

class KaptContainerModel(element: Element) : ContainerModel {
    override val typeName: TypeName
    override val stateTypeName: TypeName
    override val isStatic: Boolean

    init {
        kaptCompilePrecondition(
            check = element.kind == ElementKind.CLASS,
            message = "Reducers must be declared inside StateContainer classes",
            element = element
        )

        val mainTypeName = element.asType().asTypeName()
        val parent = element.enclosingElement

        isStatic = if (parent != null && parent.kind == ElementKind.CLASS) {
            val parentTypeName = parent.asType().asTypeName()
            "$parentTypeName.Companion" == mainTypeName.toString()
        } else {
            false
        }

        val realContainer = if (isStatic) element.enclosingElement else element
        typeName = realContainer.asType().asTypeName()

        val superTypes = realContainer.asType().getAllSuperTypes().map { it.asTypeName() }
        val stateContainerType = superTypes
            .find { it is ParameterizedTypeName && it.rawType == StateContainer::class.asTypeName() }
        kaptCompilePrecondition(
            check = stateContainerType != null,
            message = "Reducers must be declared in a StateContainer<T>",
            element = element
        )

        stateTypeName = (stateContainerType!! as ParameterizedTypeName).typeArguments[0]
    }

    override fun toString(): String {
        return stateTypeName.toString()
    }
}