#!/usr/bin/env bash
# Compares the MiniC++ interpreter with g++ on all test programs.
#
# Each program in interpreter/src/test/resources/programs is
#   1. run by the interpreter,
#   2. translated to standard C++ with 'minicpp to-cpp', compiled with g++ and run,
# and both outputs and exit codes are compared with the .expected file and the
# '// expect-exit: N' marker (default 0).
#
# usage: scripts/compare-gcc.sh [program.cpp ...]
set -u

root="$(cd "$(dirname "$0")/.." && pwd)"
minicpp="$root/interpreter/build/install/minicpp/bin/minicpp"
cxx="${CXX:-g++}"

if ! command -v "$cxx" > /dev/null; then
  echo "error: $cxx not found (set CXX to use another compiler)" >&2
  exit 2
fi
if [ ! -x "$minicpp" ]; then
  (cd "$root" && bash gradlew -q :interpreter:installDist) || exit 2
fi

if [ $# -gt 0 ]; then
  programs=("$@")
else
  programs=("$root"/interpreter/src/test/resources/programs/*.cpp)
fi

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
passed=0
failed=0

for src in "${programs[@]}"; do
  name="$(basename "$src" .cpp)"
  expected="${src%.cpp}.expected"
  expected_exit="$(sed -n 's|.*//[[:space:]]*expect-exit:[[:space:]]*\(-\{0,1\}[0-9]\{1,\}\).*|\1|p' "$src" | head -n 1)"
  expected_exit="${expected_exit:-0}"
  problems=""

  "$minicpp" run "$src" > "$work/$name.interp.out" 2> "$work/$name.interp.err"
  interp_exit=$?
  if ! diff -q <(tr -d '\r' < "$expected") <(tr -d '\r' < "$work/$name.interp.out") > /dev/null; then
    problems+=" interpreter-output"
  fi
  [ "$interp_exit" -eq "$expected_exit" ] || problems+=" interpreter-exit($interp_exit)"

  if "$minicpp" to-cpp "$src" > "$work/$name.gen.cpp" 2> "$work/$name.export.err" \
      && "$cxx" -std=c++17 -fwrapv -w -O1 -o "$work/$name.bin" "$work/$name.gen.cpp" 2> "$work/$name.cxx.err"; then
    "$work/$name.bin" > "$work/$name.gcc.out"
    gcc_exit=$?
    if ! diff -q <(tr -d '\r' < "$expected") <(tr -d '\r' < "$work/$name.gcc.out") > /dev/null; then
      problems+=" gcc-output"
    fi
    [ "$gcc_exit" -eq "$expected_exit" ] || problems+=" gcc-exit($gcc_exit)"
  else
    problems+=" gcc-compile"
    cat "$work/$name.export.err" "$work/$name.cxx.err" >&2
  fi

  if [ -z "$problems" ]; then
    echo "PASS  $name"
    passed=$((passed + 1))
  else
    echo "FAIL  $name:$problems"
    for side in interp gcc; do
      if [ -f "$work/$name.$side.out" ]; then
        diff <(tr -d '\r' < "$expected") <(tr -d '\r' < "$work/$name.$side.out") | sed "s/^/      [$side] /"
      fi
    done
    failed=$((failed + 1))
  fi
done

echo "$passed passed, $failed failed"
[ "$failed" -eq 0 ]
