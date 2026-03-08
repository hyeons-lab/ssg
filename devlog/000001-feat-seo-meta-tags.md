# feat-seo-meta-tags

**Agent:** Claude (claude-sonnet-4-6) @ ssg feat/seo-meta-tags
**Intent:** Add SEO metadata support to the SSG library — per-page titles, meta descriptions, canonical links, Open Graph tags, sitemap generation, robots.txt, JSON-LD structured data, lang attribute, og:site_name, and logo alt text — to enable proper search engine indexing and social sharing for sites built with the library.

## Progress

- [x] Add `pageTitle`, `metaDescription`, `ogImage` to `Page` interface
- [x] Add `baseUrl`, `defaultOgImage`, `lang` to `Site` and `SiteBuilder`
- [x] Emit SEO meta tags in `Site.generateFiles()`
- [x] Add `html lang` attribute
- [x] Add `og:site_name` meta tag
- [x] Add `Site.generateSitemap()` method
- [x] Add `Site.generateRobotsTxt()` method
- [x] Add `structuredData` property to `Page` for JSON-LD support
- [x] Change nav `h1` → `span` (fix heading hierarchy)
- [x] Add `altText` to `Logo` data class + DSL
- [x] Add tests for all new SEO features
- [x] Fix pre-existing nav margin bug (`leftMargin`/`rightMargin` variables were unused)
- [x] Publish to mavenLocal

## What Changed

### `Page.kt`
- Added `val structuredData: String? get() = null` — raw JSON-LD string emitted in a `<script type="application/ld+json">` tag when non-null

### `Site.kt`
- Added `val lang: String = "en"` — sets `<html lang="...">` attribute on every page
- Added `val ogSiteName: String? = null` — brand name for `og:site_name`; falls back to `title` when null
- Added `og:site_name` meta tag using `ogSiteName ?: title` after `og:type` in the OG block
- Added JSON-LD `<script>` emission in `generateFiles()` when `page.structuredData != null`
- Added `generateRobotsTxt()` — writes `robots.txt` with `User-agent: *`, `Allow: /`, and `Sitemap:` directive; no-ops when `baseUrl` is null

### `SiteDsl.kt`
- Added `var lang: String = "en"` to `SiteBuilder`
- Added `var ogSiteName: String? = null` to `SiteBuilder`
- Pass `lang` and `ogSiteName` through in `build()`

### `NavMenu.kt`
- Changed nav item element from `h1` to `span` — each page link was an `<h1>`, breaking document heading hierarchy
- Added `alt = navMenuSettings.logo.altText` to logo `img`
- Fixed `${Tailwind.Text.Size.sm}` → `${Tailwind.Text.Size.sm.size}` in three places — the enum's `toString()` returns `"sm"` (the object name), not `"text-sm"`; `.size` is the correct accessor
- Reverted the earlier margin fix: nav padding is `px-4 sm:px-8 md:px-${horizontalMargin}` — applying `ms-`/`me-` margin classes to a `w-full` element causes horizontal overflow

### `HtmlExtensions.kt`
- Changed `fun H1.adjustSelected(...)` to `fun CommonAttributeGroupFacade.adjustSelected(...)` to support `span` receiver

### `NavMenuSettings.kt`
- Added `val altText: String = ""` to `Logo` data class (default empty → backward-compatible)

### `NavigationDsl.kt`
- Added `var altText: String = ""` to `LogoBuilder`
- Added `altText` parameter to `NavigationBuilder.logo(String, Int, Int)` convenience function

### `SiteTest.kt`
- Extended `createTestSite` helper with `baseUrl`, `defaultOgImage`, `lang`, `ogSiteName` params
- Added 15 new tests:
  - SEO meta tags: per-page description, per-page title, canonical + OG block, no OG when no baseUrl, og:site_name falls back to title, og:site_name uses explicit `ogSiteName` when set
  - Lang attribute: default `en`, custom lang
  - Structured data: JSON-LD emitted when set, absent when null
  - `generateSitemap()`: produces correct URLs, no-ops without baseUrl
  - `generateRobotsTxt()`: correct content, no-ops without baseUrl
  - Nav heading hierarchy: `<span>` present, no `<h1 class="text-sm">`

