package mini.processor.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
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

        val packageNames = (actionSymbols + reducerSymbols)
            .filterIsInstance<KSDeclaration>()
            .map { it.packageName.asString() }
            .distinct()

        val (containerFile, container, className) = getContainerBuilders(registryName, packageNames)

        try {
            ActionTypesGenerator(KspActionTypesGeneratorDelegate(actionSymbols.asSequence())).generate(container)
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
        codeGenerator.createNewFileByPath(
            Dependencies(aggregating = true, *originatingKsFiles.toTypedArray()),
            "META-INF/services/mini.MiniRegistry",
            ""
        ).writer().use { writer ->
            writer.write("${className.canonicalName}\n")
        }

        return emptyList()
    }
}
