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

import kotlinx.serialization.Serializable

/**
 * Configuration for the navigation menu appearance and behavior.
 *
 * Controls all aspects of the navigation bar including colors, layout, logo, social media links,
 * and visual effects. All CSS class parameters are validated to prevent HTML attribute injection
 * attacks.
 *
 * @property backgroundColor Tailwind background color class for the navigation bar (e.g.,
 *   "bg-gray-100", "bg-white/90")
 * @property navSelectedColor Tailwind text color class for the currently selected/active page
 *   (e.g., "text-blue-600")
 * @property navDefaultColor Tailwind text color class for unselected navigation links (e.g.,
 *   "text-gray-700")
 * @property isSticky Whether the navigation should stick to the top when scrolling (applies `sticky
 *   top-0` classes)
 * @property instagram Optional Instagram username without the @ symbol (e.g., "johndoe"). Validated
 *   to prevent XSS.
 * @property email Optional email address for contact link (e.g., "contact@example.com"). Validated
 *   to prevent XSS.
 * @property logo Logo configuration including image URL and dimensions
 * @property blurNavBackground Whether to apply backdrop blur effect to navigation (applies
 *   `backdrop-blur-md`)
 * @property fontFamily Tailwind font family class (default: "font-plex-sans")
 * @property horizontalMargin Tailwind spacing unit for left/right margins (default: "16" for
 *   ms-16/me-16)
 * @throws IllegalArgumentException if any CSS class contains invalid characters (prevents injection
 *   attacks)
 * @throws IllegalArgumentException if Instagram username contains invalid characters
 * @throws IllegalArgumentException if email address is invalid
 *
 * Example:
 * ```kotlin
 * val navigation = NavMenuSettings(
 *     backgroundColor = "bg-white/90",
 *     navSelectedColor = "text-blue-600",
 *     navDefaultColor = "text-gray-700",
 *     isSticky = true,
 *     instagram = "myhandle",
 *     email = "contact@example.com",
 *     logo = Logo(
 *         imageUrl = "images/logo.png",
 *         width = 120,
 *         height = 60
 *     ),
 *     blurNavBackground = true,
 *     fontFamily = "font-inter",
 *     horizontalMargin = "24"
 * )
 * ```
 *
 * @see Logo
 * @see com.hyeonslab.ssg.core.Site
 */
@Serializable
data class NavMenuSettings(
  val backgroundColor: String,
  val navSelectedColor: String,
  val navDefaultColor: String,
  val isSticky: Boolean,
  val instagram: String? = null, // Instagram username (without @)
  val email: String? = null,
  val logo: Logo,
  val blurNavBackground: Boolean,
  val fontFamily: String = "font-plex-sans", // Tailwind font family class (default: IBM Plex Sans)
  val horizontalMargin: String =
    "16", // Tailwind spacing unit for left/right margins (e.g., "16" for ms-16/me-16)
) {
  init {
    // Validate CSS class strings to prevent HTML attribute injection
    fun validateCssClasses(classes: String, fieldName: String) {
      require(classes.matches(Regex("^[a-zA-Z0-9\\s\\-_:/\\[\\].%]+$"))) {
        "$fieldName contains invalid characters: '$classes'\n" +
          "Allowed characters: letters, numbers, spaces, hyphens, underscores, colons, slashes, brackets, dots, percent signs\n" +
          "Valid examples: 'bg-white', 'text-blue-600 hover:text-blue-700', 'w-1/2', 'z-[255]', 'bg-white/90'\n" +
          "This validation prevents HTML attribute injection attacks."
      }
    }

    validateCssClasses(backgroundColor, "backgroundColor")
    validateCssClasses(navSelectedColor, "navSelectedColor")
    validateCssClasses(navDefaultColor, "navDefaultColor")
    validateCssClasses(fontFamily, "fontFamily")

    // Validate Instagram username to prevent XSS injection
    instagram?.let { username ->
      require(username.matches(Regex("^[a-zA-Z0-9._]{1,30}$"))) {
        "Invalid Instagram username: '$username'\n" +
          "Format: Must contain only letters, numbers, dots, and underscores (max 30 characters)\n" +
          "Valid examples: 'johndoe', 'jane.doe', 'user_123'\n" +
          "This validation prevents XSS injection via href attributes."
      }
    }

    // Validate email address to prevent XSS injection
    email?.let { emailAddress ->
      require(emailAddress.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))) {
        "Invalid email address: '$emailAddress'\n" +
          "Format: Must be a valid email address (e.g., user@example.com)\n" +
          "This validation prevents XSS injection via mailto: links."
      }
    }
  }
}

/**
 * Configuration for the site logo displayed in the navigation menu.
 *
 * Specifies the logo image file path and its display dimensions. The image URL is validated to
 * prevent XSS injection via the src attribute.
 *
 * @property imageUrl Relative path to the logo image (e.g., "images/logo.png",
 *   "assets/brand/logo.svg")
 * @property width Logo width in pixels (must be between 1 and 2000)
 * @property height Logo height in pixels (must be between 1 and 2000)
 * @throws IllegalArgumentException if imageUrl contains quotes or angle brackets (prevents XSS)
 * @throws IllegalArgumentException if width or height is outside the valid range (1-2000 pixels)
 *
 * Example:
 * ```kotlin
 * val logo = Logo(
 *     imageUrl = "images/logo.png",
 *     width = 120,
 *     height = 60
 * )
 * ```
 *
 * @see NavMenuSettings
 */
@Serializable
data class Logo(val imageUrl: String, val width: Int, val height: Int, val altText: String = "") {
  init {
    // Validate logo URL to prevent XSS injection via src attribute
    require(
      !imageUrl.contains("\"") &&
        !imageUrl.contains("'") &&
        !imageUrl.contains("<") &&
        !imageUrl.contains(">")
    ) {
      "Logo imageUrl contains invalid characters: '$imageUrl'\n" +
        "Invalid characters: quotes (\", '), angle brackets (<, >)\n" +
        "Valid examples: 'logo.png', 'images/logo.svg', 'assets/brand/logo.webp'\n" +
        "This validation prevents XSS injection via src attributes."
    }

    // Validate dimensions are reasonable
    require(width in 1..2000) {
      "Logo width must be between 1 and 2000 pixels, got: $width\n" + "Valid range: 1-2000 pixels"
    }
    require(height in 1..2000) {
      "Logo height must be between 1 and 2000 pixels, got: $height\n" + "Valid range: 1-2000 pixels"
    }
  }
}
