# Golf Swing Fault-Detection Engine — Core Spec

A 2D, telestrator-style detection engine: find the critical positions, draw the lines a coach would draw, flag deviations against simple geometric rules, and attach grounded coaching explanations and drills. No 3D measurement claims — every rule operates on relationships visible in the image plane.

Convention: thresholds written for a right-handed golfer. Mirror left/right for lefties. "Target side" = lead side = left for RH. "Ball-target line" = the line the ball starts on.

---

## 1. The engineering spine

**Scale normalization.** Never threshold in pixels. At P1, compute body scale unit `S` = vertical distance from hip midpoint to shoulder midpoint. Express every displacement as a fraction of `S`. Transfers across phones, zoom levels, and golfer heights.

**View classification.** Each swing is down-the-line (DTL) or face-on. Auto-detect: face-on if shoulder line is wide in frame and hips face camera; DTL if body is side-on. If uncertain, ask the user to confirm — wrong view = wrong rules = wrong diagnosis.

**Temporal smoothing.** Apply a moving-average or one-euro filter to keypoint tracks before computing angles. Jitter will trip thresholds spuriously without this.

**Confidence gating.** Every keypoint has a visibility/confidence score. A fault fires only if all required keypoints are above the confidence floor at the inspected frames. Below floor → "can't see this clearly," not a guess.

**The reference frame.** Establish all baseline lines at P1 (address): spine line, butt/pelvis line, trail-hip vertical, head circle. Most faults are measured as deviation from these P1 anchors.

### Fault function signature

Every fault is a pure function returning a structured record:

```
detect_<fault>(keypoints, view, scale_S, p_positions) -> FaultRecord {
  flagged: bool,
  severity: float,           // excess past threshold / S
  confidence: float,         // min keypoint visibility over inspected frames
  evidenceFrames: [p_idx],   // frames to show the user
  overlays: [{type, coords, label}],
  rootCauseBranches: [{id, rank, description, physicalScreen}],
  contentRefs: {drillIds, feelIds, screenIds, theoryIds}
}
```

Ranked by severity × confidence × priority. Overlays drawn on evidence frames. Faults below the confidence floor are surfaced as "possible — here's what to check," never as assertions.

---

## 2. Reliability tiers

**Tier A — geometrically clean (image-plane, depth-independent).** Early extension, sway, slide, reverse spine angle, loss of posture, head movement. Lead with these. Make confident claims.

**Tier B — camera-placement-sensitive but doable.** Over-the-top / swing plane, shaft lean. Requires standardized capture. Hand-path-relative version degrades more gracefully than absolute plane lines.

**Tier C — depth-sensitive, flag extremes only, no degree numbers.** Shoulder/hip turn amount, X-factor. Never report a precise figure; phrase as "your turn looks short here."

**Tier D — unreliable, infer and hedge only.** Clubface angle, kinematic-sequence timing. Infer indirectly (wrist cupping as a face proxy); always framed as hypothesis, never measurement.

---

## 3. Fault library

### Early extension — Tier A
- **View:** DTL.
- **Inspect:** P1 baseline → P4 → P7.
- **Lines/keypoints:** vertical line tangent to trail hip at P1 (seat proxy = trail-hip X + half hip-width offset). Track pelvis-midpoint horizontal position.
- **Trigger:** pelvis moves toward ball-target line (X decreases in standard DTL) by more than **0.10·S** between P4 and P7.
- **Root-cause branches (ranked):** (1) physical — limited ankle dorsiflexion / hip mobility; recommend TPI screen before drilling. (2) sequencing — arms stuck behind body at transition. (3) loss of posture co-occurring.
- **Content:** chair/wall drill, glute activation, hip-hinge feel.

### Loss of posture — Tier A
- **View:** DTL.
- **Inspect:** P1 → P4 → P6/P7.
- **Lines/keypoints:** spine line = hip-mid to shoulder-mid. Measure forward tilt from vertical at P1; track change.
- **Trigger:** spine tilt changes by more than **~10–12°** from P1 baseline. Often co-fires with early extension.
- **Root-cause branches:** same mobility/stability family as early extension; also over-active early rotation.

### Over-the-top / steep shaft — Tier B
- **View:** DTL.
- **Inspect:** P4 → P5 → P6.
- **Method (preferred):** hand-path-relative. Track wrist keypoint trajectory. Flag if downswing hand path is outside backswing hand path by more than **0.08·S**.
- **Root-cause branches (ranked):** (1) open clubface — brain throws club out to rescue. (2) weak grip. (3) upper body fires before hips. (4) limited thoracic rotation. (5) intent (steering). Engine pushes face/grip to top if wrist-cupping proxy also fired.

### Reverse spine angle — Tier A (safety flag)
- **View:** face-on.
- **Inspect:** P4.
- **Trigger:** upper spine tilts toward target (lead side) at top instead of away from it. Raise severity — associated with lower-back stress.
- **Root-cause branches:** core/trunk stability, mobility limitations, misguided "keep head down" intent.

### Sway — Tier A
- **View:** face-on.
- **Inspect:** P1 baseline → P4.
- **Trigger:** pelvis center translates away from target past trail-hip baseline by more than **~0.10× stance width**. Distinguish from good turn: turn = rotation with minimal center shift; sway = lateral translation.

### Slide — Tier A
- **View:** face-on.
- **Inspect:** P5 → P7.
- **Trigger:** lead hip translates toward target past lead-foot baseline before rotating open, by more than **~0.10× stance width**.

### Head movement — Tier A (corroborating)
- **View:** both.
- **Inspect:** P1 baseline → through swing.
- **Lines/keypoints:** head circle at P1, radius ~**0.25·S**.
- **Trigger:** head exits the circle. Use as corroborating signal to boost confidence in other flags, not usually standalone.

### Clubface / lead-wrist — Tier D (infer and hedge)
- **Approach:** never claim to measure clubface angle. Detect lead-wrist cupping (extension) as proxy. If confident, surface as hypothesis: "your lead wrist looks cupped here, which often means an open face — and an open face is a common hidden cause of an over-the-top move."

---

## 4. What not to claim (honesty layer)

- No precise 3D angles or degree figures for Tier C/D.
- No kinematic/kinetic sequence timing from phone video.
- No clubface-angle measurement.
- Low keypoint confidence → "I can't see this clearly," skip the flag.
- Physical branch present → recommend the screen before technique.
- Frame faults against actual ball flight where possible — a "fault" that produces good shots may be a tour-legal idiosyncrasy.

Under-promise on numbers, over-deliver on clear, drawn, well-explained reasoning.
