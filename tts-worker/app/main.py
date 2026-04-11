from __future__ import annotations

from fastapi import FastAPI, Response
from fastapi.responses import JSONResponse

from .models import SynthesizeRequest
from .service import health_status, synthesize


app = FastAPI(title="SapientiaCloud TTS Worker", version="1.0.0")


@app.get("/health")
def health() -> JSONResponse:
    status = health_status()
    status_code = 200 if status["status"] == "ok" else 503
    return JSONResponse(content=status, status_code=status_code)


@app.post("/tts/synthesize")
def synthesize_audio(request: SynthesizeRequest) -> Response:
    audio_bytes, duration_ms = synthesize(request)
    headers = {}
    if duration_ms is not None:
        headers["X-Audio-Duration-Ms"] = str(duration_ms)
    return Response(content=audio_bytes, media_type="audio/wav", headers=headers)