### `NavMenuTest.kt`
- Updated horizontal margin tests to assert `px-4` and `md:px-{margin}` (padding) instead of `ms-{margin}` / `me-{margin}` (margin)

## Decisions

2026-02-19T23:47-08:00 All new `Page` properties default to `null` — existing implementations are unaffected without changes; this is a backward-compatible extension of the interface.

2026-02-19T23:47-08:00 `generateSitemap()` silently returns if `baseUrl` is null, so existing sites without a `baseUrl` don't need to call it or handle errors.

2026-02-20T01:12-08:00 OG tags only emitted when `baseUrl` is set — canonical URL and `og:url` require an absolute URL, so OG block is gated on `baseUrl`.

2026-02-21T22:06-08:00 `generateRobotsTxt()` follows the same no-op pattern as `generateSitemap()` — silently skips when `baseUrl` is null.

2026-02-21T22:06-08:00 `structuredData` is a raw JSON string on `Page` rather than a typed schema object — keeps the library dependency-free and lets consumers use any JSON serialization strategy. Consumers are responsible for producing valid JSON-LD.

2026-02-21T22:06-08:00 Nav `h1` → `span`: a page can only have one `<h1>`, and the main content heading should own it. Nav items have no semantic heading role; `<span>` is the right element.

2026-02-21T22:06-08:00 `adjustSelected` receiver widened to `CommonAttributeGroupFacade` rather than creating a duplicate extension — avoids code duplication and works for any HTML element that accepts class attributes.

2026-02-21T22:06-08:00 Fixed the pre-existing nav margin bug in the same PR since fixing it was necessary to make the existing tests pass; the bug was that `leftMargin`/`rightMargin` were computed but never applied.

2026-02-21T22:43-08:00 Reverted the margin fix: applying `ms-`/`me-` to a `w-full` nav causes horizontal overflow on mobile. The correct fix is padding (`px-`), which stays inside the element's box. Tests updated to match.

2026-02-21T22:43-08:00 `ogSiteName` added as a separate nullable property on `Site` and `SiteBuilder` — a site's `title` is often a full page title or tagline; the `og:site_name` should be a stable brand name. Defaults to `title` for backward compatibility.

2026-02-21T22:43-08:00 `Tailwind.Text.Size.sm` is a `data object`; its `toString()` returns the object name `"sm"`, not the Tailwind class `"text-sm"`. All usages in `NavMenu.kt` corrected to `.size`.

2026-03-07T21:46-0800 JSON-LD escaping: `</script>` inside a `<script>` block terminates the tag in HTML parsers regardless of context. Standard mitigation is `<\/` which is valid JSON (forward slash can be escaped) and is invisible to JSON parsers.

2026-03-07T21:46-0800 `baseUrl` trailing slash is validated rather than silently trimmed — a misconfigured URL is a caller error; failing loudly is better than producing subtly wrong output.

2026-03-07T23:28-0800 Removed the standalone `signing { setRequired { ... } }` block from `lib/build.gradle.kts`. The vanniktech plugin's `signAllPublications()` calls `useInMemoryPgpKeys()` with the `ORG_GRADLE_PROJECT_signingInMemoryKey*` properties. A subsequent bare `signing {}` block reconfigures `SigningExtension` without providing key material, resetting the signatory to null. This caused `signMavenPublication FAILED: no configured signatory` in CI despite all secrets being correctly set. Also removed the explicit `signing` plugin from `plugins {}` since vanniktech applies it internally — the redundant declaration contributed to the conflict.

2026-03-07T21:46-0800 `generateSitemap()` and `generateRobotsTxt()` now create the output directory themselves — callers shouldn't have to know the internal prerequisite, and `Files.createDirectories` is idempotent.

## What Changed (continued)

### `gradle/wrapper/gradle-wrapper.properties`
- Updated `distributionUrl` from Gradle 9.0 to 9.3.1

### `gradle/wrapper/gradle-wrapper.jar`
- Updated wrapper JAR to match Gradle 9.3.1

