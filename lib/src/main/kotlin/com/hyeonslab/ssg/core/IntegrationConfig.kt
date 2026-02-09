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
 * Configuration for third-party integrations.
 *
 * Groups all integration-related configuration such as analytics, tracking, and external services.
 *
 * @property googleTagId Google Analytics 4 or Google Tag Manager ID (e.g., "G-XXXXXXXXXX")
 */
@Serializable data class IntegrationConfig(val googleTagId: String? = null)
