# Contributing to Hyeons' Lab SSG

Thank you for your interest in contributing! This document provides guidelines for contributing to the project.

## Getting Started

### Prerequisites

- **JDK 21** (required for JVM Toolchain)
- **Gradle 9.0+** (wrapper included)
- **Git** for version control

### Setup

```bash
# Clone the repository
git clone https://github.com/hyeons-lab/ssg.git
cd ssg

# Build the project
./gradlew build

# Run tests
./gradlew test

# Publish to Maven Local for testing
./gradlew publishToMavenLocal
```

## Development Workflow

### 1. Create a Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/issue-description
```

### 2. Make Changes

- Write code following the style guide below
- Add tests for new functionality
- Update documentation as needed

### 3. Run Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "*.YourTestClass"

# Verify build
./gradlew clean build
```

### 4. Commit Changes

Follow the commit message format:

```
<type>: <short summary>

<detailed description>

<list of changes>
```

**Types:**
- `feat:` - New feature
- `fix:` - Bug fix
- `test:` - Adding tests
- `docs:` - Documentation only
- `refactor:` - Code refactoring
- `perf:` - Performance improvement
- `chore:` - Build/tooling changes

**Example:**
```
fix: Validate Google Tag ID format to prevent XSS

Added regex validation for Google Tag IDs to ensure only valid
formats (G-XXXXXXXXXX or GT-XXXXXXX) are accepted.

- Add validation in HtmlExtensions.googleTag()
- Reject script injection attempts
- Add 10 tests for tag validation
```

### 5. Update CHANGELOG

Add your changes to `CHANGELOG.md` under the `[Unreleased]` section:

```markdown
## [Unreleased]

### Added
- Your new feature

### Fixed
- Your bug fix
```

## Code Style

### Kotlin Style

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// ✅ Good
fun calculateTotal(items: List<Item>): Int {
    return items.sumOf { it.price }
}

// ❌ Bad
fun CalculateTotal(Items: List<Item>): Int {
    var total = 0
    for(item in Items) total += item.price
    return total
}
```

### Key Conventions

1. **Indentation:** 4 spaces (no tabs)
2. **Line length:** Max 120 characters
3. **Naming:**
   - Classes: `PascalCase`
   - Functions/properties: `camelCase`
   - Constants: `UPPER_SNAKE_CASE`
4. **Imports:** Group stdlib, external, internal (alphabetical within groups)

### Documentation

```kotlin
/**
 * Generates static HTML files for all pages in the site.
 *
 * Creates output directory if it doesn't exist. If generation fails for any page,
 * collects all failures and reports them together.
 *
 * @throws IllegalStateException if any files fail to generate
 */
fun generateFiles() {
    // ...
}
```

## Testing Requirements

### All PRs Must Include Tests

- New features require tests demonstrating functionality
- Bug fixes require tests reproducing the bug
- Minimum 80% coverage for new code

### Test Framework

Use **Kotest** for writing tests:

```kotlin
class MyFeatureTest : FunSpec({
    context("feature description") {
        test("should do something specific") {
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

### Test Organization

```
lib/src/test/kotlin/com/hyeonslab/ssg/
├── core/
│   ├── SiteTest.kt
│   ├── HtmlExtensionsTest.kt
│   └── InputOutputPairTest.kt
├── page/
│   └── NavMenuTest.kt
└── utils/
    └── TailwindTest.kt
```

### Running Tests

```bash
# All tests
./gradlew test

# With coverage report
./gradlew test jacocoTestReport

# Specific tests
./gradlew test --tests "*.HtmlExtensionsTest"
```

## Security Considerations

### Input Validation

Always validate user-provided input:

```kotlin
// ✅ Good
fun processTag(tag: String) {
    require(tag.matches(Regex("^G-[A-Z0-9]{10}$"))) {
        "Invalid tag format"
    }
    // ... process tag
}

// ❌ Bad
fun processTag(tag: String) {
    // No validation - vulnerable to injection
    insertIntoScript(tag)
}
```

### Path Security

Use proper path normalization:

```kotlin
// ✅ Good
val normalized = Paths.get(userPath).normalize()
require(!normalized.isAbsolute)
require(!normalized.toString().startsWith(".."))

// ❌ Bad
require(!userPath.contains(".."))  // Insufficient
```

## Pull Request Process

### Before Submitting

- [ ] All tests pass (`./gradlew test`)
- [ ] Build succeeds (`./gradlew build`)
- [ ] Code follows style guide
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
- [ ] Commits follow commit message format

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Describe how you tested your changes

## Checklist
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] CHANGELOG updated
- [ ] No breaking changes (or documented)
```

### Review Process

1. **Automated checks** must pass (build, tests)
2. **Code review** by maintainer
3. **Changes requested** - address feedback and push updates
4. **Approval** - PR will be merged by maintainer

## Code Review Guidelines

### For Contributors

- Respond to feedback constructively
- Ask questions if feedback is unclear
- Make requested changes in new commits (don't force push)
- Be patient - reviews may take time

### For Reviewers

- Be respectful and constructive
- Explain the "why" behind suggestions
- Distinguish between "must fix" and "nice to have"
- Approve when confident in the changes

## Reporting Issues

### Bug Reports

Include:
1. **Description** - What's wrong?
2. **Reproduction steps** - How to reproduce?
3. **Expected behavior** - What should happen?
4. **Actual behavior** - What actually happens?
5. **Environment** - Kotlin version, OS, etc.
6. **Code sample** - Minimal reproducible example

### Feature Requests

Include:
1. **Use case** - Why is this needed?
2. **Proposed solution** - How should it work?
3. **Alternatives** - Other ways to solve this?
4. **Additional context** - Examples, mockups, etc.

## Development Tips

### Testing Your Changes Locally

```bash
# Publish to Maven Local
./gradlew publishToMavenLocal

# In your test project
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.hyeons-lab:ssg:0.1.0")
}
```

### Debugging

```kotlin
// Use println for quick debugging
println("Debug: value=$value")

// Or use proper logging if added later
logger.debug("Processing page: ${page.title}")
```

### Common Issues

**"Configuration cache cannot be reused"**
- Normal after changing build files
- Just a warning, build will still work

**"Resource not found in classpath"**
- Ensure resources are in `src/main/resources/`
- Check resource path is relative, not absolute

## Questions?

- **Documentation:** See [README.md](README.md) and [USAGE_GUIDE.md](USAGE_GUIDE.md)
- **Development:** See [AGENTS.md](AGENTS.md) for technical details
- **Issues:** Open an issue on GitHub
- **Security:** Report privately to maintainer

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).

---

Thank you for contributing to Hyeons' Lab SSG! 🚀
