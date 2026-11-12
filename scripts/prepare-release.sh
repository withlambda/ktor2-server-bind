#!/usr/bin/env bash
# prepare-release.sh
#
# Prepares a release by:
#   1. Reading the current projectVersion from gradle.properties.
#   2. Asking whether the next release is Major, Minor, or Patch.
#   3. Incrementing the chosen version component.
#   4. Writing the new version back to gradle.properties.
#   5. Running scripts/generate-changelog.sh to update CHANGELOG.md.
#   6. Committing all changes with a release-preparation commit message.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
GRADLE_PROPS="$REPO_ROOT/gradle.properties"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

die() { echo "ERROR: $*" >&2; exit 1; }

read_project_version() {
    grep -E '^projectVersion=' "$GRADLE_PROPS" \
        | cut -d'=' -f2 \
        | tr -d '[:space:]'
}

write_project_version() {
    local new_version="$1"
    # Use sed to replace the value in-place (portable across macOS and Linux)
    sed -i.bak "s/^projectVersion=.*/projectVersion=${new_version}/" "$GRADLE_PROPS"
    rm -f "${GRADLE_PROPS}.bak"
}

increment_version() {
    local version="$1"
    local part="$2"   # major | minor | patch

    local major minor patch
    major=$(echo "$version" | cut -d'.' -f1)
    minor=$(echo "$version" | cut -d'.' -f2)
    patch=$(echo "$version" | cut -d'.' -f3)

    case "$part" in
        major) echo "$((major + 1)).0.0" ;;
        minor) echo "${major}.$((minor + 1)).0" ;;
        patch) echo "${major}.${minor}.$((patch + 1))" ;;
        *) die "Unknown release type: $part" ;;
    esac
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

[[ -f "$GRADLE_PROPS" ]] || die "gradle.properties not found at $GRADLE_PROPS"

CURRENT_VERSION=$(read_project_version)
[[ -n "$CURRENT_VERSION" ]] || die "projectVersion not found in gradle.properties"

echo "Current version: $CURRENT_VERSION"
echo ""
echo "Select the release type:"
echo "  1) Major  (breaking changes)"
echo "  2) Minor  (new features, backwards-compatible)"
echo "  3) Patch  (bug fixes, backwards-compatible)"
echo ""
read -rp "Enter choice [1/2/3]: " choice

case "$choice" in
    1) RELEASE_TYPE="major" ;;
    2) RELEASE_TYPE="minor" ;;
    3) RELEASE_TYPE="patch" ;;
    *) die "Invalid choice '$choice'. Expected 1, 2, or 3." ;;
esac

NEW_VERSION=$(increment_version "$CURRENT_VERSION" "$RELEASE_TYPE")

echo ""
echo "Bumping version: $CURRENT_VERSION → $NEW_VERSION ($RELEASE_TYPE)"
write_project_version "$NEW_VERSION"
echo "Updated gradle.properties."

echo ""
echo "Generating changelog..."
bash "$SCRIPT_DIR/generate-changelog.sh"

echo ""
echo "Staging changes..."
git -C "$REPO_ROOT" add "$GRADLE_PROPS" "$REPO_ROOT/CHANGELOG.md"

COMMIT_MSG="chore: prepare release $NEW_VERSION

- Bump projectVersion from $CURRENT_VERSION to $NEW_VERSION ($RELEASE_TYPE)
- Update CHANGELOG.md"

git -C "$REPO_ROOT" commit -m "$COMMIT_MSG"

echo ""
echo "Release preparation for $NEW_VERSION complete."
echo "Commit: $(git -C "$REPO_ROOT" log -1 --format='%h %s')"
