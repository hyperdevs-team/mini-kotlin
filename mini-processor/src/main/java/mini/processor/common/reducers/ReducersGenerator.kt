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

package mini.processor.common.reducers

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import mini.CompositeCloseable
import mini.Dispatcher
import mini.StateContainer
import java.io.Closeable

class ReducersGenerator(private val delegate: ReducersGeneratorDelegate) {
    fun generate(container: TypeSpec.Builder) {
        val reducers = delegate.provideModels().groupBy { it.container.typeName }

        val whenBlock = CodeBlock.builder()
            .addStatement("val c = %T()", CompositeCloseable::class)
            .addStatement("when (container) {").indent()
            .apply {
                reducers.forEach { (containerName, reducerFunctions) ->
                    addStatement("is %T -> {", containerName).indent()
                    reducerFunctions.forEach { function ->
                        add(
                            "c.add(dispatcher.subscribe<%T>(priority=%L) { action -> ",
                            function.actionTypeName,
                            function.priority
                        )
                        add(function.generateCallBlock("container", "action"))
                        addStatement("})")
                    }
                    unindent().addStatement("}")
                }
            }
            .unindent()
            .addStatement("}") //Close when
            .addStatement("return c")
            .build()

        val typeParam = TypeVariableName("T", ClassName("mini", "State"))
        val oneParam = StateContainer::class.asTypeName().parameterizedBy(typeParam)

        val registerOneFn = FunSpec.builder("subscribe")
            .addModifiers(KModifier.OVERRIDE)
            .addTypeVariable(typeParam)
            .addParameter("dispatcher", Dispatcher::class)
            .addParameter("container", oneParam)
            .returns(Closeable::class)
            .addCode(whenBlock)
            .build()

        container.addFunction(registerOneFn)
    }
}