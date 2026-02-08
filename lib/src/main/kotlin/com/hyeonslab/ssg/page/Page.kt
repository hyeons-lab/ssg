package com.hyeonslab.ssg.page

import kotlinx.html.FlowContent

interface Page {
    val title: String
    val outputFilename: String
    val content: (PageSettings, FlowContent) -> Unit
    val footer: (PageSettings, FlowContent) -> Unit
}
