# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-02-08

### Added
- **NEW:** Kotlin DSL for Site configuration (`site { }` function with nested builders)
- **NEW:** Navigation DSL with `navigation { }` block and `logo()` function
- **NEW:** Resources DSL with `resources { }` block and convenience functions
- **NEW:** Integrations DSL with `integrations { }` block
- **NEW:** Layout configuration parameters: `htmlClasses`, `bodyClasses`, `contentClasses`
- **NEW:** Optional navigation menu (can be `null` for sites without navigation)
- **NEW:** Optional page footers (pages can omit footer if not needed)
- **NEW:** Comprehensive KDoc comments on all public APIs with usage examples
- **BREAKING:** Site `version` parameter for tracking site version (defaults to "1.0.0")
- ResourceConfig configuration class to group resource-related parameters
- IntegrationConfig configuration class to group third-party integration parameters
- ExternalStylesheet configuration class for CDN stylesheets with SRI support
- Configurable local stylesheets via `localStylesheets` parameter (supports multiple CSS files)
- Configurable external stylesheets via `externalStylesheets` parameter
- Configurable font families in NavMenuSettings (`fontFamily` parameter)
- Comprehensive error handling in `generateFiles()` with detailed error reporting
- Comprehensive error handling in `copyResources()` with detailed error reporting
- FONT_AWESOME_6_7_2 constant for easy Font Awesome inclusion (opt-in)
- TAILWIND_CSS_3_4_17 constant for Tailwind Play CDN (development only)
- CSS class validation for NavMenuSettings fields (backgroundColor, navSelectedColor, navDefaultColor, fontFamily)
- CSS class validation for layout configuration (htmlClasses, bodyClasses, contentClasses)
- Input validation for Instagram usernames (prevents XSS)
- Input validation for email addresses (prevents XSS)
- Input validation for logo image URLs (prevents XSS)

### Changed
- **BREAKING:** Site constructor reduced from 17 parameters to 9 using configuration objects
- **BREAKING:** `navLinks` parameter renamed to `pages`
- **BREAKING:** Navigation configuration (colors, logo, social links) moved to `navigation: NavMenuSettings?` parameter (now nullable)
- **BREAKING:** Resource configuration moved to `resources: ResourceConfig` parameter
- **BREAKING:** Integration configuration moved to `integrations: IntegrationConfig` parameter
- **BREAKING:** Font Awesome CDN is now **opt-in** instead of default (breaking change for sites using FA icons)
- **BREAKING:** Google Fonts removed from default (users must add via `externalStylesheets` if needed)
- **BREAKING:** Default site `version` changed from "0.1.0" to "1.0.0" (better default for user sites)
- **BREAKING:** Page `footer` is now optional (`((PageSettings, FlowContent) -> Unit)?` with default null)
- CSS path is now configurable instead of hard-coded to `css/tailwind.css`
- Font families are now configurable (defaults to `font-plex-sans` and `font-plex-serif`)
- Layout structure is now customizable via `htmlClasses`, `bodyClasses`, `contentClasses`
- Directory creation now uses thread-safe `Files.createDirectories()` instead of `mkdir()`
- README and USAGE_GUIDE comprehensively updated with new API, examples, and features
- All public APIs now have comprehensive KDoc documentation with examples

### Fixed
- **Security:** XSS vulnerability in Google Tag injection (now validates tag format)
- **Security:** Path traversal protection strengthened with proper path normalization
- Race condition in directory creation (TOCTOU bug)
- Invalid Tailwind class `flex-column` changed to `flex-col`
- Inline `z-index` style replaced with Tailwind `z-[255]` class for better customization

### Removed
- **BREAKING:** `articleClasses` parameter (assumed @tailwindcss/typography plugin)
- **BREAKING:** Hard-coded Font Awesome CDN link (now opt-in via `externalStylesheets`)
- **BREAKING:** Hard-coded Google Fonts links (now opt-in via `externalStylesheets`)
- Unused `posts: List<PostItem>` parameter from Site constructor
- PageStyleDefaults.kt file (provided no value)

## [0.0.7-SNAPSHOT] - 2026-02-08

### Initial Release

This is the first versioned release with a clean git history and comprehensive improvements.

---

## Migration Guide for Unreleased Changes

### Breaking Changes

#### 1. Site Constructor Refactoring (v0.1.0)

