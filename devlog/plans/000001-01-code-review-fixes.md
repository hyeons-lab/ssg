# Plan 000001-01 — Fix code-review findings

## Thinking

A `/code-review max` pass was run against **local `main` (e5134dd)** and produced 15 findings.
While setting up the worktree it turned out **`origin/main` (b56f65b) has diverged**: PR #3
("publish signing") also bundled a large SEO feature set (baseUrl, Open Graph, JSON-LD,
sitemap, robots.txt, `<html lang>`, new optional `Page` fields) **and another nav refactor**.

Re-validating the 15 findings against the *actual* `origin/main` code:

- **#1 `fixed-nav-offset` undefined** → already fixed upstream (the feature was reverted;
  content div is back to `div(classes = contentClasses)`). No action.
- **#2 `Tailwind.Text.Size.sm` missing `.size`** → already fixed upstream (now `.sm.size`
  everywhere). No action.
- **#3–#15** → still present in current code. These are the work items below.

Key constraint: kotlinx.html escapes attribute values (`<`,`>`,`&`,`"`), so the unvalidated
`horizontalMargin` is **class-injection / validation-inconsistency**, not XSS — but it is still
worth closing because the doc claims "all CSS class parameters are validated."

Decisions confirmed with the user:
- `horizontalMargin` should apply at **all breakpoints** (restore pre-refactor contract).
- Devlog is **tracked in git** (this file ships with the PR).

Defaults chosen (no user input needed, called out in PR for veto):
- `version` (dead config): render a non-breaking `<meta name="version">` rather than remove it
  (removal is breaking, deferred to v1.0 per project convention).
- DSL stylesheet opt-out: add an additive, non-breaking `noLocalStylesheets()` builder method.
- Logo link: point at the first page's `outputFilename` (fallback `index.html`).

## Plan

1. **Shared validators** (`utils/Validation.kt`, new): hoist the CSS-class regex, a no-whitespace
   spacing-token regex, a relative-path validator, and a URL-character blocklist into one place
   (resolves duplication #13 + per-call recompilation #14, enables #5/#9). `internal` top-level.
2. **NavMenuSettings.kt**: use shared `validateCssClasses`; add `validateSpacingUnit(horizontalMargin)`
   (#5); hoist instagram/email regexes to private top-level vals (#14); route `Logo` URL check
   through shared blocklist (#13); fix `horizontalMargin` KDoc drift (`ms-16/me-16` → `px-16`).
3. **NavMenu.kt**: `px-4 sm:px-8 md:px-{margin}` → `px-{margin}` (#6, all breakpoints);
   logo href → first page (#8); drop invalid `z-1` class (#12).
4. **Site.kt**: use shared `validateCssClasses` + URL blocklist (#13); add init checks for
   duplicate `outputFilename` (#3) and traversal-safe page filenames (#9); render `<meta name="version">` (#15).
5. **HtmlExtensions.kt**: hoist googleTag regex (#14); fix stale "line 14" comment.
6. **InputOutputPair.kt**: robust classloader lookup (#7); confirm resource exists before creating
   parent dirs (#11); write to temp file + atomic move so a mid-copy failure can't corrupt an
   existing file (#10); use shared `validateRelativePath` (#13).
7. **ResourcesDsl.kt**: add `noLocalStylesheets()` opt-out (#4).
8. **Tests**: update NavMenuTest px assertions (#6); add tests for horizontalMargin validation,
   duplicate/traversal filenames, version meta, and `noLocalStylesheets()`.
9. ktfmt + `./gradlew build` (Java 21 Zulu) green, then commit devlog+plan+code, push, open PR.

Out of scope (flag to user): deep review of the new, unreviewed SEO code (OG/JSON-LD/sitemap).
