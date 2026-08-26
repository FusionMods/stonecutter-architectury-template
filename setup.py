#!/usr/bin/env python3
"""Rename this template's placeholder mod identity ("Example Mod" / examplemod /
com.example.examplemod) to your own, project-wide.

Run this once, right after cloning the template, before writing any real code -
see README.md's "Using this template" section for what this automates and what
it deliberately still leaves for you to do by hand (picking a license, writing
your own README, ...).

Usage:
    python3 setup.py                     # interactive prompts for everything
    python3 setup.py --mod-id mymod --mod-group com.mycompany --mod-name "My Mod" \\
        --mod-authors "Your Name" --mod-description "..." --yes
    python3 setup.py --dry-run           # show the plan, change nothing

Safe to re-run: fields you don't change are no-ops, and nothing is touched
until you confirm the printed plan (or pass --yes).
"""
from __future__ import annotations

import argparse
import re
import shutil
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent
GRADLE_PROPERTIES = REPO_ROOT / "gradle.properties"
SETTINGS_GRADLE = REPO_ROOT / "settings.gradle.kts"
FABRIC_MOD_JSON = REPO_ROOT / "fabric/src/main/resources/fabric.mod.json"
DATAGEN_MOD_JSON = REPO_ROOT / "datagen/src/main/resources/fabric.mod.json"

# The four classic Architectury source trees Stonecutter shares across every Minecraft
# version (see README.md's "Project layout" section), plus the standalone datagen/ module
# (see its "Data generation" section) - not Stonecutter-shared, but same package/id scheme.
SOURCE_ROOTS = [
    REPO_ROOT / "src/main/java",
    REPO_ROOT / "fabric/src/main/java",
    REPO_ROOT / "forge/src/main/java",
    REPO_ROOT / "neoforge/src/main/java",
    REPO_ROOT / "datagen/src/main/java",
]


def die(message: str) -> None:
    print(f"error: {message}", file=sys.stderr)
    sys.exit(1)


def read_gradle_properties(path: Path) -> dict[str, str]:
    props: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        props[key.strip()] = value.strip()
    return props


def pascal_case(name: str) -> str:
    """"My Cool Mod" -> "MyCoolMod" - used to suggest a main class name from modName."""
    words = re.findall(r"[A-Za-z0-9]+", name)
    return "".join(word[:1].upper() + word[1:] for word in words) or "Mod"


def prompt(label: str, default: str) -> str:
    raw = input(f"{label} [{default}]: ").strip()
    return raw or default


def package_dir(root: Path, package_dotted: str) -> Path:
    return root / Path(*package_dotted.split("."))


def find_main_class(common_pkg_dir: Path) -> str:
    """The one .java file sitting directly in the common package dir (not in a
    sub-package like block/, client/, registry/) - "ExampleMod.java" in the
    pristine template, whatever it's been renamed to on a re-run."""
    candidates = sorted(p for p in common_pkg_dir.iterdir() if p.is_file() and p.suffix == ".java")
    if len(candidates) != 1:
        names = ", ".join(p.name for p in candidates) or "(none)"
        die(
            f"expected exactly one top-level class in {common_pkg_dir}, found: {names}\n"
            "       pass --old-main-class to override detection."
        )
    return candidates[0].stem


def update_gradle_property(path: Path, key: str, new_value: str) -> bool:
    """Rewrite a single `key=value` line in a .properties file in place.
    Used for modVersion/modDescription/modAuthors, which (unlike modId/modGroup/
    modName) don't drive any project-wide text replacement, so need to be set
    directly rather than found-and-replaced."""
    original = path.read_text(encoding="utf-8")
    pattern = re.compile(rf"^{re.escape(key)}=.*$", re.MULTILINE)
    if not pattern.search(original):
        die(f"couldn't find a {key}= line in {path}")
    updated = pattern.sub(f"{key}={new_value}", original, count=1)
    if updated != original:
        path.write_text(updated, encoding="utf-8")
        return True
    return False


def prune_empty_parents(start: Path, stop_at: Path) -> None:
    """Remove now-empty directories from `start` up to (not including) `stop_at` -
    e.g. a leftover empty com/example/ after com/example/examplemod/ moved out."""
    current = start
    while current != stop_at:
        try:
            current.rmdir()  # only succeeds if empty
        except OSError:
            break
        current = current.parent


