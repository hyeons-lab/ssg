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

import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Replaces this file's content by writing through a uniquely-named temporary file in the target
 * directory, then moving it into place. Parent directories are created as needed. The output file
 * is never observed half-written: readers see either the previous content or the new content.
 *
 * On failure the temporary file is removed (a failed cleanup is attached to the thrown error as a
 * suppressed exception, with a `deleteOnExit` fallback) and the original error is rethrown.
 */
internal fun File.replaceAtomically(write: (temp: File) -> Unit) {
  val dir = parentFile ?: File(".")
  Files.createDirectories(dir.toPath())
  val tempFile = File.createTempFile("ssg-", ".tmp", dir)
  try {
    write(tempFile)
    // Prefer an atomic move so the output file is never observed half-written. Not all
    // filesystems support ATOMIC_MOVE, so fall back to a plain replacing move when they don't.
    try {
      Files.move(
        tempFile.toPath(),
        toPath(),
        StandardCopyOption.ATOMIC_MOVE,
        StandardCopyOption.REPLACE_EXISTING,
      )
    } catch (_: AtomicMoveNotSupportedException) {
      Files.move(tempFile.toPath(), toPath(), StandardCopyOption.REPLACE_EXISTING)
    } catch (_: UnsupportedOperationException) {
      Files.move(tempFile.toPath(), toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
  } catch (e: Throwable) {
    runCatching { Files.deleteIfExists(tempFile.toPath()) }
      .onFailure { cleanup ->
        e.addSuppressed(cleanup)
        runCatching { tempFile.deleteOnExit() }
      }
    throw e
  }
}
