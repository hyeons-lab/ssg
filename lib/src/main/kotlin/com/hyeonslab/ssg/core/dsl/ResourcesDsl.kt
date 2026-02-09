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
package com.hyeonslab.ssg.core.dsl

import com.hyeonslab.ssg.core.ExternalStylesheet
import com.hyeonslab.ssg.core.InputOutputPair
import com.hyeonslab.ssg.core.ResourceConfig

/**
 * DSL builder for configuring static resources and stylesheets.
 *
 * Provides a type-safe way to configure static files (images, CSS, fonts), local stylesheets, and
 * external CDN stylesheets using Kotlin's DSL syntax.
 *
 * Example:
 * ```kotlin
 * resources {
 *     // Add static files to copy from classpath
 *     staticFile("images/logo.png", "build/generated_html")
 *     staticFile("images/hero.jpg", "build/generated_html")
 *     staticFile("fonts/custom.woff2", "build/generated_html", "assets/fonts/custom.woff2")
 *
 *     // Add local CSS files
 *     localStylesheet("css/tailwind.css")
 *     localStylesheet("css/custom.css")
 *
 *     // Add external stylesheets (CDNs)
 *     externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
 *     externalStylesheet(ExternalStylesheet(
 *         href = "https://fonts.googleapis.com/css2?family=Inter"
 *     ))
 *
 *     // Batch operations
 *     localStylesheets("css/tailwind.css", "css/theme.css", "css/custom.css")
 *     externalStylesheets(
 *         ExternalStylesheet.FONT_AWESOME_6_7_2,
 *         ExternalStylesheet.TAILWIND_CSS_3_4_17
 *     )
 * }
 * ```
 *
 * @see ResourceConfig
 * @see ExternalStylesheet
 * @see InputOutputPair
 * @see SiteBuilder
 */
@SsgDsl
class ResourcesBuilder {
  private val staticFiles = mutableListOf<InputOutputPair>()
  private val localStylesheets = mutableListOf<String>()
  private val externalStylesheets = mutableListOf<ExternalStylesheet>()

  /**
   * Add a static file to be copied from classpath to output directory.
   *
   * Example:
   * ```kotlin
   * staticFile("images/logo.png", outputPath = "build/generated_html")
   * staticFile("css/custom.css", outputPath = "build/generated_html", outputFilename = "css/styles.css")
   * ```
   */
  fun staticFile(inputFilename: String, outputPath: String, outputFilename: String? = null) {
    staticFiles.add(
      InputOutputPair(
        inputFilename = inputFilename,
        outputPath = outputPath,
        outputFilename = outputFilename,
      )
    )
  }

  /**
   * Add multiple static files at once.
   *
   * Example:
   * ```kotlin
   * staticFiles(
   *     InputOutputPair("images/logo.png", "build/html"),
   *     InputOutputPair("images/hero.jpg", "build/html")
   * )
   * ```
   */
  fun staticFiles(vararg files: InputOutputPair) {
    staticFiles.addAll(files)
  }

  /**
   * Add a local stylesheet path (relative to output directory).
   *
   * Example:
   * ```kotlin
   * localStylesheet("css/tailwind.css")
   * localStylesheet("css/custom.css")
   * ```
   */
  fun localStylesheet(path: String) {
    localStylesheets.add(path)
  }

  /**
   * Add multiple local stylesheets at once.
   *
   * Example:
   * ```kotlin
   * localStylesheets("css/tailwind.css", "css/theme.css", "css/custom.css")
   * ```
   */
  fun localStylesheets(vararg paths: String) {
    localStylesheets.addAll(paths)
  }

  /**
   * Add an external stylesheet (e.g., CDN) with optional SRI integrity.
   *
   * Example:
   * ```kotlin
   * externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
   * externalStylesheet(ExternalStylesheet(href = "https://fonts.googleapis.com/css2?family=Inter"))
   * ```
   */
  fun externalStylesheet(stylesheet: ExternalStylesheet) {
    externalStylesheets.add(stylesheet)
  }

  /**
   * Add multiple external stylesheets at once.
   *
   * Example:
   * ```kotlin
   * externalStylesheets(
   *     ExternalStylesheet.FONT_AWESOME_6_7_2,
   *     ExternalStylesheet(href = "https://cdn.example.com/style.css")
   * )
   * ```
   */
  fun externalStylesheets(vararg stylesheets: ExternalStylesheet) {
    externalStylesheets.addAll(stylesheets)
  }

  /** Build the ResourceConfig instance from the configured values. */
  fun build(): ResourceConfig {
    return ResourceConfig(
      staticFiles = staticFiles.toList(),
      localStylesheets = localStylesheets.ifEmpty { listOf("css/tailwind.css") },
      externalStylesheets = externalStylesheets.toList(),
    )
  }
}
