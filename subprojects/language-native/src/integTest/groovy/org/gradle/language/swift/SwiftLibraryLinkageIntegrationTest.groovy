/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.swift

import org.gradle.nativeplatform.fixtures.AbstractInstalledToolChainIntegrationSpec
import org.gradle.nativeplatform.fixtures.app.SwiftLib
import org.gradle.util.Requires
import org.gradle.util.TestPrecondition

@Requires(TestPrecondition.SWIFT_SUPPORT)
class SwiftLibraryLinkageIntegrationTest extends AbstractInstalledToolChainIntegrationSpec {

    def "can create static only library"() {
        def library = new SwiftLib()
        buildFile << """
            apply plugin: 'swift-library'

            library {
                linkage = [Linkage.STATIC]
            }
        """
        settingsFile << """
            rootProject.name = 'foo'
        """
        library.writeToProject(testDirectory)

        when:
        succeeds('assemble')

        then:
        result.assertTasksExecuted(':compileDebugStaticSwift', ':createDebugStatic', ':assemble')
        staticLibrary('build/lib/main/debug/static/Foo').assertExists()
    }

    def "can create shared library binary when explicitly request a shared linkage"() {
        def library = new SwiftLib()
        buildFile << """
            apply plugin: 'swift-library'

            library {
                linkage = [Linkage.SHARED]
            }
        """
        settingsFile << """
            rootProject.name = 'foo'
        """
        library.writeToProject(testDirectory)

        when:
        succeeds('assemble')

        then:
        result.assertTasksExecuted(':compileDebugSwift', ':linkDebug', ':assemble')
        sharedLibrary('build/lib/main/debug/Foo').assertExists()
    }

    def "creates shared library binary by default when both linkage specified"() {
        def library = new SwiftLib()
        buildFile << """
            apply plugin: 'swift-library'

            library {
                linkage = [Linkage.SHARED, Linkage.STATIC]
            }
        """
        settingsFile << """
            rootProject.name = 'foo'
        """
        library.writeToProject(testDirectory)

        when:
        succeeds('assemble')

        then:
        result.assertTasksExecuted(':compileDebugSwift', ':linkDebug', ':assemble')
        sharedLibrary('build/lib/main/debug/Foo').assertExists()
    }

    def "can create static library binary when enabling static linkage"() {
        def library = new SwiftLib()
        buildFile << """
            apply plugin: 'swift-library'

            library {
                linkage = [Linkage.SHARED, Linkage.STATIC]
            }
        """
        settingsFile << """
            rootProject.name = 'foo'
        """
        library.writeToProject(testDirectory)

        when:
        succeeds('assembleDebugStatic')

        then:
        result.assertTasksExecuted(':compileDebugStaticSwift', ':createDebugStatic', ':assembleDebugStatic')
        staticLibrary('build/lib/main/debug/static/Foo').assertExists()
    }
}
