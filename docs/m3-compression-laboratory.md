# M3 compression laboratory — Apache Commons Compress fork

**Status:** experiment contract only (2026-10-30). No performance gains, correctness checks, compiled source, or M3JDK21 promotion are claimed by this document.

**Baseline:** `hsoliwal/commons-compress` `master` @ `0c20dede0dfa566d7744583c1df384d43cc21bfa` (`1.29.0-SNAPSHOT` in `pom.xml`). **Experiment source** is this fork; **recipe and convergence owner** is the private Synexia workspace; **qualified output destination** is `hsoliwal/M3jdk21` (standalone Java 21 SDK).

## Non-negotiable contract

1. **Recipe first.** Before *any product implementation change*, define one bounded behavioral atom, capture original source SHA, public method/stream contract, exact inputs, failure semantics, tests, license/provenance, and benchmark hypotheses. Implement the change via a versioned Maven/OpenRewrite recipe in Synexia; replay the same recipe against a pinned fork commit; require an exact postimage and fixed-point rerun. Never hand-edit a codec file as a substitute for fixing the recipe.
2. Existing public Commons Compress Java APIs, header formats, framing, dictionaries, checksums, byte order, stream finish/flush/close behavior, `available`/`skip`/partial reads, exceptions, resource-limits, class names, package names and usage patterns remain compatible. A non-standard M3 format must have a **distinct format identifier** and cannot masquerade as Snappy/LZ4/DEFLATE/Zstd or ZIP.
3. **Java 21 oracle first.** Preserve a portable Java fallback. JNI is opt-in and only for demonstrably beneficial coarse-grained batches; use stable common ABI, bounded native arenas, verified length/offset multiplication, array-pointer release, no retained JVM pointers, controlled native failure fallback and no LLM inside any hot path.
4. Decompression is security-sensitive: check overflow and malicious length headers **before allocation**, prevent unbounded expansion/recursive archive processing, reject invalid offsets/truncation, and test crafted ZIP bombs and corrupt CRCs. Track per-entry as well as total expanded limits.
5. Fork branches/PRs carry *reproducible experimental output* and tests. Private Synexia carries recipes, convergence catalogues, experimentation and source seal receipts. Public M3JDK21 carries only qualified implementation/test output, module metadata, attribution, release gates and exact provenance references. Do not publish private source or private PR URLs through this public repository.
6. Use LeetCode, HackerRank, GeeksforGeeks as **problem category indexes and evaluation prompts**, not as blanket permission to copy solution code. Review license, origin, patents if applicable, algorithm specification and independently measured correctness for every donor.

## Research candidates and test order

| Phase | Codec surface | Replaceable internal atoms | Why |
| --- | --- | --- | --- |
| 0 | Snappy raw + framed, LZ4 block + frame | Independent oracle and format-specific corpus | High-throughput, bounded, byte-oriented first slices |
| 1 | Shared LZ77 mechanics | Primitive generation-stamped candidate index, byte common-prefix, overlap-correct match copy, literal packing | Common geometry without assuming cross-format compatible bits |
| 2 | Snappy/LZ4 implementation | Bounded output byte sink, match/literal selection; compare legacy match index and optimized index | Predictable throughput/ratio trade-offs |
| 3 | gzip/DEFLATE and ZIP | CRC32/CRC32C, buffered read/write, ring/window mechanics, exact entry metadata | JDK-facing compatibility and checksum control |
| 4 | Zstd, XZ, bzip2, 7z | Only format-specific isolated atoms that pass baseline and licensing review | Wider support after smaller kernels qualify |
| 5 | Optional nonstandard M3 format | Deterministic shared lexicon IDs, exact chunk reuse, indexed dictionary, pipeline stages | New opt-in format, never silently change standard formats |

The Java and JNI alternatives must independently preserve **bitstream validity** and original bytes when decoded by reference libraries. Raw Snappy, framed Snappy, raw LZ4, LZ4 frame and ZIP container are different protocols; compare only like with like. Identical compressed bytes are not always required across encoders, but decoded bytes, headers, checksums, framing and documented errors must match the relevant specification.

### Source-and-test workflow

1. Freeze fork SHA and Commons Compress public API signatures; run unchanged Maven test suite and save environment, commands, logs and result.
2. Create a category-specific deterministic corpus: length 0/1/2; 4 KiB, 64 KiB, 1 MiB and larger streaming cases; random/incompressible, binary, UTF-8, repetitive, boundary-overlap, long match, hostile/truncated, malformed varint, bad length/offset, invalid checksum, partial reads and archive nesting. Include resource-bound tests.
3. Independently encode/decode with Commons Compress, reference native Snappy/LZ4/Zlib as appropriate, and the candidate; test both codec directions and validate bytes, length, EOF and exception categories. Store corpus seed, source SHA, codec config, reference version, checksums, JUnit reports.
4. Generate each product file mutation from the Maven/OpenRewrite recipe; one owner per file, same API behavior, bounded changes. Test recipe refusal on unexpected source changes and idempotence on repeat. Never modify a public interface without a separately approved compatibility design.
5. Record JMH throughput (compress/decompress MB/s), ratio, allocation bytes/op, tail latency, peak memory, warmed/cold startup and JNI crossover thresholds on Java 21 for multiple CPUs/OSes. Compare performance with identical data, JVM options, input sizes and warmup. Show full distribution and regression cases rather than only best result.
6. Pass SAST/security malformed tests, deterministic parallel concurrency, JDK21 compilation, Maven/JUnit, native sanitizer/CTest and `-Xcheck:jni` when using native backend; record CI artifacts and license/NOTICE requirements.
7. Create **separate promotion PR** in M3JDK21 only after oracle and benchmark admission. Start with a non-exported/internal component; retain existing `java.util.zip` behavior unless the full JDK test suite and protocol compatibility matrix pass. Promote JNI only after proving fallback safety and target CPU benefit.

### Experiment PR receipt template

- Baseline SHA / target file / original SHA / recipe ID + recipe SHA / resulting SHA / fixed-point repeat.
- Atom and invariants: API descriptors, exception behavior, input/output/format specification, max output and allocated memory, cancellation/ProgressMonitor strategy.
- Donor evidence: name, version/commit, license, exact permitted use, challenge *problem category* only if applicable.
- Reproduced commands: Maven, JUnit, differential native verification, JNI `-Xcheck:jni`, fuzz/corrupt corpus, JMH and architecture/CPU/JDK.
- Metrics table: reference vs candidate median/p95, memory, ratio, failure regression and JNI threshold.
- Admission decision: **experimental / qualified for fork / qualified for JDK promotion / rejected**, with rationale and provenance.

### Promotion boundaries

`hsoliwal/M3jdk21` is an integrated standalone Java 21 SDK, not `M3 + an extra JDK`. Use recipe-driven package/module normalization and qualified internal codec implementations. Keep `Deflater`, `Inflater`, `GZIPInputStream`, `ZipFile`, `ZipInputStream`, JAR signing, multi-release JAR behavior and platform-native fallbacks intact. Any material performance improvement does **not** justify changing a standardized wire format or compromising decompression safety.

## Next concrete PR

Build **baseline-only Snappy raw and LZ4 block differential JUnit corpus** in this fork via the Synexia recipe, preserving all production code. Capture current `mvn test` baseline before introducing candidate kernels. Follow with Snappy candidates, then LZ4 candidates, then framing and ZIP/DEFLATE once parity is demonstrated.

Apache Commons Compress is Apache-2.0; retain its source headers, LICENSE/NOTICE and any required downstream notices. Optional third-party codec dependencies retain their own license restrictions and version provenance.
