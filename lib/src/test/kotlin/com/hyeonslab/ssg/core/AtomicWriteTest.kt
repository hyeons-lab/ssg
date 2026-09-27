/*
 * Copyright 2024-2026 Hyeons' Lab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyeonslab.ssg.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

class AtomicWriteTest :
  FunSpec({
    context("replaceAtomically") {
      test("should replace content on success") {
        val dir = File("build/test-atomic-success")
        try {
          Files.createDirectories(dir.toPath())
          val target = File(dir, "target.txt")
          target.writeText("original")
          target.replaceAtomically { it.writeText("replaced") }

          target.readText() shouldBe "replaced"
        } finally {
          dir.deleteRecursively()
        }
      }

      test("should write new file on success and create parent directories as needed") {
        val dir = File("build/test-atomic-nested/child")
        try {
          val target = File(dir, "new-file.txt")
          target.replaceAtomically { it.writeText("created") }

          target.readText() shouldBe "created"
        } finally {
          File("build/test-atomic-nested").deleteRecursively()
        }
      }

      test("should preserve content and remove temp file on failure") {
        val dir = File("build/test-atomic-failure")
        try {
          Files.createDirectories(dir.toPath())
          val target = File(dir, "victim.txt")
          target.writeText("original")
          shouldThrow<IllegalStateException> {
            target.replaceAtomically { throw IllegalStateException("boom") }
          }

          target.readText() shouldBe "original"
          val leftovers = dir.listFiles { _, name -> name.startsWith("ssg-") } ?: emptyArray()
          leftovers.size shouldBe 0
        } finally {
          dir.deleteRecursively()
        }
      }
    }
  })
