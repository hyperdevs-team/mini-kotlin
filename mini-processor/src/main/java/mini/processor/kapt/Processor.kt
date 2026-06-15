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

package mini.processor.kapt

import mini.Action
import mini.Reducer
import mini.processor.common.ProcessorException
import mini.processor.common.MINI_REGISTRY_NAME_OPTION
import mini.processor.common.actions.ActionTypesGenerator
import mini.processor.common.getContainerBuilders
import mini.processor.common.reducers.ReducersGenerator
import mini.processor.kapt.isSuspending
import mini.processor.kapt.actions.KaptActionTypesGeneratorDelegate
import mini.processor.kapt.reducers.KaptReducersGeneratorDelegate
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.SourceVersion

class Processor {

    val supportedAnnotationTypes: MutableSet<String> = mutableSetOf(
        Reducer::class.java, Action::class.java
    )
        .map { it.canonicalName }.toMutableSet()
    val supportedSourceVersion: SourceVersion = SourceVersion.RELEASE_8

    fun init(environment: ProcessingEnvironment) {
        env = environment
        typeUtils = env.typeUtils
        elementUtils = env.elementUtils
    }

    fun process(roundEnv: RoundEnvironment): Boolean {

        val roundActions = roundEnv.getElementsAnnotatedWith(Action::class.java)
        val roundReducers = roundEnv.getElementsAnnotatedWith(Reducer::class.java)

        if (roundActions.isEmpty() && roundReducers.isEmpty()) return false

        val registryName = env.options[MINI_REGISTRY_NAME_OPTION]
        val (containerFile, container, _) = getContainerBuilders(registryName)
        val referencedActionElements = reducerActionElements(roundReducers)

        try {
            ActionTypesGenerator(KaptActionTypesGeneratorDelegate(roundActions + referencedActionElements)).generate(container)
            ReducersGenerator(KaptReducersGeneratorDelegate(roundReducers)).generate(container)
        } catch (e: Throwable) {
            if (e !is ProcessorException) {
                kaptLogError(
                    "Compiler crashed, open an issue please!\n" +
                            " ${e.stackTraceString()}"
                )
            }
        }

        containerFile
            .addType(container.build())
            .build()
            .writeToFile(sourceElements = ((roundActions + roundReducers).toTypedArray()))

        return true
    }

    private fun reducerActionElements(reducers: Set<Element>): Set<Element> {
        return reducers
            .filterIsInstance<ExecutableElement>()
            .mapNotNull { reducer ->
                val parameters = if (reducer.isSuspending()) reducer.parameters.dropLast(1) else reducer.parameters
                val actionIndex = when (parameters.size) {
                    1 -> 0
                    2 -> 1
                    else -> return@mapNotNull null
                }
                typeUtils.asElement(parameters[actionIndex].asType())
            }
            .toSet()
    }
}
