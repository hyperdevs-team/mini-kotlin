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

package mini.processor.ksp.reducers

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import mini.DEFAULT_PRIORITY
import mini.Reducer
import mini.StateContainer
import mini.processor.common.reducers.ContainerModel
import mini.processor.common.reducers.ReducerModel
import mini.processor.kapt.safeAnyTypeName
import mini.processor.ksp.kspCompilePrecondition

@OptIn(KspExperimental::class)
class KspReducerModel(private val function: KSFunctionDeclaration) :
    ReducerModel {
    override val isPure: Boolean
    override val isSuspending: Boolean =
        function.modifiers.contains(com.google.devtools.ksp.symbol.Modifier.SUSPEND)

    override val container: KspContainerModel
    override val priority =
        function.getAnnotationsByType(Reducer::class).toList().getOrNull(0)?.priority
            ?: DEFAULT_PRIORITY

    override val actionTypeName: TypeName
    override val returnTypeName: TypeName

    init {
        kspCompilePrecondition(
            check = !function.modifiers.contains(Modifier.PRIVATE),
            message = "Reducer functions must be public.",
            declaration = function
        )

        container = KspContainerModel(function.parentDeclaration)

        returnTypeName = function.returnType!!.toTypeName()
        val parameters = function.parameters.map { it.type.toTypeName() }

        if (returnTypeName == UNIT) {
            isPure = false
            actionTypeName = parameters[0].safeAnyTypeName()
            kspCompilePrecondition(
                check = parameters.size == 1,
                message = "Expected exactly one action parameter",
                declaration = function
            )
        } else {
            isPure = true
            kspCompilePrecondition(
                check = parameters.size == 2,
                message = "Expected exactly two parameters, ${container.stateTypeName} and action",
                declaration = function
            )
            val stateTypeName = parameters[0]
            actionTypeName = parameters[1].safeAnyTypeName()

            kspCompilePrecondition(
                check = stateTypeName == container.stateTypeName,
                message = "Expected ${container.stateTypeName} as first state parameter",
                declaration = function
            )

            kspCompilePrecondition(
                check = returnTypeName == container.stateTypeName,
                message = "Expected ${container.stateTypeName} as return value",
                declaration = function
            )
        }
    }

    override fun generateCallBlock(containerParam: String, actionParam: String): CodeBlock {

        val receiver = if (container.isStatic) {
            CodeBlock.of("%T.${function.simpleName.asString()}", container.typeName)
        } else {
            CodeBlock.of("${containerParam}.${function.simpleName.asString()}")
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

class KspContainerModel(declaration: KSDeclaration?) : ContainerModel {
    override val typeName: TypeName
    override val stateTypeName: TypeName
    override val isStatic: Boolean

    init {
        kspCompilePrecondition(
            check = declaration != null && declaration is KSClassDeclaration,
            message = "Reducers must be declared inside StateContainer classes",
            declaration = declaration
        )

        val mainTypeName = (declaration as KSClassDeclaration).asStarProjectedType().toTypeName()

        isStatic =
            (declaration.parentDeclaration as? KSClassDeclaration)?.let { parentClassDeclaration ->
                val parentTypeName = parentClassDeclaration.asStarProjectedType().toTypeName()
                "$parentTypeName.Companion" == mainTypeName.toString()
            } ?: false

        val realContainerDeclaration =
            (if (isStatic) declaration.parentDeclaration else declaration) as KSClassDeclaration
        typeName = realContainerDeclaration.asStarProjectedType().toTypeName()

        val superTypes = getAllSuperTypes(type = realContainerDeclaration.asStarProjectedType())
            .map { it.toTypeName() }

        val stateContainerType = superTypes
            .find { it is ParameterizedTypeName && it.rawType == StateContainer::class.asTypeName() }

        kspCompilePrecondition(
            check = stateContainerType != null,
            message = "Reducers must be declared in a StateContainer<T>",
            declaration = declaration
        )

        stateTypeName = (stateContainerType!! as ParameterizedTypeName).typeArguments[0]
    }

    override fun toString(): String {
        return stateTypeName.toString()
    }

    private fun getAllSuperTypes(
        type: KSType,
        arguments: List<KSTypeArgument> = emptyList()
    ): Set<KSType> {
        val resolvedArguments = type.arguments
        val resolvedType =
            if (arguments.isEmpty() || type.arguments.isEmpty()) type else (type.declaration as? KSClassDeclaration)?.asType(
                arguments
            ) ?: type

        val rootSuperTypes = (resolvedType.declaration as? KSClassDeclaration)
            ?.superTypes
            ?.map {
                it
            }
            ?.map { it.resolve() }
            ?.toSet()
            ?: emptySet()

        val superTypes = rootSuperTypes
            .map {
                getAllSuperTypes(type = it, arguments = resolvedArguments)
            }
            .flatten()

        return setOf(resolvedType) + superTypes
    }
}