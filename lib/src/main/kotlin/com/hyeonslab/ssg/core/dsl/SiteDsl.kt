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

import com.hyeonslab.ssg.core.IntegrationConfig
import com.hyeonslab.ssg.core.ResourceConfig
import com.hyeonslab.ssg.core.Site
import com.hyeonslab.ssg.page.Page
import com.hyeonslab.ssg.page.PageSettings

/** DSL marker to prevent accidental scope escaping in nested builders. */
@DslMarker annotation class SsgDsl

/**
 * Top-level DSL function to create a Site instance with type-safe configuration.
 *
 * Provides a clean, readable way to configure a static site using Kotlin's DSL syntax. The DSL
 * reduces boilerplate compared to the constructor API and provides better IDE autocomplete support
 * within each configuration block.
 *
 * @param block Configuration block for the site builder
 * @return Configured Site instance ready for HTML generation
 * @throws IllegalArgumentException if required parameters (outputPath, title, pages) are not
 *   specified
 * @throws IllegalArgumentException if CSS classes contain invalid characters (prevents injection
 *   attacks)
 *
 * Example:
 * ```kotlin
 * val site = site {
 *     // Core configuration (required)
 *     outputPath = "build/generated_html"
 *     title = "My Portfolio"
 *     version = "1.0.0"
 *
 *     // Page styling
 *     backgroundColor = "bg-gray-50"
 *     htmlClasses = ""
 *     bodyClasses = "flex flex-col min-h-screen"
 *     contentClasses = "flex-1 flex flex-col"
 *
 *     // Pages (required)
 *     pages = listOf(HomePage, AboutPage, ContactPage)
 *
 *     // Optional: Navigation menu
 *     navigation {
 *         backgroundColor = "bg-white/90"
 *         navSelectedColor = "text-blue-600"
 *         navDefaultColor = "text-gray-700"
 *         isSticky = true
 *         blurNavBackground = true
 *         logo("images/logo.png", width = 120, height = 60)
 *         instagram = "myhandle"
 *         email = "contact@example.com"
 *     }
 *
 *     // Optional: Resources
 *     resources {
 *         staticFile("images/logo.png", outputPath)
 *         staticFile("css/tailwind.css", outputPath)
 *         localStylesheet("css/tailwind.css")
 *         externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
 *     }
 *
 *     // Optional: Third-party integrations
 *     integrations {
 *         googleTag = "G-ABCD1234EF"
 *     }
 *
 *     // Optional: Typography settings
 *     pageSettings = PageSettings(
 *         h1 = TextConfig(
 *             textSize = Tailwind.Text.Size.`4xl`.size,
 *             font = "font-inter",
 *             textColor = Tailwind.Colors.Text.Neutral.`900`
 *         )
 *     )
 * }
 *
 * // Generate the site
 * File(outputPath).deleteRecursively()
 * site.generateFiles()
 * site.copyResources()
 * ```
 *
 * @see SiteBuilder
 * @see Site
 */
fun site(block: SiteBuilder.() -> Unit): Site {
  return SiteBuilder().apply(block).build()
}

/**
 * DSL builder for creating and configuring Site instances.
 *
 * Provides a structured, type-safe way to build static site configurations using Kotlin's DSL
 * syntax. The builder validates all inputs and provides helpful error messages for missing or
 * invalid configuration.
 *
 * Example:
 * ```kotlin
 * site {
 *     outputPath = "build/generated_html"
 *     title = "My Site"
 *     version = "1.0.0"
 *     pages = listOf(HomePage, AboutPage)
 *
 *     navigation { /* ... */ }
 *     resources { /* ... */ }
 *     integrations { /* ... */ }
 * }
 * ```
 *
 * @see Site
 * @see NavigationBuilder
 * @see ResourcesBuilder
 * @see IntegrationsBuilder
 */
@SsgDsl
class SiteBuilder {
  var outputPath: String? = null
  var title: String? = null
  var version: String = "1.0.0"
  var backgroundColor: String = "bg-white"
  var htmlClasses: String = ""
  var bodyClasses: String = "flex flex-col min-h-screen"
  var contentClasses: String = "flex-1 flex flex-col"
  var pages: List<Page>? = null
  var pageSettings: PageSettings = PageSettings()

  /** Canonical base URL of the site (no trailing slash), e.g. `"https://example.com"`. */
  var baseUrl: String? = null

  /** Absolute URL of the default Open Graph image, e.g. `"https://example.com/images/og.jpg"`. */
  var defaultOgImage: String? = null

  /** BCP-47 language code for the `<html lang>` attribute (default: `"en"`). */
  var lang: String = "en"

  private var navigationBuilder: NavigationBuilder? = null
  private var resourcesBuilder: ResourcesBuilder? = null
  private var integrationsBuilder: IntegrationsBuilder? = null

  /**
   * Configure navigation menu settings using DSL.
   *
   * Example:
   * ```kotlin
   * navigation {
   *     backgroundColor = "bg-white"
   *     navSelectedColor = "text-blue-600"
   *     navDefaultColor = "text-gray-700"
   *     logo("images/logo.png", width = 120, height = 60)
   * }
   * ```
   *
   * @see NavigationBuilder
   */
  fun navigation(block: NavigationBuilder.() -> Unit) {
    navigationBuilder = NavigationBuilder().apply(block)
  }

  /**
   * Configure static resources and stylesheets using DSL.
   *
   * Example:
   * ```kotlin
   * resources {
   *     staticFile("images/logo.png", "build/html")
   *     localStylesheet("css/tailwind.css")
   *     externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
   * }
   * ```
   *
   * @see ResourcesBuilder
   */
  fun resources(block: ResourcesBuilder.() -> Unit) {
    resourcesBuilder = ResourcesBuilder().apply(block)
  }

  /**
   * Configure third-party integrations (analytics, tracking) using DSL.
   *
   * Example:
   * ```kotlin
   * integrations {
   *     googleTag = "G-ABCD1234EF"
   * }
   * ```
   *
   * @see IntegrationsBuilder
   */
  fun integrations(block: IntegrationsBuilder.() -> Unit) {
    integrationsBuilder = IntegrationsBuilder().apply(block)
  }

  /** Build the Site instance from the configured values. */
  fun build(): Site {
    requireNotNull(outputPath) { "outputPath must be specified" }
    requireNotNull(title) { "title must be specified" }
    requireNotNull(pages) { "pages must be specified" }

    return Site(
      outputPath = outputPath!!,
      title = title!!,
      version = version,
      backgroundColor = backgroundColor,
      htmlClasses = htmlClasses,
      bodyClasses = bodyClasses,
      contentClasses = contentClasses,
      pages = pages!!,
      navigation = navigationBuilder?.build(),
      resources = resourcesBuilder?.build() ?: ResourceConfig(),
      integrations = integrationsBuilder?.build() ?: IntegrationConfig(),
      pageSettings = pageSettings,
      baseUrl = baseUrl,
      defaultOgImage = defaultOgImage,
      lang = lang,
    )
  }
}