**Impact:** All Site constructor calls must be updated to use the new grouped configuration API.

**Before (17 parameters):**
```kotlin
val site = Site(
    outputPath = "build/generated_html",
    title = "My Site",
    backgroundColor = "bg-white",
    navBackgroundColor = "bg-gray-100",
    navSelectedColor = "text-blue-600",
    navColor = "text-gray-700",
    articleClasses = "prose",
    navLinks = listOf(HomePage, AboutPage),
    googleTagId = "G-XXXXXXXXXX",
    resources = listOf(
        InputOutputPair("css/tailwind.css", "build/generated_html")
    ),
    pageSettings = PageSettings(),
    instagram = "handle",
    email = "contact@example.com",
    logo = Logo("logo.png", 100, 50),
    blurNavBackground = false,
    localStylesheets = listOf("css/tailwind.css"),
    externalStylesheets = emptyList()
)
```

**After (9 parameters with configuration objects):**
```kotlin
val site = Site(
    // Core configuration
    outputPath = "build/generated_html",
    title = "My Site",
    version = "1.0.0",  // NEW - site version tracking

    // Page styling
    backgroundColor = "bg-white",
    articleClasses = "prose",

    // Content
    pages = listOf(HomePage, AboutPage),  // Renamed from navLinks

    // Navigation configuration (grouped)
    navigation = NavMenuSettings(
        backgroundColor = "bg-gray-100",
        navSelectedColor = "text-blue-600",
        navDefaultColor = "text-gray-700",  // Renamed from navColor
        isSticky = false,
        instagram = "handle",
        email = "contact@example.com",
        logo = Logo(imageUrl = "logo.png", width = 100, height = 50),
        blurNavBackground = false
    ),

    // Resources and stylesheets (grouped)
    resources = ResourceConfig(
        staticFiles = listOf(
            InputOutputPair("css/tailwind.css", "build/generated_html")
        ),
        localStylesheets = listOf("css/tailwind.css"),
        externalStylesheets = emptyList()
    ),

    // Third-party integrations (grouped)
    integrations = IntegrationConfig(
        googleTagId = "G-XXXXXXXXXX"
    ),

    // Page-specific settings
    pageSettings = PageSettings()
)
```

**Parameter Mapping:**
- `navLinks` → `pages`
- `navColor` → `navigation.navDefaultColor`
- `navBackgroundColor` → `navigation.backgroundColor`
- `navSelectedColor` → `navigation.navSelectedColor`
- `instagram` → `navigation.instagram`
- `email` → `navigation.email`
- `logo` → `navigation.logo`
- `blurNavBackground` → `navigation.blurNavBackground`
- `resources` → `resources.staticFiles`
- `localStylesheets` → `resources.localStylesheets`
- `externalStylesheets` → `resources.externalStylesheets`
- `googleTagId` → `integrations.googleTagId`

#### 2. Font Awesome and Google Fonts No Longer Included by Default
**Impact:** Sites using Font Awesome icons or Google Fonts will break unless explicitly included.

**Before (automatic):**
```kotlin
val site = Site(
    // Font Awesome and Google Fonts automatically included
)
```

**After (opt-in):**
```kotlin
val site = Site(
    // ...
    resources = ResourceConfig(
        externalStylesheets = listOf(
            ExternalStylesheet.FONT_AWESOME_6_7_2,  // Explicitly include Font Awesome
            ExternalStylesheet(  // Explicitly include Google Fonts
                href = "https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:wght@400;700&display=swap"
            )
        )
    )
)
```

#### 3. Navigation Now Optional
**Impact:** Navigation can be omitted for sites that don't need a navigation menu.

**Before (required):**
```kotlin
val site = Site(
    // ...
    navigation = NavMenuSettings(/* required */)
)
```

**After (optional):**
```kotlin
val site = Site(
    // ...
    navigation = null  // Optional: omit navigation
)

// Or using DSL (simply don't include navigation block)
site {
    // ...
    // No navigation { } block = no navigation menu
}
```

#### 4. Footer Now Optional on Pages
**Impact:** Pages can omit footers if not needed.

**Before (required):**
```kotlin
override val footer: (PageSettings, FlowContent) -> Unit = { _, flow ->
    // Required implementation
}
```

