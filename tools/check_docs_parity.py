#!/usr/bin/env python3
"""Keep every ICU English document in lock-step with its Chinese original.

Why this exists
---------------
ICU ships every document in two languages: a Chinese original (``X.md``) and a
1:1 English translation (``X.en.md``). A human can update one side and forget the
other, so the two languages silently drift apart. This script catches that drift
before it reaches a release by asserting that each pair has the same document
skeleton:

1. **heading count**   - lines matching ``^#{1,6}\\s``
2. **code fence count** - lines starting with ```` ``` ```` (must be even, i.e.
   every opened block is closed)
3. **table row count** - lines starting with ``|``
4. **version multiset** - every ``1.2.3``-shaped token, compared as a multiset

Heading and cell *text* is never compared: it is expected to differ between
languages. Only counts and the version strings (which TERMINOLOGY.md declares
byte-for-byte identical) must match.

Each pair must also carry the language-switch link near the top: the Chinese
side links to ``X.en.md`` and the English side links back to ``X.md``.

Usage::

    python tools/check_docs_parity.py            # check the repository
    python tools/check_docs_parity.py --root DIR # check some other tree

Exit code is 0 when every pair passes, 1 otherwise. Pure standard library; the
same script runs under ``python`` on Windows and ``python3`` on ubuntu-latest.
"""

from __future__ import annotations

import argparse
import re
import sys
from collections import Counter
from pathlib import Path

# Chinese documents that deliberately have no separate English file, keyed by
# their path relative to the repository root.
# - BUILD_FAILURES.md is appended by the CI build step and stays single-language.
# - docs/TERMINOLOGY.md is the bilingual glossary: Chinese and English already
#   live side by side in the one file, so a translation would be redundant.
EXEMPT_FILES = {
    "BUILD_FAILURES.md",
    "docs/TERMINOLOGY.md",
}

# A Markdown ATX heading: one to six '#' followed by a space (H1..H6).
HEADING_RE = re.compile(r"^#{1,6}\s")
# A fenced code block starts and ends with a line of three backticks.
FENCE_PREFIX = "```"
# A Markdown table row always begins with a pipe.
TABLE_PREFIX = "|"
# Version-like tokens ("1.21.1", "0.2.2", "21.1.252"). Numbers are declared
# byte-for-byte identical across both languages, so the multisets must match.
VERSION_RE = re.compile(r"\b\d+\.\d+\.\d+\b")
# The language-switch link must sit at the very top, before the first heading.
LINK_PREFIX_LINES = 6

_EN_SUFFIX = ".en.md"


def relpath(root: Path, path: Path) -> str:
    """Path relative to the repository root, always forward slashes."""
    return path.relative_to(root).as_posix()


def count_lines(text: str, predicate) -> int:
    return sum(1 for line in text.splitlines() if predicate(line))


def read_utf8(path: Path) -> str:
    """Read a file as UTF-8. Explicit, because Windows defaults to gbk."""
    return path.read_text(encoding="utf-8")


def english_to_chinese(path: Path) -> Path:
    """``X.en.md`` -> ``X.md`` in the same directory."""
    return path.with_name(path.name[: -len(_EN_SUFFIX)] + ".md")


def chinese_to_english(path: Path) -> Path:
    """``X.md`` -> ``X.en.md`` in the same directory."""
    return path.with_name(path.name[: -len(".md")] + _EN_SUFFIX)


