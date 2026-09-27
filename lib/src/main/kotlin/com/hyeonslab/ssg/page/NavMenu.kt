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

import com.hyeonslab.ssg.core.adjustSelected
import com.hyeonslab.ssg.utils.Tailwind
import com.hyeonslab.ssg.utils.depthPrefix
import com.hyeonslab.ssg.utils.encodeUrlPath
import com.hyeonslab.ssg.utils.normalizedPathString
import com.hyeonslab.ssg.utils.relativeAssetHref
import kotlinx.html.BODY
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.img
import kotlinx.html.nav
import kotlinx.html.span
import kotlinx.html.style

/**
 * Renders a responsive navigation menu with logo, page links, and optional social media icons.
 *
 * This function generates a horizontal navigation bar that includes:
 * - Logo image (linked to home page)
 * - Page navigation links with active state highlighting
 * - Optional social media links (Instagram, email)
 * - Responsive text sizing and spacing
 * - Optional sticky positioning and backdrop blur effects
 *
 * The navigation menu is automatically generated from the site's page list and applies different
 * styling to the currently selected page.
 *
 * @param selected The currently active page (will be highlighted with `navSelectedColor`)
 * @param pages List of all pages to include in the navigation menu
 * @param navMenuSettings Configuration for navigation appearance (colors, logo, social links)
 * @see NavMenuSettings for customization options
 *
 * Example usage:
 * ```kotlin
 * body {
 *     navMenu(
 *         selected = currentPage,
 *         pages = listOf(HomePage, AboutPage, ContactPage),
 *         navMenuSettings = NavMenuSettings(
 *             backgroundColor = "bg-white",
 *             navSelectedColor = "text-blue-600",
 *             navDefaultColor = "text-gray-700",
 *             logo = Logo("images/logo.png", width = 100, height = 50),
 *             isSticky = true,
 *             instagram = "myusername",
 *             email = "contact@example.com"
 *         )
 *     )
 * }
 * ```
 */
fun BODY.navMenu(selected: Page, pages: List<Page>, navMenuSettings: NavMenuSettings) {
  val navClasses = buildList {
    if (navMenuSettings.blurNavBackground) add("backdrop-blur-md")
    if (navMenuSettings.isSticky) add("sticky top-0")
    add("z-[255]")
    add(navMenuSettings.fontFamily)
    add("flex w-full py-4")
    add("px-${navMenuSettings.horizontalMargin}")
    add(navMenuSettings.backgroundColor)
  }
    .joinToString(" ")

  val homeFilename = pages.firstOrNull()?.outputFilename ?: "index.html"
  // Computed once per page: every link below shares the selected page's depth.
  val selectedPrefix = depthPrefix(selected.outputFilename)

  nav(classes = navClasses) {
    a(href = relativePageHref(selectedPrefix, homeFilename)) {
      div {
        style = "height: ${navMenuSettings.logo.height}px; width: ${navMenuSettings.logo.width}px;"
        img(
          src = relativeAssetHref(selected.outputFilename, navMenuSettings.logo.imageUrl),
          alt = navMenuSettings.logo.altText,
        ) {
          style =
            "height: ${navMenuSettings.logo.height}px; width: ${navMenuSettings.logo.width}px;"
        }
      }
    }
    val baseClasses =
      "${Tailwind.Text.Size.sm.size} ${navMenuSettings.fontFamily} mx-1 md:mx-2 vertical-menu horizontal-menu md:text-base lg:text-lg"

    pages.forEach { page ->
      span(classes = baseClasses) {
        adjustSelected(
          page,
          selected,
          navMenuSettings.navSelectedColor,
          navMenuSettings.navDefaultColor,
        )
        a(
          classes = "uppercase mx-1 md:mx-2 text-nowrap",
          href = relativePageHref(selectedPrefix, page.outputFilename),
        ) {
          +page.title
        }
      }
    }

    div(classes = "grow")

    // Social media links: wrapped in flex container for proper spacing
    div(classes = "flex gap-4 py-4") {
      navMenuSettings.instagram?.let { username ->
        val instagramUrl = "https://www.instagram.com/$username"
        a(
          href = instagramUrl,
          classes =
            "${navMenuSettings.navDefaultColor} ${Tailwind.Text.Size.sm.size} md:text-base lg:text-lg",
        ) {
          attributes["aria-label"] = "Instagram"
          attributes["rel"] = "noopener noreferrer"
          i(classes = "fa-brands fa-instagram")
        }
      }
      navMenuSettings.email?.let { email ->
        a(
          href = "mailto:$email",
          classes =
            "${navMenuSettings.navDefaultColor} ${Tailwind.Text.Size.sm.size} md:text-base lg:text-lg",
        ) {
          attributes["aria-label"] = "Email"
          i(classes = "fa-regular fa-envelope")
        }
      }
    }
  }
}

/** Computes a relative href from the currently active page to a target page filename. */
private fun relativePageHref(selectedPrefix: String, targetFilename: String): String {
  val normalized = normalizedPathString(targetFilename)
  return "$selectedPrefix${encodeUrlPath(normalized)}"
}
