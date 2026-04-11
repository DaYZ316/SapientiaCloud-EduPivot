from __future__ import annotations

from pydantic import AliasChoices, BaseModel, ConfigDict, Field


class SynthesizeRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    taskId: str = Field(
        ...,
        validation_alias=AliasChoices("taskId", "task_id"),
        serialization_alias="taskId",
        min_length=1,
        max_length=128,
    )
    text: str = Field(..., min_length=1, max_length=2000)
    voiceCode: str | None = Field(
        default=None,
        validation_alias=AliasChoices("voiceCode", "voice_code"),
        serialization_alias="voiceCode",
        max_length=64,
    )
    audioFormat: str | None = Field(
        default="wav",
        validation_alias=AliasChoices("audioFormat", "audio_format"),
        serialization_alias="audioFormat",
        max_length=16,
    )
