# Hyeons' Lab: Static Site Generator

A lightweight, type-safe static site generator written in Kotlin that generates modern HTML with Tailwind CSS.

## Overview

This project provides a programmatic approach to building static websites using Kotlin's type-safe DSL. Instead of working with template files, you define your site structure and content directly in Kotlin code using the kotlinx.html library.

## Features

- **Type-Safe HTML Generation**: Use kotlinx.html DSL for compile-time verified HTML generation
- **Tailwind CSS Integration**: Built-in support for Tailwind CSS styling with opt-in external CDN stylesheets
- **Kotlin DSL**: Clean, readable configuration syntax with nested builders for site, navigation, resources, and integrations
- **Optional Navigation Menu**: Configurable navigation with features including:
  - Sticky positioning
  - Blur background effects
  - Custom logo support
  - Social media links (Instagram, Email)
  - Mobile-responsive design
  - Can be omitted for sites that don't need navigation
- **Optional Footers**: Pages can include footers or omit them entirely
- **Flexible Layout Configuration**: Customizable CSS classes for HTML, body, and content wrapper elements
- **Resource Management**: Simple API for copying static resources (CSS, images, fonts) with path validation
- **External Stylesheets**: Opt-in CDN stylesheets (Font Awesome, Google Fonts) with SRI integrity support
- **Google Analytics**: Optional Google Tag Manager integration with XSS protection
- **Security First**: Input validation for CSS classes, paths, and external resources to prevent injection attacks
- **Gradle-Based**: Seamless integration with Kotlin/JVM projects

## Technology Stack

- **Kotlin**: 2.3.10
- **kotlinx.html**: 0.12.0
- **kotlinx.serialization**: 1.10.0
- **kotlinx.io**: 0.8.2
- **Gradle**: 9.0+
- **Java**: 21 (JVM toolchain)

## Current Status

**Version**: 0.1.0

This project is ready for production use. The core functionality is stable and well-tested with comprehensive test coverage.

📋 **See [CHANGELOG.md](CHANGELOG.md) for version history and recent improvements.**

### What's Working
- HTML page generation with kotlinx.html DSL
- Kotlin DSL for clean, type-safe configuration
- Optional navigation menu with full customization
- Optional page footers
- Flexible layout configuration (htmlClasses, bodyClasses, contentClasses)
- Tailwind CSS integration with opt-in external stylesheets
- Resource copying with security validation
- Maven Local publishing
- Comprehensive input validation and security features

### In Development
- Additional Tailwind utility classes
- Blog/post generation features
- Enhanced page templating
- Documentation and examples

## Usage

### Building

Build the library as a JAR:
```bash
./gradlew jar
```
The JAR will be located in `lib/build/libs/`

### Publishing

Publish to Maven Local for use in other projects:
```bash
./gradlew publish
```

### Integration

Add to your project's `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.hyeons-lab:ssg:0.1.0")
}
```

### Tailwind CSS Setup

The library generates HTML with Tailwind CSS classes. To use it effectively, you need to set up Tailwind CSS compilation in your project.

#### 1. Add Tailwind Gradle Plugin

Add to your `build.gradle.kts`:

```kotlin
plugins {
    id("com.hyeons-lab.tailwind") version "0.3.0"
}

tailwind {
    version = "4.1.18"
    configPath = "src/main/resources"
    input = "src/main/resources/tailwind/input.css"
    output = "src/main/resources/css/tailwind.css"
}
```

#### 2. Create Tailwind Configuration

Create `src/main/resources/tailwind.config.js`:

```javascript
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './build/generated_html/**/*.html',
  ],
  theme: {
    extend: {
      colors: {
        // Add your custom colors
      },
      fontFamily: {
        // Add your custom fonts
      },
    },
  },
  plugins: [],
}
```

#### 3. Create Tailwind Input CSS

Create `src/main/resources/tailwind/input.css`:

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

/* Custom styles */
.sticky {
    position: sticky;
    top: 0px;
}
```

### Build Process

The typical workflow for building a site:

```bash
# 1. Download Tailwind CLI (first time only)
./gradlew tailwindDownload

# 2. Initialize Tailwind (first time only)
./gradlew tailwindInit

# 3. Generate HTML files (runs your main() function)
./gradlew run

# 4. Compile Tailwind CSS (scans generated HTML and creates CSS)
./gradlew tailwindCompile
```

You can automate this with a build script:

```bash
#!/bin/bash
./gradlew tailwindDownload && \
./gradlew tailwindInit && \
./gradlew run && \
./gradlew tailwindCompile
```

