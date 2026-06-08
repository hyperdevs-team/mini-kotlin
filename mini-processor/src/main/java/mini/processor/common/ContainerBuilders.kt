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
import mini.Mini

const val MINI_REGISTRY_NAME_OPTION = "mini.registryName"
const val MINI_REGISTRY_PACKAGE_NAME = "mini.codegen"

private const val GENERATED_REGISTRY_SIMPLE_NAME = "Mini_Generated"

data class ContainerBuilders(
    val fileSpecBuilder: FileSpec.Builder,
    val typeSpecBuilder: TypeSpec.Builder,
    val className: ClassName
)

fun getContainerBuilders(registryName: String?, packageNames: Iterable<String>): ContainerBuilders {
    val containerClassName = generatedRegistryClassName(registryName, packageNames)
    val containerFile =
        FileSpec.builder(containerClassName.packageName, containerClassName.simpleName)
    val container = TypeSpec.classBuilder(containerClassName)
        .addKdoc("Automatically generated, do not edit.\n")
        .superclass(Mini::class)
    return ContainerBuilders(containerFile, container, containerClassName)
}

fun generatedRegistryClassName(registryName: String?, packageNames: Iterable<String>): ClassName {
    val registryPackageSegment = registryName
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let(::sanitizeRegistryName)
        ?: packageNames
            .map(::sanitizePackageName)
            .filter { it.isNotEmpty() }
            .sorted()
            .joinToString("_")
            .takeIf { it.isNotEmpty() }
            ?.let(::shortHash)
            ?: "default"

    // Keep the generated class name stable and move uniqueness into a module-specific
    // package segment so each module owns its own generated namespace.
    return ClassName("$MINI_REGISTRY_PACKAGE_NAME.$registryPackageSegment", GENERATED_REGISTRY_SIMPLE_NAME)
}

private fun sanitizeRegistryName(name: String): String {
    return name.replace(Regex("[^A-Za-z0-9_]"), "_")
}

private fun sanitizePackageName(packageName: String): String {
    return packageName.replace('.', '_')
}

private fun shortHash(value: String): String {
    return value.encodeToByteArray().fold(0x811c9dc5.toInt()) { acc, byte ->
        (acc xor byte.toInt()) * 16777619
    }.toUInt().toString(16)
}
