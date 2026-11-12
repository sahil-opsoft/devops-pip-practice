# Day 3 Git practice

## CI

Every push to `main` and every pull request into `main` runs `.github/workflows/ci.yml`.
The workflow runs `scripts/check.sh`, which fails on leftover conflict markers or shell syntax errors.

## Pre-commit hook

`.githooks/pre-commit` blocks a commit if any staged shell script has a syntax error.
Enable it once per clone with `git config core.hooksPath .githooks`.
