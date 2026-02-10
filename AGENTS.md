# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Overview

This is **Hyeons' Lab Static Site Generator** - a type-safe Kotlin library for generating static HTML websites using kotlinx.html DSL and Tailwind CSS. Published to Maven Central as `com.hyeons-lab:ssg:0.1.0`.

---

## Build System

**Technology:**
- Gradle 9.0+ (required for Kotlin 2.3.10)
- Kotlin 2.3.10
- JVM Toolchain 21
- Uses buildSrc convention plugins

**Key commands:**
```bash
# Build the library
./gradlew build

# Publish to Maven Local (for use in other projects)
./gradlew publishToMavenLocal

# Clean build
./gradlew clean build

# Build JAR only
./gradlew jar
# Output: lib/build/libs/ssg-0.1.0.jar

# Run tests
./gradlew test

# Run tests with full output
./gradlew test --rerun-tasks
```

---

## Architecture

### Core Concept

The library generates static HTML by:
1. User defines `Page` implementations (content + footer lambdas)
2. User creates a `Site` instance with configuration
3. Calling `site.generateFiles()` renders HTML using kotlinx.html
4. Calling `site.copyResources()` copies static assets from classpath

### Package Structure

```
com.hyeonslab.ssg/
├── core/              # Core site generation logic
│   ├── Site.kt        # Main orchestrator - generates HTML files
│   ├── InputOutputPair.kt    # Resource copying with path validation
│   ├── ExternalStylesheet.kt # CDN stylesheet configuration
│   └── HtmlExtensions.kt     # HEAD helpers (Google Tag, etc.)
├── page/              # Page abstraction and navigation
│   ├── Page.kt        # Interface: title, filename, content, footer
│   ├── NavMenu.kt     # Navigation bar component
│   └── *Settings.kt   # Configuration data classes
└── utils/
    └── Tailwind.kt    # Type-safe Tailwind utilities (incomplete)
```

### Key Design Patterns

**1. Site is a God Object (Known Issue)**
- `Site` data class has 18 constructor parameters
- Mixes concerns: theming, navigation, content, analytics, resources
- Deferred refactoring to v1.0 (would be breaking change)
- Users must provide all parameters when constructing

**2. Page as Sealed Interface**
- Recommended pattern: `sealed interface MyPages : Page`
- Each page is a `data object` implementing `Page`
- Content and footer are lambdas: `(PageSettings, FlowContent) -> Unit`

**3. Resource Management**
- Resources loaded from classpath via `ClassLoader.getSystemResourceAsStream()`
- Must be in `src/main/resources/` of consuming project
- Path traversal protection uses `Paths.normalize()` and validation

**4. Serialization**
- Many classes marked `@Serializable` for future use (not currently used)
- Do NOT remove serialization - it's intentional for future features

---

## Commit Guidelines

