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
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.use
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.Serializable

/**
 * Configuration for copying a static resource from classpath to the file system.
 *
 * Represents a mapping between an input file in `src/main/resources/` and an output location in the
 * generated site. Supports optional custom output filename for renaming during the copy operation.
 *
 * @property inputFilename Relative path to the input file in `src/main/resources/` (e.g.,
 *   "images/logo.png")
 * @property outputPath Directory where the file should be copied (e.g., "build/generated_html")
 * @property outputFilename Optional custom output filename relative to outputPath (e.g.,
 *   "assets/logo.png")
 *
 * Example (simple copy):
 * ```kotlin
 * val resource = InputOutputPair(
 *     inputFilename = "images/logo.png",
 *     outputPath = "build/generated_html"
 * )
 * // Copies: src/main/resources/images/logo.png -> build/generated_html/images/logo.png
 * ```
 *
 * Example (rename during copy):
 * ```kotlin
 * val resource = InputOutputPair(
 *     inputFilename = "styles/main.css",
 *     outputPath = "build/generated_html",
 *     outputFilename = "css/styles.css"
 * )
 * // Copies: src/main/resources/styles/main.css -> build/generated_html/css/styles.css
 * ```
 *
 * @see copyResource
 * @see ResourceConfig
 */
@Serializable
data class InputOutputPair(
  val inputFilename: String,
  val outputPath: String,
  val outputFilename: String? = null,
)

/**
 * Copies a resource from the classpath to the file system.
 *
 * This function performs strict path validation to prevent directory traversal attacks, then copies
 * the specified resource from `src/main/resources/` to the output directory.
 *
 * Security: All paths are validated using `Paths.normalize()` to prevent path traversal attacks.
 * The function rejects:
 * - Absolute paths (e.g., `/etc/passwd`)
 * - Paths that traverse outside the base directory (e.g., `../../../etc/passwd`)
 * - Obfuscated traversal attempts (e.g., `./foo/../../etc/passwd`)
 *
 * @throws IllegalArgumentException if any path is absolute or attempts directory traversal
 * @throws IllegalStateException if the resource is not found in the classpath
 *
 * Example:
 * ```kotlin
 * InputOutputPair(
 *     inputFilename = "css/style.css",        // Must be in src/main/resources/
 *     outputPath = "build/generated_html",
 *     outputFilename = "assets/style.css"     // Optional custom output name
 * ).copyResource()
 * ```
 *
 * Path validation examples:
 * - ✅ Valid: `"css/style.css"`, `"images/logo.png"`
 * - ❌ Rejected: `"/etc/passwd"` (absolute path)
 * - ❌ Rejected: `"../../../etc/passwd"` (directory traversal)
 * - ❌ Rejected: `"./foo/../../etc/passwd"` (obfuscated traversal)
 */
fun InputOutputPair.copyResource() {
  // Validate paths to prevent path traversal attacks
  // Use proper path normalization to catch all traversal attempts
  fun validatePath(path: String, name: String) {
    val normalized = Paths.get(path).normalize()
    require(!normalized.isAbsolute) { "$name cannot be an absolute path: $path" }
    require(!normalized.toString().startsWith("..")) {
      "$name cannot traverse outside base directory: $path"
    }
  }

  validatePath(inputFilename, "Input filename")
  validatePath(outputPath, "Output path")
  outputFilename?.let { validatePath(it, "Output filename") }

  // use the outputFilename relative to the outputPath, otherwise use the inputFilename
  val candidateOutputFilename = (outputFilename ?: inputFilename).let { "$outputPath/$it" }

  // Create parent directories (thread-safe, creates all parents, idempotent)
  val outputFile = File(candidateOutputFilename)
  outputFile.parentFile?.let { parent -> Files.createDirectories(parent.toPath()) }
  ClassLoader.getSystemResourceAsStream(inputFilename)?.asSource()?.buffered()?.use { input ->
    outputFile.outputStream().asSink().buffered().use { outputFileBuffer ->
      outputFileBuffer.transferFrom(input)
    }
  }
    ?: error(
      "Resource not found in classpath: $inputFilename\n" +
        "Troubleshooting:\n" +
        "  1. Verify the file exists in src/main/resources/$inputFilename\n" +
        "  2. Check that the file is included in your build (not in .gitignore)\n" +
        "  3. Run './gradlew clean build' to ensure resources are copied\n" +
        "  4. Verify the path uses forward slashes (/) not backslashes (\\)\n" +
        "ClassLoader searched: ${ClassLoader.getSystemClassLoader()}"
    )
}
