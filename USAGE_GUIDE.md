# Hyeons' Lab SSG - Usage Guide

A complete guide to building static websites with this Kotlin-based static site generator.

## Table of Contents

- [Quick Start](#quick-start)
- [Project Setup](#project-setup)
- [Creating Pages](#creating-pages)
- [Site Configuration](#site-configuration)
- [Styling with Tailwind CSS](#styling-with-tailwind-css)
- [Managing Resources](#managing-resources)
- [Building and Deploying](#building-and-deploying)
- [Complete Example](#complete-example)

---

## Quick Start

### 1. Add Dependency

Add to your `gradle/libs.versions.toml`:

```toml
[versions]
ssg = "0.1.0"

[libraries]
ssg = { module = "com.hyeons-lab:ssg", version.ref = "ssg" }
```

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.ssg)
}
```

### 2. Basic Usage

The library provides two ways to configure sites: **Kotlin DSL** (recommended for readability) or **Constructor API** (more explicit).

#### Using Kotlin DSL (Recommended)

```kotlin
import com.hyeonslab.ssg.core.dsl.site
import com.hyeonslab.ssg.page.*
import kotlinx.html.*

fun main() {
    val site = site {
        outputPath = "build/generated_html"
        title = "My Site"
        version = "1.0.0"
        backgroundColor = "bg-white"
        htmlClasses = ""  // Optional: Classes for <html> element
        bodyClasses = "flex flex-col min-h-screen"  // Optional: Layout classes
        contentClasses = "flex-1 flex flex-col"  // Optional: Content wrapper classes
        pages = listOf(/* your pages */)

        // Optional: Navigation menu (omit this block for sites without navigation)
        navigation {
            backgroundColor = "bg-gray-100"
            navSelectedColor = "text-blue-600"
            navDefaultColor = "text-gray-600"
            isSticky = false
            instagram = "yourhandle" // Optional: Instagram username without @
            email = "contact@example.com" // Optional: Email address
            logo("logo.png", width = 100, height = 100)
            blurNavBackground = false
        }

        resources {
            localStylesheet("css/tailwind.css")
            // Optional: Add Font Awesome (opt-in)
            externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
            // Optional: Add Google Fonts
            externalStylesheet(ExternalStylesheet(
                href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap"
            ))
        }

        integrations {
            googleTag = null  // Optional: Add "G-XXXXXXXXXX" for analytics
        }
    }

    site.generateFiles()
    site.copyResources()
}
```

#### Using Constructor API

```kotlin
import com.hyeonslab.ssg.core.*
import com.hyeonslab.ssg.page.*
import kotlinx.html.*

fun main() {
    val site = Site(
        outputPath = "build/generated_html",
        title = "My Site",
        version = "1.0.0",
        backgroundColor = "bg-white",
        htmlClasses = "",  // Optional: Classes for <html> element
        bodyClasses = "flex flex-col min-h-screen",  // Optional: Layout classes
        contentClasses = "flex-1 flex flex-col",  // Optional: Content wrapper classes
        pages = listOf(/* your pages */),
        navigation = NavMenuSettings(  // Optional: null for sites without navigation
            backgroundColor = "bg-gray-100",
            navSelectedColor = "text-blue-600",
            navDefaultColor = "text-gray-600",
            isSticky = false,
            instagram = "yourhandle", // Optional: Instagram username without @
            email = "contact@example.com", // Optional: Email address
            logo = Logo(imageUrl = "logo.png", width = 100, height = 100),
            blurNavBackground = false
        ),
        resources = ResourceConfig(
            staticFiles = emptyList(),
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
            googleTagId = null  // Optional: "G-XXXXXXXXXX" for analytics
        ),
        pageSettings = PageSettings()
    )

    site.generateFiles()
    site.copyResources()
}
```

---

## Project Setup

### Recommended Project Structure

```
my-site/
├── app/
│   ├── src/main/
│   │   ├── kotlin/
│   │   │   └── com/example/
│   │   │       ├── App.kt              # Main application
│   │   │       └── Pages.kt            # Page definitions
│   │   └── resources/
│   │       ├── css/
│   │       │   └── tailwind.css
│   │       ├── images/
│   │       │   ├── logo.png
│   │       │   └── ...
│   │       ├── tailwind/
│   │       │   └── input.css           # Tailwind source
│   │       └── tailwind.config.js      # Tailwind config
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
└── settings.gradle.kts
```

### Gradle Configuration

**build.gradle.kts:**

```kotlin
plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
    id("com.hyeons-lab.tailwind") version "0.3.0"  // For Tailwind support
}

dependencies {
    implementation(libs.ssg)
}

application {
    mainClass = "com.example.AppKt"
}

tailwind {
    version = "3.4.17"
    configPath = "src/main/resources"
    input = "src/main/resources/tailwind/input.css"
    output = "src/main/resources/css/tailwind.css"
}
```

**settings.gradle.kts:**

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "my-static-site"
```

---

## Creating Pages

### Using Sealed Interface Pattern

The recommended approach is to use a sealed interface to define all your pages:

```kotlin
import com.hyeonslab.ssg.page.Page
import com.hyeonslab.ssg.page.PageSettings
import kotlinx.html.*

sealed interface MyPages : Page {

    // Home Page
    data object Home : MyPages {
        override val title: String = "Home"
        override val outputFilename: String = "index.html"

        override val content: (PageSettings, FlowContent) -> Unit = { settings, flow ->
            flow.div(classes = "container mx-auto px-4") {
                h1(classes = "${settings.h1.textSize} ${settings.h1.font}") {
                    +"Welcome to My Site"
                }
                p(classes = settings.bodyTextColor.color) {
                    +"This is the home page content."
                }
            }
        }

        // Optional: Footer can be omitted by not overriding it
        override val footer: ((PageSettings, FlowContent) -> Unit)? = { _, flow ->
            flow.div(classes = "mt-8 py-4 border-t") {
                p(classes = "text-center text-sm text-gray-600") {
                    +"© 2026 My Site. All rights reserved."
                }
            }
        }
    }

    // About Page
    data object About : MyPages {
        override val title = "About"
        override val outputFilename = "about.html"

        override val content: (PageSettings, FlowContent) -> Unit = { settings, flow ->
            flow.div(classes = "container mx-auto px-4") {
                h1(classes = "${settings.h1.textSize} ${settings.h1.font}") {
                    +"About Us"
                }
                p(classes = settings.bodyTextColor.color) {
                    +"Learn more about what we do."
                }
            }
        }

        // Optional: This page can skip the footer by leaving it as null (default)
        // override val footer: ((PageSettings, FlowContent) -> Unit)? = null
    }
}
```

### Page Interface

Every page must implement:

- `title: String` - Display name in navigation
- `outputFilename: String` - Output HTML filename
- `content: (PageSettings, FlowContent) -> Unit` - Main page content
- `footer: ((PageSettings, FlowContent) -> Unit)?` - Optional footer content (can be omitted)

### Using kotlinx.html DSL

The library uses [kotlinx.html](https://github.com/Kotlin/kotlinx.html) for type-safe HTML generation:

```kotlin
div(classes = "container mx-auto") {
    h1 { +"My Heading" }

    p(classes = "text-gray-600") {
        +"Paragraph text"
    }

    a(href = "https://example.com", classes = "text-blue-500") {
        +"Click here"
    }

    ul(classes = "list-disc ml-4") {
        li { +"Item 1" }
        li { +"Item 2" }
    }

    img(src = "images/photo.jpg", classes = "w-full rounded")
}
```

---

## Kotlin DSL Guide

The library provides a Kotlin DSL for a more readable and concise site configuration. This section covers the DSL in detail.

### Why Use the DSL?

**Benefits:**
- **Less Boilerplate**: No need for explicit constructor calls for nested config objects
- **More Readable**: Clear hierarchical structure with nested blocks
- **Discoverable**: IDE autocomplete works naturally within each block
- **Flexible**: Mix and match - use functions or properties as preferred
- **Type-Safe**: Full compile-time type checking like the constructor API

### Basic DSL Structure

```kotlin
import com.hyeonslab.ssg.core.dsl.site

val site = site {
    // Core configuration (required)
    outputPath = "build/generated_html"
    title = "My Site"
    version = "1.0.0"

    // Page styling
    backgroundColor = "bg-white"
    pages = listOf(HomePage, AboutPage)

    // Navigation block (required)
    navigation {
        // ... nav config
    }

    // Resources block (optional)
    resources {
        // ... resources config
    }

    // Integrations block (optional)
    integrations {
        // ... integrations config
    }

    // Page settings (optional)
    pageSettings = PageSettings()
}
```

### Navigation DSL

Configure navigation menu appearance and behavior:

```kotlin
navigation {
    // Required: Colors
    backgroundColor = "bg-gray-100"
    navSelectedColor = "text-blue-600"
    navDefaultColor = "text-gray-700"

    // Required: Logo (two options)

    // Option 1: Simple function call
    logo("images/logo.png", width = 100, height = 50)

    // Option 2: Builder block for more control
    logo {
        imageUrl = "images/logo.png"
        width = 100
        height = 50
    }

    // Optional: Behavior
    isSticky = false
    blurNavBackground = false

    // Optional: Social links
    instagram = "username"  // Without @
    email = "contact@example.com"

    // Optional: Styling
    fontFamily = "font-plex-sans"
    horizontalMargin = "16"
}
```

### Resources DSL

Configure static files, stylesheets, and external resources:

```kotlin
resources {
    // Add static files to copy from classpath
    staticFile("images/logo.png", "build/generated_html")
    staticFile("images/hero.jpg", "build/generated_html")
    staticFile("styles/main.css", "build/generated_html", "css/custom.css")

    // Add local stylesheet references (relative to output)
    localStylesheet("css/tailwind.css")
    localStylesheet("css/custom.css")

    // Add external CDN stylesheets
    externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
    externalStylesheet(ExternalStylesheet(
        href = "https://fonts.googleapis.com/css2?family=Inter"
    ))
}
```

**Batch Operations:**

```kotlin
resources {
    // Add multiple files at once
    localStylesheets("css/tailwind.css", "css/theme.css", "css/custom.css")

    // Add multiple external stylesheets
    externalStylesheets(
        ExternalStylesheet.FONT_AWESOME_6_7_2,
        ExternalStylesheet(href = "...")
    )
}
```

### Integrations DSL

Configure third-party services and analytics:

```kotlin
integrations {
    // Google Analytics 4 / Google Tag Manager
    googleTag = "G-XXXXXXXXXX"

    // Or use the full property name
    googleTagId = "G-XXXXXXXXXX"
}
```

### Complete DSL Example

```kotlin
import com.hyeonslab.ssg.core.dsl.site
import com.hyeonslab.ssg.core.ExternalStylesheet

val outputPath = "build/generated_html"

val site = site {
    // Core
    outputPath = "build/generated_html"
    title = "My Portfolio"
    version = "2.0.0"

    // Styling
    backgroundColor = "bg-gray-50"
    htmlClasses = ""
    bodyClasses = "flex flex-col min-h-screen"
    contentClasses = "flex-1 flex flex-col"

    // Pages
    pages = listOf(
        HomePage,
        AboutPage,
        ProjectsPage,
        ContactPage
    )

    // Navigation
    navigation {
        backgroundColor = "bg-white/90"
        navSelectedColor = "text-blue-600"
        navDefaultColor = "text-gray-700"
        isSticky = true
        blurNavBackground = true

        logo("images/logo.png", width = 120, height = 60)

        instagram = "myhandle"
        email = "hello@example.com"

        fontFamily = "font-inter"
        horizontalMargin = "24"
    }

    // Resources
    resources {
        // Static files
        staticFile("images/logo.png", outputPath)
        staticFile("images/hero-bg.jpg", outputPath)
        staticFile("css/tailwind.css", outputPath)

        // Stylesheets
        localStylesheets("css/tailwind.css", "css/custom.css")
        externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
    }

    // Analytics
    integrations {
        googleTag = "G-ABCD1234EF"
    }

    // Typography
    pageSettings = PageSettings(
        h1 = TextConfig(
            textSize = Tailwind.Text.Size.`4xl`.size,
            font = "font-inter",
            textColor = Tailwind.Colors.Text.Neutral.`900`
        ),
        bodyTextColor = Tailwind.Colors.Text.Neutral.`600`
    )
}

// Generate site
File(outputPath).deleteRecursively()
site.generateFiles()
site.copyResources()
```

### DSL vs Constructor API

Both APIs are fully supported. Choose based on your preference:

| Aspect | DSL | Constructor |
|--------|-----|-------------|
| **Readability** | More concise, cleaner nesting | More explicit, traditional |
| **Boilerplate** | Less (no nested constructors) | More (explicit object creation) |
| **IDE Support** | Excellent (scoped autocomplete) | Excellent (parameter hints) |
| **Validation** | At build() time | At construction time |
| **When to Use** | Default choice, most projects | When you prefer explicit types |

**You can mix both approaches** - for example, create config objects separately and pass them to the DSL:

```kotlin
val myNavSettings = NavMenuSettings(/* ... */)

val site = site {
    outputPath = "build/html"
    title = "My Site"
    pages = listOf(HomePage)
    navigation = myNavSettings  // Use pre-built config

    resources {
        localStylesheet("css/tailwind.css")
    }
}
```

---

## Site Configuration

### Site Parameters

```kotlin
Site(
    // Core configuration
    outputPath = "build/generated_html",          // Where HTML files are written
    title = "My Site Title",                      // Site <title> tag
    version = "1.0.0",                            // Site version (default: "1.0.0")

    // Page styling
    backgroundColor = "bg-white",                 // <body> background
    htmlClasses = "",                             // Classes for <html> element
    bodyClasses = "flex flex-col min-h-screen",   // Classes for <body> element
    contentClasses = "flex-1 flex flex-col",      // Classes for content wrapper

    // Content
    pages = listOf(MyPages.Home, MyPages.About),  // Pages to generate

    // Navigation configuration (optional: null for sites without navigation)
    navigation = NavMenuSettings(
        backgroundColor = "bg-gray-100",          // Navigation bar background
        navSelectedColor = "text-blue-600",       // Active nav link color
        navDefaultColor = "text-gray-600",        // Inactive nav link color
        isSticky = false,                         // Enable sticky positioning
        instagram = "handle",                     // Optional: Instagram username (without @)
        email = "contact@example.com",            // Optional: Email address
        logo = Logo(
            imageUrl = "images/logo.png",
            width = 150,
            height = 100
        ),
        blurNavBackground = false,                // Enable backdrop-blur on nav
        fontFamily = "font-plex-sans",            // Tailwind font class
        horizontalMargin = "16"                   // Spacing unit for margins
    ),

    // Resources and stylesheets
    resources = ResourceConfig(
        staticFiles = listOf(/* resource pairs */),  // Files to copy
        localStylesheets = listOf("css/tailwind.css"), // Local CSS files
        externalStylesheets = listOf(                // Optional: External CDN stylesheets
            ExternalStylesheet.FONT_AWESOME_6_7_2,   // Font Awesome (opt-in)
            ExternalStylesheet(                       // Google Fonts (opt-in)
                href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap"
            )
        )
    ),

    // Third-party integrations
    integrations = IntegrationConfig(
        googleTagId = "G-XXXXXXXXXX"              // Optional: Google Analytics ID
    ),

    // Page-specific settings
    pageSettings = PageSettings()                 // Typography settings
)
```

### Page Settings

Customize typography and colors:

```kotlin
import com.hyeonslab.ssg.core.TextConfig
import com.hyeonslab.ssg.page.PageSettings
import com.hyeonslab.ssg.utils.Tailwind

val pageSettings = PageSettings(
    h1 = TextConfig(
        textSize = Tailwind.Text.Size.`3xl`.size,  // text-3xl
        font = "font-plex-serif",                   // Custom font
        textColor = Tailwind.Colors.Text.Neutral.`900`
    ),
    bodyTextColor = Tailwind.Colors.Text.Neutral.`600`
)
```

---

## Layout Customization

### HTML, Body, and Content Classes

The library provides three parameters to customize the page layout structure:

```kotlin
site {
    htmlClasses = ""  // Classes for <html> element
    bodyClasses = "flex flex-col min-h-screen"  // Classes for <body> element
    contentClasses = "flex-1 flex flex-col"  // Classes for content wrapper
}
```

**Default Layout:**
The default values create a flexbox layout where the content grows to fill available space:
- `bodyClasses = "flex flex-col min-h-screen"` - Body uses flexbox column layout with minimum viewport height
- `contentClasses = "flex-1 flex flex-col"` - Content wrapper grows to fill space, pushing footer to bottom

**Custom Layouts:**

```kotlin
// Simple layout without flexbox
site {
    bodyClasses = ""
    contentClasses = ""
}

// Dark mode with custom layout
site {
    htmlClasses = "dark"
    bodyClasses = "bg-gray-900 text-white min-h-screen"
    contentClasses = "container mx-auto px-4"
}

// Grid-based layout
site {
    bodyClasses = "grid grid-rows-[auto_1fr_auto] min-h-screen"
    contentClasses = "row-start-2"
}
```

### Optional Navigation

Navigation is completely optional. Omit the `navigation` block (DSL) or pass `null` (constructor) to create sites without a navigation menu:

**DSL (no navigation):**
```kotlin
site {
    outputPath = "build/html"
    title = "My Site"
    pages = listOf(HomePage)

    // No navigation block = no navigation menu

    resources {
        localStylesheet("css/tailwind.css")
    }
}
```

**Constructor (no navigation):**
```kotlin
Site(
    outputPath = "build/html",
    title = "My Site",
    pages = listOf(HomePage),
    navigation = null,  // Explicitly null
    resources = ResourceConfig(/* ... */),
    integrations = IntegrationConfig(),
    pageSettings = PageSettings()
)
```

### Optional Footer

Each page can optionally include a footer. By default, `footer` returns `null` (no footer):

**Page without footer:**
```kotlin
data object SimplePage : Page {
    override val title = "Simple"
    override val outputFilename = "simple.html"

    override val content: (PageSettings, FlowContent) -> Unit = { settings, flow ->
        flow.div { +"Content without footer" }
    }

    // No footer override = no footer rendered
}
```

**Page with footer:**
```kotlin
data object PageWithFooter : Page {
    override val title = "With Footer"
    override val outputFilename = "footer.html"

    override val content: (PageSettings, FlowContent) -> Unit = { settings, flow ->
        flow.div { +"Content with footer" }
    }

    override val footer: ((PageSettings, FlowContent) -> Unit)? = { _, flow ->
        flow.footer(classes = "bg-gray-800 text-white py-4 mt-8") {
            div(classes = "text-center") {
                p { +"© 2026 My Site" }
            }
        }
    }
}
```

### External Stylesheets (Font Awesome, Google Fonts)

Font Awesome and Google Fonts are now **opt-in**. They are not included by default.

**Adding Font Awesome:**
```kotlin
resources {
    // Use pre-configured constant with SRI hash
    externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
}
```

**Adding Google Fonts:**
```kotlin
resources {
    // Custom external stylesheet
    externalStylesheet(ExternalStylesheet(
        href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap"
    ))
}
```

**Adding Multiple External Stylesheets:**
```kotlin
resources {
    externalStylesheets(
        ExternalStylesheet.FONT_AWESOME_6_7_2,
        ExternalStylesheet(href = "https://fonts.googleapis.com/css2?family=Inter"),
        ExternalStylesheet(
            href = "https://cdn.example.com/custom.css",
            integrity = "sha384-...",  // Optional SRI hash
            crossorigin = "anonymous"
        )
    )
}
```

---

## Styling with Tailwind CSS

### Tailwind Setup

**1. Install Tailwind Gradle Plugin:**

```kotlin
plugins {
    id("com.hyeons-lab.tailwind") version "0.3.0"
}

tailwind {
    version = "3.4.17"
    configPath = "src/main/resources"
    input = "src/main/resources/tailwind/input.css"
    output = "src/main/resources/css/tailwind.css"
}
```

**2. Create `tailwind/input.css`:**

```css
@import url('https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@300;400;500;600;700&display=swap');

@tailwind base;
@tailwind components;
@tailwind utilities;

/* Custom classes */
.sticky {
    position: sticky;
    top: 0px;
}

/* Responsive navigation menu */
.vertical-menu {
    text-align: end;
    writing-mode: vertical-rl;
    transform: rotate(180deg);
    padding-block: calc(var(--spacing) * 4);
    margin-inline: calc(var(--spacing) * 4);
}

@media (min-width: 48rem) {
    .horizontal-menu {
        transform: rotate(0deg);
        writing-mode: horizontal-tb;
        padding-inline: calc(var(--spacing) * 4);
        margin-inline: calc(var(--spacing) * 4);
    }
}
```

**3. Create `tailwind.config.js`:**

```javascript
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './build/generated_html/**/*.html',
  ],
  theme: {
    extend: {
      colors: {
        'brand-blue': '#3b82f6',
        'brand-dark': '#1e293b',
      },
      fontFamily: {
        'plex-sans': ['IBM Plex Sans'],
        'plex-serif': ['IBM Plex Serif']
      },
    },
  },
  plugins: [
    require('@tailwindcss/typography')
  ],
}
```

### Custom Colors Example

```javascript
// tailwind.config.js
theme: {
  extend: {
    colors: {
      'cream': '#faf6f1',
      'warm-white': '#FEFCF9',
      'sandy-pink': {
        DEFAULT: '#DDB4AA',
        dark: '#C49A90'
      },
      'sage': '#6b8058',
      'soft-teal': '#4a8f88',
    },
  },
}
```

### Build Process

```bash
# Download Tailwind
./gradlew tailwindDownload

# Initialize Tailwind
./gradlew tailwindInit

# Generate site (creates HTML files)
./gradlew run

# Compile Tailwind CSS
./gradlew tailwindCompile
```

Or use a build script (`build.sh`):

```bash
#!/bin/bash
./gradlew tailwindDownload && \
./gradlew tailwindInit && \
./gradlew run && \
./gradlew tailwindCompile
```

---

## Managing Resources

### Resource Configuration

Resources (images, CSS, etc.) are configured using `ResourceConfig` and copied from your classpath to the output directory:

```kotlin
import com.hyeonslab.ssg.core.InputOutputPair
import com.hyeonslab.ssg.core.ResourceConfig
import com.hyeonslab.ssg.core.ExternalStylesheet

val resources = ResourceConfig(
    staticFiles = listOf(
        // Basic resource (copies to outputPath with same name)
        InputOutputPair(
            inputFilename = "images/logo.png",
            outputPath = "build/generated_html"
        ),

        // Resource with custom output name
        InputOutputPair(
            inputFilename = "styles/main.css",
            outputPath = "build/generated_html",
            outputFilename = "css/styles.css"
        ),

        // Tailwind CSS
        InputOutputPair(
            inputFilename = "css/tailwind.css",
            outputPath = "build/generated_html"
        )
    ),
    localStylesheets = listOf("css/tailwind.css", "css/custom.css"),
    externalStylesheets = listOf(ExternalStylesheet.FONT_AWESOME_6_7_2)
)
```

### Directory Structure

Place resources in `src/main/resources/`:

```
src/main/resources/
├── css/
│   └── tailwind.css
├── images/
│   ├── logo.png
│   ├── hero.jpg
│   └── background.webp
└── fonts/
    └── custom-font.woff2
```

These will be copied to your output directory maintaining the structure.

---

## Building and Deploying

### Build Commands

```bash
# Generate HTML files only
./gradlew run

# Full build with Tailwind compilation
./build.sh

# Clean output
rm -rf build/generated_html
```

### Typical Workflow

```kotlin
fun main() {
    val outputPath = "build/generated_html"

    // 1. Clean previous output (optional)
    File(outputPath).deleteRecursively()

    // 2. Configure site
    val site = Site(/* configuration */)

    // 3. Generate HTML files
    site.generateFiles()

    // 4. Copy static resources
    site.copyResources()
}
```

### Deployment

The generated site in `build/generated_html/` is ready for static hosting:

- Upload to AWS S3
- Deploy to Netlify/Vercel
- Serve with Nginx/Apache
- GitHub Pages
- Any static file host

---

## Complete Example

### App.kt

```kotlin
package com.example.mysite

import com.hyeonslab.ssg.core.*
import com.hyeonslab.ssg.page.*
import com.hyeonslab.ssg.utils.Tailwind
import java.io.File

fun main() {
    val outputPath = "build/generated_html"

    val site = Site(
        outputPath = outputPath,
        title = "My Portfolio",
        version = "1.0.0",
        backgroundColor = "bg-gray-50",
        pages = listOf(
            SitePages.Home,
            SitePages.About,
            SitePages.Projects,
            SitePages.Contact
        ),
        navigation = NavMenuSettings(
            backgroundColor = "bg-white/90",
            navSelectedColor = "text-blue-600",
            navDefaultColor = "text-gray-700",
            isSticky = false,
            instagram = "johndoe", // Instagram username without @
            email = "john@example.com",
            logo = Logo(
                imageUrl = "images/logo.png",
                width = 120,
                height = 60
            ),
            blurNavBackground = true
        ),
        resources = ResourceConfig(
            staticFiles = listOf(
                InputOutputPair("images/logo.png", outputPath),
                InputOutputPair("images/hero-bg.jpg", outputPath),
                InputOutputPair("css/tailwind.css", outputPath)
            ),
            localStylesheets = listOf("css/tailwind.css"),
            externalStylesheets = emptyList()
        ),
        integrations = IntegrationConfig(
            googleTagId = null
        ),
        pageSettings = PageSettings(
            h1 = TextConfig(
                textSize = Tailwind.Text.Size.`4xl`.size,
                font = "font-plex-serif",
                textColor = Tailwind.Colors.Text.Neutral.`900`
            ),
            bodyTextColor = Tailwind.Colors.Text.Neutral.`600`
        )
    )

    // Clean and generate
    File(outputPath).deleteRecursively()
    site.generateFiles()
    site.copyResources()

    println("✅ Site generated successfully at: $outputPath")
}
```

### Pages.kt

```kotlin
package com.example.mysite

import com.hyeonslab.ssg.page.Page
import com.hyeonslab.ssg.page.PageSettings
import kotlinx.html.*

sealed interface SitePages : Page {

    data object Home : SitePages {
        override val title = "Home"
        override val outputFilename = "index.html"

        override val content: (PageSettings, FlowContent) -> Unit = { settings, flow ->
            flow.div(classes = "min-h-screen") {
                // Hero section
                div(classes = "relative h-96 flex items-center justify-center") {
                    img(
                        src = "images/hero-bg.jpg",
                        classes = "absolute inset-0 w-full h-full object-cover"
                    )
                    div(classes = "relative z-10 text-center text-white") {
                        h1(classes = "text-5xl font-bold mb-4") {
                            +"Welcome to My Portfolio"
                        }
                        p(classes = "text-xl") {
                            +"Full Stack Developer & Designer"
                        }
                    }
                }

                // Content section
                div(classes = "container mx-auto px-4 py-16") {
                    div(classes = "grid md:grid-cols-2 gap-8") {
                        div {
                            h2(classes = "text-3xl font-bold mb-4") {
                                +"About My Work"
                            }
                            p(classes = settings.bodyTextColor.color) {
                                +"I create beautiful, functional websites and applications."
                            }
                        }
                        div {
                            h2(classes = "text-3xl font-bold mb-4") {
                                +"Technologies"
                            }
                            ul(classes = "list-disc ml-6 ${settings.bodyTextColor.color}") {
                                li { +"Kotlin" }
                                li { +"React" }
                                li { +"Tailwind CSS" }
                            }
                        }
                    }
                }
            }
        }

        // Optional: Footer can be omitted
        override val footer: ((PageSettings, FlowContent) -> Unit)? = { _, flow ->
            flow.footer(classes = "bg-gray-800 text-white py-8 mt-16") {
                div(classes = "container mx-auto px-4 text-center") {
                    p { +"© 2026 Your Name. Built with Hyeons' Lab SSG." }
                }
            }
        }
    }

    data object About : SitePages {
        override val title = "About"
        override val outputFilename = "about.html"

        override val content: (PageSettings, FlowContent) -> Unit = { settings, flow ->
            flow.div(classes = "container mx-auto px-4 py-16") {
                h1(classes = "${settings.h1.textSize} ${settings.h1.font} mb-8") {
                    +"About Me"
                }
                div(classes = settings.bodyTextColor.color) {
                    p(classes = "mb-4") {
                        +"I'm a passionate developer with 10 years of experience..."
                    }
                }
            }
        }

        // Optional: Footer can be omitted
        override val footer: ((PageSettings, FlowContent) -> Unit)? = { _, flow ->
            flow.footer(classes = "bg-gray-800 text-white py-8 mt-16") {
                div(classes = "container mx-auto px-4 text-center") {
                    p { +"© 2026 Your Name" }
                }
            }
        }
    }

    data object Projects : SitePages {
        override val title = "Projects"
        override val outputFilename = "projects.html"

        override val content: (PageSettings, FlowContent) -> Unit = { settings, flow ->
            flow.div(classes = "container mx-auto px-4 py-16") {
                h1(classes = "${settings.h1.textSize} ${settings.h1.font} mb-8") {
                    +"My Projects"
                }
                // Project grid would go here
            }
        }

        // Example: This page skips footer (default is null)
        // override val footer: ((PageSettings, FlowContent) -> Unit)? = null
    }

    data object Contact : SitePages {
        override val title = "Contact"
        override val outputFilename = "contact.html"

        override val content: (PageSettings, FlowContent) -> Unit = { settings, flow ->
            flow.div(classes = "container mx-auto px-4 py-16") {
                h1(classes = "${settings.h1.textSize} ${settings.h1.font} mb-8") {
                    +"Get In Touch"
                }
                p(classes = settings.bodyTextColor.color) {
                    +"Email: john@example.com"
                }
            }
        }

        // Optional: Include footer
        override val footer: ((PageSettings, FlowContent) -> Unit)? = { _, flow ->
            flow.footer(classes = "bg-gray-800 text-white py-8 mt-16") {
                div(classes = "container mx-auto px-4 text-center") {
                    p { +"© 2026 Your Name" }
                }
            }
        }
    }
}
```

---

## Tips & Best Practices

### 1. Use Sealed Interfaces for Pages
Organize all pages in a sealed interface for type safety and better IDE support.

### 2. Reuse Footer Content
Create a common footer function to avoid duplication:

```kotlin
val commonFooter: (PageSettings, FlowContent) -> Unit = { _, flow ->
    flow.footer(classes = "bg-gray-800 text-white py-8") {
        div(classes = "container mx-auto text-center") {
            p { +"© 2026 My Site" }
        }
    }
}
```

### 3. Custom Tailwind Classes
Define custom classes in `input.css` for complex styling that Tailwind doesn't cover.

### 4. Clean Output Before Generating
Always clean the output directory before regenerating to avoid stale files:

```kotlin
File(outputPath).deleteRecursively()
```

### 5. Use Tailwind JIT Mode
Configure Tailwind to use JIT mode for faster builds and smaller CSS files.

### 6. Organize Resources
Keep resources organized in logical directories (images/, css/, fonts/) for easier management.

### 7. Test Locally
Use a local server to test your generated site:

```bash
cd build/generated_html
python3 -m http.server 8000
# Visit http://localhost:8000
```

---

## Troubleshooting

### Resources Not Found
- Ensure resources are in `src/main/resources/`
- Check `inputFilename` paths match actual file locations
- Verify resources are included in the JAR (check `build/resources/`)

### Tailwind Classes Not Applied
- Ensure `content` paths in `tailwind.config.js` are correct
- Run `./gradlew tailwindCompile` after HTML generation
- Check that generated HTML is in the path Tailwind is scanning

### Navigation Not Working
- Verify all pages have unique `outputFilename` values
- Check that `navLinks` includes all pages you want in navigation
- Ensure page links use relative paths (`./page.html` not `/page.html`)

---

## Further Reading

- [kotlinx.html Documentation](https://github.com/Kotlin/kotlinx.html)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html)

---

**Happy site building! 🚀**