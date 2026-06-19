package com.joelhellsten.golfswing.pose

/**
 * The full keypoint timeline for a swing clip, after smoothing.
 * Frames are in ascending order by frameIndex.
 */
data class PoseTimeline(
    val frames: List<PoseFrame>,
    val videoWidthPx: Int,
    val videoHeightPx: Int,
) {
    fun frameAt(index: Int): PoseFrame? = frames.getOrNull(index)

    fun landmarkAt(frameIndex: Int, landmarkIdx: Int): PoseLandmark? =
        frames.getOrNull(frameIndex)?.landmarks?.get(landmarkIdx)
}
