# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Kotlin-based static site generator (SSG) library that produces HTML + CSS websites using Tailwind CSS. It is published as a JAR to Maven Local and consumed by downstream site projects (e.g., jacquelineberrios.com).

**Coordinates**: `com.hyeonslab:hyeons-lab-ssg:0.0.6`

## Build Commands

```bash
./gradlew build              # Full build (compile + test)
./gradlew jar                # Build JAR only → lib/build/libs/hyeons-lab-ssg-*.jar
./gradlew test               # Run tests (JUnit Platform)
./gradlew publish            # Publish to Maven Local (~/.m2)
./gradlew clean build        # Clean rebuild
```

Gradle 8.14 with configuration cache and build cache enabled. Java 17 toolchain (auto-downloaded via Foojay resolver).

## Architecture

### Module Layout

Single library module at `:lib` (source: `lib/src/main/kotlin/com/hyeonslab/ssg/`).

Convention plugin in `buildSrc/` (`kotlin-jvm.gradle.kts`) provides shared Kotlin JVM config, Java 17 toolchain, and JUnit Platform setup.

### Core Components

- **`Site`** (`core/Site.kt`) — Main orchestrator. Configures site-wide settings (colors, nav, metadata, Google Tag Manager) and drives generation via `generateFiles()` (renders pages to HTML) and `copyResources()` (copies static assets). Builds full HTML documents using kotlinx-html DSL: head with stylesheets/fonts/meta, sticky nav, page content, footer.

- **`Page`** (`page/Page.kt`) — Interface representing a single page. Has `title`, `outputFilename`, `content` lambda, and `footer` lambda. Content and footer lambdas receive `PageSettings` and a kotlinx-html `FlowContent` receiver for type-safe HTML building.

- **`NavMenu`** (`page/NavMenu.kt`) — Generates the sticky navigation bar with logo, page links (with selected highlighting), and optional Instagram link. Configured via `NavMenuSettings`.

- **`PostItem`** (`core/PostItem.kt`) — Serializable data class for blog post metadata (title, path).

- **`InputOutputPair`** (`core/InputOutputPair.kt`) — Maps classpath resources to output files, handles file copying.

### Styling System

- **`Tailwind`** (`utils/Tailwind.kt`) — Sealed interface hierarchy providing compile-time-safe Tailwind CSS class names (text sizes, colors).
- **`TextConfig`** / **`PageSettings`** / **`PageStyleDefaults`** — Configuration objects for per-page text styling (size, font, color).

### Key Dependencies

- **kotlinx-html** (0.12.0) — Type-safe HTML DSL (exposed as `api` dependency)
- **kotlinx-serialization-json** (1.7.3) — JSON serialization for config
- **kotlinx-io-core** (0.7.0) — File I/O operations

Version catalog: `gradle/libs.versions.toml`

### Design Patterns

Pages are defined as lambdas (`(PageSettings, FlowContent) -> Unit`), not subclasses. The `Site` class composes pages into full HTML documents using the kotlinx-html builder DSL. Tailwind classes use sealed interface hierarchies for type safety rather than raw strings.

## Code Style

All files must end with a newline.