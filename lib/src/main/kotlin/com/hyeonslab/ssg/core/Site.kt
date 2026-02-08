package com.hyeonslab.ssg.core

import com.hyeonslab.ssg.page.Logo
import com.hyeonslab.ssg.page.NavMenuSettings
import com.hyeonslab.ssg.page.Page
import com.hyeonslab.ssg.page.PageSettings
import com.hyeonslab.ssg.page.navMenu
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.stream.appendHTML
import kotlinx.html.title
import java.io.File

data class Site(
    val outputPath: String,
    val title: String,
    val backgroundColor: String,
    val navBackgroundColor: String,
    val navSelectedColor: String,
    val navColor: String,
    val articleClasses: String,
    val posts: List<PostItem>,
    val navLinks: List<Page>,
    val googleTagId: String?,
    val resources: List<InputOutputPair>,
    val pageSettings: PageSettings,
    val instagram: String,
    val logo: Logo,
    val blurNavBackground: Boolean = false,
) {
    fun copyResources() {
        resources.forEach { inputOutputPair ->
            inputOutputPair.copyResource()
        }
    }

    fun generateFiles() {
        navLinks.forEach { page ->
            File(outputPath).mkdir()
            val generatedHtml =
                buildString {
                    appendHTML().html {
                        head {
                            title { +this@Site.title }
                            link {
                                href = "css/tailwind.css"
                                rel = "stylesheet"
                            }
                            link {
                                rel = "preconnect"
                                href = "https://fonts.googleapis.com"
                            }
                            link {
                                rel = "preconnect"
                                href = "https://fonts.gstatic.com"
                            }
                            link {
                                rel = "stylesheet"
                                href =
                                    "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"
                            }
                            meta {
                                charset = "utf-8"
                            }
                            meta {
                                name = "viewport"
                                content = "width=device-width, initial-scale=1.0"
                            }
                            googleTagId?.let {
                                googleTag(it)
                            }
                        }
                        body(classes = "flex flex-col grow $backgroundColor") {
                            navMenu(
                                selected = page,
                                navMenuSettings =
                                    NavMenuSettings(
                                        backgroundColor = navBackgroundColor,
                                        navSelectedColor = navSelectedColor,
                                        navDefaultColor = navColor,
                                        isSticky = true,
                                        instagram = instagram,
                                        logo = logo,
                                    ),
                                pages = navLinks,
                            )
                            div(classes = "mx-4 my-4") {
                                page.content(pageSettings, this)
                            }
                            page.footer(pageSettings, this)
                        }
                    }
                }
            File("$outputPath/${page.outputFilename}").writeText(generatedHtml)
        }
    }
}
