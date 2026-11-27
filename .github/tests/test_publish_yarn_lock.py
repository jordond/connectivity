"""Exercise publishing regenerated yarn locks against a disposable remote and checkout."""

import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / "scripts/publish-yarn-lock"
JS_LOCK = "kotlin-js-store/yarn.lock"
WASM_LOCK = "kotlin-js-store/wasm/yarn.lock"
BRANCH = "renovate/kotlin"
BOT = {"BOT_NAME": "lock-bot", "BOT_EMAIL": "lock-bot@example.com", "TITLE": "Update the yarn lock files"}


class PublishYarnLockTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        base = Path(self.temporary.name)
        self.remote = base / "remote.git"
        self.root = base / "repo"
        self.locks = base / "locks"
        subprocess.run(["git", "init", "--quiet", "--bare", str(self.remote)], check=True)
        subprocess.run(["git", "clone", "--quiet", str(self.remote), str(self.root)],
                       check=True, capture_output=True)
        (self.root / ".github/scripts").mkdir(parents=True)
        shutil.copy2(SCRIPT, self.root / ".github/scripts/publish-yarn-lock")
        (self.root / "kotlin-js-store/wasm").mkdir(parents=True)
        (self.root / JS_LOCK).write_text("js v1")
        (self.root / WASM_LOCK).write_text("wasm v1")
        self.git("switch", "--quiet", "-c", BRANCH)
        self.git("add", ".")
        self.commit("Update Kotlin")
        self.git("push", "--quiet", "origin", BRANCH)
        self.head = self.rev("HEAD")
        (self.locks / "kotlin-js-store/wasm").mkdir(parents=True)
        (self.locks / JS_LOCK).write_text("js v2")
        (self.locks / WASM_LOCK).write_text("wasm v1")

    def git(self, *args, cwd=None):
        return subprocess.run(["git", *args], cwd=cwd or self.root, text=True,
                              capture_output=True, check=True).stdout.strip()

    def commit(self, message):
        self.git("-c", "user.name=renovate", "-c", "user.email=renovate@example.com",
                 "commit", "--quiet", "-m", message)

    def rev(self, ref, cwd=None):
        return self.git("rev-parse", ref, cwd=cwd)

    def remote_head(self):
        return self.rev(f"refs/heads/{BRANCH}", cwd=self.remote)

    def remote_file(self, path):
        return self.git("show", f"refs/heads/{BRANCH}:{path}", cwd=self.remote)

    def run_script(self, *args, **overrides):
        env = {"PATH": os.environ["PATH"], "BRANCH": BRANCH, "EXPECTED_SHA": self.head, **BOT}
        env.update(overrides)
        return subprocess.run(
            ["bash", str(self.root / ".github/scripts/publish-yarn-lock"), *args],
            cwd=self.root, text=True, capture_output=True,
            env={name: value for name, value in env.items() if value is not None},
        )

    def run_default(self, **overrides):
        return self.run_script("--locks", str(self.locks), **overrides)

    def test_pushes_one_commit_with_the_new_lock_as_the_bot(self):
        result = self.run_default()

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(self.head, self.rev(f"{self.remote_head()}~1", cwd=self.remote))
        self.assertEqual("js v2", self.remote_file(JS_LOCK))
        self.assertEqual(
            "lock-bot|lock-bot@example.com|Update the yarn lock files",
            self.git("show", "-s", "--format=%an|%ae|%s", self.remote_head(), cwd=self.remote),
        )

    def test_commits_only_the_lock_files(self):
        (self.locks / "build.gradle.kts").write_text("injected")
        (self.locks / "kotlin-js-store/extra.txt").write_text("injected")
        (self.root / "README.md").write_text("stray edit in the checkout")

        result = self.run_default()

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(
            JS_LOCK,
            self.git("show", "--format=", "--name-only", self.remote_head(), cwd=self.remote),
        )

    def test_does_nothing_when_the_locks_already_match(self):
        (self.locks / JS_LOCK).write_text("js v1")

        result = self.run_default()

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(self.head, self.remote_head())

    def test_leaves_the_branch_alone_when_the_checkout_is_not_the_expected_commit(self):
        result = self.run_default(EXPECTED_SHA="0" * 40)

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(self.head, self.remote_head())
        self.assertIn("leaving it for the next run", result.stdout)

    def test_does_not_overwrite_a_branch_that_moved_on_the_remote(self):
        other = Path(self.temporary.name) / "other"
        subprocess.run(["git", "clone", "--quiet", "--branch", BRANCH, str(self.remote), str(other)],
                       check=True, capture_output=True)
        (other / "README.md").write_text("rebased by renovate")
        self.git("add", ".", cwd=other)
        self.git("-c", "user.name=renovate", "-c", "user.email=renovate@example.com",
                 "commit", "--quiet", "-m", "Rebase", cwd=other)
        self.git("push", "--quiet", "origin", BRANCH, cwd=other)
        moved = self.remote_head()

        result = self.run_default()

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(moved, self.remote_head())

    def test_rejects_a_missing_setting(self):
        result = self.run_default(TITLE=None)

        self.assertEqual(1, result.returncode)
        self.assertIn("Missing TITLE", result.stderr)
        self.assertEqual(self.head, self.remote_head())


if __name__ == "__main__":
    unittest.main()
