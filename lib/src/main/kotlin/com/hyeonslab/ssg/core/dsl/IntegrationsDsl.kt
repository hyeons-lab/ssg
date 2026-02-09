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

import com.hyeonslab.ssg.core.IntegrationConfig

/**
 * DSL builder for configuring third-party integrations.
 *
 * Provides a type-safe way to configure external services such as Google Analytics, Google Tag
 * Manager, and other tracking services.
 *
 * Example:
 * ```kotlin
 * integrations {
 *     googleTag = "G-ABCD1234EF"  // Google Analytics 4
 *     // or
 *     googleTagId = "GT-ABCD123"  // Google Tag Manager
 * }
 * ```
 *
 * @see IntegrationConfig
 * @see SiteBuilder
 */
@SsgDsl
class IntegrationsBuilder {
  var googleTagId: String? = null

  /**
   * Convenience property alias for googleTagId.
   *
   * Allows using either `googleTag` or `googleTagId` for better readability.
   *
   * Example:
   * ```kotlin
   * googleTag = "G-ABCD1234EF"  // Same as googleTagId
   * ```
   */
  var googleTag: String?
    get() = googleTagId
    set(value) {
      googleTagId = value
    }

  /** Build the IntegrationConfig instance from the configured values. */
  fun build(): IntegrationConfig {
    return IntegrationConfig(googleTagId = googleTagId)
  }
}
