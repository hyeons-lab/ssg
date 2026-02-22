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
import kotlinx.html.BODY
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.img
import kotlinx.html.span
import kotlinx.html.nav
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
  val sticky = if (navMenuSettings.isSticky) "sticky" else ""
  val blur = if (navMenuSettings.blurNavBackground) "backdrop-blur-md" else ""
  val leftMargin = "ms-4 sm:ms-8 md:ms-${navMenuSettings.horizontalMargin}"
  val rightMargin = "me-4 sm:me-8 md:me-${navMenuSettings.horizontalMargin}"

  nav(
    classes =
      "$blur $sticky z-[255] ${navMenuSettings.fontFamily} flex w-full py-4 $leftMargin $rightMargin ${navMenuSettings.backgroundColor}"
  ) {
    a(href = "./index.html") {
      div {
        style = "height: ${navMenuSettings.logo.height}px; width: ${navMenuSettings.logo.width}px;"
        img(src = navMenuSettings.logo.imageUrl, alt = navMenuSettings.logo.altText) {
          style =
            "height: ${navMenuSettings.logo.height}px; width: ${navMenuSettings.logo.width}px;"
        }
      }
    }
    val baseClasses =
      "${Tailwind.Text.Size.sm} ${navMenuSettings.fontFamily} mx-1 md:mx-2 vertical-menu horizontal-menu md:text-base lg:text-lg"

    pages.forEach { page ->
      span(classes = baseClasses) {
        adjustSelected(
          page,
          selected,
          navMenuSettings.navSelectedColor,
          navMenuSettings.navDefaultColor,
        )
        a(classes = "uppercase z-1 mx-1 md:mx-2 text-nowrap", href = "./${page.outputFilename}") {
          +page.title
        }
      }
    }

    div(classes = "grow")

    // Social media links - wrapped in flex container for proper spacing
    div(classes = "flex gap-4 py-4") {
      navMenuSettings.instagram?.let { username ->
        val instagramUrl = "https://www.instagram.com/$username"
        a(
          href = instagramUrl,
          classes =
            "${navMenuSettings.navDefaultColor} ${Tailwind.Text.Size.sm} md:text-base lg:text-lg",
        ) {
          i(classes = "fa-brands fa-instagram")
        }
      }
      navMenuSettings.email?.let { email ->
        a(
          href = "mailto:$email",
          classes =
            "${navMenuSettings.navDefaultColor} ${Tailwind.Text.Size.sm} md:text-base lg:text-lg",
        ) {
          i(classes = "fa-regular fa-envelope")
        }
      }
    }
  }
}
