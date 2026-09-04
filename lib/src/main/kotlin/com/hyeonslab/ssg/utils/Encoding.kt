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
package com.hyeonslab.ssg.utils

import java.net.URLEncoder

/**
 * Escapes a string for safe inclusion in XML element text or attribute values. `&` is replaced
 * first so the entities introduced for the other characters are not double-escaped.
 */
internal fun escapeXml(value: String): String =
  value
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&apos;")

/**
 * Percent-encodes each segment of a relative URL path while preserving `/` separators, so filenames
 * containing spaces or reserved characters produce valid URLs. Spaces become `%20` (not `+`).
 */
internal fun encodeUrlPath(path: String): String =
  path.split("/").joinToString("/") { segment ->
    URLEncoder.encode(segment, Charsets.UTF_8).replace("+", "%20")
  }
