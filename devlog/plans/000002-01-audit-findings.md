# Plan 000002-01: Fix Code Review Audit Findings

## Thinking

A full-codebase code review surfaced multiple issues across correctness, web standards compliance, security, portability, API contracts, and testing:

1. **HTML5 Doctype**: Modern browsers require `<!DOCTYPE html>` at the start of the document to avoid Quirks Mode. In `Site.kt:246`, `appendHTML().html` must be preceded by `appendLine("<!DOCTYPE html>")`.
2. **Nested Page Directory Creation & Atomic Writes**: `Site.kt:354` writes directly with `File.writeText()`. When page filenames contain nested subpaths (e.g. `docs/guide.html`), this crashes with `FileNotFoundException` because intermediate directories are missing. We must ensure parent directories exist via `Files.createDirectories(...)` and stage file writes through sibling temporary files with atomic rename semantics, consistent with `InputOutputPair.kt`.
3. **Path Traversal & Windows Root Path Handling**: `Validation.kt:80` accepts blank paths and fails to reject Windows drive-relative root paths (`/etc/passwd` or `\Windows\System32`) because `Path.isAbsolute` returns false on Windows for paths lacking drive letters. We must check that `path.isNotBlank()`, `normalized.root == null && !normalized.isAbsolute`, and reject drive letters on POSIX. Also, `Site.init` must validate `outputPath`.
4. **Tailwind Play CDN**: `ExternalStylesheet.TAILWIND_CSS_3_4_17` points to `https://cdn.tailwindcss.com/3.4.17` which is a JavaScript bundle, not a stylesheet. Emitting it inside `<link rel="stylesheet">` causes modern browsers to reject it due to strict CSS MIME type checking. We should deprecate `TAILWIND_CSS_3_4_17` and clarify in KDoc.
5. **Serialization Support**: `TextConfig` and `PageSettings` are marked `@Serializable`, but `Tailwind.Colors.Text` and its concrete subtypes (`Neutral.600`, `Neutral.900`, `Custom`) lack `@Serializable`. Serializing settings throws runtime exceptions. We must annotate the `Text` hierarchy with `@Serializable`.
6. **Sticky Navigation**: `NavMenu.kt:67` emits `sticky` without `top-0`. In CSS, `position: sticky` has no effect without a threshold offset. We must emit `sticky top-0`.
7. **Nested Navigation Links**: `NavMenu.kt` hardcodes `./$homeFilename` and `./${page.outputFilename}`. When browsing nested pages (e.g., `docs/guide.html`), relative `./` links resolve to nonexistent subfolder locations (404 errors). We will compute links relative to the active page depth, so root pages use `./` and nested pages use `../` prefixes. We will also URL-encode link filenames.
8. **Input Validation**:
   - `Site.init` must validate `resources.localStylesheets` (relative path) and `resources.externalStylesheets` (URL characters).
   - `Tailwind.Colors.Text.Custom` must validate CSS class safety in its `init` block.
   - `Validation.kt:32` must expand `CSS_CLASS_REGEX` to allow `#`, `!`, `@`, calc expressions, `,`, `()`, `*`, `&`.
9. **ClassLoader Resource Lookup**: In `InputOutputPair.kt:35`, `openClasspathResource` chains Elvis on classloaders instead of stream results. If the context classloader exists but doesn't have the resource, it never falls back. We must query streams sequentially and normalize backslashes.
10. **I/O & XML Escaping Performance**: In `InputOutputPair.kt`, replace `kotlinx.io` double buffering with direct `Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)`. In `Encoding.kt`, optimize `escapeXml` with a fast-path check and single-pass builder.
11. **Accessibility**: Add `aria-label="Instagram"` and `aria-label="Email"` along with `rel="noopener noreferrer"` to social links in `NavMenu.kt`.
12. **Settings and CI Build**:
   - In `settings.gradle.kts`, guard `includeBuild("vendor/gradle-tailwind/plugin")` with directory existence checks so non-recursive clones do not crash.
   - In `.github/workflows/build.yml`, broaden branch patterns to include conventional branch types (`feat/**`, `fix/**`, etc.) and remove unnecessary PR write permission.
