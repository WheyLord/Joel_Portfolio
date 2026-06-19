"""
Golf Swing reasoning API.

Receives a structured FaultRecord JSON from the Android app.
Loads relevant knowledge-base context from YAML.
Calls the LLM. Returns explanation + root cause + drill refs.

Never receives raw video — only the structured numeric record.
Confidence from the device engine flows through: low confidence
returns a hedged response rather than a fabricated diagnosis.
"""

from __future__ import annotations

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from routers.analyze import router as analyze_router

app = FastAPI(title="Golf Swing Reasoning API", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["POST", "GET"],
    allow_headers=["*"],
)

app.include_router(analyze_router, prefix="/api/v1")


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}
