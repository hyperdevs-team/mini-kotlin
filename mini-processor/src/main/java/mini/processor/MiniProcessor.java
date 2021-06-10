/*
 * Copyright 2021 HyperDevs
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

package mini.processor;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Dummy Java wrapper that delegates to Kotlin one
 */
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
@SupportedOptions("kapt.kotlin.generated")
public class MiniProcessor extends AbstractProcessor {

   private final Processor processor = new Processor();

   @Override
   public synchronized void init(ProcessingEnvironment processingEnvironment) {
      super.init(processingEnvironment);
      processor.init(processingEnvironment);
   }

   @Override
   public Set<String> getSupportedAnnotationTypes() {
      return processor.getSupportedAnnotationTypes();
   }

   @Override
   public SourceVersion getSupportedSourceVersion() {
      return processor.getSupportedSourceVersion();
   }

   @Override
   public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
      return processor.process(roundEnvironment);
   }
}
