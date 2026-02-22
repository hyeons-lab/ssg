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

import com.hyeonslab.ssg.page.Page
import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.HEAD
import kotlinx.html.classes
import kotlinx.html.script
import kotlinx.html.unsafe

/**
 * Adds Google Tag Manager / Google Analytics 4 tracking code to the HTML head.
 *
 * This function performs strict validation to prevent XSS (Cross-Site Scripting) attacks by
 * ensuring the tag ID matches Google's official format before injection.
 *
 * @param tag The Google Tag ID (e.g., "G-ABCD1234EF" for GA4 or "GT-ABCD123" for Google Tag). Can
 *   be blank or empty to skip tag insertion.
 * @throws IllegalArgumentException if the tag format is invalid or contains potential injection
 *   attempts
 *
 * Security: This function validates tag IDs using a strict regex pattern that only allows uppercase
 * letters and numbers in the format: G-XXXXXXXXXX or GT-XXXXXXXX. Examples of rejected inputs:
 * - `"G-<script>alert(1)</script>"` (script injection attempt)
 * - `"../../../etc/passwd"` (path traversal attempt)
 * - `"G-lowercase"` (invalid characters)
 *
 * Valid formats:
 * - Google Analytics 4: `G-` followed by 7-12 uppercase alphanumeric characters
 * - Google Tag: `GT-` followed by 7-12 uppercase alphanumeric characters
 *
 * Example:
 * ```kotlin
 * head {
 *     googleTag("G-ABCD1234EF")  // ✅ Valid
 *     googleTag("")              // ✅ No-op (skips insertion)
 * }
 * ```
 */
fun HEAD.googleTag(tag: String = "") {
  tag
    .takeIf { it.isNotBlank() }
    ?.let {
      // Validate Google Tag ID format to prevent XSS injection
      // Valid formats: G-XXXXXXXXXX (Google Analytics 4) or GT-XXXXXXXX (Google Tag)
      require(tag.matches(Regex("^(G|GT)-[A-Z0-9]{7,12}$"))) {
        "Invalid Google Tag ID format: '$tag'. Expected format: G-XXXXXXXXXX or GT-XXXXXXXX"
      }

      script {
        async = true
        src = "https://www.googletagmanager.com/gtag/js?id=$tag"
      }
      script {
        // SAFETY: Tag ID is validated by strict regex on line 14 to prevent XSS injection
        // Valid formats: G-XXXXXXXXXX or GT-XXXXXXXX (uppercase alphanumeric only)
        // The unsafe block is necessary here because kotlinx.html doesn't support
        // inserting raw JavaScript code through safe APIs.
        unsafe {
          +"""
                window.dataLayer = window.dataLayer || [];
                function gtag (){ dataLayer.push(arguments); }
                gtag('js', new Date ());
                gtag('config', '$tag');
                """
            .trimIndent()
        }
      }
    }
}

/**
 * Applies color classes to a navigation menu item based on whether it's the currently selected
 * page.
 *
 * This helper function adds Tailwind CSS color classes to navigation menu elements to visually
 * distinguish the current page from other navigation links.
 *
 * @param selection The page that this element represents
 * @param selected The currently selected/active page
 * @param selectedColor Tailwind color class for the selected state (e.g., "text-blue-600")
 * @param unselectedColor Tailwind color class for the unselected state (e.g., "text-gray-700")
 *
 * Example:
 * ```kotlin
 * h1 {
 *     adjustSelected(
 *         selection = aboutPage,
 *         selected = currentPage,
 *         selectedColor = "text-blue-600",
 *         unselectedColor = "text-gray-700"
 *     )
 *     a(href = "about.html") { +"About" }
 * }
 * ```
 */
fun CommonAttributeGroupFacade.adjustSelected(
  selection: Page,
  selected: Page,
  selectedColor: String,
  unselectedColor: String,
) {
  if (selected == selection) {
    classes += " $selectedColor"
  } else {
    classes += " $unselectedColor"
  }
}
