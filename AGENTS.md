<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

     https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# AGENTS.md

## Priority 0 — additive API superset, historical loss checks and last-checked anchors (2026-11-18)

Before source promotion and merges, preserve all applicable accepted upstream plus fork capabilities as one optimized **backward-compatible superset** (public API/ABI, expected behavior, fixes, tests, serializations, ordering, concurrency, native/resource contracts and prior measured optimizations). Compare and select the best *proven* algorithm from the incumbent, donors, old branches and PRs using correctness tests and performance/memory measurements; add APIs without silently discarding older features. Review Git history **from earliest reachable ancestors to all relevant heads**, old/merged/closed/reverted PRs, tags, merge bases, both parents and changes to deleted/renamed code to find bad commits, source loss and abandoned optimizations. Never rebase `develop`. Record last-checked date, ref/head SHA, scope, root coverage, PR/branch coverage, last full-verified anchor, loss findings, and PENDING/PARTIAL/VERIFIED/HOLD in canonical Synexia history ledger. HEAD observation does not certify an audit. Use existing Synexia Maven/OpenRewrite source-sealed recipes and target proof gates, respecting each repo's role. Default NEW separable Synexia-authored code to Apache-2.0 where permitted; retain all original upstream/donor licenses, NOTICE and attribution. Abstract algorithms are generally reusable ideas, but LLM translation/patternization does not itself relicense code. See [canonical Synexia PR #18764](https://github.com/hsoliwal/com.synexia/pull/18764) and its explicitly PENDING 236-repo registry.


Guidance for AI coding agents working in this repository.

The actual AGENTS.md file is [AGENTS.md](https://raw.githubusercontent.com/apache/commons-parent/refs/heads/master/AGENTS.md)
