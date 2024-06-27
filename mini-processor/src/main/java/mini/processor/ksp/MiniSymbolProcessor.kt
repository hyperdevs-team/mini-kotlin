package mini.processor.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
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
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Get elements with the @Reducer or @Action annotations
        val actionSymbols = resolver.getSymbolsWithAnnotation(Action::class.java.canonicalName)
        val reducerSymbols = resolver.getSymbolsWithAnnotation(Reducer::class.java.canonicalName)

        val originatingKsFiles = (actionSymbols + reducerSymbols).
            filterIsInstance<KSClassDeclaration>()
            .mapNotNull { it.containingFile }
            .distinct()
            .toList()

        if (!actionSymbols.iterator().hasNext()) return emptyList()

        val (containerFile, container) = getContainerBuilders()

        try {
            ActionTypesGenerator(KspActionTypesGeneratorDelegate(actionSymbols)).generate(container)
            ReducersGenerator(KspReducersGeneratorDelegate(reducerSymbols)).generate(container)
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
                originatingKSFiles = originatingKsFiles)

        return emptyList()
    }
}