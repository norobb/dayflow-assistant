package com.dayflow.app.integrations.accessibility.control

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Pure, Android-free motion planner (AURA's HumanizedMotion pattern, re-implemented).
 *
 * Turns gesture endpoints into touch geometry: taps carry a hair of positional
 * jitter, swipes follow a gentle quadratic curve with an ease-in-out velocity
 * profile. This makes injected input register the way real touches do — a
 * dead-straight constant-velocity line is both a "bot" signature and a cause
 * of misfired fling detection in apps with velocity-based gesture detectors.
 *
 * Randomness is bounded and seedable; it exists to make motion register
 * naturally, not to defeat any app's analytics.
 */
object HumanizedMotion {

    /** Max positional jitter, in pixels, applied to tap / long-press points. */
    const val JITTER_PX = 2

    /** Max lateral deviation, in pixels, of a swipe from its straight path. */
    const val CURVE_DEVIATION_PX = 14

    /** Number of intermediate points along a swipe path. */
    const val SWIPE_STEPS = 8

    data class Point(val x: Float, val y: Float)

    /** Jittered tap / long-press point. */
    fun tapPoint(x: Float, y: Float, random: Random = Random.Default): Point {
        val jitterX = random.nextInt(-JITTER_PX, JITTER_PX + 1)
        val jitterY = random.nextInt(-JITTER_PX, JITTER_PX + 1)
        return Point(x + jitterX, y + jitterY)
    }

    /**
     * Curved swipe path from (x1,y1) to (x2,y2). Returns [SWIPE_STEPS] + 2
     * points including both endpoints. The curve bows perpendicular to the
     * direction of travel with ease-in-out progress so velocity starts and
     * ends slow, like a real finger.
     */
    fun swipePath(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        random: Random = Random.Default
    ): List<Point> {
        val directionX = x2 - x1
        val directionY = y2 - y1
        val length = kotlin.math.hypot(directionX.toDouble(), directionY.toDouble()).toFloat()
        if (length < 1f) return listOf(Point(x1, y1), Point(x2, y2))

        // Unit perpendicular (left of travel direction).
        val perpX = -directionY / length
        val perpY = directionX / length

        // Random signed bow; flips the curve side and scales within the cap.
        val bow = random.nextInt(-CURVE_DEVIATION_PX, CURVE_DEVIATION_PX + 1).toFloat()

        val points = mutableListOf<Point>()
        for (i in 0..SWIPE_STEPS + 1) {
            val t = i.toFloat() / (SWIPE_STEPS + 1).toFloat()
            // Ease-in-out so touch velocity starts and ends slow.
            val eased = t * t * (3f - 2f * t)
            val bx = x1 + directionX * eased + perpX * bow * sin(t * Math.PI.toFloat())
            val by = y1 + directionY * eased + perpY * bow * cos(t * Math.PI.toFloat())
            points += Point(bx, by)
        }
        return points
    }

    /**
     * Segments (endPoint, durationMs) the OS should replay in order; the
     * caller converts these into GestureDescription strokes.
     *
     * The pace is deliberately modest (~500 ms): too-fast injected swipes are
     * read by the system as flings — or worse, as navigation gestures (an
     * upward fling can dismiss the app to the launcher), the exact misfire
     * class AURA documents for humanized motion.
     */
    fun swipeSegments(path: List<Point>): List<Pair<Point, Long>> {
        if (path.size < 2) return emptyList()
        val baseDuration = 500L
        val segmentDuration = (baseDuration / (path.size - 1)).coerceAtLeast(10)
        return path.drop(1).map { Pair(it, segmentDuration) }
    }
}
