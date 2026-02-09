package com.hyeonslab.ssg.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import kotlin.test.assertTrue
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.stream.appendHTML

class HtmlExtensionsTest :
  FunSpec({
    context("googleTag validation") {
      test("should accept valid Google Analytics 4 tag ID") {
        val html = buildString { appendHTML().html { head { googleTag("G-ABCD1234EF") } } }
        html shouldContain "G-ABCD1234EF"
      }

      test("should accept valid Google Tag ID") {
        val html = buildString { appendHTML().html { head { googleTag("GT-ABCD123") } } }
        html shouldContain "GT-ABCD123"
      }

      test("should reject tag ID with script injection") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            buildString { appendHTML().html { head { googleTag("G-<script>alert(1)</script>") } } }
          }
        exception.message shouldContain "Invalid Google Tag ID format"
      }

      test("should reject tag ID with path traversal") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            buildString { appendHTML().html { head { googleTag("../../../etc/passwd") } } }
          }
        exception.message shouldContain "Invalid Google Tag ID format"
      }

      test("should reject malformed tag IDs") {
        val invalidIds =
          listOf(
            "G-",
            "G-123", // Too short
            "INVALID-123",
            "G-lowercase",
            "G-ABCD12345678901234", // Too long
          )

        invalidIds.forEach { invalidId ->
          shouldThrow<IllegalArgumentException> {
            buildString { appendHTML().html { head { googleTag(invalidId) } } }
          }
        }
      }

      test("should allow blank tag (no-op)") {
        val html = buildString { appendHTML().html { head { googleTag("") } } }
        // Should not throw, just do nothing
        assertTrue(html.isNotEmpty())
      }
    }
  })
