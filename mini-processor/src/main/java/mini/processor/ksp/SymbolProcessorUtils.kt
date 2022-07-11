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

package mini.processor.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSDeclaration
import mini.processor.common.ProcessorException
import javax.tools.Diagnostic

lateinit var logger: KSPLogger

fun kspCompilePrecondition(
    check: Boolean,
    message: String,
    declaration: KSDeclaration? = null
) {
    if (!check) {
        kspLogError(message, declaration)
        throw ProcessorException()
    }
}

fun kspLogError(message: String, declaration: KSDeclaration? = null) {
    kspLogMessage(Diagnostic.Kind.ERROR, message, declaration)
}

fun kspWarning(message: String, declaration: KSDeclaration? = null) {
    kspLogMessage(Diagnostic.Kind.MANDATORY_WARNING, message, declaration)
}

fun kspLogMessage(
    kind: Diagnostic.Kind,
    message: String,
    declaration: KSDeclaration? = null
) {
    when (kind) {
        Diagnostic.Kind.ERROR -> logger.error(message, declaration)
        Diagnostic.Kind.WARNING, Diagnostic.Kind.MANDATORY_WARNING -> logger.warn(
            message,
            declaration
        )
        else -> logger.logging(message, declaration)
    }
}