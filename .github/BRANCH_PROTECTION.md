# Branch Protection Setup

To enforce that PRs must pass the build before merging, configure branch protection rules on GitHub.

## Steps to Enable Branch Protection

1. Go to your repository on GitHub
2. Navigate to **Settings** → **Branches**
3. Click **Add branch protection rule** or edit the existing rule for `main`
4. Configure the following settings:

### Required Settings

**Branch name pattern:** `main`

**Protect matching branches:**
- ✅ **Require status checks to pass before merging**
  - ✅ **Require branches to be up to date before merging**
  - Add required status check: `build` (from the Build workflow)
- ✅ **Require a pull request before merging**
  - Number of approvals: 1 (optional, adjust as needed)
- ✅ **Do not allow bypassing the above settings** (recommended)

### Optional Recommended Settings

- ✅ **Require linear history** (prevents merge commits)
- ✅ **Require deployments to succeed before merging** (if using deployments)
- ✅ **Lock branch** (for production-critical branches)

## Testing the Protection

1. Create a branch with a failing build
2. Open a PR to `main`
3. The PR should show the build status check
4. The merge button should be disabled until the build passes

## CI/CD Workflow

The `.github/workflows/build.yml` workflow runs on:
- Every push to `main` and `feature/*` branches
- Every pull request targeting `main`

The workflow:
- Checks out the code
- Sets up JDK 21
- Runs `./gradlew build`
- Uploads build reports if the build fails