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
package com.hyeonslab.ssg.page

import kotlinx.html.FlowContent

/**
 * Represents a page in the static site.
 *
 * @property title Page title used in navigation menus
 * @property outputFilename Output HTML filename (e.g., "index.html", "about.html")
 * @property content Function to generate page content
 * @property footer Optional function to generate page footer (defaults to null)
 * @property pageTitle Optional HTML `<title>` for this page; falls back to the site-level title
 * @property metaDescription Optional `<meta name="description">` content (150–160 chars
 *   recommended)
 * @property ogImage Optional absolute URL for `<meta property="og:image">`; falls back to the
 *   site-level `defaultOgImage`
 */
interface Page {
  val title: String
  val outputFilename: String
  val content: (PageSettings, FlowContent) -> Unit
  val footer: ((PageSettings, FlowContent) -> Unit)?
    get() = null

  val pageTitle: String?
    get() = null

  val metaDescription: String?
    get() = null

  val ogImage: String?
    get() = null
}
