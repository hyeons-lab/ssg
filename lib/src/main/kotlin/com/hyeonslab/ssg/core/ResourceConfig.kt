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
 * Configuration for static resources and stylesheets.
 *
 * Groups all resource-related configuration including static files to copy, local CSS files, and
 * external CDN stylesheets.
 *
 * @property staticFiles List of static files to copy from classpath to output directory
 * @property localStylesheets List of local CSS files (relative paths from output directory)
 * @property externalStylesheets List of external CDN stylesheets with SRI integrity
 */
@Serializable
data class ResourceConfig(
  val staticFiles: List<InputOutputPair> = emptyList(),
  val localStylesheets: List<String> = listOf("css/tailwind.css"),
  val externalStylesheets: List<ExternalStylesheet> = emptyList(),
)
