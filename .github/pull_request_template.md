<!--
PR title must be a Conventional Commit — it becomes the squash-merge commit subject.
e.g. feat(recipedetail): build screen from Figma 371:728
-->

## Summary

<!-- What changed and why. Link the issue: Closes #NNN -->

## Modules touched

<!-- e.g. feature:recipedetail, core:designsystem, domain -->

## Screenshots

<!-- REQUIRED for any visible change in a feature:* module. Before / after. -->

## Checklist

- [ ] `./gradlew build detekt :konsist:test` passes locally
- [ ] No `feature:* → feature:*` dependency introduced
- [ ] `domain` / `core:model` stay free of Android & Compose
- [ ] All user-facing text is English (Hungarian design text translated)
- [ ] New screen: nav wired in `androidApp` and Koin module registered
- [ ] Non-trivial ViewModel / mapper logic has `commonTest` coverage
