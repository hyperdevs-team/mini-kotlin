package mini.processor.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ksp.writeTo
import mini.Action
import mini.Reducer
import mini.processor.common.ProcessorException
import mini.processor.common.actions.ActionTypesGenerator
import mini.processor.common.getContainerBuilders
import mini.processor.common.reducers.ReducersGenerator
import mini.processor.kapt.stackTraceString
import mini.processor.ksp.actions.KspActionTypesGeneratorDelegate
import mini.processor.ksp.reducers.KspReducersGeneratorDelegate

class MiniSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val registryName: String?
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Get elements with the @Reducer or @Action annotations
        val actionSymbols = resolver.getSymbolsWithAnnotation(Action::class.java.canonicalName).toList()
        val reducerSymbols = resolver.getSymbolsWithAnnotation(Reducer::class.java.canonicalName).toList()

        // Collect the files that contain the symbols, we will use this to set the originating files
        // for the generated code and incremental processing.
        val originatingKsFiles = (actionSymbols + reducerSymbols)
            .filterIsInstance<KSDeclaration>()
            .mapNotNull { it.containingFile }
            .distinct()
            .toList()

        if (actionSymbols.isEmpty() && reducerSymbols.isEmpty()) return emptyList()

        val (containerFile, container, _) = getContainerBuilders(registryName)
        val referencedActionSymbols = reducerActionDeclarations(reducerSymbols)

        try {
            ActionTypesGenerator(KspActionTypesGeneratorDelegate((actionSymbols + referencedActionSymbols).asSequence())).generate(container)
            ReducersGenerator(KspReducersGeneratorDelegate(reducerSymbols.asSequence())).generate(container)
        } catch (e: Throwable) {
            if (e !is ProcessorException) {
                kspLogError(
                    "Compiler crashed, open an issue please!\n" +
                            " ${e.stackTraceString()}"
                )
            }
        }

        containerFile
            .addType(container.build())
            .build()
            .writeTo(
                codeGenerator = codeGenerator,
                aggregating = true,
                originatingKSFiles = originatingKsFiles
            )

        return emptyList()
    }

    private fun reducerActionDeclarations(reducerSymbols: List<KSAnnotated>): List<KSClassDeclaration> {
        return reducerSymbols
            .filterIsInstance<KSFunctionDeclaration>()
            .mapNotNull { reducer ->
                val parameters = reducer.parameters
                val actionIndex = when (parameters.size) {
                    1 -> 0
                    2 -> 1
                    else -> return@mapNotNull null
                }
                parameters[actionIndex].type.resolve().declaration as? KSClassDeclaration
            }
            .distinctBy { it.qualifiedName?.asString() ?: it.simpleName.asString() }
    }
}
