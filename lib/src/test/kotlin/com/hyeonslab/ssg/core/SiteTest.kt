package com.hyeonslab.ssg.core

import com.hyeonslab.ssg.page.Logo
import com.hyeonslab.ssg.page.NavMenuSettings
import com.hyeonslab.ssg.page.Page
import com.hyeonslab.ssg.page.PageSettings
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p

class SiteTest :
  FunSpec({

    // Test pages
    val homePage =
      object : Page {
        override val title = "Home"
        override val outputFilename = "index.html"
        override val content = { _: PageSettings, flow: kotlinx.html.FlowContent ->
          flow.div {
            h1 { +"Welcome Home" }
            p { +"This is the home page" }
          }
        }
        override val footer = { _: PageSettings, flow: kotlinx.html.FlowContent ->
          flow.div { p { +"© 2026 Test" } }
        }
      }

    val aboutPage =
      object : Page {
        override val title = "About"
        override val outputFilename = "about.html"
        override val content = { _: PageSettings, flow: kotlinx.html.FlowContent ->
          flow.div {
            h1 { +"About Us" }
            p { +"This is the about page" }
          }
        }
        override val footer = { _: PageSettings, flow: kotlinx.html.FlowContent ->
          flow.div { p { +"© 2026 Test" } }
        }
      }

    // Helper function to create a test site using new API
    fun createTestSite(
      outputPath: String,
      pages: List<Page> = listOf(homePage),
      googleTagId: String? = null,
      localStylesheets: List<String> = listOf("css/tailwind.css"),
      externalStylesheets: List<ExternalStylesheet> = emptyList(),
    ): Site {
      return Site(
        outputPath = outputPath,
        title = "Test Site",
        version = "0.1.0-test",
        backgroundColor = "bg-white",
        pages = pages,
        navigation =
          NavMenuSettings(
            backgroundColor = "bg-gray-100",
            navSelectedColor = "text-blue-600",
            navDefaultColor = "text-gray-700",
            isSticky = false,
            logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
            blurNavBackground = false,
          ),
        resources =
          ResourceConfig(
            staticFiles = emptyList(),
            localStylesheets = localStylesheets,
            externalStylesheets = externalStylesheets,
          ),
        integrations = IntegrationConfig(googleTagId = googleTagId),
        pageSettings = PageSettings(),
      )
    }

    // Clean up test directories after each test
    afterEach {
      listOf("build/test-output", "build/test-output-multi", "build/test-output-google").forEach {
        dir ->
        File(dir).deleteRecursively()
      }
    }

    context("CSS class validation") {
      test("should accept valid Tailwind CSS classes") {
        val validClasses =
          listOf(
            "bg-white",
            "text-blue-600 hover:text-blue-700",
            "w-1/2",
            "z-[255]",
            "bg-white/90",
            "text-sm md:text-base lg:text-lg",
            "flex flex-col items-center",
          )

        validClasses.forEach { classes ->
          // Create site with each valid class
          val site =
            createTestSite(outputPath = "build/test-css-validation", pages = listOf(homePage))
              .copy(backgroundColor = classes)

          site.backgroundColor shouldBe classes
        }
      }

      test("should reject CSS classes with HTML attribute injection attempt") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            createTestSite(outputPath = "build/test", pages = listOf(homePage))
              .copy(backgroundColor = "bg-white\" onclick=\"alert('XSS')\" class=\"")
          }
        exception.message shouldContain "backgroundColor contains invalid characters"
      }

      test("should reject CSS classes with angle brackets") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            createTestSite(outputPath = "build/test", pages = listOf(homePage))
              .copy(
                navigation =
                  NavMenuSettings(
                    backgroundColor = "bg-gray-100",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray<script>alert(1)</script>",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  )
              )
          }
        exception.message shouldContain "navDefaultColor contains invalid characters"
      }

      test("should reject CSS classes with quotes") {
        shouldThrow<IllegalArgumentException> {
          createTestSite(outputPath = "build/test", pages = listOf(homePage))
            .copy(
              navigation =
                NavMenuSettings(
                  backgroundColor = "bg-white\"",
                  navSelectedColor = "text-blue-600",
                  navDefaultColor = "text-gray-700",
                  isSticky = false,
                  logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                  blurNavBackground = false,
                )
            )
        }
      }

      test("should validate all Site-level CSS class fields") {
        val invalidClass = "bg-white<script>"

        // Test Site-level CSS fields (nav colors are now in NavMenuSettings)
        val fields = listOf("backgroundColor", "htmlClasses", "bodyClasses", "contentClasses")

        fields.forEach { fieldName ->
          val exception =
            shouldThrow<IllegalArgumentException> {
              when (fieldName) {
                "backgroundColor" ->
                  createTestSite("build/test", listOf(homePage))
                    .copy(backgroundColor = invalidClass)
                "htmlClasses" ->
                  createTestSite("build/test", listOf(homePage)).copy(htmlClasses = invalidClass)
                "bodyClasses" ->
                  createTestSite("build/test", listOf(homePage)).copy(bodyClasses = invalidClass)
                "contentClasses" ->
                  createTestSite("build/test", listOf(homePage)).copy(contentClasses = invalidClass)
                else -> error("Unknown field")
              }
            }
          exception.message shouldContain fieldName
        }
      }
    }

    context("copyResources()") {
      test("should copy resources successfully") {
        // Create a test resource file
        val tempDir = Files.createTempDirectory("ssg-test")
        val resourceFile = tempDir.resolve("test.txt")
        Files.writeString(resourceFile, "test content")

        val outputPath = "build/test-output-resources"
        val site =
          createTestSite(outputPath)
            .copy(
              resources =
                ResourceConfig(
                  staticFiles =
                    listOf(InputOutputPair(inputFilename = "test.txt", outputPath = outputPath))
                )
            )

        // Note: This test will fail with "Resource not found" because
        // InputOutputPair loads from classpath, not filesystem.
        // This is testing the error handling behavior.
        val exception = shouldThrow<IllegalStateException> { site.copyResources() }
        exception.message shouldContain "Failed to copy"
        exception.message shouldContain "test.txt"

        // Clean up
        tempDir.toFile().deleteRecursively()
        File(outputPath).deleteRecursively()
      }

      test("should collect and report all resource copy failures") {
        val outputPath = "build/test-output-resources-multi"
        val site =
          createTestSite(outputPath)
            .copy(
              resources =
                ResourceConfig(
                  staticFiles =
                    listOf(
                      InputOutputPair("nonexistent1.txt", outputPath),
                      InputOutputPair("nonexistent2.txt", outputPath),
                      InputOutputPair("nonexistent3.txt", outputPath),
                    )
                )
            )

        val exception = shouldThrow<IllegalStateException> { site.copyResources() }

        // Should report all 3 failures
        exception.message shouldContain "Failed to copy 3 resource(s)"
        exception.message shouldContain "nonexistent1.txt"
        exception.message shouldContain "nonexistent2.txt"
        exception.message shouldContain "nonexistent3.txt"

        File(outputPath).deleteRecursively()
      }

      test("should succeed when resources list is empty") {
        val outputPath = "build/test-output-no-resources"
        val site = createTestSite(outputPath)

        // Should not throw when resources list is empty (default ResourceConfig has emptyList())
        site.copyResources()

        File(outputPath).deleteRecursively()
      }
    }

    context("generateFiles()") {
      test("should create output directory if it doesn't exist") {
        val outputPath = "build/test-output"
        val site = createTestSite(outputPath)

        // Ensure directory doesn't exist
        File(outputPath).deleteRecursively()

        site.generateFiles()

        // Directory should now exist
        Files.exists(Path(outputPath)) shouldBe true
      }

      test("should generate HTML file for each page") {
        val outputPath = "build/test-output"
        val site = createTestSite(outputPath, pages = listOf(homePage, aboutPage))

        site.generateFiles()

        // Check both files exist
        File("$outputPath/index.html").exists() shouldBe true
        File("$outputPath/about.html").exists() shouldBe true
      }

      test("should include page title in generated HTML") {
        val outputPath = "build/test-output"
        val site = createTestSite(outputPath)

        site.generateFiles()

        val html = File("$outputPath/index.html").readText()
        html shouldContain "<title>Test Site</title>"
      }

      test("should include page content in generated HTML") {
        val outputPath = "build/test-output"
        val site = createTestSite(outputPath)

        site.generateFiles()

        val html = File("$outputPath/index.html").readText()
        html shouldContain "Welcome Home"
        html shouldContain "This is the home page"
      }

      test("should include page footer in generated HTML") {
        val outputPath = "build/test-output"
        val site = createTestSite(outputPath)

        site.generateFiles()

        val html = File("$outputPath/index.html").readText()
        html shouldContain "© 2026 Test"
      }

      test("should include local stylesheet links") {
        val outputPath = "build/test-output"
        val site =
          createTestSite(
            outputPath,
            localStylesheets = listOf("css/tailwind.css", "css/custom.css"),
          )

        site.generateFiles()

        val html = File("$outputPath/index.html").readText()
        html shouldContain "href=\"css/tailwind.css\""
        html shouldContain "href=\"css/custom.css\""
        html shouldContain "rel=\"stylesheet\""
      }

      test("should include external stylesheet links with SRI") {
        val outputPath = "build/test-output"
        val site =
          createTestSite(
            outputPath,
            externalStylesheets = listOf(ExternalStylesheet.FONT_AWESOME_6_7_2),
          )

        site.generateFiles()

        val html = File("$outputPath/index.html").readText()
        html shouldContain "font-awesome"
        html shouldContain "integrity=\""
        html shouldContain "crossorigin=\"anonymous\""
      }

      test("should include Google Tag when googleTagId is provided") {
        val outputPath = "build/test-output-google"
        val site = createTestSite(outputPath, googleTagId = "G-ABCD1234EF")

        site.generateFiles()

        val html = File("$outputPath/index.html").readText()
        html shouldContain "G-ABCD1234EF"
        html shouldContain "gtag"
      }

      test("should not include Google Tag when googleTagId is null") {
        val outputPath = "build/test-output"
        val site = createTestSite(outputPath, googleTagId = null)

        site.generateFiles()

        val html = File("$outputPath/index.html").readText()
        html shouldNotContain "gtag"
      }

      test("should include viewport meta tag") {
        val outputPath = "build/test-output"
        val site = createTestSite(outputPath)

        site.generateFiles()

        val html = File("$outputPath/index.html").readText()
        html shouldContain "name=\"viewport\""
        html shouldContain "width=device-width"
      }

      test("should include charset meta tag") {
        val outputPath = "build/test-output"
        val site = createTestSite(outputPath)

        site.generateFiles()

        val html = File("$outputPath/index.html").readText()
        html shouldContain "charset=\"utf-8\""
      }

      test("should throw error when output directory creation fails") {
        // Use an invalid path that will fail to create
        val site = createTestSite("/invalid/path/that/cannot/be/created")

        val exception = shouldThrow<IllegalStateException> { site.generateFiles() }
        exception.message shouldContain "Failed to create output directory"
      }

      test("should handle exceptions thrown during page content rendering") {
        val crashingPage =
          object : Page {
            override val title = "Crashing Page"
            override val outputFilename = "crash.html"
            override val content = { _: PageSettings, _: kotlinx.html.FlowContent ->
              throw RuntimeException("Simulated rendering error")
            }
            override val footer = { _: PageSettings, flow: kotlinx.html.FlowContent ->
              flow.div { p { +"Footer" } }
            }
          }

        val outputPath = "build/test-crash-handling"
        val site = createTestSite(outputPath, pages = listOf(crashingPage))

        val exception = shouldThrow<IllegalStateException> { site.generateFiles() }
        exception.message shouldContain "Failed to generate 1 file(s)"
        exception.message shouldContain "crash.html"

        File(outputPath).deleteRecursively()
      }

      test("should collect multiple page rendering failures") {
        val crashingPage1 =
          object : Page {
            override val title = "Crash 1"
            override val outputFilename = "crash1.html"
            override val content = { _: PageSettings, _: kotlinx.html.FlowContent ->
              throw RuntimeException("Error 1")
            }
            override val footer = { _: PageSettings, flow: kotlinx.html.FlowContent ->
              flow.div { p { +"Footer" } }
            }
          }

        val crashingPage2 =
          object : Page {
            override val title = "Crash 2"
            override val outputFilename = "crash2.html"
            override val content = { _: PageSettings, _: kotlinx.html.FlowContent ->
              throw RuntimeException("Error 2")
            }
            override val footer = { _: PageSettings, flow: kotlinx.html.FlowContent ->
              flow.div { p { +"Footer" } }
            }
          }

        val outputPath = "build/test-multi-crash"
        val site =
          createTestSite(outputPath, pages = listOf(crashingPage1, homePage, crashingPage2))

        val exception = shouldThrow<IllegalStateException> { site.generateFiles() }
        exception.message shouldContain "Failed to generate 2 file(s)"
        exception.message shouldContain "crash1.html"
        exception.message shouldContain "crash2.html"
        // homePage should not be in error list
        exception.message shouldNotContain "index.html"

        File(outputPath).deleteRecursively()
      }

      test("should handle exceptions thrown during footer rendering") {
        val crashingFooterPage =
          object : Page {
            override val title = "Footer Crash"
            override val outputFilename = "footer-crash.html"
            override val content = { _: PageSettings, flow: kotlinx.html.FlowContent ->
              flow.div { p { +"Content renders fine" } }
            }
            override val footer = { _: PageSettings, _: kotlinx.html.FlowContent ->
              throw RuntimeException("Footer rendering error")
            }
          }

        val outputPath = "build/test-footer-crash"
        val site = createTestSite(outputPath, pages = listOf(crashingFooterPage))

        val exception = shouldThrow<IllegalStateException> { site.generateFiles() }
        exception.message shouldContain "Failed to generate 1 file(s)"
        exception.message shouldContain "footer-crash.html"

        File(outputPath).deleteRecursively()
      }

      test("should continue processing other pages when one fails") {
        val crashingPage =
          object : Page {
            override val title = "Crash"
            override val outputFilename = "crash.html"
            override val content = { _: PageSettings, _: kotlinx.html.FlowContent ->
              throw RuntimeException("Error")
            }
            override val footer = { _: PageSettings, flow: kotlinx.html.FlowContent ->
              flow.div { p { +"Footer" } }
            }
          }

        val outputPath = "build/test-partial-failure"
        val site = createTestSite(outputPath, pages = listOf(homePage, crashingPage, aboutPage))

        val exception = shouldThrow<IllegalStateException> { site.generateFiles() }

        // Should have attempted all pages and only failed on crashingPage
        exception.message shouldContain "Failed to generate 1 file(s)"
        exception.message shouldContain "crash.html"

        // Successful pages should have been written
        File("$outputPath/index.html").exists() shouldBe true
        File("$outputPath/about.html").exists() shouldBe true
        File("$outputPath/crash.html").exists() shouldBe false

        File(outputPath).deleteRecursively()
      }
    }
  })
