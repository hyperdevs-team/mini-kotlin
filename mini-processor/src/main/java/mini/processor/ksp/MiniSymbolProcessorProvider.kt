package mini.processor.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import mini.processor.common.MINI_REGISTRY_NAME_OPTION

class MiniSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        logger = environment.logger

        return MiniSymbolProcessor(
            codeGenerator = environment.codeGenerator,
            registryName = environment.options[MINI_REGISTRY_NAME_OPTION]
        )
    }
}
