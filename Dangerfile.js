// Automated PR review checks the linters can't express. Every check here is a `warn`, never
// a `fail` — this is advisory, not a gate. See docs/TOOLING.md.

const allFiles = [...danger.git.modified_files, ...danger.git.created_files];

// 1. Large PRs are hard to review well.
const LARGE_PR_THRESHOLD = 40;
if (allFiles.length > LARGE_PR_THRESHOLD) {
  warn(
    `This PR touches ${allFiles.length} files (> ${LARGE_PR_THRESHOLD}). ` +
      'Consider splitting it into smaller, independently reviewable PRs.',
  );
}

// 2. A feature screen's production code changed with no matching screenshot-baseline update.
const changedScreenSources = allFiles.filter(
  (f) => /^feature\/[^/]+\/src\/main\/.*\.kt$/.test(f) && !f.includes('/di/'),
);
const changedScreenshotBaselines = allFiles.some((f) =>
  /^feature\/[^/]+\/src\/test\/screenshots\//.test(f),
);
if (changedScreenSources.length > 0 && !changedScreenshotBaselines) {
  warn(
    'This PR changes feature screen code but no `src/test/screenshots/` baseline. ' +
      'If the UI actually changed, run `./gradlew :feature:<name>:recordRoborazziDebug` and ' +
      'commit the updated PNGs (see docs/TOOLING.md §4/§6a).',
  );
}

// 3. A new ViewModel with no matching test.
const newViewModels = danger.git.created_files.filter((f) =>
  /^feature\/[^/]+\/src\/main\/.*\/[A-Za-z0-9]+ViewModel\.kt$/.test(f),
);
for (const vm of newViewModels) {
  const name = vm.split('/').pop().replace(/\.kt$/, '');
  const hasTest = allFiles.some(
    (f) => f.startsWith(`${vm.split('/src/main/')[0]}/src/test/`) && f.includes(`${name}Test`),
  );
  if (!hasTest) {
    warn(`\`${name}\` is new but has no matching \`${name}Test.kt\`. See AGENTS.md "Tests".`);
  }
}
