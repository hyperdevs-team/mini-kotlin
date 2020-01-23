package masmini.processor

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import masmini.Action
import masmini.Reducer
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * Mini annotation processor.
 *
 * In order to support incremental annotation processing we must specify the kind of processing this
 * does. For our use case, the processor looks up all the classes in the project in order to find
 * [Reducer] and [Action] annotations and created a single [MiniGen] file with the info, so it would
 * make our processor an AGGREGATING one.
 *
 * More info and implementations can be found in:
 * Official docs:
 * - https://docs.gradle.org/current/userguide/java_plugin.html#sec:incremental_annotation_processing
 *
 * Isolating incremental annotation processors implementations:
 * - https://github.com/square/moshi/pull/824
 * - https://github.com/JakeWharton/butterknife/pull/1546
 *
 * Aggregating incremental annotation processors implementations:
 * - https://github.com/greenrobot/EventBus/pull/617
 *
 * Libraries used:
 * - https://github.com/tbroyer/gradle-incap-helper
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("kapt.kotlin.generated")
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
class MiniProcessor : AbstractProcessor() {

    override fun init(environment: ProcessingEnvironment) {
        env = environment
        typeUtils = env.typeUtils
        elementUtils = env.elementUtils
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Reducer::class.java, Action::class.java)
            .map { it.canonicalName }
            .toMutableSet()
    }

    override fun process(set: MutableSet<out TypeElement>,
                         roundEnv: RoundEnvironment): Boolean {

        val roundActions = roundEnv.getElementsAnnotatedWith(Action::class.java)
        val roundReducers = roundEnv.getElementsAnnotatedWith(Reducer::class.java)

        if (roundActions.isEmpty()) return false

        val className = "MasMiniGen"
        val file = FileSpec.builder("masmini", className)
        val container = TypeSpec.objectBuilder(className)
            .addKdoc("Automatically generated, do not edit.\n")

        //Get non-abstract actions
        ActionTypesGenerator.generate(container, roundActions)
        ReducersGenerator.generate(container, roundReducers)

        file.addType(container.build())
        file.build().writeToFile()

        return true
    }
}
