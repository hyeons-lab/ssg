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
import java.nio.file.Path

/**
 * Escapes a string for safe inclusion in XML element text or attribute values. Appends each
 * character in a single pass, emitting entities directly, so output can never be double-escaped.
 */
internal fun escapeXml(value: String): String {
  if (value.none { it == '&' || it == '<' || it == '>' || it == '"' || it == '\'' }) {
    return value
  }
  return buildString(value.length + 16) {
    for (ch in value) {
      when (ch) {
        '&' -> append("&amp;")
        '<' -> append("&lt;")
        '>' -> append("&gt;")
        '"' -> append("&quot;")
        '\'' -> append("&apos;")
        else -> append(ch)
      }
    }
  }
}

/**
 * Percent-encodes each segment of a relative URL path while preserving `/` separators, so filenames
 * containing spaces or reserved characters produce valid URLs. Spaces become `%20` (not `+`).
 * Backslashes are unified to `/` first, matching path validation.
 */
internal fun encodeUrlPath(path: String): String =
  path.replace('\\', '/').split("/").joinToString("/") { segment ->
    URLEncoder.encode(segment, Charsets.UTF_8).replace("+", "%20")
  }

/**
 * Unifies separators and resolves `.`/`..` segments, returning a `/`-separated path string. The
 * canonical spelling shared by duplicate detection and depth computation.
 */
internal fun normalizedPathString(path: String): String =
  Path.of(path.replace('\\', '/')).normalize().toString().replace('\\', '/')

/**
 * Counts the directory depth of a page output filename (0 for top-level pages). Normalizes
 * separators and `.`/`..` segments first, so equivalent spellings (`./x.html`, `docs//y.html`,
 * `sub/../z.html`) all yield the depth of the file's real location.
 */
internal fun pageDepth(outputFilename: String): Int {
  return normalizedPathString(outputFilename).count { it == '/' }
}

/**
 * Returns the relative prefix (`./` for top-level pages, `../` per directory level otherwise) that
 * makes a page-relative href resolve from a page at the given output filename.
 */
internal fun depthPrefix(outputFilename: String): String {
  val depth = pageDepth(outputFilename)
  return if (depth == 0) "./" else "../".repeat(depth)
}

/** Matches absolute-URI schemes (`https:`, `data:`, `mailto:`) at the start of a value. */
private val URI_SCHEME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:")

/**
 * Prefixes a page-relative asset href (`css/app.css`, `images/logo.png`) with `./` or `../`
 * segments so it resolves from a page at the given output filename. Absolute URIs (any leading
 * `scheme:`, including `https://` and slash-less `data:`/`mailto:`) and root-relative hrefs
 * (starting with `/`) are returned untouched. Unlike page links, the target is not percent-encoded:
 * asset hrefs may carry query strings (`app.css?v=2`) that encoding would mangle.
 */
internal fun relativeAssetHref(fromOutputFilename: String, target: String): String {
  val unified = target.replace('\\', '/')
  // Absolute URIs always carry a leading scheme, so no :// check is needed. Malformed non-URIs
  // containing :// (such as `1http://x`) intentionally fall through to prefixing.
  if (unified.startsWith("/") || URI_SCHEME_REGEX.containsMatchIn(unified)) return unified
  val cleanTarget = unified.removePrefix("./")
  return "${depthPrefix(fromOutputFilename)}$cleanTarget"
}
