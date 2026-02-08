package com.hyeonslab.ssg.core

import com.hyeonslab.ssg.page.Page
import kotlinx.html.H1
import kotlinx.html.HEAD
import kotlinx.html.classes
import kotlinx.html.script

fun HEAD.googleTag(tag: String = "") {
    tag.takeIf { it.isNotBlank() }?.let {
        script {
            async = true
            src = "https://www.googletagmanager.com/gtag/js?id=$tag"
        }
        script {
            +"""
                window.dataLayer = window.dataLayer || [];
                function gtag (){ dataLayer.push(arguments); }
                gtag('js', new Date ());
                gtag('config', '$tag');
                """.trimIndent()
        }
    }
}

fun H1.adjustSelected(
    selection: Page,
    selected: Page,
    selectedColor: String,
    unselectedColor: String,
) {
    if (selected == selection) {
        classes += " $selectedColor"
    } else {
        classes += " $unselectedColor"
    }
}
