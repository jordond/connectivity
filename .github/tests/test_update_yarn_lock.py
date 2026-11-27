"""Exercise the yarn lock regeneration against a disposable checkout and a stand-in gradlew."""

from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / "scripts/update-yarn-lock"
JS_LOCK = "kotlin-js-store/yarn.lock"
WASM_LOCK = "kotlin-js-store/wasm/yarn.lock"

GRADLEW = """#!/bin/bash
echo "$@" >> gradle-calls.txt
[ -z "${NEW_JS_LOCK:-}" ] || printf '%s' "$NEW_JS_LOCK" > kotlin-js-store/yarn.lock
[ -z "${NEW_WASM_LOCK:-}" ] || printf '%s' "$NEW_WASM_LOCK" > kotlin-js-store/wasm/yarn.lock
exit "${GRADLE_EXIT:-0}"
"""


class UpdateYarnLockTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name) / "repo"
        self.out = Path(self.temporary.name) / "out"
        self.output = Path(self.temporary.name) / "github-output"
        (self.root / ".github/scripts").mkdir(parents=True)
        shutil.copy2(SCRIPT, self.root / ".github/scripts/update-yarn-lock")
        gradlew = self.root / "gradlew"
        gradlew.write_text(GRADLEW)
        gradlew.chmod(0o755)
        (self.root / "kotlin-js-store/wasm").mkdir(parents=True)
        (self.root / JS_LOCK).write_text("js v1")
        (self.root / WASM_LOCK).write_text("wasm v1")
        (self.root / ".gitignore").write_text("gradle-calls.txt\n")
        self.git("init", "--quiet")
        self.git("add", ".")
        self.git("-c", "user.name=t", "-c", "user.email=t@example.com", "commit", "--quiet", "-m", "init")

    def git(self, *args):
        return subprocess.run(["git", *args], cwd=self.root, text=True, capture_output=True, check=True)

    def run_script(self, *args, **env):
        return subprocess.run(
            ["bash", str(self.root / ".github/scripts/update-yarn-lock"), *args],
            cwd=self.root, text=True, capture_output=True,
            env={"PATH": "/usr/bin:/bin:/usr/local/bin:/opt/homebrew/bin", **env},
        )

    def run_default(self, **env):
        return self.run_script("--github-output", str(self.output), "--out", str(self.out), **env)

    def test_runs_both_upgrade_tasks(self):
        result = self.run_default()

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(
            "kotlinUpgradeYarnLock kotlinWasmUpgradeYarnLock\n",
            (self.root / "gradle-calls.txt").read_text(),
        )

    def test_reports_unchanged_and_copies_nothing_when_the_locks_match(self):
        result = self.run_default()

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual("changed=false\n", self.output.read_text())
        self.assertFalse(self.out.exists())

    def test_reports_changed_and_copies_both_locks_when_one_differs(self):
        result = self.run_default(NEW_JS_LOCK="js v2")

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual("changed=true\n", self.output.read_text())
        self.assertEqual("js v2", (self.out / JS_LOCK).read_text())
        self.assertEqual("wasm v1", (self.out / WASM_LOCK).read_text())

    def test_reports_changed_when_only_the_wasm_lock_differs(self):
        result = self.run_default(NEW_WASM_LOCK="wasm v2")

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual("changed=true\n", self.output.read_text())
        self.assertEqual("wasm v2", (self.out / WASM_LOCK).read_text())

    def test_ignores_changes_outside_the_lock_files(self):
        (self.root / "README.md").write_text("edited by the build")

        result = self.run_default()

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual("changed=false\n", self.output.read_text())

    def test_fails_without_reporting_when_gradle_fails(self):
        result = self.run_default(NEW_JS_LOCK="js v2", GRADLE_EXIT="1")

        self.assertNotEqual(0, result.returncode)
        self.assertFalse(self.output.exists())

    def test_rejects_missing_arguments(self):
        result = self.run_script("--out", str(self.out))

        self.assertEqual(1, result.returncode)
        self.assertIn("Usage", result.stderr)


if __name__ == "__main__":
    unittest.main()
