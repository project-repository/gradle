/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.internal.execution.steps

import org.gradle.api.file.FileCollection
import org.gradle.internal.execution.OutputChangeListener
import org.gradle.internal.execution.Result
import org.gradle.internal.execution.UnitOfWork
import org.gradle.internal.file.TreeType

class BroadcastChangingOutputsStepTest extends ContextInsensitiveStepSpec {
    def outputChangeListener = Mock(OutputChangeListener)
    def step = new BroadcastChangingOutputsStep<>(outputChangeListener, delegate)
    def delegateResult = Mock(Result)

    def "notifies listener about specific outputs changing"() {
        def outputDir = file("output-dir")
        def localStateDir = file("local-state-dir")
        def destroyableDir = file("destroyable-dir")

        when:
        def result = step.execute(context)

        then:
        result == delegateResult

        _ * work.visitOutputProperties(_ as UnitOfWork.OutputPropertyVisitor) >> { UnitOfWork.OutputPropertyVisitor visitor ->
            visitor.visitOutputProperty("output", TreeType.DIRECTORY, outputDir, Mock(FileCollection))
        }
        _ * work.visitDestroyableRoots(_ as UnitOfWork.DestroyableVisitor) >> { UnitOfWork.DestroyableVisitor visitor ->
            visitor.visitDestroyableRoot(destroyableDir)
        }
        _ * work.visitLocalState(_ as UnitOfWork.LocalStateVisitor) >> { UnitOfWork.LocalStateVisitor visitor ->
            visitor.visitLocalStateRoot(localStateDir)
        }

        then:
        1 * outputChangeListener.beforeOutputChange([
            outputDir.absolutePath,
            destroyableDir.absolutePath,
            localStateDir.absolutePath
        ])

        then:
        1 * delegate.execute(context) >> delegateResult
        0 * _
    }
}
