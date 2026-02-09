/*
 * Copyright 2024-2026 Hyeons' Lab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyeonslab.ssg.core

import kotlinx.serialization.Serializable

/**
 * Represents a blog post or article item with title and path.
 *
 * This class can be used to maintain a list of blog posts or articles for navigation, sitemaps, or
 * RSS feeds.
 *
 * @property title The display title of the post or article
 * @property path The relative or absolute URL path to the post (e.g., "posts/my-article.html")
 *
 * Example:
 * ```kotlin
 * val posts = listOf(
 *     PostItem(
 *         title = "Getting Started with SSG",
 *         path = "posts/getting-started.html"
 *     ),
 *     PostItem(
 *         title = "Advanced Tailwind Techniques",
 *         path = "posts/advanced-tailwind.html"
 *     )
 * )
 * ```
 */
@Serializable data class PostItem(val title: String, val path: String)
