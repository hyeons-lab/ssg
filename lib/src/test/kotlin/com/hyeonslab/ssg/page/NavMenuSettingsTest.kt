package com.hyeonslab.ssg.page

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class NavMenuSettingsTest :
  FunSpec({
    context("Instagram username validation") {
      test("should accept valid Instagram usernames") {
        val validUsernames =
          listOf(
            "testuser",
            "test.user",
            "test_user",
            "test123",
            "a",
            "a" + "b".repeat(29), // 30 characters total
          )

        validUsernames.forEach { username ->
          val settings =
            NavMenuSettings(
              backgroundColor = "bg-white",
              navSelectedColor = "text-blue-600",
              navDefaultColor = "text-gray-700",
              isSticky = false,
              instagram = username,
              logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
              blurNavBackground = false,
            )
          settings.instagram shouldBe username
        }
      }

      test("should reject Instagram username with invalid characters") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            NavMenuSettings(
              backgroundColor = "bg-white",
              navSelectedColor = "text-blue-600",
              navDefaultColor = "text-gray-700",
              isSticky = false,
              instagram = "test\" onclick=\"alert('XSS')",
              logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
              blurNavBackground = false,
            )
          }
        exception.message shouldContain "Invalid Instagram username"
      }

      test("should reject Instagram username that is too long") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            NavMenuSettings(
              backgroundColor = "bg-white",
              navSelectedColor = "text-blue-600",
              navDefaultColor = "text-gray-700",
              isSticky = false,
              instagram = "a".repeat(31), // 31 characters
              logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
              blurNavBackground = false,
            )
          }
        exception.message shouldContain "Invalid Instagram username"
      }

      test("should reject Instagram username with special characters") {
        val invalidUsernames =
          listOf("test<script>", "test@user", "test user", "test#user", "test&user")

        invalidUsernames.forEach { username ->
          shouldThrow<IllegalArgumentException> {
            NavMenuSettings(
              backgroundColor = "bg-white",
              navSelectedColor = "text-blue-600",
              navDefaultColor = "text-gray-700",
              isSticky = false,
              instagram = username,
              logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
              blurNavBackground = false,
            )
          }
        }
      }

      test("should accept null Instagram username") {
        val settings =
          NavMenuSettings(
            backgroundColor = "bg-white",
            navSelectedColor = "text-blue-600",
            navDefaultColor = "text-gray-700",
            isSticky = false,
            instagram = null,
            logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
            blurNavBackground = false,
          )
        settings.instagram shouldBe null
      }
    }

    context("Email address validation") {
      test("should accept valid email addresses") {
        val validEmails =
          listOf(
            "user@example.com",
            "test.user@example.com",
            "test+tag@example.co.uk",
            "test_user@sub.example.com",
          )

        validEmails.forEach { email ->
          val settings =
            NavMenuSettings(
              backgroundColor = "bg-white",
              navSelectedColor = "text-blue-600",
              navDefaultColor = "text-gray-700",
              isSticky = false,
              email = email,
              logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
              blurNavBackground = false,
            )
          settings.email shouldBe email
        }
      }

      test("should reject email with XSS injection attempt") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            NavMenuSettings(
              backgroundColor = "bg-white",
              navSelectedColor = "text-blue-600",
              navDefaultColor = "text-gray-700",
              isSticky = false,
              email = "user@example.com\" onclick=\"alert('XSS')",
              logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
              blurNavBackground = false,
            )
          }
        exception.message shouldContain "Invalid email address"
      }

      test("should reject malformed email addresses") {
        val invalidEmails =
          listOf("notanemail", "@example.com", "user@", "user@.com", "user <script>@example.com")

        invalidEmails.forEach { email ->
          shouldThrow<IllegalArgumentException> {
            NavMenuSettings(
              backgroundColor = "bg-white",
              navSelectedColor = "text-blue-600",
              navDefaultColor = "text-gray-700",
              isSticky = false,
              email = email,
              logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
              blurNavBackground = false,
            )
          }
        }
      }

      test("should accept null email address") {
        val settings =
          NavMenuSettings(
            backgroundColor = "bg-white",
            navSelectedColor = "text-blue-600",
            navDefaultColor = "text-gray-700",
            isSticky = false,
            email = null,
            logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
            blurNavBackground = false,
          )
        settings.email shouldBe null
      }
    }

    context("Logo validation") {
      test("should accept valid logo configurations") {
        val validLogos =
          listOf(
            Logo("logo.png", 50, 50),
            Logo("images/logo.svg", 100, 100),
            Logo("assets/brand/logo.webp", 200, 150),
            Logo("logo-2024.png", 1, 1),
            Logo("huge-logo.png", 2000, 2000),
          )

        validLogos.forEach { logo ->
          logo.imageUrl shouldBe logo.imageUrl
          logo.width shouldBe logo.width
          logo.height shouldBe logo.height
        }
      }

      test("should reject logo URL with double quotes") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            Logo("logo.png\" onerror=\"alert('XSS')", 50, 50)
          }
        exception.message shouldContain "Logo imageUrl contains invalid characters"
      }

      test("should reject logo URL with single quotes") {
        val exception =
          shouldThrow<IllegalArgumentException> { Logo("logo.png' onerror='alert(1)", 50, 50) }
        exception.message shouldContain "Logo imageUrl contains invalid characters"
      }

      test("should reject logo URL with angle brackets") {
        val invalidUrls =
          listOf("logo.png<script>", "<img src=x>", "logo>test.png", "logo<test.png")

        invalidUrls.forEach { url -> shouldThrow<IllegalArgumentException> { Logo(url, 50, 50) } }
      }

      test("should reject logo with width too small") {
        val exception = shouldThrow<IllegalArgumentException> { Logo("logo.png", 0, 50) }
        exception.message shouldContain "Logo width must be between 1 and 2000"
      }

      test("should reject logo with width too large") {
        val exception = shouldThrow<IllegalArgumentException> { Logo("logo.png", 2001, 50) }
        exception.message shouldContain "Logo width must be between 1 and 2000"
      }

      test("should reject logo with height too small") {
        val exception = shouldThrow<IllegalArgumentException> { Logo("logo.png", 50, 0) }
        exception.message shouldContain "Logo height must be between 1 and 2000"
      }

      test("should reject logo with height too large") {
        val exception = shouldThrow<IllegalArgumentException> { Logo("logo.png", 50, 2001) }
        exception.message shouldContain "Logo height must be between 1 and 2000"
      }
    }
  })
