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

import com.hyeonslab.ssg.utils.Tailwind
import kotlinx.serialization.Serializable

/**
 * Configuration for text styling including size, font family, and color.
 *
 * This class is typically used to configure typography settings for headings and body text across
 * the site.
 *
 * @property textSize Tailwind text size class (e.g., "text-xl", "text-3xl")
 * @property font Tailwind font family class (e.g., "font-plex-sans", "font-serif")
 * @property textColor Tailwind text color from the Tailwind.Colors.Text enum
 *
 * Example:
 * ```kotlin
 * val h1Config = TextConfig(
 *     textSize = Tailwind.Text.Size.`4xl`.size,  // "text-4xl"
 *     font = "font-plex-serif",
 *     textColor = Tailwind.Colors.Text.Neutral.`900`  // "text-neutral-900"
 * )
 * ```
 *
 * @see PageSettings
 * @see Tailwind.Text.Size
 * @see Tailwind.Colors.Text
 */
@Serializable
data class TextConfig(val textSize: String, val font: String, val textColor: Tailwind.Colors.Text)
