# M3 compression workbench — public scratch candidate

**Status: EXPLORATORY / NOT A PRODUCTION OPTIMIZATION** (2026-10-30)

This Apache Commons Compress fork is a **public source experiment**, not an alternate Maven/OpenRewrite recipe authority or an M3JDK21 distribution. It retains the Commons Compress Java and archive/codec contracts and existing Apache-2.0/NOTICE obligations. Keep private Synexia recipes, source-sealed fixtures, memory and transformation tooling out of this public fork. **No production compressor has been modified by this candidate.**

## Frozen baseline

- Fork `hsoliwal/commons-compress` at `master@0c20dede0dfa566d7744583c1df384d43cc21bfa`; POM `1.29.0-SNAPSHOT`, Java 8 source level.
- Existing LZ77 owner `src/main/java/org/apache/commons/compress/compressors/lz77support/LZ77Compressor.java`, Git blob `2ed3f38a8e782987d07ac0a1715f7eb76ff8fdf9`.
- Candidate category: hash-chain longest match (greedy, strict better-wins); proposal: precheck the byte at current winning match length to reject candidates incapable of winning.
- Preserve the exact match winner, back-reference offsets/lengths, encoded bytes, callback ordering, lazy matching, prefill and sliding-window behavior. **Fewer byte comparisons are not a speedup claim.**

## Runnable candidate probe (no Maven dependencies)

From repository root, with any Java 8+ compiler:

```sh
mkdir -p target/m3-lz77-probe
javac --release 8 -d target/m3-lz77-probe \
  src/test/java/org/apache/commons/compress/compressors/lz77support/M3Lz77CandidateProbe.java
java -cp target/m3-lz77-probe \
  org.apache.commons.compress.compressors.lz77support.M3Lz77CandidateProbe
```

The self-contained probe runs 20,000 deterministic generated chain comparisons across incompressible, uniform, repeating and collision-like byte distributions. It requires identical winner length/start and records logical byte comparisons. A locally run Java 21 `javac --release 8` candidate-equivalent version passed all 20,000 cases: baseline 1,054,490 byte comparisons versus candidate 922,226. **The exact committed file has not yet passed repository Maven, Checkstyle, JUnit, or CI.** This diagnostic is not an interoperability oracle and does not establish compressed-byte parity or throughput.

## Benchmarks and acceptance requirements

1. Add a pinned baseline output corpus and matching encoder/decoder differential tests for empty/short/streamed/repetitive/random/adversarial data; preserve behavior for every flag and malformed-input case.
2. Run full `mvn` quality checks, then measure real throughput/ratio, CPU allocations/heap and latency on identical builds, including JDK 8 compatibility and non-default LZ77 parameters. Record warmup and variance; report any regression.
3. Refine and **execute** the canonical hash-pinned Maven/OpenRewrite recipe privately in `hsoliwal/com.synexia`, with stale-preimage refusal, fixed-point replay, source/postimage receipts and licence checks. This public workbench must never receive the recipe source.
4. In a separate reviewed M3JDK21 receiving PR, port only independently qualified source compatible with the precise target license and original API/ABI/JNI contracts; test with the OpenJDK build and jtreg plus installed-image execution. A Java LZ77 optimization does **not** automatically accelerate the JDK native zlib implementation.

**Promotion is held:** do not directly merge this scratch algorithm into `java.util.zip`, `libzip`, or GPLv2-only OpenJDK files. Reimplement algorithmic ideas separately when required by the target license; preserve origin and notice obligations for copied licensed expression.

No `java.util.zip` code has been changed. Future source optimizations belong in small, reviewable experimental commits, with serial per-file changes and proof before promotion.
