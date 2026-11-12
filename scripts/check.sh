#!/usr/bin/env bash
# Repository checks run by CI on every push and pull request.
# Same rules as the pre-commit hook, applied to every tracked file.
status=0

# 1. No leftover merge-conflict markers in any tracked file
if git grep -nE '^(<<<<<<<( |$)|=======$|>>>>>>>( |$))'; then
  echo "check: conflict markers found" >&2
  status=1
fi

# 2. Every tracked shell script must pass a syntax check
while IFS= read -r file; do
  if ! bash -n "$file"; then
    echo "check: syntax error in $file" >&2
    status=1
  fi
done < <(git ls-files -- '*.sh')

if [ "$status" -eq 0 ]; then
  echo "All checks passed"
fi
exit "$status"