The generated site will be in your configured `outputPath` (e.g., `build/generated_html/`).

### Basic Example

The library provides two ways to create a Site: using the **Kotlin DSL** (recommended) or the **constructor API**.

#### Using Kotlin DSL (Recommended)

```kotlin
import com.hyeonslab.ssg.core.dsl.site
import com.hyeonslab.ssg.page.*
import java.io.File

fun main() {
    val outputPath = "build/generated_html"

    val site = site {
        outputPath = "build/generated_html"
        title = "My Site"
        version = "1.0.0"
        backgroundColor = "bg-white"
        htmlClasses = ""  // Optional: Classes for <html> element
        bodyClasses = "flex flex-col min-h-screen"  // Optional: Classes for <body>
        contentClasses = "flex-1 flex flex-col"  // Optional: Classes for content wrapper
        pages = listOf(/* your pages */)

        // Optional: Navigation menu (can be omitted)
        navigation {
            backgroundColor = "bg-gray-100"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-800"
            isSticky = false
            instagram = "yourhandle" // Optional: Instagram username (without @)
            email = "contact@example.com" // Optional: Email address
            logo("images/logo.png", width = 100, height = 100)
            blurNavBackground = false
        }

        resources {
            staticFile("images/logo.png", outputPath)
            staticFile("css/tailwind.css", outputPath)
            localStylesheet("css/tailwind.css")
            // Optional: Add Font Awesome (opt-in, not included by default)
            externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
            // Optional: Add Google Fonts
            externalStylesheet(ExternalStylesheet(
                href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap"
            ))
        }

        integrations {
            googleTag = "G-XXXXXXXXXX" // Optional: Google Analytics
        }
    }

    // Clean previous output (optional)
    File(outputPath).deleteRecursively()

    // Generate HTML files
    site.generateFiles()

    // Copy static resources (images, CSS, etc.)
    site.copyResources()
}
```

#### Using Constructor API

```kotlin
import com.hyeonslab.ssg.core.*
import com.hyeonslab.ssg.page.*
import java.io.File

fun main() {
    val outputPath = "build/generated_html"

    val site = Site(
        outputPath = outputPath,
        title = "My Site",
        version = "1.0.0", // Optional: defaults to "1.0.0"
        backgroundColor = "bg-white",
        htmlClasses = "",  // Optional: Classes for <html> element
        bodyClasses = "flex flex-col min-h-screen",  // Optional: Default layout
        contentClasses = "flex-1 flex flex-col",  // Optional: Content wrapper layout
        pages = listOf(/* your pages */),
        navigation = NavMenuSettings(  // Optional: null for sites without navigation
            backgroundColor = "bg-gray-100",
            navSelectedColor = "text-blue-600",
            navDefaultColor = "text-gray-800",
            isSticky = false,
            instagram = "yourhandle", // Optional: Instagram username (without @)
            email = "contact@example.com", // Optional: Email address
            logo = Logo(imageUrl = "images/logo.png", width = 100, height = 100),
            blurNavBackground = false
        ),
        resources = ResourceConfig(
            staticFiles = listOf(
                InputOutputPair("images/logo.png", outputPath),
                InputOutputPair("css/tailwind.css", outputPath)
            ),
            localStylesheets = listOf("css/tailwind.css"),
            externalStylesheets = listOf(
                // Optional: Font Awesome (opt-in, not included by default)
                ExternalStylesheet.FONT_AWESOME_6_7_2,
                // Optional: Google Fonts
                ExternalStylesheet(
                    href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap"
                )
            )
        ),
        integrations = IntegrationConfig(
            googleTagId = "G-XXXXXXXXXX" // Optional: null to skip analytics
        ),
        pageSettings = PageSettings()
    )

    // Clean previous output (optional)
    File(outputPath).deleteRecursively()

    // Generate HTML files
    site.generateFiles()

    // Copy static resources (images, CSS, etc.)
    site.copyResources()
}
```

For a complete working example, see [USAGE_GUIDE.md](USAGE_GUIDE.md).

## Project Structure

```
lib/src/main/kotlin/com/hyeonslab/ssg/
├── core/           # Core site generation logic
│   ├── Site.kt     # Main site builder
│   └── ...
├── page/           # Page interfaces and components
│   ├── Page.kt     # Page interface
│   ├── NavMenu.kt  # Navigation menu component
│   └── ...
└── utils/          # Utilities (Tailwind helpers, etc.)
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please open an issue to discuss significant changes before submitting a PR.
