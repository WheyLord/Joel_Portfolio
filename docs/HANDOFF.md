# HANDOFF — Golf Swing Companion App

Full context from the planning conversation. Companion files: `CLAUDE.md` (orientation) and `golf-fault-engine-spec.md` (fault engine detail). Read all three.

---

## 1. The one-line idea
A clean, honest golf companion app that watches your swing, tells you what is actually happening in plain language with the lines drawn on screen, explains the likely root cause, and gives you a personalized, adaptive program to fix it — then proves it's working over time.

## 2. The positioning
Reference product: **Runna** (running). Runna is a structured program with a destination that adapts to how you're actually doing. It is not a "running analyzer." This app takes the same shape for golf. Existing golf apps are swing analyzers — they show 3D numbers and leave you to be your own coach. The opportunity is the Runna shape: **a coached improvement program built around your actual swing, where the analysis is the personalized input, not the product itself.**

The gap: running has Runna; golf has analyzers and conflicting YouTube videos that mostly don't relate to the watcher's actual problem. Nobody owns "the program that actually understands your swing."

## 3. The core problem this fixes
Golfers watch generic videos that may not relate to their fault and may make them worse — e.g. someone watches a "stop coming over the top" sequencing video when their real cause is an open clubface from a weak grip. Same visible symptom, different root cause, wrong fix wastes months. The app's edge is diagnosing *which* root cause applies to *this* golfer.

## 4. What's feasible (honest)
- Phone cameras (120/240fps slow-mo) capture the swing fine; real risks are motion blur and inconsistent setup.
- On-device 2D pose estimation (MediaPipe Pose Landmarker) is free, on-device, accurate enough for image-plane geometry.
- Automatic P-position detection is largely solved (GolfDB dataset, SwingNet-style models).
- Single-camera 3D is not the goal — not competing there.
- The defensible, hard, valuable part is the **root-cause reasoning + knowledge base**, which competitors skip.

## 5. Architecture
1. **Capture** — guided filming (DTL, then face-on later) with framing/lighting/shutter help. Capture UX is first-class; bad input is the #1 reason apps feel like gimmicks.
2. **P-segmentation** — detect P1–P10 from the clip. V1: wrist-trajectory heuristics. Upgrade: SwingNet TFLite on GolfDB weights.
3. **Pose extraction** — MediaPipe Pose Landmarker, on-device, VIDEO mode, GPU delegate. 33 landmarks with per-landmark visibility scores.
4. **Fault engine** — deterministic Kotlin rules in `core-engine`. Each returns `FaultRecord` {flagged, severity, confidence, evidenceFrames, overlays, rootCauseBranches, contentRefs}. See `golf-fault-engine-spec.md`.
5. **Knowledge base** — YAML in `knowledge-base/`. Each fault: explanation, root-cause branches, drills, physical screens (TPI-style), theory. This is the IP.
6. **Reasoning backend** — FastAPI on Azure Container Apps. Receives structured FaultRecord JSON. Loads KB context from YAML. Calls Anthropic API. Returns explanation + ranked root cause + drill refs. Never receives raw video. Confidence flows through — low confidence returns "couldn't see this clearly."
7. **Program + progress** — re-tests the same fault on new swings, shows the fault line moving back toward baseline. Retention loop.

## 6. Key decisions locked
- **Body-referenced normalization.** Thresholds as fractions of body scale unit S (hip-to-shoulder at P1). Never pixels.
- **P1 is the reference frame.** All faults measured as deviation from address baseline.
- **The drawn line is the trust mechanism**, not decoration.
- **Reason over structured measurements, not raw video.**
- **Confidence end-to-end** — low keypoint confidence → engine and backend both hedge or skip.
- **Physical screen before technique** when a root-cause branch points at the body.

## 7. Reliability tiers
- **Tier A — image-plane, depth-independent:** early extension, sway, slide, reverse spine angle, loss of posture, head movement. Make confident claims.
- **Tier B — camera-placement-sensitive but doable:** over-the-top / swing plane. Standardized capture required; hand-path-relative method most stable.
- **Tier C — depth-sensitive, extremes only, no degree numbers:** shoulder/hip turn, X-factor.
- **Tier D — infer and hedge only:** clubface angle, kinematic sequence timing.

## 8. MVP scope (Stage 1)
Android, 7-iron, **early extension** (Tier A, DTL), full loop end to end, stored record for re-test. DTL view only. Out of scope: face-on, over-the-top, 3D, sequence timing, GPS/social, iOS.

Success bar: P4/P6/P7 within ±3 frames on test swings; early-extension flag agrees with a coach's read on most test swings.

## 9. Stack (optimal, not skill-driven)
| Layer | Choice | Why |
|---|---|---|
| Android UI | Kotlin + Compose + Hilt | Industry standard |
| State management | ViewModel + StateFlow | Jetpack standard |
| Camera | CameraX | Google's modern camera API |
| Pose | MediaPipe Pose Landmarker | On-device, free, 33 landmarks, GPU delegate |
| P-detection | Wrist heuristics → SwingNet TFLite | Heuristics for v1, proven upgrade path |
| Fault engine | Pure Kotlin in core-engine | Deterministic rules, unit-testable, on-device |
| Storage | Room | Jetpack standard for SQLite |
| Backend | Python FastAPI + uv | Industry standard for AI/LLM backends |
| RAG v1 | YAML prompt injection | KB is small; no vector DB until it outgrows context |
| LLM | Anthropic Claude API | Via backend only |
| Deployment | Docker + Azure Container Apps | Scale to zero, clean container model |
| Eval | Python script over GolfDB | Python-native dataset |

## 10. Biggest risks (still live)
1. **Capture friction** — active filming every session vs passive watch data. Practice flow must make filming feel part of the drill.
2. **Cadence** — golf practice is sporadic; the program/plan is the lever for return habit.
3. **Attribution** — can't promise score drops; show the fault line moving toward baseline as "it works" proof.
4. **Reasoning trust** — deterministic rule gates, structured outputs, YAML-grounded prompts, surfaced uncertainty. Validate against a real coach before claiming accuracy.
