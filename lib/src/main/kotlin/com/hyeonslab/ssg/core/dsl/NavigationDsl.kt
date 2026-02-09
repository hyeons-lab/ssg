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
package com.hyeonslab.ssg.core.dsl

import com.hyeonslab.ssg.page.Logo
import com.hyeonslab.ssg.page.NavMenuSettings

/**
 * DSL builder for creating navigation menu settings.
 *
 * Provides a type-safe way to configure navigation appearance and behavior using Kotlin's DSL
 * syntax. All properties support validation to prevent injection attacks.
 *
 * Example:
 * ```kotlin
 * navigation {
 *     backgroundColor = "bg-white/90"
 *     navSelectedColor = "text-blue-600"
 *     navDefaultColor = "text-gray-700"
 *     isSticky = true
 *     blurNavBackground = true
 *
 *     // Option 1: Simple logo configuration
 *     logo("images/logo.png", width = 120, height = 60)
 *
 *     // Option 2: Logo with builder block
 *     logo {
 *         imageUrl = "images/logo.png"
 *         width = 120
 *         height = 60
 *     }
 *
 *     // Optional social links
 *     instagram = "myhandle"
 *     email = "contact@example.com"
 *
 *     // Optional styling
 *     fontFamily = "font-inter"
 *     horizontalMargin = "24"
 * }
 * ```
 *
 * @see NavMenuSettings
 * @see LogoBuilder
 * @see SiteBuilder
 */
@SsgDsl
class NavigationBuilder {
  var backgroundColor: String? = null
  var navSelectedColor: String? = null
  var navDefaultColor: String? = null
  var isSticky: Boolean = false
  var instagram: String? = null
  var email: String? = null
  var blurNavBackground: Boolean = false
  var fontFamily: String = "font-plex-sans"
  var horizontalMargin: String = "16"

  private var logoConfig: Logo? = null

  /**
   * Configure the logo with a simpler syntax.
   *
   * Example:
   * ```kotlin
   * logo("images/logo.png", width = 100, height = 50)
   * ```
   */
  fun logo(imageUrl: String, width: Int, height: Int) {
    logoConfig = Logo(imageUrl = imageUrl, width = width, height = height)
  }

  /**
   * Configure the logo using a builder block for more control.
   *
   * Example:
   * ```kotlin
   * logo {
   *     imageUrl = "images/logo.png"
   *     width = 100
   *     height = 50
   * }
   * ```
   */
  fun logo(block: LogoBuilder.() -> Unit) {
    logoConfig = LogoBuilder().apply(block).build()
  }

  /** Build the NavMenuSettings instance from the configured values. */
  fun build(): NavMenuSettings {
    requireNotNull(backgroundColor) { "navigation.backgroundColor must be specified" }
    requireNotNull(navSelectedColor) { "navigation.navSelectedColor must be specified" }
    requireNotNull(navDefaultColor) { "navigation.navDefaultColor must be specified" }
    requireNotNull(logoConfig) { "navigation.logo must be specified" }

    return NavMenuSettings(
      backgroundColor = backgroundColor!!,
      navSelectedColor = navSelectedColor!!,
      navDefaultColor = navDefaultColor!!,
      isSticky = isSticky,
      instagram = instagram,
      email = email,
      logo = logoConfig!!,
      blurNavBackground = blurNavBackground,
      fontFamily = fontFamily,
      horizontalMargin = horizontalMargin,
    )
  }
}

/**
 * DSL builder for creating logo configurations.
 *
 * Provides a type-safe way to configure the site logo using Kotlin's DSL syntax. Validates
 * dimensions and image URLs to prevent XSS attacks.
 *
 * Example:
 * ```kotlin
 * logo {
 *     imageUrl = "images/logo.png"
 *     width = 120
 *     height = 60
 * }
 * ```
 *
 * @see Logo
 * @see NavigationBuilder
 */
@SsgDsl
class LogoBuilder {
  var imageUrl: String? = null
  var width: Int? = null
  var height: Int? = null

  fun build(): Logo {
    requireNotNull(imageUrl) { "logo.imageUrl must be specified" }
    requireNotNull(width) { "logo.width must be specified" }
    requireNotNull(height) { "logo.height must be specified" }

    return Logo(imageUrl = imageUrl!!, width = width!!, height = height!!)
  }
}
