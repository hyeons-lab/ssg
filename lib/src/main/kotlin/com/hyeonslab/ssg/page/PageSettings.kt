package com.hyeonslab.ssg.page

import com.hyeonslab.ssg.core.TextConfig
import com.hyeonslab.ssg.utils.Tailwind
import kotlinx.serialization.Serializable

@Serializable
data class PageSettings(
    val h1: TextConfig =
        TextConfig(
            textSize = Tailwind.Text.Size.`2xl`.size,
            font = "font-plex-serif",
            textColor = Tailwind.Colors.Text.Neutral.`600`,
        ),
    val bodyTextColor: Tailwind.Colors.Text = Tailwind.Colors.Text.Neutral.`600`,
)
