# Take-Home Assessment (Kotlin)

This repository contains two exercises:

- **Hierarchy filter implementation** with unit tests.
- **SimpleCache code review** with production-impact analysis and comparison tests.

## Structure

- `src/main/kotlin/hierarchy/README.md` - explanation of hierarchy requirements, implementation approach, and tests.
- `src/main/kotlin/hierarchy/` - hierarchy model and `filter()` implementation.
- `src/test/kotlin/hierarchy/` - test cases for hierarchy filtering.
- `src/main/kotlin/cache/README.md` - cache review write-up (main document for the review task).
- `src/main/kotlin/cache/` - baseline `SimpleCache` and improved `ExpiringSimpleCache` implementations.
- `src/test/kotlin/cache/` - tests proving baseline issues and improved behavior.

## Run

```bash
./gradlew test
```

## Notes

- Build tool: Gradle `9.4.1`
- Kotlin: `2.3.20`
