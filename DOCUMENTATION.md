# Documentation Guide

## KDoc HTML Documentation

HTML documentation has been generated from KDoc comments using Dokka.

### Location

**Main Entry Point:**
```
lib/build/docs/kdoc/index.html
```

**Full Path:**
```
/Users/dberrios/development/ssg/lib/build/docs/kdoc/
```

### Opening the Documentation

**Option 1: Open in Browser**
```bash
open lib/build/docs/kdoc/index.html
```

**Option 2: Navigate in Finder**
```bash
open lib/build/docs/kdoc/
```

**Option 3: Use Python HTTP Server**
```bash
cd lib/build/docs/kdoc
python3 -m http.server 8080
# Then open http://localhost:8080 in your browser
```

### Regenerating Documentation

To regenerate the HTML documentation after making changes:

```bash
./gradlew :lib:dokkaHtml
```

The documentation will be regenerated in the same location:
```
lib/build/docs/kdoc/
```

### Documentation Structure

```
lib/build/docs/kdoc/
├── index.html                    # Main entry point
├── navigation.html               # Navigation sidebar
├── -hyeons' -lab -s-s-g/        # Module documentation
│   ├── com.hyeonslab.ssg.core/           # Core package
│   │   ├── -site/                         # Site class
│   │   ├── -resource-config/              # ResourceConfig class
│   │   ├── -integration-config/           # IntegrationConfig class
│   │   └── dsl/                           # DSL subpackage
│   │       ├── site.html                  # site() function
│   │       ├── -site-builder/             # SiteBuilder class
│   │       ├── -navigation-builder/       # NavigationBuilder class
│   │       ├── -resources-builder/        # ResourcesBuilder class
│   │       └── -integrations-builder/     # IntegrationsBuilder class
│   ├── com.hyeonslab.ssg.page/           # Page package
│   │   ├── -page/                         # Page interface
│   │   ├── -nav-menu-settings/            # NavMenuSettings class
│   │   ├── -logo/                         # Logo class
│   │   └── -page-settings/                # PageSettings class
│   └── com.hyeonslab.ssg.utils/          # Utils package
│       └── -tailwind/                     # Tailwind helpers
├── images/                       # Documentation images
├── scripts/                      # JavaScript for interactive docs
└── styles/                       # CSS styling for docs
```

### Features of Generated Documentation

✅ **Full API Reference**
- All public classes, functions, and properties
- Parameter descriptions
- Return value descriptions
- Thrown exceptions
- Usage examples

✅ **Interactive Navigation**
- Searchable package and class index
- Hierarchical package browser
- Cross-referenced links

✅ **Source Code Links**
- Links to GitHub source (when repository is public)
- Direct navigation from docs to source

✅ **Rich Formatting**
- Syntax-highlighted code examples
- Markdown formatting in descriptions
- Tables, lists, and inline code

### Publishing Documentation

To publish the documentation to GitHub Pages or a web server:

1. **Copy the docs directory:**
   ```bash
   cp -r lib/build/docs/kdoc/ /path/to/web/root/api-docs/
   ```

2. **Or commit to gh-pages branch:**
   ```bash
   # In your repository root
   git checkout gh-pages
   cp -r lib/build/docs/kdoc/* docs/
   git add docs/
   git commit -m "Update API documentation"
   git push origin gh-pages
   ```

3. **Access at:**
   ```
   https://yourusername.github.io/repository/docs/
   ```

### Gradle Tasks

```bash
# Generate HTML documentation
./gradlew :lib:dokkaHtml

# Clean generated documentation
./gradlew clean

# Generate and immediately open
./gradlew :lib:dokkaHtml && open lib/build/docs/kdoc/index.html
```

### Configuration

Dokka is configured in `lib/build.gradle.kts`:

```kotlin
tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("docs/kdoc"))

    dokkaSourceSets {
        configureEach {
            moduleName.set("Hyeons' Lab SSG")
            includes.from("Module.md")

            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(uri("https://github.com/hyeons-lab/ssg/tree/main/lib/src/main/kotlin").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
}
```

### Module Documentation

Package-level documentation is defined in `lib/Module.md` and appears on the documentation homepage.

---

## Other Documentation Files

### User Documentation
- **README.md** - Quick start and overview
- **USAGE_GUIDE.md** - Comprehensive usage guide with examples
- **CHANGELOG.md** - Version history and migration guides

### Development Documentation
- **CONTRIBUTING.md** - Contribution guidelines
- **CRITICAL_ISSUES.md** - Pre-release issue review
- **FIXES_SUMMARY.md** - Summary of fixes applied
- **AGENTS.md** - Development workflow guide
- **CLAUDE.md** - Claude-specific instructions

---

## Quick Reference

| Documentation Type | Location | Command |
|-------------------|----------|---------|
| **API Docs (HTML)** | `lib/build/docs/kdoc/index.html` | `./gradlew :lib:dokkaHtml` |
| **User Guide** | `USAGE_GUIDE.md` | N/A |
| **Quick Start** | `README.md` | N/A |
| **Changelog** | `CHANGELOG.md` | N/A |
| **KDoc (Source)** | `lib/src/main/kotlin/**/*.kt` | In IDE or source files |

---

**Generated:** 2026-02-08
**Library Version:** 0.1.0
**Dokka Version:** 2.1.0
