# Module Hyeons' Lab SSG

A lightweight, type-safe static site generator written in Kotlin that generates modern HTML with Tailwind CSS support.

## Overview

This library provides a programmatic approach to building static websites using Kotlin's type-safe DSL.
Instead of working with template files, you define your site structure and content directly in Kotlin code
using the kotlinx.html library.

## Key Features

- **Type-Safe HTML Generation**: Compile-time verified HTML using kotlinx.html DSL
- **Kotlin DSL**: Clean, readable configuration with nested builders
- **Flexible Configuration**: Optional navigation, optional footers, customizable layout
- **Security First**: Input validation to prevent XSS and injection attacks
- **Tailwind CSS Integration**: Built-in support with opt-in external stylesheets
- **Resource Management**: Simple API for copying static files with path validation

## Main Packages

### `com.hyeonslab.ssg.core`
Core site generation functionality including the main `Site` class, configuration classes,
and resource management.

### `com.hyeonslab.ssg.core.dsl`
Kotlin DSL builders for clean, type-safe site configuration. Provides the `site { }` function
and nested builder classes.

### `com.hyeonslab.ssg.page`
Page interfaces and navigation menu components. Defines the `Page` interface and navigation
menu settings.

### `com.hyeonslab.ssg.utils`
Utility classes including Tailwind CSS helpers and color definitions.

## Quick Start

### Using Kotlin DSL (Recommended)

```kotlin
import com.hyeonslab.ssg.core.dsl.site
import com.hyeonslab.ssg.core.ExternalStylesheet

val site = site {
    outputPath = "build/generated_html"
    title = "My Site"
    version = "1.0.0"
    pages = listOf(HomePage, AboutPage)

    navigation {
        backgroundColor = "bg-gray-100"
        navSelectedColor = "text-blue-600"
        navDefaultColor = "text-gray-700"
        logo("images/logo.png", width = 100, height = 50)
    }

    resources {
        staticFile("css/tailwind.css", "build/generated_html")
        localStylesheet("css/tailwind.css")
    }
}

site.generateFiles()
site.copyResources()
```

### Using Constructor API

```kotlin
import com.hyeonslab.ssg.core.*
import com.hyeonslab.ssg.page.*

val site = Site(
    outputPath = "build/generated_html",
    title = "My Site",
    version = "1.0.0",
    pages = listOf(HomePage, AboutPage),
    navigation = NavMenuSettings(
        backgroundColor = "bg-gray-100",
        navSelectedColor = "text-blue-600",
        navDefaultColor = "text-gray-700",
        isSticky = false,
        logo = Logo(imageUrl = "images/logo.png", width = 100, height = 50),
        blurNavBackground = false
    ),
    resources = ResourceConfig(
        localStylesheets = listOf("css/tailwind.css")
    )
)

site.generateFiles()
site.copyResources()
```

## Security

All user-provided strings (CSS classes, paths, URLs) are validated to prevent:
- HTML attribute injection attacks
- XSS via src/href attributes
- Path traversal attacks
- Invalid email addresses and social media handles

## License

Licensed under the Apache License 2.0
