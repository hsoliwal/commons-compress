/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.compressors.lz77support;

import java.util.Arrays;
import java.util.Random;

/**
 * Candidate-only Java 8 laboratory for a fast reject during LZ77 hash-chain
 * traversal. This is not a production compressor, format change or JDK port.
 *
 * <p>Compares the original LZ77 candidate scan against an extra byte gate
 * that rejects a candidate only when it cannot beat the current longest
 * match. A mismatch in match length or winning start always throws.</p>
 */
public final class M3Lz77CandidateProbe {
    private static final int NO_MATCH = -1;
    private static final int CASES = 20000;

    private M3Lz77CandidateProbe() {
    }

    private static final class Result {
        final int length;
        final int start;
        final long comparisons;

        Result(final int length, final int start, final long comparisons) {
            this.length = length;
            this.start = start;
            this.comparisons = comparisons;
        }
    }

    private static Result scan(final byte[] window, final int[] prev,
            final int currentPosition, final int lookahead,
            final int minLength, final int maxBackReferenceLength,
            final int maxOffset, final int niceBackReferenceLength,
            final int maxCandidates, final int startHead,
            final boolean probe) {
        int bestLength = minLength - 1;
        int bestStart = NO_MATCH;
        final int maxPossibleLength = Math.min(maxBackReferenceLength, lookahead);
        final int minIndex = Math.max(0, currentPosition - maxOffset);
        final int nice = Math.min(maxPossibleLength, niceBackReferenceLength);
        final int wMask = prev.length - 1;
        int matchHead = startHead;
        long comparisons = 0;
        for (int candidates = 0; candidates < maxCandidates && matchHead >= minIndex; candidates++) {
            int currentLength = 0;
            boolean scanCandidate = true;
            if (probe && bestLength < maxPossibleLength) {
                // A node cannot BEAT the winner if this particular byte differs.
                comparisons++;
                scanCandidate = window[matchHead + bestLength]
                        == window[currentPosition + bestLength];
            }
            if (scanCandidate) {
                for (int i = 0; i < maxPossibleLength; i++) {
                    comparisons++;
                    if (window[matchHead + i] != window[currentPosition + i]) {
                        break;
                    }
                    currentLength++;
                }
            }
            if (currentLength > bestLength) {
                bestLength = currentLength;
                bestStart = matchHead;
                if (currentLength >= nice) {
                    break;
                }
            }
            matchHead = prev[matchHead & wMask];
        }
        return new Result(bestLength, bestStart, comparisons);
    }

    private static long[] verify(final int id) {
        final Random random = new Random(0x4d334c5aL + (long) id * 2654435761L);
        final int windowSize = 1 << (4 + random.nextInt(9));
        final byte[] window = new byte[windowSize * 2];
        final int pattern = id % 7;
        for (int i = 0; i < window.length; i++) {
            switch (pattern) {
                case 0: window[i] = (byte) random.nextInt(256); break;
                case 1: window[i] = 0; break;
                case 2: window[i] = (byte) (i % 3); break;
                case 3: window[i] = (byte) (i % 7); break;
                case 4: window[i] = (byte) (i % 17); break;
                case 5: window[i] = (byte) ((i % 8 == 0) ? random.nextInt(256) : 'A'); break;
                default: window[i] = (byte) ((i % 3 == 2) ? random.nextInt(256) : (65 + (i % 3))); break;
            }
        }
        final int currentPosition = windowSize + random.nextInt(windowSize / 2);
        final int maxLookahead = Math.min(256, window.length - currentPosition);
        final int lookahead = 3 + random.nextInt(Math.max(1, maxLookahead - 2));
        final int minLength = 3;
        final int maxBackReferenceLength = Math.max(minLength, random.nextInt(maxLookahead + 1));
        final int maxOffset = 1 + random.nextInt(windowSize - 1);
        final int niceLength = 3 + random.nextInt(256);
        final int maxCandidates = 1 + random.nextInt(128);
        final int[] prev = new int[windowSize];
        Arrays.fill(prev, NO_MATCH);
        final int minIndex = Math.max(0, currentPosition - maxOffset);
        final int step = 1 + random.nextInt(7);
        final int firstHead = currentPosition - (1 + random.nextInt(3));
        for (int pos = firstHead; pos >= minIndex; pos -= step) {
            final int next = pos - step;
            prev[pos & (windowSize - 1)] = next >= minIndex ? next : NO_MATCH;
        }
        final Result original = scan(window, prev, currentPosition, lookahead,
                minLength, maxBackReferenceLength, maxOffset, niceLength,
                maxCandidates, firstHead, false);
        final Result optimized = scan(window, prev, currentPosition, lookahead,
                minLength, maxBackReferenceLength, maxOffset, niceLength,
                maxCandidates, firstHead, true);
        if (original.length != optimized.length || original.start != optimized.start) {
            throw new AssertionError("case " + id + ": original=[" + original.length + "," + original.start
                    + "] candidate=[" + optimized.length + "," + optimized.start + "]");
        }
        return new long[] { original.comparisons, optimized.comparisons };
    }

    public static void main(final String[] args) {
        long baselineComparisons = 0;
        long candidateComparisons = 0;
        for (int i = 0; i < CASES; i++) {
            final long[] comparisonCounts = verify(i);
            baselineComparisons += comparisonCounts[0];
            candidateComparisons += comparisonCounts[1];
        }
        System.out.println("PASS " + CASES + " deterministic candidate-chain cases (candidate only; no Maven/JNI/JDK gate)");
        System.out.println("Examined bytes: baseline=" + baselineComparisons + ", candidate=" + candidateComparisons);
    }
}
