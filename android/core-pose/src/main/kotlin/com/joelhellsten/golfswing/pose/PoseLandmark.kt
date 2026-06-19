package com.joelhellsten.golfswing.pose

/**
 * Normalised 2D keypoint from MediaPipe Pose Landmarker.
 * x/y are in [0,1] relative to frame dimensions.
 * visibility is the per-landmark confidence score from MediaPipe.
 */
data class PoseLandmark(
    val x: Float,
    val y: Float,
    val visibility: Float,
)

/**
 * Indices matching MediaPipe Pose Landmarker's 33-landmark model.
 * Only the subset the fault engine needs.
 */
object LandmarkIndex {
    const val NOSE = 0
    const val LEFT_SHOULDER = 11
    const val RIGHT_SHOULDER = 12
    const val LEFT_HIP = 23
    const val RIGHT_HIP = 24
    const val LEFT_KNEE = 25
    const val RIGHT_KNEE = 26
    const val LEFT_ANKLE = 27
    const val RIGHT_ANKLE = 28
    const val LEFT_WRIST = 15
    const val RIGHT_WRIST = 16
}

/**
 * One frame's worth of landmarks, keyed by landmark index.
 */
data class PoseFrame(
    val frameIndex: Int,
    val timestampMs: Long,
    val landmarks: Map<Int, PoseLandmark>,
)
