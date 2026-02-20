# feat-seo-meta-tags

**Agent:** Claude (claude-sonnet-4-6) @ ssg feat/seo-meta-tags
**Intent:** Add SEO metadata support to the SSG library — per-page titles, meta descriptions, canonical links, Open Graph tags, and sitemap generation — to enable proper search engine indexing and social sharing for sites built with the library.

## Progress

- [ ] Add `pageTitle`, `metaDescription`, `ogImage` to `Page` interface
- [ ] Add `baseUrl`, `defaultOgImage` to `Site` and `SiteBuilder`
- [ ] Emit SEO meta tags in `Site.generateFiles()`
- [ ] Add `Site.generateSitemap()` method
- [ ] Publish to mavenLocal

## What Changed

<!-- populated as work progresses -->

## Decisions

2026-02-19T00:00-08:00 All new `Page` properties default to `null` — existing implementations are unaffected without changes; this is a backward-compatible extension of the interface.

2026-02-19T00:00-08:00 `generateSitemap()` silently returns if `baseUrl` is null, so existing sites without a `baseUrl` don't need to call it or handle errors.

2026-02-19T00:00-08:00 OG tags only emitted when `baseUrl` is set — canonical URL and `og:url` require an absolute URL, so OG block is gated on `baseUrl`.

## Commits

<!-- populated as commits are made -->