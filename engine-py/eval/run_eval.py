"""
GolfDB P-detection eval harness.

Validates wrist-trajectory heuristics against labeled GolfDB clips.
Success target: P4/P6/P7 within ±3 frames of ground truth.

Usage:
  python -m eval.run_eval --data-dir /path/to/golfdb --labels labels.json

Labels JSON format:
  [
    {
      "clip": "clip_001.mp4",
      "p_positions": {"1": 0, "4": 45, "6": 72, "7": 85}
    },
    ...
  ]
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path


P_POSITIONS_TO_EVAL = [4, 6, 7]
TOLERANCE_FRAMES = 3


def evaluate_p_detection(
    predicted: dict[int, int],
    ground_truth: dict[int, int],
) -> dict[int, bool]:
    results = {}
    for p_num in P_POSITIONS_TO_EVAL:
        gt = ground_truth.get(p_num)
        pred = predicted.get(p_num)
        if gt is None:
            continue
        results[p_num] = pred is not None and abs(pred - gt) <= TOLERANCE_FRAMES
    return results


def run(data_dir: Path, labels_path: Path) -> None:
    with labels_path.open() as f:
        labels = json.load(f)

    hits: dict[int, list[bool]] = {p: [] for p in P_POSITIONS_TO_EVAL}

    for item in labels:
        clip_path = data_dir / item["clip"]
        if not clip_path.exists():
            print(f"SKIP {item['clip']} — not found")
            continue

        gt_p = {int(k): v for k, v in item["p_positions"].items()}

        # TODO: wire up pose extraction + heuristics.detect_p_positions here
        predicted_p: dict[int, int] = {}

        result = evaluate_p_detection(predicted_p, gt_p)
        for p_num, hit in result.items():
            hits[p_num].append(hit)

    print(f"\nP-detection accuracy (tolerance ±{TOLERANCE_FRAMES} frames)")
    print("-" * 40)
    for p_num in P_POSITIONS_TO_EVAL:
        clip_hits = hits[p_num]
        if not clip_hits:
            print(f"  P{p_num}: no data")
            continue
        acc = sum(clip_hits) / len(clip_hits)
        status = "PASS" if acc >= 0.80 else "FAIL"
        print(f"  P{p_num}: {acc:.0%}  ({sum(clip_hits)}/{len(clip_hits)} clips)  [{status}]")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--data-dir", type=Path, required=True)
    parser.add_argument("--labels", type=Path, required=True)
    args = parser.parse_args()
    run(args.data_dir, args.labels)