**After (optional):**
```kotlin
// Option 1: Omit footer entirely (default is null)
// No override needed

// Option 2: Explicitly set to null
override val footer: ((PageSettings, FlowContent) -> Unit)? = null

// Option 3: Provide footer implementation
override val footer: ((PageSettings, FlowContent) -> Unit)? = { _, flow ->
    // Optional implementation
}
```

#### 5. articleClasses Removed
**Impact:** Sites using `articleClasses` must move styling to page content.

**Before:**
```kotlin
val site = Site(
    articleClasses = "prose max-w-none"
)
```

**After:**
```kotlin
// Apply styling directly in page content
override val content: (PageSettings, FlowContent) -> Unit = { settings, flow ->
    flow.article(classes = "prose max-w-none") {
        // Page content
    }
}
```

#### 6. New Layout Configuration Parameters
**Impact:** Non-breaking, but allows customization of HTML/body/content structure.

**New parameters:**
```kotlin
val site = Site(
    htmlClasses = "",  // Classes for <html> element
    bodyClasses = "flex flex-col min-h-screen",  // Classes for <body>
    contentClasses = "flex-1 flex flex-col"  // Classes for content wrapper
)
```

These have backward-compatible defaults that maintain the existing flexbox layout.

### New Feature: Kotlin DSL

The library now supports a Kotlin DSL for more readable configuration:

**Using DSL (Recommended):**
```kotlin
import com.hyeonslab.ssg.core.dsl.site

val site = site {
    outputPath = "build/generated_html"
    title = "My Site"
    version = "1.0.0"
    pages = listOf(HomePage, AboutPage)

    navigation {
        backgroundColor = "bg-gray-100"
        navSelectedColor = "text-blue-600"
        navDefaultColor = "text-gray-700"
        logo("logo.png", width = 100, height = 50)
    }

    resources {
        staticFile("css/tailwind.css", "build/generated_html")
        localStylesheet("css/tailwind.css")
        externalStylesheet(ExternalStylesheet.FONT_AWESOME_6_7_2)
    }

    integrations {
        googleTag = "G-XXXXXXXXXX"
    }
}
```

**Constructor API (Still Fully Supported):**
```kotlin
val site = Site(
    outputPath = "build/generated_html",
    title = "My Site",
    // ... all parameters as shown in migration guide above
)
```

Both APIs are equivalent and fully supported. Choose based on your preference.

### Non-Breaking Changes (Backward Compatible)

All other changes are backward compatible with default values that match previous behavior:

- **CSS Path:** Defaults to `listOf("css/tailwind.css")` (same as before)
- **Fonts:** Default to `font-plex-sans` and `font-plex-serif` (same as before)
- **Error Handling:** Now catches and reports errors instead of crashing

### Recommended Updates

While not required, consider updating to use the new configuration options:

```kotlin
Site(
    // ... existing parameters ...

    // NEW: Customize layout structure
    htmlClasses = "",
    bodyClasses = "flex flex-col min-h-screen",
    contentClasses = "flex-1 flex flex-col",

    // NEW: Navigation is now optional (can be null)
    navigation = NavMenuSettings(/* ... */) // or null for no navigation

    // NEW: Resources with external stylesheets
    resources = ResourceConfig(
        staticFiles = listOf(/* ... */),
        localStylesheets = listOf("css/tailwind.css", "css/custom.css"),
        externalStylesheets = listOf(
            ExternalStylesheet.FONT_AWESOME_6_7_2,
            ExternalStylesheet(
                href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap"
            )
        )
    )
)

// NEW: Customize navigation fonts
NavMenuSettings(
    // ... existing parameters ...
    fontFamily = "font-inter"  // Use custom font
)

// NEW: Pages can have optional footers
data object MyPage : Page {
    override val title = "My Page"
    override val outputFilename = "page.html"
    override val content: (PageSettings, FlowContent) -> Unit = { _, flow -> /* ... */ }

    // Optional: Omit footer by leaving default (null)
    // Or explicitly provide:
    override val footer: ((PageSettings, FlowContent) -> Unit)? = { _, flow ->
        flow.footer { /* footer content */ }
    }
}
```

---

## Version History

- **0.1.0** (2026-02-08) - Initial public release with Kotlin DSL, comprehensive documentation, and Maven Central publishing

---

[Unreleased]: https://github.com/hyeons-lab/ssg/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/hyeons-lab/ssg/releases/tag/v0.1.0