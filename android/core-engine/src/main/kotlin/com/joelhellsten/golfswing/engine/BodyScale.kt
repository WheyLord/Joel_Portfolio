package com.joelhellsten.golfswing.engine

import com.joelhellsten.golfswing.pose.LandmarkIndex
import com.joelhellsten.golfswing.pose.PoseFrame
import kotlin.math.sqrt

/**
 * Computes the body scale unit S = vertical distance from hip midpoint to shoulder midpoint
 * at P1 (address). All fault thresholds are expressed as multiples of S.
 */
object BodyScale {

    fun compute(p1Frame: PoseFrame): Float? {
        val lh = p1Frame.landmarks[LandmarkIndex.LEFT_HIP] ?: return null
        val rh = p1Frame.landmarks[LandmarkIndex.RIGHT_HIP] ?: return null
        val ls = p1Frame.landmarks[LandmarkIndex.LEFT_SHOULDER] ?: return null
        val rs = p1Frame.landmarks[LandmarkIndex.RIGHT_SHOULDER] ?: return null

        if (listOf(lh, rh, ls, rs).any { it.visibility < CONFIDENCE_FLOOR }) return null

        val hipMidY = (lh.y + rh.y) / 2f
        val shoulderMidY = (ls.y + rs.y) / 2f
        return kotlin.math.abs(shoulderMidY - hipMidY)
    }
}

fun dist2d(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    return sqrt(dx * dx + dy * dy)
}
