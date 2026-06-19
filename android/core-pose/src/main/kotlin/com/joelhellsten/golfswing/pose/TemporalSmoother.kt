package com.joelhellsten.golfswing.pose

/**
 * Simple moving-average smoother over a PoseTimeline.
 * Reduces keypoint jitter before the fault engine computes angles and displacements.
 * windowSize of 3-5 frames is enough for standard slow-mo video.
 */
class TemporalSmoother(private val windowSize: Int = 3) {

    fun smooth(timeline: PoseTimeline): PoseTimeline {
        val smoothed = timeline.frames.mapIndexed { i, frame ->
            val windowStart = maxOf(0, i - windowSize / 2)
            val windowEnd = minOf(timeline.frames.size - 1, i + windowSize / 2)
            val window = timeline.frames.subList(windowStart, windowEnd + 1)
            val smoothedLandmarks = frame.landmarks.mapValues { (idx, _) ->
                val visible = window.mapNotNull { it.landmarks[idx] }
                if (visible.isEmpty()) return@mapValues frame.landmarks[idx]!!
                PoseLandmark(
                    x = visible.map { it.x }.average().toFloat(),
                    y = visible.map { it.y }.average().toFloat(),
                    visibility = visible.map { it.visibility }.average().toFloat(),
                )
            }
            frame.copy(landmarks = smoothedLandmarks)
        }
        return timeline.copy(frames = smoothed)
    }
}
