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

import com.hyeonslab.ssg.core.ExternalStylesheet
import com.hyeonslab.ssg.page.Page
import com.hyeonslab.ssg.page.PageSettings
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.p

class SiteDslTest :
  FunSpec({

    // Test page
    val homePage =
      object : Page {
        override val title = "Home"
        override val outputFilename = "index.html"
        override val content = { _: PageSettings, flow: kotlinx.html.FlowContent ->
          flow.div {
            h1 { +"Home" }
            p { +"Content" }
          }
        }
        override val footer = { _: PageSettings, flow: kotlinx.html.FlowContent ->
          flow.div { p { +"Footer" } }
        }
      }

    context("site DSL") {
      test("should create Site with minimal configuration") {
        val site = site {
          outputPath = "build/test"
          title = "Test Site"
          pages = listOf(homePage)

          navigation {
            backgroundColor = "bg-white"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-700"
            logo("logo.png", width = 100, height = 50)
          }
        }

        site.outputPath shouldBe "build/test"
        site.title shouldBe "Test Site"
        site.version shouldBe "1.0.0" // default
        site.pages shouldBe listOf(homePage)
        site.navigation?.backgroundColor shouldBe "bg-white"
        site.navigation?.logo?.imageUrl shouldBe "logo.png"
        site.navigation?.logo?.width shouldBe 100
        site.navigation?.logo?.height shouldBe 50
      }

      test("should create Site with full configuration") {
        val site = site {
          outputPath = "build/test"
          title = "Test Site"
          version = "2.0.0"
          backgroundColor = "bg-gray-50"
          pages = listOf(homePage)

          navigation {
            backgroundColor = "bg-white"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-700"
            isSticky = true
            instagram = "testuser"
            email = "test@example.com"
            logo("logo.png", width = 100, height = 50)
            blurNavBackground = true
            fontFamily = "font-inter"
            horizontalMargin = "24"
          }

          resources {
            staticFile("images/logo.png", "build/test")
            staticFile("css/custom.css", "build/test", "css/styles.css")
            localStylesheet("css/tailwind.css")
            localStylesheet("css/custom.css")
            externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
          }

          integrations { googleTag = "G-XXXXXXXXXX" }
        }

        site.version shouldBe "2.0.0"
        site.backgroundColor shouldBe "bg-gray-50"
        site.navigation?.isSticky shouldBe true
        site.navigation?.instagram shouldBe "testuser"
        site.navigation?.email shouldBe "test@example.com"
        site.navigation?.blurNavBackground shouldBe true
        site.navigation?.fontFamily shouldBe "font-inter"
        site.navigation?.horizontalMargin shouldBe "24"
        site.resources.staticFiles.size shouldBe 2
        site.resources.localStylesheets shouldBe listOf("css/tailwind.css", "css/custom.css")
        site.resources.externalStylesheets.size shouldBe 1
        site.integrations.googleTagId shouldBe "G-XXXXXXXXXX"
      }

      test("should use default values when resources and integrations not specified") {
        val site = site {
          outputPath = "build/test"
          title = "Test Site"
          pages = listOf(homePage)

          navigation {
            backgroundColor = "bg-white"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-700"
            logo("logo.png", width = 100, height = 50)
          }
        }

        site.resources shouldNotBe null
        site.resources.staticFiles shouldBe emptyList()
        site.resources.localStylesheets shouldBe listOf("css/tailwind.css")
        site.resources.externalStylesheets shouldBe emptyList()
        site.integrations shouldNotBe null
        site.integrations.googleTagId shouldBe null
      }

      test("should throw error when outputPath is missing") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            site {
              title = "Test Site"
              pages = listOf(homePage)
              navigation {
                backgroundColor = "bg-white"
                navSelectedColor = "text-blue-600"
                navDefaultColor = "text-gray-700"
                logo("logo.png", width = 100, height = 50)
              }
            }
          }
        exception.message shouldContain "outputPath must be specified"
      }

      test("should throw error when title is missing") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            site {
              outputPath = "build/test"
              pages = listOf(homePage)
              navigation {
                backgroundColor = "bg-white"
                navSelectedColor = "text-blue-600"
                navDefaultColor = "text-gray-700"
                logo("logo.png", width = 100, height = 50)
              }
            }
          }
        exception.message shouldContain "title must be specified"
      }

      test("should throw error when pages is missing") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            site {
              outputPath = "build/test"
              title = "Test Site"
              navigation {
                backgroundColor = "bg-white"
                navSelectedColor = "text-blue-600"
                navDefaultColor = "text-gray-700"
                logo("logo.png", width = 100, height = 50)
              }
            }
          }
        exception.message shouldContain "pages must be specified"
      }

      test("should allow creating site without navigation") {
        val site = site {
          outputPath = "build/test"
          title = "Test Site"
          pages = listOf(homePage)
        }

        site.navigation shouldBe null
        site.outputPath shouldBe "build/test"
        site.title shouldBe "Test Site"
      }
    }

    context("navigation DSL") {
      test("should create navigation with logo function") {
        val site = site {
          outputPath = "build/test"
          title = "Test"
          pages = listOf(homePage)

          navigation {
            backgroundColor = "bg-white"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-700"
            logo("logo.png", width = 100, height = 50)
          }
        }

        site.navigation?.logo?.imageUrl shouldBe "logo.png"
        site.navigation?.logo?.width shouldBe 100
        site.navigation?.logo?.height shouldBe 50
      }

      test("should create navigation with logo builder block") {
        val site = site {
          outputPath = "build/test"
          title = "Test"
          pages = listOf(homePage)

          navigation {
            backgroundColor = "bg-white"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-700"
            logo {
              imageUrl = "logo.png"
              width = 100
              height = 50
            }
          }
        }

        site.navigation?.logo?.imageUrl shouldBe "logo.png"
        site.navigation?.logo?.width shouldBe 100
        site.navigation?.logo?.height shouldBe 50
      }

      test("should throw error when logo is missing") {
        val exception =
          shouldThrow<IllegalArgumentException> {
            site {
              outputPath = "build/test"
              title = "Test"
              pages = listOf(homePage)

              navigation {
                backgroundColor = "bg-white"
                navSelectedColor = "text-blue-600"
                navDefaultColor = "text-gray-700"
              }
            }
          }
        exception.message shouldContain "logo must be specified"
      }
    }

    context("resources DSL") {
      test("should add static files using vararg function") {
        val site = site {
          outputPath = "build/test"
          title = "Test"
          pages = listOf(homePage)

          navigation {
            backgroundColor = "bg-white"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-700"
            logo("logo.png", width = 100, height = 50)
          }

          resources {
            staticFile("file1.txt", "build/test")
            staticFile("file2.txt", "build/test")
          }
        }

        site.resources.staticFiles.size shouldBe 2
        site.resources.staticFiles[0].inputFilename shouldBe "file1.txt"
        site.resources.staticFiles[1].inputFilename shouldBe "file2.txt"
      }

      test("should add multiple stylesheets using vararg function") {
        val site = site {
          outputPath = "build/test"
          title = "Test"
          pages = listOf(homePage)

          navigation {
            backgroundColor = "bg-white"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-700"
            logo("logo.png", width = 100, height = 50)
          }

          resources { localStylesheets("css/tailwind.css", "css/custom.css", "css/theme.css") }
        }

        site.resources.localStylesheets shouldBe
          listOf("css/tailwind.css", "css/custom.css", "css/theme.css")
      }

      test("should add external stylesheets") {
        val site = site {
          outputPath = "build/test"
          title = "Test"
          pages = listOf(homePage)

          navigation {
            backgroundColor = "bg-white"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-700"
            logo("logo.png", width = 100, height = 50)
          }

          resources {
            externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
            externalStylesheet(
              ExternalStylesheet(href = "https://fonts.googleapis.com/css2?family=Inter")
            )
          }
        }

        site.resources.externalStylesheets.size shouldBe 2
      }
    }

    context("integrations DSL") {
      test("should set googleTagId using property") {
        val site = site {
          outputPath = "build/test"
          title = "Test"
          pages = listOf(homePage)

          navigation {
            backgroundColor = "bg-white"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-700"
            logo("logo.png", width = 100, height = 50)
          }

          integrations { googleTagId = "G-TEST123" }
        }

        site.integrations.googleTagId shouldBe "G-TEST123"
      }

      test("should set googleTag using alias property") {
        val site = site {
          outputPath = "build/test"
          title = "Test"
          pages = listOf(homePage)

          navigation {
            backgroundColor = "bg-white"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-700"
            logo("logo.png", width = 100, height = 50)
          }

          integrations { googleTag = "G-TEST456" }
        }

        site.integrations.googleTagId shouldBe "G-TEST456"
      }
    }
  })
