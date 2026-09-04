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

import java.nio.file.Paths

/**
 * Shared input validators used across the library.
 *
 * These guards are defense-in-depth: kotlinx.html already escapes attribute values, but validating
 * configuration up front gives callers clear, early errors and prevents unintended extra classes
 * from leaking into generated attributes. The patterns are compiled once here instead of on every
 * call/construction.
 */

/**
 * Allowed characters for Tailwind class strings (letters, numbers, spaces, and CSS punctuation).
 */
internal val CSS_CLASS_REGEX = Regex("^[a-zA-Z0-9\\s\\-_:/\\[\\].%]+$")

/** A single Tailwind spacing token — no whitespace, so it cannot inject additional classes. */
internal val SPACING_UNIT_REGEX = Regex("^[a-zA-Z0-9._/\\[\\]-]+$")

/**
 * Validates a Tailwind class string. Empty strings are allowed (they render no classes); any
 * non-empty value must contain only safe class characters.
 */
internal fun validateCssClasses(classes: String, fieldName: String) {
  if (classes.isEmpty()) return
  require(classes.matches(CSS_CLASS_REGEX)) {
    "$fieldName contains invalid characters: '$classes'\n" +
      "Allowed characters: letters, numbers, spaces, hyphens, underscores, colons, slashes, brackets, dots, percent signs\n" +
      "Valid examples: 'bg-white', 'text-blue-600 hover:text-blue-700', 'w-1/2', 'z-[255]', 'bg-white/90'\n" +
      "This validation prevents HTML attribute injection attacks."
  }
}

/**
 * Validates a single Tailwind spacing token (e.g. the value interpolated into `px-<n>`). Unlike a
 * full class string this must not contain whitespace, so a value like "16 hidden" cannot smuggle
 * extra utility classes into the element.
 */
internal fun validateSpacingUnit(value: String, fieldName: String) {
  require(value.matches(SPACING_UNIT_REGEX)) {
    "$fieldName must be a single Tailwind spacing token with no spaces: '$value'\n" +
      "Valid examples: '16', '8', '1.5', '[20px]'\n" +
      "This validation prevents extra classes from being injected into the navigation."
  }
}

/**
 * Validates that a string used as an attribute URL does not contain characters that could break out
 * of the attribute (quotes or angle brackets).
 */
internal fun validateUrlChars(url: String, fieldName: String) {
  require(!url.contains("\"") && !url.contains("'") && !url.contains("<") && !url.contains(">")) {
    "$fieldName contains invalid characters: '$url'\n" +
      "Invalid characters: quotes (\", '), angle brackets (<, >)\n" +
      "This validation prevents XSS injection via URL attributes."
  }
}

/**
 * Validates that a path is relative and does not traverse outside its base directory. Rejects
 * absolute paths and paths that normalize to a parent reference (starting with "..").
 */
internal fun validateRelativePath(path: String, name: String) {
  val normalized = Paths.get(path).normalize()
  require(!normalized.isAbsolute) { "$name cannot be an absolute path: $path" }
  // Compare path segments, not the string prefix, so a valid name like "..hidden/logo.png"
  // (whose first segment merely starts with "..") is not mistaken for a parent traversal.
  require(!normalized.startsWith("..")) { "$name cannot traverse outside base directory: $path" }
}
