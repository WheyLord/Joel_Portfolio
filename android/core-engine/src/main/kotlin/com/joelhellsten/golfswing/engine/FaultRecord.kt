package com.joelhellsten.golfswing.engine

/**
 * Structured output of every fault detector, matching the spec signature:
 *
 *   detect_<fault>(keypoints, view, scale_S, p_positions) -> FaultRecord
 *
 * The UI draws overlays from this record; the backend explains it.
 * Confidence flows all the way to the user — low confidence means the UI hedges.
 */
data class FaultRecord(
    val faultId: String,
    val flagged: Boolean,
    /** How far past threshold the measurement is, normalised by S. 0 if not flagged. */
    val severity: Float,
    /** Min keypoint visibility over all inspected frames. Fault only fires if above confidenceFloor. */
    val confidence: Float,
    /** Frame indices where the fault evidence is strongest — shown to the user. */
    val evidenceFrames: List<Int>,
    /** Lines, circles, angles to draw on the evidence frame. Serializable. */
    val overlays: List<Overlay>,
    /** Ranked probable root causes from the knowledge base. */
    val rootCauseBranches: List<RootCauseBranch>,
    /** IDs into the knowledge base for content retrieval. */
    val contentRefs: ContentRefs,
)

data class Overlay(
    val type: OverlayType,
    val coords: List<Float>,  // [x1,y1, x2,y2] for line; [cx,cy,r] for circle; [ax,ay, bx,by, cx,cy] for angle
    val label: String? = null,
)

enum class OverlayType { LINE, CIRCLE, ANGLE }

data class RootCauseBranch(
    val id: String,
    val rank: Int,
    val description: String,
    val physicalScreen: Boolean,
)

data class ContentRefs(
    val drillIds: List<String> = emptyList(),
    val feelIds: List<String> = emptyList(),
    val screenIds: List<String> = emptyList(),
    val theoryIds: List<String> = emptyList(),
)

/** Minimum keypoint confidence for a fault to fire. Below this = "can't see clearly." */
const val CONFIDENCE_FLOOR = 0.6f
