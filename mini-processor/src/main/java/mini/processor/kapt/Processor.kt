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
import mini.processor.common.actions.ActionTypesGenerator
import mini.processor.common.getContainerBuilders
import mini.processor.common.reducers.ReducersGenerator
import mini.processor.kapt.actions.KaptActionTypesGeneratorDelegate
import mini.processor.kapt.reducers.KaptReducersGeneratorDelegate
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
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

        if (roundActions.isEmpty()) return false

        val (containerFile, container) = getContainerBuilders()

        try {
            ActionTypesGenerator(KaptActionTypesGeneratorDelegate(roundActions)).generate(container)
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
}
