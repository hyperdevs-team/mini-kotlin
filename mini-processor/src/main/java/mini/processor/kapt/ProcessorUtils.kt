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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import mini.processor.common.ProcessorException
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.StandardLocation

lateinit var env: ProcessingEnvironment
lateinit var elementUtils: Elements
lateinit var typeUtils: Types

fun Throwable.stackTraceString(): String {
    val out = ByteArrayOutputStream()
    printStackTrace(PrintStream(out))
    return out.toString()
}

fun ExecutableElement.isSuspending(): Boolean {
    return parameters.last().asType().toString().startsWith("kotlin.coroutines.Continuation")
}

fun TypeMirror.getAllSuperTypes(depth: Int = 0): Set<TypeMirror> {
    //We want to sort them by depth
    val superTypes = typeUtils.directSupertypes(this).toSet()
        .map { it.getAllSuperTypes(depth + 1) }
        .flatten()
    return setOf(this) + superTypes
}

fun kaptCompilePrecondition(
    check: Boolean,
    message: String,
    element: Element? = null
) {
    if (!check) {
        kaptLogError(message, element)
        throw ProcessorException()
    }
}

fun kaptLogError(message: String, element: Element? = null) {
    kaptLogMessage(Diagnostic.Kind.ERROR, message, element)
}

fun kaptWarning(message: String, element: Element? = null) {
    kaptLogMessage(Diagnostic.Kind.MANDATORY_WARNING, message, element)
}

fun kaptLogMessage(kind: Diagnostic.Kind, message: String, element: Element? = null) {
    env.messager.printMessage(kind, "\n" + message, element)
}

//KotlinPoet utils

fun FileSpec.writeToFile(vararg sourceElements: Element) {
    val kotlinFileObject = env.filer
        .createResource(StandardLocation.SOURCE_OUTPUT, packageName, "$name.kt", *sourceElements)
    val openWriter = kotlinFileObject.openWriter()
    writeTo(openWriter)
    openWriter.close()
}

/**
 * Map [java.lang.Object] to [Any]
 */
fun TypeName.safeAnyTypeName(): TypeName =
    if (this is ClassName && this == ClassName("java.lang", "Object")) {
        Any::class.asTypeName()
    } else {
        this
    }
