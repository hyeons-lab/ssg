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

import com.hyeonslab.ssg.page.NavMenuSettings
import com.hyeonslab.ssg.page.Page
import com.hyeonslab.ssg.page.PageSettings
import com.hyeonslab.ssg.page.navMenu
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.stream.appendHTML
import kotlinx.html.title
import kotlinx.html.unsafe

/**
 * Main configuration for a static site generator.
 *
 * Simplified constructor with logical grouping of configuration parameters. This is a breaking
 * change from previous versions which had 17 individual parameters.
 *
 * @property outputPath Directory where generated HTML files will be written. **Security Warning:**
 *   Use relative paths within your project directory (e.g., "build/generated_html"). Avoid absolute
 *   paths or system directories (e.g., /etc, /usr, /home) to prevent accidental file overwrites. Be
 *   cautious when using `File(outputPath).deleteRecursively()` - verify the path before deleting.
 * @property title Site title (used in HTML <title> tags when a page doesn't set its own)
 * @property version Site version for tracking (e.g., "1.0.0", "2024-02-08")
 * @property backgroundColor Tailwind background color class for page body
 * @property htmlClasses Tailwind classes for <html> element (default: empty)
 * @property bodyClasses Tailwind classes for <body> element (default: flexbox layout)
 * @property contentClasses Tailwind classes for content wrapper (default: flex-grow layout)
 * @property pages List of pages to generate
 * @property navigation Navigation menu configuration (colors, logo, social links), optional
 * @property resources Static resources and stylesheets configuration
 * @property integrations Third-party integrations (analytics, tracking)
 * @property pageSettings Page-specific styling configuration
 * @property baseUrl Canonical base URL of the site (e.g., "https://example.com") used for `<link
 *   rel="canonical">`, Open Graph tags, and sitemap generation. No trailing slash.
 * @property defaultOgImage Absolute URL of the default Open Graph image used when a page does not
 *   set its own `ogImage` (e.g., "https://example.com/images/og-default.jpg")
 * @property lang BCP-47 language code for the `<html lang>` attribute (default: `"en"`)
 * @property ogSiteName Brand name for the `og:site_name` meta tag. When null, falls back to
 *   [title]. Use this to specify a shorter or distinct brand name separate from the page title.
 */
