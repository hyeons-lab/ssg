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
import com.hyeonslab.ssg.utils.encodeUrlPath
import com.hyeonslab.ssg.utils.escapeXml
import com.hyeonslab.ssg.utils.validateCssClasses
import com.hyeonslab.ssg.utils.validateRelativePath
import com.hyeonslab.ssg.utils.validateUrlChars
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
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

/** Plausible BCP-47 language tag for the `<html lang>` attribute. */
private val LANG_REGEX = Regex("^[a-zA-Z]{2,8}(-[a-zA-Z0-9]{2,8})*$")

/**
 * The site-root-relative URL path for a page: "/" for index.html, otherwise the URL-encoded
 * filename. Shared by the canonical/og:url tags and the sitemap so they cannot drift apart.
 */
private fun pageUrlPath(page: Page): String =
  if (page.outputFilename == "index.html") "/" else "/" + encodeUrlPath(page.outputFilename)

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
    validateRelativePath(outputPath, "outputPath")
    require(title.isNotBlank()) { "title cannot be blank" }

    // Validate CSS class strings to prevent HTML attribute injection
    validateCssClasses(backgroundColor, "backgroundColor")
    validateCssClasses(htmlClasses, "htmlClasses")
    validateCssClasses(bodyClasses, "bodyClasses")
    validateCssClasses(contentClasses, "contentClasses")

    // Page output filenames must be unique (otherwise generated files silently overwrite each
    // other) and must stay inside the output directory.
    val normalizedNames =
      pages.map { Path.of(it.outputFilename.replace('\\', '/')).normalize().toString() }
    require(normalizedNames.toSet().size == pages.size) {
      val duplicates = normalizedNames.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
      "Duplicate page outputFilename(s): ${duplicates.joinToString()}\n" +
        "Each page must have a distinct outputFilename or generated files will overwrite each other."
    }
    pages.forEach { page ->
      validateRelativePath(page.outputFilename, "Page outputFilename '${page.outputFilename}'")
      page.ogImage?.let { validateUrlChars(it, "Page ogImage") }
    }

    // Validate lang is a plausible BCP-47 tag
    require(lang.matches(LANG_REGEX)) {
      "lang contains an invalid BCP-47 language tag: '$lang'\n" +
        "Valid examples: 'en', 'es', 'zh-Hant', 'pt-BR'\n" +
        "This validation prevents HTML attribute injection via the lang attribute."
    }

    baseUrl?.let { url ->
      require(url.isNotBlank()) { "baseUrl cannot be blank" }
      require(!url.endsWith("/")) {
        "baseUrl must not have a trailing slash: '$url'\n" +
          "Valid example: 'https://example.com' (not 'https://example.com/')"
      }
      validateUrlChars(url, "baseUrl")
    }
    defaultOgImage?.let { validateUrlChars(it, "defaultOgImage") }

    resources.localStylesheets.forEach { cssPath ->
      validateRelativePath(cssPath, "localStylesheet '$cssPath'")
      validateUrlChars(cssPath, "localStylesheet '$cssPath'")
    }
    resources.externalStylesheets.forEach { stylesheet ->
      validateUrlChars(stylesheet.href, "externalStylesheet href '${stylesheet.href}'")
    }
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
      ensureOutputDir()
    } catch (e: Exception) {
      error("Failed to create output directory '$outputPath': ${e.message}")
    }

    val results = mutableListOf<Pair<String, Result<Unit>>>()

    pages.forEach { page ->
      val result = runCatching {
        val generatedHtml = buildString {
          appendLine("<!DOCTYPE html>")
          appendHTML().html {
            attributes["lang"] = this@Site.lang
            if (htmlClasses.isNotEmpty()) {
              attributes["class"] = htmlClasses
            }
            head {
              // Fall back to the site title/description when a page omits or blanks its own
              val effectiveTitle = page.pageTitle?.takeIf { it.isNotBlank() } ?: this@Site.title
              val description = page.metaDescription?.takeIf { it.isNotBlank() }
              title { +effectiveTitle }
              meta { charset = "utf-8" }
              meta {
                name = "viewport"
                content = "width=device-width, initial-scale=1.0"
              }
              // Site version for tracking
              if (this@Site.version.isNotBlank()) {
                meta {
                  name = "version"
                  content = this@Site.version
                }
              }
              // Per-page meta description
              description?.let { desc ->
                meta {
                  name = "description"
                  content = desc
                }
              }
              // Canonical URL + Open Graph + Twitter Card (requires baseUrl for absolute URLs)
              baseUrl?.let { base ->
                val canonicalUrl = "$base${pageUrlPath(page)}"
                link {
                  rel = "canonical"
                  href = canonicalUrl
                }
                meta {
                  attributes["property"] = "og:type"
                  content = page.ogType ?: "website"
                }
                meta {
                  attributes["property"] = "og:site_name"
                  content = this@Site.ogSiteName ?: this@Site.title
                }
                meta {
                  attributes["property"] = "og:title"
                  content = effectiveTitle
                }
                description?.let { desc ->
                  meta {
                    attributes["property"] = "og:description"
                    content = desc
                  }
                }
                meta {
                  attributes["property"] = "og:url"
                  content = canonicalUrl
                }
                val cardImage = page.ogImage ?: defaultOgImage
                cardImage?.let { img ->
                  meta {
                    attributes["property"] = "og:image"
                    content = img
                  }
                }
                meta {
                  name = "twitter:card"
                  content = if (cardImage != null) "summary_large_image" else "summary"
                }
              }
              // JSON-LD structured data. Escape angle brackets as their JSON unicode escapes
              // (< / >) so the content cannot terminate the <script> tag or trigger the
              // script-data escaped states (e.g. via "<!--<script>").
              page.structuredData
                ?.takeIf { it.isNotBlank() }
                ?.let { json ->
                  script(type = "application/ld+json") {
                    unsafe { +json.replace("<", "\\u003c").replace(">", "\\u003e") }
                  }
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
        writeFileAtomically(File(outputPath, page.outputFilename), generatedHtml)
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
   * Generates a `sitemap.xml` file in the output directory listing all page URLs. URLs are
   * URL-encoded and XML-escaped so filenames with spaces or reserved characters stay well-formed.
   *
   * Only runs when [baseUrl] is set on this site. If [baseUrl] is null this method returns without
   * creating any file, so it is safe to call unconditionally.
   *
   * @param lastmod Optional `<lastmod>` date applied to every URL. When null (the default) no
   *   `<lastmod>` is emitted, keeping output deterministic; pass a fixed date for reproducible
   *   builds. Prefer [generate] to produce the pages, sitemap, and robots.txt together.
   */
  fun generateSitemap(lastmod: LocalDate? = null) {
    val base = baseUrl ?: return
    ensureOutputDir()
    val xml = buildString {
      appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
      appendLine("""<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">""")
      pages.forEach { page ->
        appendLine("  <url>")
        appendLine("    <loc>${escapeXml("$base${pageUrlPath(page)}")}</loc>")
        lastmod?.let { appendLine("    <lastmod>$it</lastmod>") }
        appendLine("  </url>")
      }
      appendLine("</urlset>")
    }
    writeFileAtomically(File(outputPath, "sitemap.xml"), xml)
  }

  /**
   * Generates a `robots.txt` file in the output directory.
   *
   * Only runs when [baseUrl] is set on this site. If [baseUrl] is null this method returns without
   * creating any file, so it is safe to call unconditionally.
   *
   * The generated file allows all user agents (empty `Disallow:`, the standard allow-all form) and
   * includes a `Sitemap:` directive pointing to `sitemap.xml` at the base URL.
   */
  fun generateRobotsTxt() {
    val base = baseUrl ?: return
    ensureOutputDir()
    val txt = buildString {
      appendLine("User-agent: *")
      appendLine("Disallow:")
      appendLine("Sitemap: $base/sitemap.xml")
    }
    writeFileAtomically(File(outputPath, "robots.txt"), txt)
  }

  /** Creates the output directory (and parents) if needed. Thread-safe and idempotent. */
  private fun ensureOutputDir() {
    Files.createDirectories(Path.of(outputPath))
  }

  /**
   * Writes content to a file atomically by writing to a temporary file in the target directory and
   * moving it into place. Ensures parent directories exist.
   */
  private fun writeFileAtomically(file: File, content: String) {
    val parentDir = file.parentFile ?: File(outputPath)
    Files.createDirectories(parentDir.toPath())
    val tempFile = File.createTempFile("ssg-", ".tmp", parentDir)
    try {
      tempFile.writeText(content)
      try {
        Files.move(
          tempFile.toPath(),
          file.toPath(),
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING,
        )
      } catch (_: AtomicMoveNotSupportedException) {
        Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
      }
    } catch (e: Throwable) {
      tempFile.delete()
      throw e
    }
  }

  /**
   * Convenience entry point: generates the HTML pages and, when [baseUrl] is set, the matching
   * `sitemap.xml` and `robots.txt`. Producing them together keeps robots.txt from advertising a
   * sitemap that was never generated.
   *
   * @param sitemapLastmod Optional `<lastmod>` date for the sitemap (see [generateSitemap]).
   */
  fun generate(sitemapLastmod: LocalDate? = null) {
    generateFiles()
    generateSitemap(sitemapLastmod)
    generateRobotsTxt()
  }
}
