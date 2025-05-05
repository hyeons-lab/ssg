package com.hyeonslab.ssg.core

import kotlinx.serialization.Serializable

@Serializable
data class PostItem(
    val title: String,
    val path: String,
)
