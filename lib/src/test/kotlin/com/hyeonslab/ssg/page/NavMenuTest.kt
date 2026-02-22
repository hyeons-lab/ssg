package com.hyeonslab.ssg.page

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.appendHTML

class NavMenuTest :
  FunSpec({

    // Test pages
    val homePage =
      object : Page {
        override val title = "Home"
        override val outputFilename = "index.html"
        override val content = { _: PageSettings, _: kotlinx.html.FlowContent -> }
        override val footer = { _: PageSettings, _: kotlinx.html.FlowContent -> }
      }

    val aboutPage =
      object : Page {
        override val title = "About"
        override val outputFilename = "about.html"
        override val content = { _: PageSettings, _: kotlinx.html.FlowContent -> }
        override val footer = { _: PageSettings, _: kotlinx.html.FlowContent -> }
      }

    val contactPage =
      object : Page {
        override val title = "Contact"
        override val outputFilename = "contact.html"
        override val content = { _: PageSettings, _: kotlinx.html.FlowContent -> }
        override val footer = { _: PageSettings, _: kotlinx.html.FlowContent -> }
      }

    context("navMenu rendering") {
      test("should render navigation with logo") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage, aboutPage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        html shouldContain "<nav"
        html shouldContain "logo.png"
        html shouldContain "width: 50px"
        html shouldContain "height: 50px"
      }

      test("should render all page links") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage, aboutPage, contactPage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        // NavMenu uses CSS uppercase class, text content is original case
        html shouldContain ">Home<"
        html shouldContain ">About<"
        html shouldContain ">Contact<"
        html shouldContain "class=\"uppercase"
        html shouldContain "index.html"
        html shouldContain "about.html"
        html shouldContain "contact.html"
      }

      test("should highlight selected page with selected color") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = aboutPage,
                pages = listOf(homePage, aboutPage, contactPage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        // Should contain selected color class
        html shouldContain "text-blue-600"
        // Should contain default color class for unselected pages
        html shouldContain "text-gray-700"
      }

      test("should apply sticky class when isSticky is true") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = true,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        html shouldContain "sticky"
      }

      test("should not apply sticky class when isSticky is false") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        // Should have empty sticky class value (rendered as just whitespace)
        // The implementation uses "$sticky" where sticky = "" when false
        html shouldNotContain "sticky"
      }

      test("should apply blur class when blurNavBackground is true") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = true,
                  ),
              )
            }
          }
        }

        html shouldContain "backdrop-blur-md"
      }

      test("should not apply blur class when blurNavBackground is false") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        html shouldNotContain "backdrop-blur-xl"
      }

      test("should include Instagram link when instagram username is provided") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    instagram = "testuser",
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        html shouldContain "https://www.instagram.com/testuser"
        html shouldContain "fa-instagram"
      }

      test("should not include Instagram link when instagram is null") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    instagram = null,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        html shouldNotContain "instagram.com"
        html shouldNotContain "fa-instagram"
      }

      test("should include email link when email is provided") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    email = "test@example.com",
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        html shouldContain "mailto:test@example.com"
        html shouldContain "fa-envelope"
      }

      test("should not include email link when email is null") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    email = null,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        html shouldNotContain "mailto:"
        html shouldNotContain "fa-envelope"
      }

      test("should apply custom font family") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                    fontFamily = "font-inter",
                  ),
              )
            }
          }
        }

        html shouldContain "font-inter"
      }

      test("should apply default font family when not specified") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        html shouldContain "font-plex-sans"
      }

      test("should apply background color") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-gray-800",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        html shouldContain "bg-gray-800"
      }

      test("should include both Instagram and email links when both are provided") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    instagram = "testuser",
                    email = "test@example.com",
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        html shouldContain "instagram.com/testuser"
        html shouldContain "fa-instagram"
        html shouldContain "mailto:test@example.com"
        html shouldContain "fa-envelope"
      }

      test("should apply default horizontal margins") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                  ),
              )
            }
          }
        }

        html shouldContain "px-4"
        html shouldContain "md:px-16"
      }

      test("should apply custom horizontal margins") {
        val html = buildString {
          appendHTML().html {
            body {
              navMenu(
                selected = homePage,
                pages = listOf(homePage),
                navMenuSettings =
                  NavMenuSettings(
                    backgroundColor = "bg-white",
                    navSelectedColor = "text-blue-600",
                    navDefaultColor = "text-gray-700",
                    isSticky = false,
                    logo = Logo(imageUrl = "logo.png", width = 50, height = 50),
                    blurNavBackground = false,
                    horizontalMargin = "8",
                  ),
              )
            }
          }
        }

        html shouldContain "px-4"
        html shouldContain "md:px-8"
      }
    }
  })
