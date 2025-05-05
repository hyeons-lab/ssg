package com.hyeonslab.ssg.page

import kotlinx.html.FlowContent

interface Page {
    val title: String
    val outputFilename: String
    val render: (PageSettings, FlowContent) -> Unit
}
