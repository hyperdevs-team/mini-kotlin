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
import mini.CompositeCloseable
import mini.Dispatcher
import mini.Reducer
import java.io.Closeable
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

object ReducersGenerator {
    fun generate(container: TypeSpec.Builder, elements: Set<Element>) {
        val reducers = elements.map { ReducerModel(it) }
            .groupBy { it.containerName }

        val reducerContainerType = Any::class.asTypeName()
        val reducerContainerListType = List::class.asTypeName().parameterizedBy(reducerContainerType)

        val newDispatcherFn = FunSpec.builder("newDispatcher")
            .returns(Dispatcher::class)
            .addCode(CodeBlock.builder()
                .addStatement("return Dispatcher(actionTypes)")
                .build())
            .build()

        val subscribeSingleWhenBlock = CodeBlock.builder()
            .addStatement("val c = %T()", CompositeCloseable::class)
            .beginControlFlow("when (container)")
            .apply {
                reducers.forEach { (containerName, reducerFunctions) ->
                    beginControlFlow("is %T ->", containerName)
                    reducerFunctions.forEach { function ->
                        addStatement("c.add(dispatcher.subscribe<%T>(priority=%L) { container.%N(it) })",
                            function.function.parameters[0].asType(), //Action type
                            function.priority, //Priority
                            function.function.simpleName //Function name
                        )
                    }
                    endControlFlow()
                }
            }
            .addStatement("else -> throw IllegalArgumentException(\"Container \$container has no reducers\")")
            .endControlFlow()
            .addStatement("return c")
            .build()

        val subscribeSingleFn = FunSpec.builder("subscribe")
            .addModifiers(KModifier.PRIVATE)
            .addParameters(listOf(
                ParameterSpec.builder("dispatcher", Dispatcher::class).build(),
                ParameterSpec.builder("container", reducerContainerType).build()
            ))
            .returns(Closeable::class)
            .addCode(subscribeSingleWhenBlock)
            .build()

        val subscribeListFn = FunSpec.builder("subscribe")
            .addParameters(listOf(
                ParameterSpec.builder("dispatcher", Dispatcher::class).build(),
                ParameterSpec.builder("containers", reducerContainerListType).build()
            ))
            .returns(Closeable::class)
            .addStatement("val c = %T()", CompositeCloseable::class)
            .beginControlFlow("containers.forEach { container ->")
            .addStatement("c.add(subscribe(dispatcher, container))")
            .endControlFlow()
            .addStatement("return c")
            .build()

        with(container) {
            addFunction(newDispatcherFn)
            addFunction(subscribeSingleFn)
            addFunction(subscribeListFn)
        }
    }
}

private class ReducerModel(element: Element) {
    val priority = element.getAnnotation(Reducer::class.java).priority
    val function = element as ExecutableElement
    val container = element.enclosingElement.asType()!!
    val containerName = container.asTypeName()
}