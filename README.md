# Golf Swing Companion App

A golf improvement program for Android. Films your swing, detects critical positions, draws the lines a coach would draw, diagnoses the likely root cause, prescribes drills — then re-tests and adapts over time.

## Repo layout

| Directory | What it is |
|---|---|
| `android/` | Kotlin + Compose Android app (CameraX, MediaPipe, Room, Hilt) |
| `engine-py/` | GolfDB P-detection eval harness (Python) |
| `backend/` | FastAPI reasoning service — receives fault records, returns LLM explanations |
| `knowledge-base/` | YAML fault ontology: root-cause branches, drills, screens, theory |
| `docs/` | Planning documents — read `CLAUDE.md` first, then `HANDOFF.md` |

## Getting started

### Android app
Open `android/` in Android Studio. Let it sync Gradle and download dependencies (requires internet on first sync). Minimum SDK 26, targets SDK 35.

### Backend
Requires [uv](https://docs.astral.sh/uv/).
```bash
cd backend
cp .env.example .env           # add your ANTHROPIC_API_KEY
uv sync
uv run uvicorn main:app --reload
```

### Eval harness
```bash
cd engine-py
uv sync
python -m eval.run_eval --data-dir /path/to/golfdb --labels labels.json
```

## MVP scope

Android, 7-iron, **early extension** (Tier A, DTL view), full loop:
capture → P-detection → pose → fault rule + confidence → drawn overlay → explanation + root cause + drills → stored record → progress re-test.

See `docs/HANDOFF.md` for the full build plan and `docs/golf-fault-engine-spec.md` for the fault engine specification.
