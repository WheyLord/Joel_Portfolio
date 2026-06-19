"""POST /api/v1/analyze — receive a FaultRecord, return a coaching explanation."""

from __future__ import annotations

import os
from pathlib import Path

import anthropic
import yaml
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

router = APIRouter()

CONFIDENCE_FLOOR = 0.6
KB_PATH = Path(os.getenv("KNOWLEDGE_BASE_PATH", "../knowledge-base"))


# ── Request / response models ──────────────────────────────────────────────────

class RootCauseBranch(BaseModel):
    id: str
    rank: int
    description: str
    physical_screen: bool = False


class ContentRefs(BaseModel):
    drill_ids: list[str] = []
    feel_ids: list[str] = []
    screen_ids: list[str] = []
    theory_ids: list[str] = []


class FaultRecordRequest(BaseModel):
    fault_id: str
    flagged: bool
    severity: float
    confidence: float
    evidence_frames: list[int]
    root_cause_branches: list[RootCauseBranch]
    content_refs: ContentRefs


class AnalysisResponse(BaseModel):
    explanation: str
    primary_root_cause: str
    secondary_root_cause: str | None = None
    drills: list[str]
    physical_screen_recommended: bool
    confidence_note: str | None = None


# ── Endpoint ───────────────────────────────────────────────────────────────────

@router.post("/analyze", response_model=AnalysisResponse)
async def analyze(record: FaultRecordRequest) -> AnalysisResponse:
    if not record.flagged:
        return AnalysisResponse(
            explanation="No fault was detected in this swing.",
            primary_root_cause="",
            drills=[],
            physical_screen_recommended=False,
        )

    if record.confidence < CONFIDENCE_FLOOR:
        return AnalysisResponse(
            explanation=(
                "The keypoints needed for this analysis weren't visible clearly enough "
                "to make a confident call. Try re-recording with better lighting and "
                "making sure your full body stays in frame throughout the swing."
            ),
            primary_root_cause="",
            drills=[],
            physical_screen_recommended=False,
            confidence_note=f"Keypoint confidence {record.confidence:.0%} — below {CONFIDENCE_FLOOR:.0%} floor.",
        )

    kb_context = _load_kb_context(record.fault_id)
    explanation = _call_llm(record, kb_context)

    primary = record.root_cause_branches[0] if record.root_cause_branches else None
    secondary = record.root_cause_branches[1] if len(record.root_cause_branches) > 1 else None

    return AnalysisResponse(
        explanation=explanation,
        primary_root_cause=primary.description if primary else "",
        secondary_root_cause=secondary.description if secondary else None,
        drills=record.content_refs.drill_ids[:2],
        physical_screen_recommended=bool(primary and primary.physical_screen),
    )


# ── Helpers ────────────────────────────────────────────────────────────────────

def _load_kb_context(fault_id: str) -> str:
    """Load the YAML knowledge-base entry as plain text for prompt injection."""
    fault_file = KB_PATH / "faults" / f"{fault_id}.yaml"
    if not fault_file.exists():
        return ""
    with fault_file.open() as f:
        data = yaml.safe_load(f)
    return yaml.dump(data, default_flow_style=False, allow_unicode=True)


def _call_llm(record: FaultRecordRequest, kb_context: str) -> str:
    client = anthropic.Anthropic(api_key=os.getenv("ANTHROPIC_API_KEY"))

    system = (
        "You are the coaching voice of a golf improvement app. "
        "You receive a structured fault detection record and knowledge-base context. "
        "Explain the fault in plain language: what happened in the swing, why it matters, "
        "and what the most likely root cause is for this specific golfer. "
        "Be honest about what 2D video can and cannot show. "
        "Keep the explanation under 120 words. "
        "Never claim 3D precision. Never report degree numbers for depth-sensitive measures. "
        "If the top root-cause branch has physical_screen=true, recommend completing "
        "the physical screen before starting technique drills."
    )

    branches_text = "\n".join(
        f"  {b.rank}. {b.description} (physical screen: {b.physical_screen})"
        for b in record.root_cause_branches
    )

    user = f"""Fault record:
- Fault: {record.fault_id}
- Severity (excess past threshold / S): {record.severity:.3f}
- Confidence: {record.confidence:.0%}
- Root cause branches (ranked):
{branches_text}

Knowledge base context:
{kb_context}

Write the coaching explanation."""

    message = client.messages.create(
        model="claude-sonnet-4-6",
        max_tokens=300,
        messages=[{"role": "user", "content": user}],
        system=system,
    )
    return message.content[0].text
