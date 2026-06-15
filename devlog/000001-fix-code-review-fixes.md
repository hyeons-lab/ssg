# 000001 — fix/code-review-fixes

**Agent:** Claude (claude-opus-4-8) @ ssg branch fix/code-review-fixes

## Intent

Fix the actionable findings from a `/code-review max` pass of the project. The review ran
against local `main` (e5134dd); the canonical `origin/main` (b56f65b) had diverged via PR #3,
which bundled a large SEO feature set plus another nav refactor. Findings were re-validated
against `origin/main` before fixing.

## Decisions

- 2026-06-15T08:48-0700 Branch from `origin/main`, not local `main` — origin is ahead and is the
  PR base; the lib source there differs materially from what was reviewed.
- 2026-06-15T08:48-0700 Skip findings #1 (`fixed-nav-offset`) and #2 (`Tailwind…sm` missing `.size`)
  — both already fixed in `origin/main`. Re-validated the other 13 as still present.
- 2026-06-15T08:48-0700 `horizontalMargin` applies at all breakpoints (user decision) — restores
  the pre-refactor spacing contract that the responsive `md:px-` ramp had dropped below `md`.
- 2026-06-15T08:48-0700 Resolve `version` (dead config) by rendering a non-breaking
  `<meta name="version">` instead of removing it (removal is breaking → v1.0).
- 2026-06-15T08:48-0700 DSL stylesheet opt-out via additive `noLocalStylesheets()` (non-breaking).
- 2026-06-15T08:48-0700 `horizontalMargin` not XSS (kotlinx.html escapes attributes); treated as a
  validation-consistency / class-injection fix with a no-whitespace spacing-token regex.

## What Changed

- 2026-06-15T09:02-0700 `utils/Validation.kt` (new) — shared `validateCssClasses`,
  `validateSpacingUnit`, `validateUrlChars`, `validateRelativePath` with hoisted regexes; removes
  the per-class duplication and per-call recompilation (#13, #14).
- 2026-06-15T09:02-0700 `page/NavMenuSettings.kt` — use shared validators; validate
  `horizontalMargin` as a no-whitespace spacing token (#5); hoist instagram/email regexes (#14);
  route `Logo` URL through shared blocklist (#13); fix `horizontalMargin` KDoc drift.
- 2026-06-15T09:02-0700 `page/NavMenu.kt` — `px-{horizontalMargin}` at all breakpoints (#6); logo
  links to the first page (#8); dropped invalid `z-1` class (#12).
- 2026-06-15T09:02-0700 `core/Site.kt` — shared CSS/URL validators (#13); hoisted lang regex (#14);
  init now rejects duplicate page `outputFilename`s (#3) and traversal-unsafe page filenames (#9);
  renders `<meta name="version">` so `version` is no longer dead config (#15).
- 2026-06-15T09:02-0700 `core/HtmlExtensions.kt` — hoisted `GOOGLE_TAG_REGEX` (#14); fixed stale
  "line 14" safety comment.
- 2026-06-15T09:02-0700 `core/InputOutputPair.kt` — context-classloader resource lookup (#7);
  confirm resource exists before creating directories (#11); temp-file + atomic move so a mid-copy
  failure can't corrupt an existing file (#10); shared path validator (#13).
- 2026-06-15T09:02-0700 `core/dsl/ResourcesDsl.kt` — additive `noLocalStylesheets()` opt-out (#4).
- 2026-06-15T09:02-0700 Tests — updated NavMenu padding assertions (#6); added coverage for
  `horizontalMargin` validation, logo-link, duplicate/traversal page filenames, the version meta,
  and `noLocalStylesheets()`. `./gradlew build` green.

### SEO follow-up review (PR #2 surface)

A second `/code-review` pass over the newly-merged SEO code (OG/JSON-LD/sitemap/robots/lang)
surfaced 12 findings; all fixed in this same branch/PR.

- 2026-06-15T12:54-0700 `utils/Encoding.kt` (new) — `escapeXml` and `encodeUrlPath` helpers.
- 2026-06-15T12:54-0700 `core/Site.kt` — sitemap `<loc>` now URL-encoded + XML-escaped via a shared
  `pageUrlPath()` (also used by canonical/og:url, removing the duplicated path logic); JSON-LD now
  escapes `<`/`>` as `<`/`>` (defeats the `<!--<script>` script-data-escape vector, not
  just `</script>`); blank `pageTitle`/`metaDescription` fall back instead of emitting empty tags;
  per-page `ogImage` validated like `defaultOgImage`; `og:type` taken from new `Page.ogType`;
  `generateSitemap(lastmod = null)` omits `<lastmod>` by default (deterministic, no `now()`);
  `robots.txt` uses standard `Disallow:`; added `ensureOutputDir()` (dedup) and a `generate()`
  convenience that emits pages+sitemap+robots together; fixed stale KDoc.
- 2026-06-15T12:54-0700 `page/Page.kt` — added optional `ogType` (default "website").
- 2026-06-15T12:54-0700 Tests — updated JSON-LD/sitemap/robots assertions to the new behavior;
  added coverage for URL-encoding/XML-escaping, deterministic lastmod, `og:type`, blank-title
  fallback, and `generate()`. `./gradlew build` green (115 test blocks).

## Issues

- Review was performed against stale local `main`; discovered the divergence only when the worktree
  (from `origin/main`) showed different `NavMenuTest` assertions (`px-4`/`md:px-16`). Re-read all
  changed worktree files before editing.

## Research & Discoveries

- kotlinx.html `escapeAppend` escapes `<`,`>`,`&`,`"` in attribute values (verified by decompiling
  the cached jar), so config class strings cannot break out of attributes — the validation is
  defense-in-depth, and the real risk from an unvalidated class field is injecting *extra* utility
  classes (e.g. `"16 hidden"`), not script execution.

## Next Steps

- See plan 000001-01. After green build: commit, push, open PR; offer a follow-up review of the
  new SEO code.

## Commits

- 283df24 — fix: address code-review findings (validation, resource copy, nav layout)
- HEAD — fix(seo): harden SEO output (sitemap escaping/encoding, JSON-LD, og:type, generate())