data class Site(
  // Core configuration
  val outputPath: String,
  val title: String,
  val version: String = "1.0.0",

  // Page styling
  val backgroundColor: String = "bg-white",
  val htmlClasses: String = "",
  val bodyClasses: String = "flex flex-col min-h-screen",
  val contentClasses: String = "flex-1 flex flex-col",

  // Content
  val pages: List<Page>,

  // Navigation configuration (includes logo, colors, social links)
  val navigation: NavMenuSettings? = null,

  // Resources and stylesheets
  val resources: ResourceConfig = ResourceConfig(),

  // Third-party integrations
  val integrations: IntegrationConfig = IntegrationConfig(),

  // Page-specific settings
  val pageSettings: PageSettings = PageSettings(),

  // SEO configuration
  val baseUrl: String? = null,
  val defaultOgImage: String? = null,
  val lang: String = "en",
  val ogSiteName: String? = null,
) {
  init {
    // Validate CSS class strings to prevent HTML attribute injection
    fun validateCssClasses(classes: String, fieldName: String) {
      if (classes.isEmpty()) return // Empty strings are allowed
      require(classes.matches(Regex("^[a-zA-Z0-9\\s\\-_:/\\[\\].%]+$"))) {
        "$fieldName contains invalid characters: '$classes'\n" +
          "Allowed characters: letters, numbers, spaces, hyphens, underscores, colons, slashes, brackets, dots, percent signs\n" +
          "Valid examples: 'bg-white', 'text-blue-600 hover:text-blue-700', 'w-1/2', 'z-[255]', 'bg-white/90'\n" +
          "This validation prevents HTML attribute injection attacks."
      }
    }

    validateCssClasses(backgroundColor, "backgroundColor")
    validateCssClasses(htmlClasses, "htmlClasses")
    validateCssClasses(bodyClasses, "bodyClasses")
    validateCssClasses(contentClasses, "contentClasses")
  }

  /**
   * Copies all configured static resources from classpath to the output directory.
   *
   * Iterates through all `staticFiles` in the `resources` configuration and copies each file from
   * `src/main/resources/` to the specified output location. Files can optionally be renamed during
   * the copy operation.
   *
   * This method validates all file paths to prevent directory traversal attacks and provides
   * detailed error messages if any resources fail to copy.
   *
   * @throws IllegalStateException if any resources fail to copy (provides details of all failures)
   * @throws IllegalArgumentException if any paths are invalid or attempt directory traversal
   *
   * Example:
   * ```kotlin
   * val site = site {
   *     outputPath = "build/generated_html"
   *     // ... other configuration
   *     resources {
   *         staticFile("images/logo.png", outputPath)
   *         staticFile("css/tailwind.css", outputPath)
   *     }
   * }
   *
   * site.copyResources()  // Copies all configured resources
   * ```
   *
   * @see InputOutputPair
   * @see ResourceConfig
   */
  fun copyResources() {
    val results =
      resources.staticFiles.map { resource -> resource to runCatching { resource.copyResource() } }

    // Report any failures
    val failures = results.filter { it.second.isFailure }
    if (failures.isNotEmpty()) {
      val errorMessage =
        failures.joinToString("\n") { (resource, result) ->
          "  - ${resource.inputFilename}: ${result.exceptionOrNull()?.message}"
        }
      error("Failed to copy ${failures.size} resource(s):\n$errorMessage")
    }
  }

  /**
   * Generates HTML files for all configured pages.
   *
   * Creates complete HTML documents for each page in the `pages` list, including:
   * - HTML head with meta tags, title, and stylesheets
   * - Optional navigation menu (if configured)
   * - Page content
   * - Optional page footer (if provided by the page)
   * - Optional Google Analytics tracking code (if configured)
   *
   * All HTML files are written to the `outputPath` directory. The directory and any required parent
   * directories are created automatically if they don't exist.
   *
   * This method provides comprehensive error handling and reports detailed information about any
   * pages that fail to generate.
   *
   * @throws IllegalStateException if output directory cannot be created
   * @throws IllegalStateException if any pages fail to generate (provides details of all failures)
   *
   * Example:
   * ```kotlin
   * val site = site {
   *     outputPath = "build/generated_html"
   *     title = "My Site"
   *     pages = listOf(HomePage, AboutPage, ContactPage)
   *     // ... other configuration
   * }
   *
   * // Generate all HTML files
   * site.generateFiles()
   * // Creates: build/generated_html/index.html
   * //          build/generated_html/about.html
   * //          build/generated_html/contact.html
   * ```
   *
   * @see Page
   * @see NavMenuSettings
   * @see copyResources
   */
  fun generateFiles() {
    try {
      // Create output directory (thread-safe, creates parents, idempotent)
      Files.createDirectories(Paths.get(outputPath))
    } catch (e: Exception) {
      error("Failed to create output directory '$outputPath': ${e.message}")
    }

    val results = mutableListOf<Pair<String, Result<Unit>>>()

    pages.forEach { page ->
      val result = runCatching {
        val generatedHtml = buildString {
          appendHTML().html {
            attributes["lang"] = this@Site.lang
            if (htmlClasses.isNotEmpty()) {
              attributes["class"] = htmlClasses
            }
            head {
              title { +(page.pageTitle ?: this@Site.title) }
              meta { charset = "utf-8" }
              meta {
                name = "viewport"
                content = "width=device-width, initial-scale=1.0"
              }
              // Per-page meta description
              page.metaDescription?.let { desc ->
                meta {
                  name = "description"
                  content = desc
                }
              }
              // Canonical URL
              baseUrl?.let { base ->
                val path =
                  if (page.outputFilename == "index.html") "/" else "/${page.outputFilename}"
                link {
                  rel = "canonical"
                  href = "$base$path"
                }
              }
              // Open Graph + Twitter Card (requires baseUrl for absolute URLs)
              baseUrl?.let { base ->
                val path =
                  if (page.outputFilename == "index.html") "/" else "/${page.outputFilename}"
                val canonicalUrl = "$base$path"
                val ogTitle = page.pageTitle ?: this@Site.title
                meta {
                  attributes["property"] = "og:type"
                  content = "website"
                }
                meta {
                  attributes["property"] = "og:site_name"
                  content = this@Site.ogSiteName ?: this@Site.title
                }
                meta {
                  attributes["property"] = "og:title"
                  content = ogTitle
                }
                page.metaDescription?.let { desc ->
                  meta {
                    attributes["property"] = "og:description"
                    content = desc
                  }
                }
                meta {
                  attributes["property"] = "og:url"
                  content = canonicalUrl
                }
                (page.ogImage ?: defaultOgImage)?.let { img ->
                  meta {
                    attributes["property"] = "og:image"
                    content = img
                  }
                }
                meta {
                  name = "twitter:card"
                  content = "summary_large_image"
                }
              }
              // JSON-LD structured data
              page.structuredData?.let { json ->
                script(type = "application/ld+json") { unsafe { +json } }
              }
              // Include local stylesheets
              resources.localStylesheets.forEach { cssPath ->
                link {
                  href = cssPath
                  rel = "stylesheet"
                }
              }
              // Include external stylesheets (e.g., Font Awesome, Google Fonts)
              resources.externalStylesheets.forEach { stylesheet ->
                link {
                  rel = "stylesheet"
                  href = stylesheet.href
                  stylesheet.integrity?.let { integrity = it }
                  stylesheet.crossorigin?.let { attributes["crossorigin"] = it }
                  stylesheet.referrerpolicy?.let { attributes["referrerpolicy"] = it }
                }
              }
              integrations.googleTagId?.let { googleTag(it) }
            }
            body(classes = "$backgroundColor $bodyClasses".trim()) {
              // Only render navigation if configured
              navigation?.let { nav ->
                navMenu(selected = page, navMenuSettings = nav, pages = pages)
              }
              div(classes = contentClasses) { page.content(pageSettings, this) }
              // Only render footer if page provides one
              page.footer?.let { footerFn -> div { footerFn(pageSettings, this) } }
            }
          }
        }
        File("$outputPath/${page.outputFilename}").writeText(generatedHtml)
      }
      results.add(page.outputFilename to result)
    }

    // Report any failures
    val failures = results.filter { it.second.isFailure }
    if (failures.isNotEmpty()) {
      val errorMessage =
        failures.joinToString("\n") { (filename, result) ->
          "  - $filename: ${result.exceptionOrNull()?.message}"
        }
      error("Failed to generate ${failures.size} file(s):\n$errorMessage")
    }
  }

  /**
   * Generates a `sitemap.xml` file in the output directory listing all page URLs.
   *
   * Only runs when [baseUrl] is set on this site. If [baseUrl] is null this method returns without
   * creating any file, so it is safe to call unconditionally.
   *
   * The sitemap uses today's date as the `<lastmod>` value for every URL.
   *
   * Example:
   * ```kotlin
   * val site = site {
   *     outputPath = "build/generated_html"
   *     baseUrl = "https://example.com"
   *     pages = listOf(HomePage, AboutPage)
   * }
   * File(outputPath).deleteRecursively()
   * site.generateFiles()
   * site.generateSitemap()  // writes build/generated_html/sitemap.xml
   * site.copyResources()
   * ```
   */
  fun generateSitemap() {
    val base = baseUrl ?: return
    val today = LocalDate.now()
    val xml = buildString {
      appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
      appendLine("""<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">""")
      pages.forEach { page ->
        val path = if (page.outputFilename == "index.html") "/" else "/${page.outputFilename}"
        appendLine("  <url>")
        appendLine("    <loc>$base$path</loc>")
        appendLine("    <lastmod>$today</lastmod>")
        appendLine("  </url>")
      }
      appendLine("</urlset>")
    }
    File("$outputPath/sitemap.xml").writeText(xml)
  }

  /**
   * Generates a `robots.txt` file in the output directory.
   *
   * Only runs when [baseUrl] is set on this site. If [baseUrl] is null this method returns without
   * creating any file, so it is safe to call unconditionally.
   *
   * The generated file allows all user agents and includes a `Sitemap:` directive pointing to
   * `sitemap.xml` at the base URL.
   */
  fun generateRobotsTxt() {
    val base = baseUrl ?: return
    val txt = buildString {
      appendLine("User-agent: *")
      appendLine("Allow: /")
      appendLine("Sitemap: $base/sitemap.xml")
    }
    File("$outputPath/robots.txt").writeText(txt)
  }
}
