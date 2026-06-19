# CLAUDE.md — Golf Swing Companion App

Read this file first. Depth lives in `HANDOFF.md` and `golf-fault-engine-spec.md` in this same directory — read those before doing substantial work.

## What this project is
A golf improvement program, not a swing analyzer. Model: Runna (running app). The swing analysis is the personalized input; the structured, adaptive program is the product. The app films a swing, detects P-positions (P1–P10), draws the lines a coach would draw, flags visible faults with confidence scores, explains the likely root cause from a grounded knowledge base, prescribes drills/feels/screens, and re-tests over time.

## Stack
- **Android app:** Kotlin + Jetpack Compose, Hilt DI, ViewModel + StateFlow, CameraX, MediaPipe Pose Landmarker (on-device), Room
- **Fault engine:** Pure Kotlin on-device deterministic rules in `android/core-engine/`. No Python mirror needed.
- **Backend:** Python FastAPI, managed with uv, deployed as Docker container on Azure Container Apps
- **Knowledge base:** YAML in `knowledge-base/`, injected directly into LLM prompt context (no vector DB until KB outgrows context window)
- **LLM:** Anthropic Claude API (called from backend only — key never on device)
- **Eval harness:** Standalone Python script in `engine-py/eval/` for GolfDB P-detection validation

## Non-negotiable design principles
1. **Honesty over wow.** Confident claims only on Tier A/B faults. No fake 3D precision, no degree numbers for depth-sensitive measurements, no clubface-angle measurement.
2. **The drawn line is the trust mechanism.** Every flag shows the geometry that produced it, on the evidence frame.
3. **Reason over structured records, never over raw video.** CV pipeline → measurements + confidence → backend explains. Confidence flows all the way through to the user.
4. **Body-referenced units, not pixels.** Everything normalized to body scale S (hip-to-shoulder at P1).
5. **When the body might be the cause, recommend a physical screen before prescribing technique.**

## MVP scope (Stage 1)
Android, 7-iron, **early extension** (Tier A, DTL), full loop: capture → P-detection → pose → fault rule + confidence → drawn overlay → explanation + ranked root cause + drills → stored record → progress re-test. DTL view only for v1.

Out of scope for v1: face-on view, over-the-top, 3D, sequence timing, GPS/social, iOS.

## Repo layout
```
android/       Kotlin + Compose app (app, core-pose, core-engine, core-data modules)
engine-py/     GolfDB eval harness only (Python)
backend/       FastAPI reasoning service (Python, uv)
knowledge-base/ YAML fault ontology, drills, screens, theory
docs/          Planning documents (read these)
```

## Build sequence (narrow-and-deep)
Phase 0 ✓ — Android scaffold committed  
Phase 1 — CameraX capture with guided framing overlay  
Phase 2 — MediaPipe pose on playback, keypoint timeline, smoothing  
Phase 3 — Body scale S + P-position detection (wrist heuristics)  
Phase 4 — Early extension Kotlin rule + drawn overlay + confidence gate  
Phase 5 — Room storage of structured FaultRecord  
Phase 6 — FastAPI backend + KB context + LLM explanation  
Phase 7 — Result UI (evidence frame hero, explanation, root cause, drills)  
Phase 8 — Progress/re-test view  
