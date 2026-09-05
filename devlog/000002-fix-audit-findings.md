# 000002: fix/audit-findings

**Agent:** Antigravity (gemini-3.8-flash) @ ssg branch fix/audit-findings

## Intent

Fix all issues surfaced during the full-codebase code review audit:
- Web standards: emit HTML5 `<!DOCTYPE html>` to prevent Quirks Mode.
- Correctness & I/O: create parent directories for nested pages in `Site.generateFiles()`, write files via temporary files with atomic move fallback.
- Security: reject blank and Windows drive-relative root paths in `validateRelativePath`, validate `outputPath` and local/external stylesheets in `Site.init`, validate `Tailwind.Colors.Text.Custom`.
- Accessibility: add `aria-label` and `rel="noopener noreferrer"` to social links in `NavMenu`.
- Multiplatform: resolve classloader streams sequentially with backslash normalization, compute relative navigation links for nested pages.
- Serialization: add `@Serializable` to `Tailwind.Colors.Text` hierarchy.
- Performance: single-pass `escapeXml` and direct NIO `Files.copy`.
- Toolchains: guard composite build in `settings.gradle.kts` and broaden CI branch triggers in `build.yml`.

## Decisions

- 2026-09-05T08:31-07:00 Prepend `<!DOCTYPE html>` before `appendHTML().html` in `Site.generateFiles()`.
- 2026-09-05T08:31-07:00 Use relative link prefixing (`../` count based on page nesting depth) in `NavMenu` so navigation works seamlessly on root domains, subpaths, and offline local file views.
- 2026-09-05T08:31-07:00 Deprecate `ExternalStylesheet.TAILWIND_CSS_3_4_17` because Tailwind Play CDN is a JavaScript runtime script rejected by browsers inside `<link rel="stylesheet">`.
- 2026-09-05T08:31-07:00 Annotate `Tailwind.Colors.Text` sealed interface and its concrete subtypes with `@Serializable` to satisfy the serializable contract of `TextConfig` and `PageSettings`.
- 2026-09-05T08:31-07:00 Expand `CSS_CLASS_REGEX` to permit Tailwind arbitrary hex values (`#`), important modifiers (`!`), container queries (`@`), calc parentheses, commas, and selectors.

## Issues

- 2026-09-05T08:30-07:00 `SiteTest` directory creation failure test relied on absolute path: `should throw error when output directory creation fails` previously used an absolute path (`/invalid/path/that/cannot/be/created`) which failed earlier at construction time due to `outputPath` validation. Resolved by using a conflicting file path collision (`child-dir` under a regular file) so directory creation fails cleanly during `generateFiles()`.

## What Changed

- 2026-09-05T08:31-07:00 `lib/src/main/kotlin/com/hyeonslab/ssg/core/Site.kt`: Prepend `<!DOCTYPE html>`, validate `outputPath` and stylesheets in `init`, normalize page names for duplicate checks, ensure parent directories exist before writing, write files atomically via temp files, and fallback to "summary" card when no image is present.
- 2026-09-05T08:31-07:00 `lib/src/main/kotlin/com/hyeonslab/ssg/page/NavMenu.kt`: Add `top-0` when `isSticky` is enabled, compute relative link hrefs (`../`) for nested pages, and add `aria-label` and `rel="noopener noreferrer"` to social links.
- 2026-09-05T08:31-07:00 `lib/src/main/kotlin/com/hyeonslab/ssg/core/HtmlExtensions.kt`: Remove leading whitespace when prepending color classes in `adjustSelected`.
- 2026-09-05T08:31-07:00 `lib/src/main/kotlin/com/hyeonslab/ssg/core/InputOutputPair.kt`: Sequential ClassLoader querying in `openClasspathResource` with backslash normalization, resolve destination file via `File(outputPath, relativeTarget)`, and use direct `Files.copy`.
- 2026-09-05T08:31-07:00 `lib/src/main/kotlin/com/hyeonslab/ssg/core/ExternalStylesheet.kt`: Deprecate `TAILWIND_CSS_3_4_17`.
- 2026-09-05T08:31-07:00 `lib/src/main/kotlin/com/hyeonslab/ssg/utils/Validation.kt`: Reject Windows drive-relative root paths, drive specifiers, and blank paths in `validateRelativePath`; disallow newlines in `validateUrlChars`; expand `CSS_CLASS_REGEX` for Tailwind arbitrary values and modifiers.
- 2026-09-05T08:31-07:00 `lib/src/main/kotlin/com/hyeonslab/ssg/utils/Tailwind.kt`: Annotate `Text` hierarchy with `@Serializable` and validate `Custom` color strings.
- 2026-09-05T08:31-07:00 `lib/src/main/kotlin/com/hyeonslab/ssg/utils/Encoding.kt`: Fast-path and single-pass XML escaping.
- 2026-09-05T08:31-07:00 `lib/src/main/kotlin/com/hyeonslab/ssg/core/dsl/NavigationDsl.kt`: Add default colors and `logo(Logo)` overload in `NavigationBuilder`.
- 2026-09-05T08:31-07:00 `lib/src/main/kotlin/com/hyeonslab/ssg/core/dsl/SiteDsl.kt`: Add `callsInPlace` contract on `site()` and expose setters for `navigation`, `resources`, `integrations`.
- 2026-09-05T08:31-07:00 `settings.gradle.kts`: Guard submodule composite build against missing directories.
- 2026-09-05T08:31-07:00 `.github/workflows/build.yml`: Broaden branch triggers and drop PR write permissions.
- 2026-09-05T08:31-07:00 `CHANGELOG.md`: Document unreleased features and fixes.
- 2026-09-05T08:31-07:00 `lib/src/test/kotlin/`: Add test coverage for resource copying, doctype emission, nested pages, sticky navigation, arbitrary Tailwind classes, and serialization.

## Commits

- HEAD: fix: resolve full-codebase audit findings
