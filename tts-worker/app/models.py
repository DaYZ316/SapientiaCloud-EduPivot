from __future__ import annotations

from pydantic import BaseModel, Field


class SynthesizeRequest(BaseModel):
    taskId: str = Field(..., min_length=1, max_length=128)
    text: str = Field(..., min_length=1, max_length=2000)
    voiceCode: str | None = Field(default=None, max_length=64)
    audioFormat: str | None = Field(default="wav", max_length=16)
