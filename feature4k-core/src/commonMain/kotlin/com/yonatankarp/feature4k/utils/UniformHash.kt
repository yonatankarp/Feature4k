package com.yonatankarp.feature4k.utils

import kotlin.math.abs

/**
 * Provides uniformly distributed hash values from string inputs.
 *
 * Standard `String.hashCode()` can produce non-uniform distributions for certain input patterns,
 * particularly sequential strings. This object applies bit-mixing to spread hash values evenly
 * across the range [0.0, 1.0), ensuring that similar inputs don't cluster together.
 *
 * ## When to Use
 *
 * Use this when you need:
 * - Uniform distribution of hash values across a range
 * - Deterministic hashing (same input always produces same output)
 * - Better distribution than raw `hashCode()` for patterned inputs
 *
 * Common applications include percentage-based sampling, load distribution,
 * bucketing, and deterministic random selection.
 *
 * ## How It Works
 *
 * Applies bit-mixing based on MurmurHash finalization to break clustering patterns
 * in the input hash codes, then normalizes to [0.0, 1.0).
 *
 * @author Yonatan Karp-Rudin
 */
object UniformHash {
    /**
     * Computes a uniformly distributed hash value in the range [0.0, 1.0).
     *
     * The same input always produces the same output (deterministic), but the outputs
     * are spread evenly across the range regardless of input patterns.
     *
     * @param input The string to hash
     * @return A normalized value in [0.0, 1.0) with uniform distribution
     */
    operator fun invoke(input: String): Double {
        val mixed = abs(mixBits(input.hashCode()))
        return mixed.toDouble() / Int.MAX_VALUE.toDouble()
    }

    /*
     * Applies bit-mixing to spread hash bits evenly across the full Int range.
     *
     * Uses MurmurHash3 finalization step with constants chosen for good avalanche properties.
     */
    private fun mixBits(hash: Int): Int {
        var h = hash
        h = h xor (h ushr 16)
        h = (h * 0x85ebca6b).toInt()
        h = h xor (h ushr 13)
        h = (h * 0xc2b2ae35).toInt()
        h = h xor (h ushr 16)
        return h
    }
}
