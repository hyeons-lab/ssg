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

import com.hyeonslab.ssg.core.TextConfig
import com.hyeonslab.ssg.utils.Tailwind
import kotlinx.serialization.Serializable

/**
 * Typography and styling configuration for page content.
 *
 * Defines default text styling for headings and body text across all pages. These settings are
 * passed to each page's content and footer functions.
 *
 * @property h1 Text configuration for h1 headings (size, font, color)
 * @property bodyTextColor Default text color for body content
 *
 * Example:
 * ```kotlin
 * val pageSettings = PageSettings(
 *     h1 = TextConfig(
 *         textSize = Tailwind.Text.Size.`4xl`.size,  // "text-4xl"
 *         font = "font-plex-serif",
 *         textColor = Tailwind.Colors.Text.Neutral.`900`
 *     ),
 *     bodyTextColor = Tailwind.Colors.Text.Neutral.`600`  // "text-neutral-600"
 * )
 * ```
 *
 * Usage in page content:
 * ```kotlin
 * override val content: (PageSettings, FlowContent) -> Unit = { settings, flow ->
 *     flow.div {
 *         h1(classes = "${settings.h1.textSize} ${settings.h1.font}") {
 *             +"Page Title"
 *         }
 *         p(classes = settings.bodyTextColor.color) {
 *             +"Body text content"
 *         }
 *     }
 * }
 * ```
 *
 * @see TextConfig
 * @see Page
 * @see com.hyeonslab.ssg.core.Site
 */
@Serializable
data class PageSettings(
  val h1: TextConfig =
    TextConfig(
      textSize = Tailwind.Text.Size.`2xl`.size,
      font = "font-plex-serif", // Default: IBM Plex Serif
      textColor = Tailwind.Colors.Text.Neutral.`600`,
    ),
  val bodyTextColor: Tailwind.Colors.Text = Tailwind.Colors.Text.Neutral.`600`,
)
