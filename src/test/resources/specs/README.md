# Cached specs

Copies of published OpenAPI specs, taken verbatim from the upstream repository at the date recorded
in each file's header. Tests parse these rather than fetching over the network.

## Why cached

`./gradlew check` must not fail because GitHub is unreachable, and an upstream spec change must not
turn an unrelated PR red. These files pin behaviour, not currency.

## What that costs

They go stale. If hrds renames an operation, the tests here stay green while the deployed service
starts returning 404 for the old path. Drift is caught by the functional tests, which run against a
deployed instance and read the live spec.

## Refreshing

```bash
curl -s <source url from the file header> -o src/test/resources/specs/<name>.cached.yml
```

Re-add the header afterwards, updating `Retrieved` and `sha256`. If tests then fail, that failure is
the upstream change — read it before assuming the test is wrong.
