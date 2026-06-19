package com.joelhellsten.golfswing.engine

import com.joelhellsten.golfswing.pose.LandmarkIndex
import com.joelhellsten.golfswing.pose.PoseTimeline

/**
 * Detects early extension from a DTL view.
 *
 * Rule (from spec): pelvis midpoint moves toward the ball-target line (toward the camera
 * in DTL = decreasing normalised X in a standard DTL setup) by more than 0.10*S between
 * P4 and P7. Severity scales with the excess past the threshold.
 *
 * DTL assumption: golfer faces left, so smaller normalised X = closer to camera.
 * Mirror if golfer faces right (lefty filmed mirrored).
 */
object EarlyExtensionDetector {

    private const val FAULT_ID = "early_extension"
    private const val THRESHOLD_FACTOR = 0.10f

    fun detect(
        timeline: PoseTimeline,
        scaleS: Float,
        pPositions: Map<Int, Int>,  // P-number -> frame index
    ): FaultRecord {
        val p4Frame = pPositions[4]?.let { timeline.frameAt(it) }
        val p7Frame = pPositions[7]?.let { timeline.frameAt(it) }

        if (p4Frame == null || p7Frame == null) {
            return notDetectable("P4 or P7 frame not found")
        }

        val lhP4 = p4Frame.landmarks[LandmarkIndex.LEFT_HIP]
        val rhP4 = p4Frame.landmarks[LandmarkIndex.RIGHT_HIP]
        val lhP7 = p7Frame.landmarks[LandmarkIndex.LEFT_HIP]
        val rhP7 = p7Frame.landmarks[LandmarkIndex.RIGHT_HIP]

        val allLandmarks = listOfNotNull(lhP4, rhP4, lhP7, rhP7)
        if (allLandmarks.size < 4) return notDetectable("Hip keypoints not visible")

        val confidence = allLandmarks.minOf { it.visibility }
        if (confidence < CONFIDENCE_FLOOR) return notDetectable("Hip keypoint confidence too low")

        val pelvisXatP4 = ((lhP4!!.x + rhP4!!.x) / 2f)
        val pelvisXatP7 = ((lhP7!!.x + rhP7!!.x) / 2f)

        // In DTL with golfer facing left: pelvis moving toward camera = X decreasing
        val displacement = pelvisXatP4 - pelvisXatP7  // positive = moved toward camera
        val threshold = THRESHOLD_FACTOR * scaleS

        val flagged = displacement > threshold
        val severity = if (flagged) (displacement - threshold) / scaleS else 0f

        val evidenceFrames = listOfNotNull(pPositions[4], pPositions[7])

        val overlays = if (flagged) buildOverlays(p4Frame, pelvisXatP4, pelvisXatP7) else emptyList()

        return FaultRecord(
            faultId = FAULT_ID,
            flagged = flagged,
            severity = severity,
            confidence = confidence,
            evidenceFrames = evidenceFrames,
            overlays = overlays,
            rootCauseBranches = if (flagged) rootCauses() else emptyList(),
            contentRefs = if (flagged) contentRefs() else ContentRefs(),
        )
    }

    private fun buildOverlays(
        p4Frame: com.joelhellsten.golfswing.pose.PoseFrame,
        pelvisXatP4: Float,
        pelvisXatP7: Float,
    ): List<Overlay> {
        val rh = p4Frame.landmarks[LandmarkIndex.RIGHT_HIP] ?: return emptyList()
        // Vertical seat-proxy line at P1 trail hip
        val seatProxyX = rh.x + 0.03f
        return listOf(
            Overlay(
                type = OverlayType.LINE,
                coords = listOf(seatProxyX, 0.3f, seatProxyX, 0.8f),
                label = "seat line",
            ),
            Overlay(
                type = OverlayType.LINE,
                coords = listOf(pelvisXatP4, 0.55f, pelvisXatP7, 0.55f),
                label = "pelvis shift",
            ),
        )
    }

    private fun rootCauses() = listOf(
        RootCauseBranch(
            id = "ankle_hip_mobility",
            rank = 1,
            description = "Limited ankle dorsiflexion or hip mobility — the body can't hold the hinge so it stands up. Check the deep-squat screen first.",
            physicalScreen = true,
        ),
        RootCauseBranch(
            id = "arm_stuck",
            rank = 2,
            description = "Arms getting stuck too far behind the body at transition, causing the pelvis to thrust forward to make room.",
            physicalScreen = false,
        ),
        RootCauseBranch(
            id = "loss_of_posture_cofire",
            rank = 3,
            description = "Co-occurring loss of posture — the spine straightens and the pelvis follows. Check spine angle at P6.",
            physicalScreen = false,
        ),
    )

    private fun contentRefs() = ContentRefs(
        drillIds = listOf("chair_drill", "wall_seat_drill"),
        feelIds = listOf("seat_stays_back_feel"),
        screenIds = listOf("deep_squat_screen", "hip_hinge_screen"),
        theoryIds = listOf("early_extension_theory"),
    )

    private fun notDetectable(reason: String) = FaultRecord(
        faultId = FAULT_ID,
        flagged = false,
        severity = 0f,
        confidence = 0f,
        evidenceFrames = emptyList(),
        overlays = emptyList(),
        rootCauseBranches = emptyList(),
        contentRefs = ContentRefs(),
    )
}