def check_pair(root: Path, cn_path: Path, en_path: Path) -> tuple[str, list[str]]:
    """Compare one Chinese/English pair; return (ok_line, failures)."""
    cn_name = cn_path.name
    en_name = en_path.name
    en_rel = relpath(root, en_path)

    try:
        cn_text = read_utf8(cn_path)
    except OSError as exc:
        return "", [f"FAIL {en_rel} read {cn_name}: {exc}"]
    try:
        en_text = read_utf8(en_path)
    except OSError as exc:
        return "", [f"FAIL {en_rel} read {en_name}: {exc}"]

    cn_lines = cn_text.splitlines()
    en_lines = en_text.splitlines()

    head_cn = count_lines(cn_text, HEADING_RE.match)
    head_en = count_lines(en_text, HEADING_RE.match)
    fence_cn = count_lines(cn_text, lambda line: line.startswith(FENCE_PREFIX))
    fence_en = count_lines(en_text, lambda line: line.startswith(FENCE_PREFIX))
    table_cn = count_lines(cn_text, lambda line: line.startswith(TABLE_PREFIX))
    table_en = count_lines(en_text, lambda line: line.startswith(TABLE_PREFIX))
    ver_cn = sorted(VERSION_RE.findall(cn_text))
    ver_en = sorted(VERSION_RE.findall(en_text))

    failures: list[str] = []

    # Language-switch link: Chinese side points at the English file and vice
    # versa, both within the first few lines (before the first heading).
    cn_head = "\n".join(cn_lines[:LINK_PREFIX_LINES])
    en_head = "\n".join(en_lines[:LINK_PREFIX_LINES])
    if f"]({en_name})" not in cn_head:
        failures.append(f"FAIL {en_rel} link cn: missing ]({en_name}) in first {LINK_PREFIX_LINES} lines")
    if f"]({cn_name})" not in en_head:
        failures.append(f"FAIL {en_rel} link en: missing ]({cn_name}) in first {LINK_PREFIX_LINES} lines")

    if head_cn != head_en:
        failures.append(f"FAIL {en_rel} head cn={head_cn} en={head_en}")
    if fence_cn != fence_en or fence_cn % 2 != 0 or fence_en % 2 != 0:
        failures.append(f"FAIL {en_rel} fence cn={fence_cn} en={fence_en}")
    if table_cn != table_en:
        failures.append(f"FAIL {en_rel} table cn={table_cn} en={table_en}")
    if ver_cn != ver_en:
        only_cn = sorted((Counter(ver_cn) - Counter(ver_en)).elements())
        only_en = sorted((Counter(ver_en) - Counter(ver_cn)).elements())
        failures.append(f"FAIL {en_rel} versions cn={len(ver_cn)} en={len(ver_en)}")
        if only_cn:
            failures.append(f"  only in cn: {only_cn}")
        if only_en:
            failures.append(f"  only in en: {only_en}")

    ok_line = f"head={head_en} fence={fence_en} table={table_en} versions={len(ver_en)}"
    return ok_line, failures


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Check that every ICU English document matches its Chinese original.",
    )
    parser.add_argument(
        "--root",
        default=None,
        help="directory tree to scan (default: repository root, the parent of the "
        "script's own directory)",
    )
    args = parser.parse_args()

    if args.root:
        root = Path(args.root).resolve()
    else:
        # tools/check_docs_parity.py -> tools/ -> repository root.
        root = Path(__file__).resolve().parent.parent

    if not root.is_dir():
        print(f"root directory not found: {root}", file=sys.stderr)
        return 1

    # English side: every *.en.md anywhere in the tree. Its Chinese partner is
    # the same file name with ".en" removed, in the same directory.
    en_files = sorted(root.rglob("*" + _EN_SUFFIX))

    # Chinese side: every .md that is not an English file, in the repository
    # root and directly under docs/. Any such file must have an English partner
    # unless it is on the exemption list.
    cn_candidates: list[Path] = []
    for directory in (root, root / "docs"):
        if directory.is_dir():
            cn_candidates.extend(sorted(directory.glob("*.md")))
    cn_candidates = [p for p in cn_candidates if not p.name.endswith(_EN_SUFFIX)]

    errors: list[str] = []

    # Build pairs from the English side; a missing Chinese partner is an error.
    pairs: list[tuple[Path, Path]] = []
    for en_path in en_files:
        cn_path = english_to_chinese(en_path)
        if not cn_path.is_file():
            errors.append(
                f"FAIL {relpath(root, en_path)} missing Chinese counterpart: {relpath(root, cn_path)}"
            )
            continue
        pairs.append((cn_path, en_path))

    # Reverse direction: a Chinese file without an English partner is drift too.
    for cn_path in cn_candidates:
        cn_rel = relpath(root, cn_path)
        if cn_rel in EXEMPT_FILES:
            continue
        en_path = chinese_to_english(cn_path)
        if not en_path.is_file():
            errors.append(
                f"FAIL {cn_rel} missing English counterpart: {relpath(root, en_path)}"
            )

    ok_count = 0
    for cn_path, en_path in pairs:
        ok_line, failures = check_pair(root, cn_path, en_path)
        if failures:
            errors.extend(failures)
        else:
            ok_count += 1
            print(f"OK {relpath(root, en_path)} {ok_line}")

    for error in errors:
        print(error, file=sys.stderr)

    print(f"{ok_count}/{len(pairs)} pairs OK")
    return 0 if not errors and ok_count == len(pairs) else 1


if __name__ == "__main__":
    sys.exit(main())
