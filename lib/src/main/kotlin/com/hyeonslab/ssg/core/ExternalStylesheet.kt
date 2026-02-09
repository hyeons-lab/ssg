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

import kotlinx.serialization.Serializable

/**
 * Configuration for external stylesheets loaded from CDNs or external URLs.
 *
 * @property href URL to the stylesheet
 * @property integrity Subresource Integrity (SRI) hash for security (optional)
 * @property crossorigin CORS setting (e.g., "anonymous")
 * @property referrerpolicy Referrer policy for the request
 */
@Serializable
data class ExternalStylesheet(
  val href: String,
  val integrity: String? = null,
  val crossorigin: String? = null,
  val referrerpolicy: String? = null,
) {
  companion object {
    /**
     * Font Awesome 6.7.2 CDN configuration with SRI hash.
     *
     * Pre-configured CDN link for Font Awesome icon library with Subresource Integrity (SRI) hash
     * for security. Font Awesome is now opt-in and must be explicitly added if needed.
     *
     * Usage:
     * ```kotlin
     * resources {
     *     externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
     * }
     * ```
     *
     * @see <a href="https://fontawesome.com/">Font Awesome</a>
     */
    val FONT_AWESOME_6_7_2 =
      ExternalStylesheet(
        href = "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css",
        integrity =
          "sha512-Evv84Mr4kqVGRNSgIGL/F/aIDqQb7xQ2vcrdIwxfjThSH8CSR7PBEakCr51Ck+w+/U6swU2Im1vVX0SVk9ABhg==",
        crossorigin = "anonymous",
        referrerpolicy = "no-referrer",
      )

    /**
     * Tailwind CSS 3.4.17 Play CDN configuration.
     *
     * Pre-configured CDN link for Tailwind CSS Play CDN. This is designed for development and
     * prototyping purposes only. The Play CDN includes the full Tailwind CSS framework and
     * processes classes at runtime using JavaScript.
     *
     * **Important:** For production sites, use a custom Tailwind build with the Tailwind CLI or
     * Gradle plugin to generate optimized CSS containing only the classes you use.
     *
     * Usage (development only):
     * ```kotlin
     * resources {
     *     externalStylesheet(ExternalStylesheet.TAILWIND_CSS_3_4_17)
     * }
     * ```
     *
     * @see <a href="https://tailwindcss.com/docs/installation/play-cdn">Tailwind Play CDN
     *   Documentation</a>
     */
    val TAILWIND_CSS_3_4_17 = ExternalStylesheet(href = "https://cdn.tailwindcss.com/3.4.17")
  }
}
