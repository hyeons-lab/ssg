# Plan: SEO Meta Tags — SSG Library Changes

## Thinking

The SSG library's `Page` interface currently exposes only `title` (nav label), `outputFilename`, `content`, and `footer`. Nothing in the `head {}` block is customizable per-page — every page gets the same site-level `<title>` and no `<meta description>`, canonical, or OG tags.

The two-part problem:
1. **Library**: needs new nullable properties on `Page` (with defaults so existing code compiles), plus `baseUrl`/`defaultOgImage` on `Site`/`SiteBuilder`, plus emission logic in `generateFiles()`, plus a new `generateSitemap()` method.
2. **Site project**: needs to set those values per-page and call the new method.

Since the `Page` interface properties all default to `null`, no existing implementors break. The `Site` data class gains new optional parameters with defaults, so the `SiteBuilder.build()` call is backward-compatible too.

The canonical URL for `index.html` should be the site root with trailing slash (e.g., `https://example.com/`); all other pages use `/$outputFilename`.

OG tags are only emitted when `baseUrl` is set — they require an absolute URL and are useless without one.

`generateSitemap()` silently no-ops when `baseUrl` is null.

## Plan

1. **`Page.kt`**: add three nullable properties with default `null`:
   - `val pageTitle: String? get() = null`
   - `val metaDescription: String? get() = null`
   - `val ogImage: String? get() = null`

2. **`Site.kt`**: add two new constructor parameters after `integrations`:
   - `val baseUrl: String? = null`
   - `val defaultOgImage: String? = null`
   In `generateFiles()`:
   - Change `title { +this@Site.title }` → `title { +(page.pageTitle ?: this@Site.title) }`
   - After viewport meta: emit `<meta name="description">` when `page.metaDescription != null`
   - After stylesheets: emit `<link rel="canonical">` when `baseUrl != null`
   - After canonical: emit full OG block when `baseUrl != null`
   Add `generateSitemap()` method that writes `sitemap.xml` to `outputPath`.

3. **`SiteDsl.kt`**: add `var baseUrl: String? = null` and `var defaultOgImage: String? = null` to `SiteBuilder`; pass both to `Site(...)` in `build()`.

4. **Publish**: `./gradlew publishToMavenLocal` from the SSG library worktree.