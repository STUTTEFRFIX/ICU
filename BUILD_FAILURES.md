# Build failures

This file exists so the `Build` workflow can append a one line note when the
compiler fails on the runner. It is a safety net: the maintainer's own machine
cannot finish a NeoForge decompile, so a build failure would otherwise only be
visible to someone who opens the Actions tab.

The workflow skips appending when the current commit already carries a note for
the same SHA, which keeps it from looping.

No entries means the last build succeeded.
