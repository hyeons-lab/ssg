package com.hyeonslab.ssg.core

import com.hyeonslab.ssg.utils.Tailwind
import kotlinx.serialization.Serializable

@Serializable
data class TextConfig(
    val textSize: String,
    val font: String,
    val textColor: Tailwind.Colors.Text,
)
