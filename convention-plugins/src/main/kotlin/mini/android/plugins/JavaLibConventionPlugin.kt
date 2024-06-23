/*
 * Copyright 2024 HyperDevs
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

package mini.android.plugins

import mini.android.plugins.extensions.applyJavaLibPublishing
import mini.android.plugins.extensions.applyVersioning
import org.gradle.api.Plugin
import org.gradle.api.Project

class JavaLibConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val (versionName, _) = applyVersioning()
            applyJavaLibPublishing(versionName)
        }
    }
}
