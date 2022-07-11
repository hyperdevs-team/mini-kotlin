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

package mini.processor.common

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import mini.DISPATCHER_FACTORY_CLASS_NAME
import mini.Mini

data class ContainerBuilders(
    val fileSpecBuilder: FileSpec.Builder,
    val typeSpecBuilder: TypeSpec.Builder
)

fun getContainerBuilders(): ContainerBuilders {
    val containerClassName = ClassName.bestGuess(DISPATCHER_FACTORY_CLASS_NAME)
    val containerFile =
        FileSpec.builder(containerClassName.packageName, containerClassName.simpleName)
    val container = TypeSpec.objectBuilder(containerClassName)
        .addKdoc("Automatically generated, do not edit.\n")
        .superclass(Mini::class)
    return ContainerBuilders(containerFile, container)
}