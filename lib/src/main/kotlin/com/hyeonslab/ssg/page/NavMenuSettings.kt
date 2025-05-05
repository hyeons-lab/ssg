package com.hyeonslab.ssg.page

import kotlinx.serialization.Serializable

data class NavMenuSettings(
    val backgroundColor: String,
    val navSelectedColor: String,
    val navDefaultColor: String,
    val isSticky: Boolean,
    val instagram: String,
    val logo: Logo,
)

@Serializable
data class Logo(
    val imageUrl: String,
    val width: Int,
    val height: Int,
)
