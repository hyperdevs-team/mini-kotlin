/*
 * Copyright 2026 HyperDevs
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

import org.junit.Assert.assertEquals
import org.junit.Test

class ContainerBuildersTest {

    @Test
    fun `uses stable default registry package when name is missing`() {
        val className = generatedRegistryClassName(null)

        assertEquals("mini.codegen", className.packageName)
        assertEquals("Mini_Generated", className.simpleName)
    }

    @Test
    fun `uses stable default registry package when name is blank`() {
        val className = generatedRegistryClassName("   ")

        assertEquals("mini.codegen", className.packageName)
        assertEquals("Mini_Generated", className.simpleName)
    }

    @Test
    fun `uses explicit registry name as package segment`() {
        val className = generatedRegistryClassName("feature")

        assertEquals("mini.codegen.feature", className.packageName)
        assertEquals("Mini_Generated", className.simpleName)
    }

    @Test
    fun `sanitizes explicit registry name`() {
        val className = generatedRegistryClassName("my-feature name")

        assertEquals("mini.codegen.my_feature_name", className.packageName)
        assertEquals("Mini_Generated", className.simpleName)
    }
}
