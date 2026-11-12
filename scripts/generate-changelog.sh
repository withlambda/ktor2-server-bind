#!/usr/bin/env bash
# generate-changelog.sh
#
# Generates or updates CHANGELOG.md from git commit history.
#
# Format follows https://keepachangelog.com/en/1.1.0/
#
# Behaviour:
#   - If CHANGELOG.md does not exist: creates it with all commits.
#   - If CHANGELOG.md exists: prepends a new section containing only commits
#     whose hashes are not yet present in the file.
#
# Version is read from gradle.properties (key: projectVersion).
# Falls back to "Unreleased" when the property is absent.
#
# Commit entry format:
#   YYYY-MM-DDTHH:MM:SSZ <subject> (<short-hash>)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CHANGELOG="$REPO_ROOT/CHANGELOG.md"
GRADLE_PROPS="$REPO_ROOT/gradle.properties"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

read_project_version() {
    if [[ -f "$GRADLE_PROPS" ]]; then
        grep -E '^projectVersion=' "$GRADLE_PROPS" \
            | cut -d'=' -f2 \
            | tr -d '[:space:]'
    fi
}

# Emits commits newer than $1 (exclusive). Pass empty string for all commits.
# Format per line: <full-hash> <YYYY-MM-DDTHH:MM:SSZ> <subject>
git_commits_since() {
    local since_hash="${1:-}"
    if [[ -n "$since_hash" ]]; then
        TZ=UTC git -C "$REPO_ROOT" log "${since_hash}..HEAD" \
            --format="%H %ad %s" --date=format-local:'%Y-%m-%dT%H:%M:%SZ'
    else
        TZ=UTC git -C "$REPO_ROOT" log \
            --format="%H %ad %s" --date=format-local:'%Y-%m-%dT%H:%M:%SZ'
    fi
}

# Returns the first commit hash from the log that is already recorded in the
# existing CHANGELOG, or an empty string when none is found.
find_latest_recorded_hash() {
    while IFS= read -r line; do
        local full_hash short_hash
        full_hash=$(echo "$line" | awk '{print $1}')
        short_hash="${full_hash:0:7}"
        if grep -qF "($short_hash)" "$CHANGELOG" 2>/dev/null; then
            echo "$full_hash"
            return
        fi
    done < <(git -C "$REPO_ROOT" log --format="%H")
    echo ""
}

# Renders a changelog section from commit lines.
# Usage: render_section <version> <date> <commits-string>
render_section() {
    local version="$1"
    local date="$2"
    local commits="$3"

    local first_hash short_hash
    first_hash=$(echo "$commits" | head -1 | awk '{print $1}')
    short_hash="${first_hash:0:7}"

    printf '## [%s] - %s (%s)\n\n' "$version" "$date" "$short_hash"
    while IFS= read -r line; do
        [[ -z "$line" ]] && continue
        local full_hash short_hash datetime subject
        full_hash=$(echo "$line" | awk '{print $1}')
        short_hash="${full_hash:0:7}"
        # datetime is a single ISO-8601 UTC token (field 2)
        datetime=$(echo "$line" | awk '{print $2}')
        subject=$(echo "$line" | cut -d' ' -f3-)
        printf -- '- %s %s (%s)\n' "$datetime" "$subject" "$short_hash"
    done <<< "$commits"
    printf '\n'
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

VERSION=$(read_project_version)
TODAY=$(TZ=UTC date +%Y-%m-%d)

if [[ ! -f "$CHANGELOG" ]]; then
    # -----------------------------------------------------------------------
    # Fresh changelog
    # -----------------------------------------------------------------------
    commits=$(git_commits_since "")

    if [[ -z "$commits" ]]; then
        echo "No git commits found — CHANGELOG.md not created." >&2
        exit 0
    fi

    {
        printf '# Changelog\n\n'
        printf 'All notable changes to this project will be documented in this file.\n\n'
        printf 'The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),\n'
        printf 'and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).\n\n'
        printf '## [Unreleased]\n\n'
        render_section "${VERSION:-Unreleased}" "$TODAY" "$commits"
    } > "$CHANGELOG"

    echo "Created $CHANGELOG (section: '[${VERSION:-Unreleased}]')."
else
    # -----------------------------------------------------------------------
    # Update existing changelog
    # -----------------------------------------------------------------------
    last_recorded=$(find_latest_recorded_hash)
    commits=$(git_commits_since "$last_recorded")

    if [[ -z "$commits" ]]; then
        echo "No new commits to add — $CHANGELOG is already up to date."
        exit 0
    fi

    new_section=$(render_section "${VERSION:-Unreleased}" "$TODAY" "$commits")

    # Insert the new section before the first existing versioned '## [' heading
    # (i.e., after the preamble and [Unreleased] block), so newest appears first.
    first_version_line=$(grep -n '^## \[' "$CHANGELOG" | grep -v '^\S*:\[Unreleased\]' | head -1 | cut -d':' -f1)

    tmp=$(mktemp)
    trap 'rm -f "$tmp"' EXIT

    if [[ -n "$first_version_line" ]]; then
        head -n "$((first_version_line - 1))" "$CHANGELOG" > "$tmp"
        printf '%s\n' "$new_section" >> "$tmp"
        tail -n "+${first_version_line}" "$CHANGELOG" >> "$tmp"
    else
        # No existing version section — find the [Unreleased] line and insert after it
        unreleased_line=$(grep -n '^\[Unreleased\]\|^## \[Unreleased\]' "$CHANGELOG" | head -1 | cut -d':' -f1)
        if [[ -n "$unreleased_line" ]]; then
            head -n "$unreleased_line" "$CHANGELOG" > "$tmp"
            printf '\n%s\n' "$new_section" >> "$tmp"
            tail -n "+$((unreleased_line + 1))" "$CHANGELOG" >> "$tmp"
        else
            printf '%s\n' "$new_section" > "$tmp"
            cat "$CHANGELOG" >> "$tmp"
        fi
    fi

    mv "$tmp" "$CHANGELOG"
    echo "Updated $CHANGELOG (section: '[${VERSION:-Unreleased}]', $(echo "$commits" | wc -l | tr -d ' ') new commit(s))."
fi
