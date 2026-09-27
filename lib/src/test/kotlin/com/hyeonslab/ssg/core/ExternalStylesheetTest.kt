package com.hyeonslab.ssg.core

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ExternalStylesheetTest :
  DescribeSpec({
    describe("ExternalStylesheet configuration") {
      it("should create stylesheet with all attributes") {
        val stylesheet =
          ExternalStylesheet(
            href = "https://example.com/style.css",
            integrity = "sha512-abc123",
            crossorigin = "anonymous",
            referrerpolicy = "no-referrer",
          )

        stylesheet.href shouldBe "https://example.com/style.css"
        stylesheet.integrity shouldBe "sha512-abc123"
        stylesheet.crossorigin shouldBe "anonymous"
        stylesheet.referrerpolicy shouldBe "no-referrer"
      }

      it("should create stylesheet with minimal attributes") {
        val stylesheet = ExternalStylesheet(href = "https://example.com/style.css")

        stylesheet.href shouldBe "https://example.com/style.css"
        stylesheet.integrity shouldBe null
        stylesheet.crossorigin shouldBe null
        stylesheet.referrerpolicy shouldBe null
      }

      it("should provide Font Awesome 6.7.2 constant") {
        val fa = ExternalStylesheet.FONT_AWESOME_6_7_2

        fa.href shouldBe "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css"
        fa.integrity shouldNotBe null
        fa.crossorigin shouldBe "anonymous"
        fa.referrerpolicy shouldBe "no-referrer"
      }

      it("Font Awesome constant should have valid SRI hash") {
        val fa = ExternalStylesheet.FONT_AWESOME_6_7_2

        // SRI hash should start with sha512- or sha384- or sha256-
        val integrity = fa.integrity!!
        val isValidSRI =
          integrity.startsWith("sha512-") ||
            integrity.startsWith("sha384-") ||
            integrity.startsWith("sha256-")

        isValidSRI shouldBe true
      }

      it("should pin the Play CDN constant to the newest version the CDN hosts") {
        // 3.4.17 is the newest version cdn.tailwindcss.com serves (3.4.18+ return an error stub),
        // even though the npm v3 LTS train is newer. Do not bump this from the npm version.
        @Suppress("DEPRECATION") val play = ExternalStylesheet.TAILWIND_CSS_3_4_17

        play.href shouldBe "https://cdn.tailwindcss.com/3.4.17"
      }
    }
  })
