# Day 3 Git practice

## CI

Every push to `main` and every pull request into `main` runs `.github/workflows/ci.yml`.
The workflow runs `scripts/check.sh`, which fails on leftover conflict markers or shell syntax errors.