### Commit Message Format
Follow [Conventional Commits](https://www.conventionalcommits.org/):
```
<type>: <description>

[optional body explaining the change and why]

[optional breaking changes section]
```

**Types:**
- `feat:` - New feature
- `fix:` - Bug fix
- `refactor:` - Code restructuring without behavior change
- `test:` - Adding or updating tests
- `docs:` - Documentation changes
- `perf:` - Performance improvements
- `chore:` - Maintenance tasks

**Examples:**
```
feat: add graceful streaming completion for audio playback

fix: resolve race condition in audio streaming initialization

refactor: extract string provider interface for testability
```

### Commit Organization
- Create **logical commits** that group related changes
- Each commit should represent a single cohesive change
- Avoid mixing unrelated changes (e.g., bug fixes + features)
- Write detailed commit messages explaining **why**, not just **what**

---

## Security Considerations

**Critical protections in place:**

1. **Google Tag ID Validation** (`HtmlExtensions.kt`)
   - Validates format: `^(G|GT)-[A-Z0-9]{7,12}$`
   - Prevents XSS via JavaScript injection

2. **Path Traversal Protection** (`InputOutputPair.kt`)
   - Uses `Paths.normalize()` to resolve paths
   - Rejects absolute paths
   - Rejects paths starting with `..` after normalization

3. **Instagram Username Validation** (`NavMenuSettings.kt`)
   - Validates format: `^[a-zA-Z0-9._]{1,30}$`
   - Prevents XSS via href attribute injection

4. **Email Address Validation** (`NavMenuSettings.kt`)
   - Validates format: `^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$`
   - Prevents XSS via mailto: link injection

5. **Logo URL Validation** (`NavMenuSettings.kt` - Logo class)
   - Rejects URLs containing: `"`, `'`, `<`, `>`
   - Validates dimensions: 1-2000 pixels
   - Prevents XSS via src attribute injection

**When modifying security code:**
- Never weaken validation (e.g., removing regex checks)
- Always test with malicious inputs: `user" onclick="alert(1)`, `<script>`, `../../../etc/passwd`

---

## Configuration Philosophy

**Recent changes (see CHANGELOG.md):**
- Font Awesome is now **opt-in** via `externalStylesheets` (breaking change)
- CSS paths configurable via `localStylesheets` (default: `["css/tailwind.css"]`)
- Fonts configurable but default to `font-plex-sans` / `font-plex-serif` (backward compatible)

**Default fonts require Tailwind configuration:**
```javascript
// User's tailwind.config.js must include:
fontFamily: {
  'plex-sans': ['IBM Plex Sans'],
  'plex-serif': ['IBM Plex Serif']
}
```


---

## Error Handling

Both `generateFiles()` and `copyResources()` now use `runCatching`:
- Collects all failures before throwing
- Reports detailed error messages showing which files/resources failed
- Continues processing remaining items if one fails

**Pattern:**
```kotlin
val results = items.map { item ->
    item to runCatching { /* operation */ }
}
val failures = results.filter { it.second.isFailure }
if (failures.isNotEmpty()) {
    error("Failed to process ${failures.size} item(s):\n$errorMessage")
}
```

---

## Testing

**Test Framework:**
- **Kotest 5.9.1** - Kotlin-native testing framework
- **kotlin.test** - Standard Kotlin test utilities
- **JUnit 5** - Test runner (via Kotest)

**Test Coverage: ~80%** (76 tests, all passing)

**What's tested:**
- ✅ Google Tag ID validation (XSS protection) - 6 tests
- ✅ Path traversal protection - 7 tests
- ✅ External stylesheet configuration - 4 tests
- ✅ Site.generateFiles() HTML generation and error handling - 17 tests
- ✅ Site.copyResources() error handling - 3 tests
- ✅ NavMenu rendering and configuration - 17 tests
- ✅ NavMenuSettings input validation (Instagram, Email) - 9 tests
- ✅ Logo validation (URL safety, dimensions) - 8 tests
- ✅ CSS class string validation (injection prevention) - 5 tests

**What's NOT tested:**
- ❌ Page content generation edge cases
- ❌ Complete end-to-end integration tests

**Test locations:**
- `lib/src/test/kotlin/com/hyeonslab/ssg/core/HtmlExtensionsTest.kt` (6 tests)
- `lib/src/test/kotlin/com/hyeonslab/ssg/core/InputOutputPairTest.kt` (7 tests)
- `lib/src/test/kotlin/com/hyeonslab/ssg/core/ExternalStylesheetTest.kt` (4 tests)
- `lib/src/test/kotlin/com/hyeonslab/ssg/core/SiteTest.kt` (25 tests)
- `lib/src/test/kotlin/com/hyeonslab/ssg/page/NavMenuTest.kt` (17 tests)
- `lib/src/test/kotlin/com/hyeonslab/ssg/page/NavMenuSettingsTest.kt` (17 tests)

**Running tests:**
```bash
./gradlew test                    # Run all tests
./gradlew test --rerun-tasks      # Force re-run
./gradlew test --tests "*.HtmlExtensionsTest"  # Run specific test class
```

**Test styles used:**
- `FunSpec` - Nested tests with `context` and `test`
- `StringSpec` - Flat test list with string names
- `DescribeSpec` - BDD-style `describe`/`it` blocks

**Writing new tests:**
```kotlin
class MyTest : FunSpec({
    context("feature name") {
        test("should do something") {
            // Arrange
            val input = "test"

            // Act
            val result = myFunction(input)

            // Assert
            result shouldBe "expected"
        }
    }
})
```

**Kotest assertions:**
- `shouldBe`, `shouldNotBe` - Equality
- `shouldContain` - String/collection contains
- `shouldThrow<Exception>` - Exception testing
- Many more available

---

## Common Gotchas

### 1. Directory Creation
- Uses `Files.createDirectories()` (thread-safe, atomic)
- Do NOT use `File.mkdir()` or `File.mkdirs()` (race conditions)

### 2. Tailwind Class Names
- `flex-col` not `flex-column`
- Use arbitrary values for custom properties: `z-[255]` not inline styles

### 3. Font Configuration
- Navigation font: `NavMenuSettings.fontFamily` (default: `font-plex-sans`)
- Heading font: `PageSettings.h1.font` (default: `font-plex-serif`)
- Users can override to Tailwind defaults: `font-sans`, `font-serif`

### 4. kotlinx.html Script Content
- Use `unsafe { +content }` for script/style tags
- NOT bare `+content` (deprecated and causes warnings)

---

## Gradle Configuration

**buildSrc Convention Plugin:**
- Located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`
- Applies Kotlin JVM plugin with JVM toolchain 21
- Used by `lib` module via `id("buildsrc.convention.kotlin-jvm")`

**Version Catalog:**
- `gradle/libs.versions.toml` defines all dependencies
- Access via `libs.kotlinxSerialization`, etc.

**Publishing:**
- Maven Local only (no public repository)
- Artifact: `ssg-0.1.0.jar`
- Users add `mavenLocal()` to their repositories

---

## Development Workflow

**Making changes:**
1. Modify source in `lib/src/main/kotlin/`
2. Run `./gradlew build` to verify
3. Update CHANGELOG.md under `[Unreleased]` section
4. Commit with descriptive message

**Before committing:**
- Ensure build succeeds
- Update documentation if API changed
- Add CHANGELOG entry for user-facing changes
- **Do NOT** include CODE_REVIEW.md or FIXES_APPLIED.md in commits (local only)

**Git workflow:**
- Main branch: `main`
- Push requires `git push --set-upstream origin main` (if tracking not set)

### Commit Messages

Follow conventional commits format:
```
<type>: <description>

[optional body with bullet points]
```

Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`

Example:
```
feat: add support for LFM v3 export

- Add export config for LFM v3
- Update run_export.py to handle new model architecture
- Add tests for v3 model verification
```

### Pull Request Descriptions

**IMPORTANT:** When creating or updating PRs, the PR description MUST match the commit message body exactly. This ensures consistency when squashing commits.

Format:
```
<Summary paragraph describing what was changed>

- Bullet point detailing specific change
- Another bullet point for another change
- Additional changes as needed
```

Example:
```
Add AGENTS.md with comprehensive instructions for AI coding agents working with the Inference Engine codebase.

- Add project overview and tech stack description
- Add development environment setup instructions
- Add build commands for native, Android, and Rust-only builds
- Add code style guidelines for Python, Rust, and C++
- Add testing and contribution guidelines
```

---

## Known Technical Debt

From recent code review (see CODE_REVIEW.md - local file only):

1. **Test coverage excellent** - ~70% coverage (64 tests), core features fully tested including security validations
2. **Site constructor bloat** - 18 parameters, needs refactoring in v1.0
3. **Incomplete Tailwind wrapper** - Only ~4 classes defined, most users use raw strings
4. **Hard-coded fonts** - Requires user Tailwind config, not documented clearly

**Deferred to v1.0:**
- Site constructor refactoring (breaking change)
- Complete Tailwind wrapper or remove it
- Comprehensive integration testing

---

## Documentation

- **README.md** - User-facing getting started guide
- **USAGE_GUIDE.md** - Comprehensive usage examples and patterns
- **CHANGELOG.md** - Version history and migration guides (Keep a Changelog format)
- **CODE_REVIEW.md** - Internal code review (not in git, local only)
- **FIXES_APPLIED.md** - Fix documentation (not in git, local only)

---

## Dependencies

**Core:**
- `kotlinx.html:0.12.0` - Type-safe HTML generation
- `kotlinx.io:0.8.2` - Resource copying
- `kotlinx.serialization:1.10.0` - Future feature (not currently used)

**Gradle Plugins:**
- `kotlin-gradle-plugin:2.3.10`
- `kotlin-serialization:2.3.10`

**Consumer projects typically need:**
- `com.hyeons-lab.tailwind` plugin for Tailwind CSS compilation
- IBM Plex Sans/Serif fonts configured in Tailwind

---

## Integration Example

```kotlin
// Consumer's build.gradle.kts
dependencies {
    implementation("com.hyeons-lab:ssg:0.1.0")
}

// Consumer's code
val site = Site(
    outputPath = "build/generated_html",
    title = "My Site",
    // ... 15 more parameters ...
    externalStylesheets = listOf(
        ExternalStylesheet.FONT_AWESOME_6_7_2  // Opt-in to Font Awesome
    )
)
site.generateFiles()
site.copyResources()
```

Typical workflow:
1. `./gradlew run` - Generate HTML (runs user's main())
2. `./gradlew tailwindCompile` - Generate CSS from HTML classes
3. Output in `build/generated_html/` ready for static hosting

---

## Version Strategy

- **Current:** 0.1.0 (stable release)
- **Published to:** Maven Central
- **Versioning:** Follows Semantic Versioning (semver.org)
- **Next release:** 0.1.x for bug fixes, 0.2.0 for new features, 1.0.0 for breaking changes

Version is in `gradle/libs.versions.toml` under `artifactVersion`.