def collect_entry_point_files(pkg_dir: Path, old_main_class: str) -> list[Path]:
    """ExampleMod.java, ExampleModFabric.java, ExampleModFabricClient.java, ... -
    anything named `<old_main_class><suffix>.java` anywhere under the package."""
    return sorted(pkg_dir.rglob(f"{old_main_class}*.java"))


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--mod-id", help="e.g. mymod (lowercase, matches Minecraft mod id rules)")
    parser.add_argument("--mod-group", help="Java package prefix, e.g. com.mycompany")
    parser.add_argument("--mod-name", help='Display name, e.g. "My Mod"')
    parser.add_argument("--mod-version", help="e.g. 0.1.0")
    parser.add_argument("--mod-description", help="One-line description")
    parser.add_argument("--mod-authors", help="e.g. Your Name")
    parser.add_argument("--main-class", help='Java class name, e.g. "MyMod" - default: derived from --mod-name')
    parser.add_argument("--old-main-class", help="Override auto-detection of the current main class name")
    parser.add_argument("--yes", action="store_true", help="Skip the confirmation prompt")
    parser.add_argument("--dry-run", action="store_true", help="Print the plan; change nothing")
    args = parser.parse_args()

    if not GRADLE_PROPERTIES.exists():
        die(f"{GRADLE_PROPERTIES} not found - run this from the repository root")

    current = read_gradle_properties(GRADLE_PROPERTIES)
    old_id = current.get("modId", "examplemod")
    old_group = current.get("modGroup", "com.example")
    old_name = current.get("modName", "Example Mod")
    old_version = current.get("modVersion", "0.1.0")
    old_description = current.get("modDescription", "")
    old_authors = current.get("modAuthors", "")
    old_pkg_dotted = f"{old_group}.{old_id}"

    common_pkg_dir = package_dir(SOURCE_ROOTS[0], old_pkg_dotted)
    if not common_pkg_dir.is_dir():
        die(
            f"couldn't find {common_pkg_dir} - has this template already been "
            "renamed by hand? Pass --old-main-class / edit this script's assumptions if so."
        )
    old_main_class = args.old_main_class or find_main_class(common_pkg_dir)

    interactive = not (args.mod_id and args.mod_group and args.mod_name)
    if interactive:
        print("Renaming this template - press Enter to keep the current value.\n")
    new_id = args.mod_id or prompt("modId", old_id)
    new_group = args.mod_group or prompt("modGroup", old_group)
    new_name = args.mod_name or prompt("modName", old_name)
    new_version = args.mod_version or (prompt("modVersion", old_version) if interactive else old_version)
    new_description = args.mod_description or (
        prompt("modDescription", old_description) if interactive else old_description
    )
    new_authors = args.mod_authors or (prompt("modAuthors", old_authors) if interactive else old_authors)
    suggested_main_class = pascal_case(new_name)
    new_main_class = args.main_class or (
        prompt("Main class name", suggested_main_class) if interactive else suggested_main_class
    )

    if not re.fullmatch(r"[a-z][a-z0-9_-]*", new_id):
        die(f"modId {new_id!r} should be lowercase letters/digits/underscore/dash (Minecraft mod id rules)")
    if not re.fullmatch(r"[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)*", new_group):
        die(f"modGroup {new_group!r} should look like a Java package, e.g. com.mycompany")
    if not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*", new_main_class):
        die(f"main class name {new_main_class!r} isn't a valid Java identifier")

    new_pkg_dotted = f"{new_group}.{new_id}"

    # Longest/most-specific substrings first, so e.g. replacing the full dotted
    # package doesn't get clobbered by the shorter modGroup replacement after it.
    text_replacements = [
        (old_pkg_dotted, new_pkg_dotted),
        (old_group, new_group),
        (old_main_class, new_main_class),
        (old_id, new_id),
        (old_name, new_name),
    ]
    text_replacements = [(old, new) for old, new in text_replacements if old != new]

    # dir_moves: (old_dir, new_dir, source_root) - source_root is where pruning
    # empty leftover parent directories must stop.
    dir_moves: list[tuple[Path, Path, Path]] = []
    for root in SOURCE_ROOTS:
        old_dir = package_dir(root, old_pkg_dotted)
        if old_dir.is_dir() and old_dir != package_dir(root, new_pkg_dotted):
            dir_moves.append((old_dir, package_dir(root, new_pkg_dotted), root))

    # Planning-only preview (scans the *current*, pre-move locations) so the
    # printed plan can show real filenames before anything actually happens.
    planned_renames = 0
    if old_main_class != new_main_class:
        for old_dir, _, _ in dir_moves:
            planned_renames += len(collect_entry_point_files(old_dir, old_main_class))
    planned_java_files = sum(len(list(old_dir.rglob("*.java"))) for old_dir, _, _ in dir_moves)

    print("Plan:")
    print(f"  modId:          {old_id!r} -> {new_id!r}")
    print(f"  modGroup:       {old_group!r} -> {new_group!r}")
    print(f"  modName:        {old_name!r} -> {new_name!r}")
    print(f"  modVersion:     {old_version!r} -> {new_version!r}")
    print(f"  modDescription: {old_description!r} -> {new_description!r}")
    print(f"  modAuthors:     {old_authors!r} -> {new_authors!r}")
    print(f"  main class:     {old_main_class!r} -> {new_main_class!r}")
    print(f"  package:        {old_pkg_dotted!r} -> {new_pkg_dotted!r}")
    if dir_moves:
        print("  directories to move:")
        for old_dir, new_dir, _ in dir_moves:
            print(f"    {old_dir.relative_to(REPO_ROOT)} -> {new_dir.relative_to(REPO_ROOT)}")
    if planned_renames:
        print(f"  {planned_renames} entry-point file(s) to rename (e.g. {old_main_class}.java -> {new_main_class}.java)")
    print(f"  text replacements across {planned_java_files} java file(s) plus gradle.properties, "
          "settings.gradle.kts, and both fabric.mod.json files (fabric/ and datagen/)")

    if not dir_moves and not text_replacements:
        print("\nNothing to do - all values already match.")
        return

    if args.dry_run:
        print("\n--dry-run: no changes made.")
        return

    status = subprocess.run(["git", "status", "--porcelain"], cwd=REPO_ROOT, capture_output=True, text=True)
    if status.returncode == 0 and status.stdout.strip():
        print(
            "\nwarning: you have uncommitted changes - this makes them easier to review "
            "and undo (`git diff` / `git checkout .`) if anything looks wrong afterward."
        )

    if not args.yes:
        answer = input("\nProceed? [y/N]: ").strip().lower()
        if answer not in ("y", "yes"):
            print("Aborted - nothing changed.")
            return

    # 1. Move package directories first, then do everything else against their
    #    new location - avoids ever having to remap a pre-computed old path.
    for old_dir, new_dir, root in dir_moves:
        new_dir.parent.mkdir(parents=True, exist_ok=True)
        shutil.move(str(old_dir), str(new_dir))
        prune_empty_parents(old_dir.parent, stop_at=root)

    new_pkg_dirs = [new_dir for _, new_dir, _ in dir_moves]
    # Roots with no old package dir (e.g. forge/ before its client entrypoint
    # existed) still need to be checked in case the package already matched.
    new_pkg_dirs += [
        package_dir(root, new_pkg_dotted)
        for root in SOURCE_ROOTS
        if package_dir(root, new_pkg_dotted).is_dir() and package_dir(root, new_pkg_dotted) not in new_pkg_dirs
    ]

    # 2. Rename the entry-point class files at their new location.
    renamed = 0
    if old_main_class != new_main_class:
        for pkg_dir in new_pkg_dirs:
            for old_file in collect_entry_point_files(pkg_dir, old_main_class):
                new_file = old_file.with_name(new_main_class + old_file.name[len(old_main_class):])
                old_file.rename(new_file)
                renamed += 1

    # 3. Rewrite package/import/id/name references everywhere affected.
    text_files = [GRADLE_PROPERTIES, SETTINGS_GRADLE, FABRIC_MOD_JSON, DATAGEN_MOD_JSON]
    for pkg_dir in new_pkg_dirs:
        text_files.extend(pkg_dir.rglob("*.java"))

    changed = 0
    for path in text_files:
        if not path.exists():
            continue
        original = path.read_text(encoding="utf-8")
        updated = original
        for old, new in text_replacements:
            updated = updated.replace(old, new)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed += 1

    # 4. modVersion/modDescription/modAuthors don't correspond to any text
    #    token found elsewhere, so they're set directly rather than replaced.
    # (a plain list, not `any(generator)`, so all three calls actually run -
    # `any()` would short-circuit and skip later ones once it hits a True)
    property_results = [
        update_gradle_property(GRADLE_PROPERTIES, key, new_value)
        for key, new_value in (
            ("modVersion", new_version),
            ("modDescription", new_description),
            ("modAuthors", new_authors),
        )
    ]
    if any(property_results) and GRADLE_PROPERTIES not in text_files:
        changed += 1

    print(f"\nDone - moved {len(dir_moves)} director{'y' if len(dir_moves) == 1 else 'ies'}, "
          f"renamed {renamed} file(s), updated {changed} file(s).")
    print("\nStill worth doing by hand (see README.md's \"Using this template\"):")
    print("  - Pick a real license and add a LICENSE file (modLicense is still the ARR placeholder).")
    print("  - Replace README.md with one about your actual mod.")
    print("  - Check versions/<mcVersion>/gradle.properties for stale dependency numbers.")
    print("  - Re-run `./gradlew :datagen:runDatagen :datagen:copyGenerated` - the already-generated")
    print("    assets/data JSON in fabric/, forge/ and neoforge/'s resources still refers to the old")
    print("    mod id (this script only renames source files, not generated resources) - see")
    print("    README.md's \"Data generation\" section.")
    print("  - Run `./gradlew chiseledBuild --no-parallel` to confirm everything still builds.")


if __name__ == "__main__":
    main()