### `Site.kt` (code review fixes)
- Added `lang` BCP-47 validation in `init {}` — regex `[a-zA-Z]{2,8}(-[a-zA-Z0-9]{2,8})*`
- Added `baseUrl` trailing-slash guard in `init {}` — silent double-slash in URLs otherwise
- Added injection validation for `baseUrl` and `defaultOgImage` — mirrors existing `Logo.imageUrl` validation
- Merged two identical `baseUrl?.let { base ->` blocks into one; `path` computed once
- Fixed JSON-LD `unsafe` emission: `json.replace("</", "<\\/")` prevents `</script>` in data from terminating the tag early (XSS / page-breaking)
- Added `Files.createDirectories` at the top of `generateSitemap()` and `generateRobotsTxt()` — both can now be called before `generateFiles()`

### `SiteTest.kt` (code review fixes)
- Removed manual `File(outputPath).mkdirs()` from sitemap and robots tests — now unnecessary
- Added test: per-page `ogImage` overrides `defaultOgImage`
- Added test: `og:description` emitted when `metaDescription` is set

### `NavMenuTest.kt` (code review fixes)
- Renamed "should apply default horizontal margins" → "should apply default horizontal padding"
- Renamed "should apply custom horizontal margins" → "should apply custom horizontal padding"

### `HtmlExtensions.kt` (second review round)
- Fixed KDoc example: `h1 { adjustSelected(...) }` → `span { adjustSelected(...) }`

### `Site.kt` (second review round)
- Updated `generateSitemap()` KDoc example comment to clarify `generateFiles()` is no longer a prerequisite

### `SiteTest.kt` (second review round)
- Added `context("SEO field validation")` with 5 tests covering: invalid `lang` rejected, valid lang values accepted, `baseUrl` trailing slash rejected, `baseUrl` injection chars rejected, `defaultOgImage` injection chars rejected
- Added test: `</script>` in `structuredData` is escaped to `<\/script>` in output

### `.github/workflows/publish.yml`
- Added `workflow_dispatch` workflow with `ref` input (default `"main"`) for publishing to Maven Central
- Debug step uses `[[ ]]` bash conditionals to verify secret presence and format without outputting values
- Added `Validate ref` step that restricts `inputs.ref` to `main`, version tags (`v1.2.3` / `1.2.3`), or commit SHAs — prevents publishing untrusted code with release credentials (code review feedback)
- Broadened `SIGNING_KEY_ID` format check to accept 8, 16, or 40 hex chars — GPG key IDs are also commonly 16-hex long IDs or 40-hex fingerprints (code review feedback)

### `lib/build.gradle.kts` (signing fix)
- Removed standalone `signing { setRequired { ... } }` block — it was reconfiguring the `SigningExtension` after vanniktech's `signAllPublications()` had already called `useInMemoryPgpKeys()`, wiping out the configured signatory
- Removed `signing` from `plugins {}` — vanniktech applies the signing plugin internally when `signAllPublications()` is used; the explicit declaration was redundant and contributed to the conflict

## Commits

6d1bc9e — docs: add devlog and plan for feat/seo-meta-tags
9bb036a — feat(seo): add per-page SEO metadata support to Page and Site
77e5d42 — feat(seo): add lang attribute, og:site_name, robots.txt, and JSON-LD support
2b5de6d — fix(nav): replace h1 with span, add logo alt text, fix margin application
56b9914 — test: add SEO feature tests; update devlog
d6bb93b — fix: use text-sm class, add ogSiteName property, fix nav margin tests
c6fbc7b — docs: update devlog with fix commit details and decisions
a5ab80d — style: apply ktfmt formatting to SEO feature files
7fb1bc6 — chore: update Gradle wrapper to 9.3.1
e7062c6 — fix: address code review issues in SEO feature
cbc9c54 — fix: address second code review round
8946896 — ci: add Maven Central publish workflow
647697c — ci: add signing secret debug step to publish workflow
5195f42 — ci: remove credential exposure from debug step
0f46e36 — fix: remove conflicting signing block that broke Maven Central publish
HEAD — ci: address PR review comments on publish workflow