13. **Testing**: Add test cases for happy-path resource copying, nested page generation, doctype presence, sticky classes, arbitrary Tailwind values, Windows path traversal rejection, and serialization.

## Plan

1. Update `lib/src/main/kotlin/com/hyeonslab/ssg/utils/Validation.kt`:
   - Expand `CSS_CLASS_REGEX` to allow `#`, `!`, `@`, `,`, `(`, `)`, `*`, `&`.
   - Update `validateRelativePath` to reject blank paths, normalize backslashes, check `normalized.root == null && !normalized.isAbsolute`, and reject drive letters.
   - Update `validateUrlChars` to reject newlines (`\r`, `\n`) and blank strings.
2. Update `lib/src/main/kotlin/com/hyeonslab/ssg/utils/Tailwind.kt`:
   - Annotate `Tailwind.Colors.Text`, `Custom`, `Neutral`, `Neutral.600`, `Neutral.900` with `@Serializable`.
   - Add `init { validateCssClasses(color, "Custom text color") }` to `Custom`.
   - Make `Colors : Tailwind`.
3. Update `lib/src/main/kotlin/com/hyeonslab/ssg/utils/Encoding.kt`:
   - Optimize `escapeXml` with fast-path `value.none { ... }` check and single-pass `buildString`.
4. Update `lib/src/main/kotlin/com/hyeonslab/ssg/core/ExternalStylesheet.kt`:
   - Deprecate `TAILWIND_CSS_3_4_17` with detailed explanation.
5. Update `lib/src/main/kotlin/com/hyeonslab/ssg/core/InputOutputPair.kt`:
   - Sequential classloader stream query in `openClasspathResource` with backslash normalization.
   - Use `File(outputPath, relativeFileName)`.
   - Replace `kotlinx.io` double buffering with direct `Files.copy`.
6. Update `lib/src/main/kotlin/com/hyeonslab/ssg/core/Site.kt`:
   - Validate `outputPath` with `validateRelativePath` in `init`.
   - Validate `resources.localStylesheets` and `resources.externalStylesheets` in `init`.
   - Prepend `<!DOCTYPE html>` in `generateFiles()`.
   - Guard `page.structuredData` with `takeIf { it.isNotBlank() }`.
   - Ensure parent directories exist before writing HTML and XML files; write through temporary file with atomic move fallback.
7. Update `lib/src/main/kotlin/com/hyeonslab/ssg/page/NavMenu.kt` and `HtmlExtensions.kt`:
   - In `NavMenu.kt`, emit `sticky top-0` when `isSticky` is true.
   - Clean class token assembly without duplicate spaces.
   - Compute relative hrefs based on current page depth and URL-encode filenames.
   - Add accessibility `aria-label` and `rel="noopener noreferrer"` to social links.
   - In `HtmlExtensions.kt:adjustSelected`, do not add leading space to class tokens.
8. Update DSL builders (`NavigationDsl.kt` and `SiteDsl.kt`):
   - Provide default colors in `NavigationBuilder` (`"bg-white"`, `"text-blue-600"`, `"text-gray-700"`).
   - Add `logo(logo: Logo)` overload.
   - Add `callsInPlace` contract to `site { ... }`.
   - Allow setting `navigation`, `resources`, `integrations` properties directly on `SiteBuilder`.
9. Update `settings.gradle.kts` and `.github/workflows/build.yml`:
   - Check directory existence before `includeBuild("vendor/gradle-tailwind/plugin")`.
   - Update branch trigger patterns and remove PR write permission in `build.yml`.
10. Update and add tests in `lib/src/test/kotlin/`:
    - Add test resource in `lib/src/test/resources/fixtures/test.txt` and test happy-path resource copying.
    - Test doctype emission in `SiteTest`.
    - Test nested page generation in `SiteTest`.
    - Test sticky navigation classes in `NavMenuTest`.
    - Test arbitrary Tailwind classes in `NavMenuSettingsTest` / `SiteTest`.
    - Test serialization of `TextConfig` and `PageSettings`.
    - Verify all existing tests pass with `./gradlew test`.
11. Update devlog `devlog/000002-fix-audit-findings.md` and commit.
