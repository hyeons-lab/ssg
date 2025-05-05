package com.hyeonslab.ssg.page

import com.hyeonslab.ssg.core.adjustSelected
import com.hyeonslab.ssg.utils.Tailwind
import kotlinx.html.BODY
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.nav
import kotlinx.html.style

fun BODY.navMenu(
    selected: Page,
    pages: List<Page>,
    navMenuSettings: NavMenuSettings,
) {
    val sticky = if (navMenuSettings.isSticky) "sticky" else ""
    nav(classes = "$sticky font-plex-sans flex w-full py-4 px-2 ${navMenuSettings.backgroundColor}") {
        style = "z-index: 255;"
        a(href = "./index.html") {
            div(classes = "h-[${navMenuSettings.logo.height}px] w-[${navMenuSettings.logo.width}px]") {
                img(
                    src = navMenuSettings.logo.imageUrl,
                    classes = "h-[${navMenuSettings.logo.height}px] w-[${navMenuSettings.logo.width}px]",
                )
            }
        }
        val baseClasses = "${Tailwind.Text.Size.sm} font-plex-sans mx-2 me-2 vertical-menu horizontal-menu md:text-base lg:text-lg"

        pages.forEach { page ->
            h1(classes = baseClasses) {
                adjustSelected(page, selected, navMenuSettings.navSelectedColor, navMenuSettings.navDefaultColor)
                a(classes = "uppercase z-1 mx-4 text-nowrap", href = "./${page.outputFilename}") {
                    +page.title
                }
            }
        }

        div(classes = "grow")

        a(href = navMenuSettings.instagram, classes = "${navMenuSettings.navDefaultColor} ms-4") {
            div(classes = "fa fa-instagram ${Tailwind.Text.Size.`2xl`}")
        }
        div(classes = "mx-2")
    }
}
