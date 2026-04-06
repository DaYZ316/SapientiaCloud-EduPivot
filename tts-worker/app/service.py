from __future__ import annotations

import contextlib
import pathlib
import subprocess
import wave

from fastapi import HTTPException

from .config import settings
from .models import SynthesizeRequest


def health_status() -> dict:
    model_path = pathlib.Path(settings.default_model_path)
    piper_path = pathlib.Path(settings.piper_bin)
    return {
        "status": "ok" if piper_path.exists() and model_path.exists() else "degraded",
        "piperBin": str(piper_path),
        "piperExists": piper_path.exists(),
        "modelPath": str(model_path),
        "modelExists": model_path.exists(),
        "defaultVoiceCode": settings.default_voice_code,
    }


def synthesize(request: SynthesizeRequest) -> tuple[bytes, int | None]:
    if request.audioFormat and request.audioFormat.lower() != "wav":
        raise HTTPException(status_code=400, detail="Only wav output is supported")
    if len(request.text) > settings.max_text_length:
        raise HTTPException(status_code=400, detail="Text exceeds synthesis length limit")

    output_dir = pathlib.Path(settings.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    output_file = output_dir / f"{request.taskId}.wav"

    model_path = pathlib.Path(settings.default_model_path)
    if not model_path.exists():
        raise HTTPException(status_code=500, detail="Piper model file does not exist")

    piper_path = pathlib.Path(settings.piper_bin)
    if not piper_path.exists():
        raise HTTPException(status_code=500, detail="Piper executable does not exist")

    command = [
        str(piper_path),
        "--model",
        str(model_path),
        "--output_file",
        str(output_file),
    ]
    config_path = settings.default_config_path.strip()
    if config_path:
        command.extend(["--config", config_path])

    try:
        subprocess.run(
            command,
            input=request.text,
            capture_output=True,
            text=True,
            check=True,
            timeout=settings.synthesize_timeout_seconds,
        )
    except subprocess.TimeoutExpired as exc:
        raise HTTPException(status_code=504, detail="Piper synthesis timed out") from exc
    except subprocess.CalledProcessError as exc:
        detail = exc.stderr.strip() or exc.stdout.strip() or "Piper synthesis failed"
        raise HTTPException(status_code=500, detail=detail[:500]) from exc

    if not output_file.exists():
        raise HTTPException(status_code=500, detail="Piper did not generate output audio")

    try:
        audio_bytes = output_file.read_bytes()
        duration_ms = _read_duration_ms(output_file)
        return audio_bytes, duration_ms
    finally:
        with contextlib.suppress(OSError):
            output_file.unlink()


def _read_duration_ms(path: pathlib.Path) -> int | None:
    try:
        with wave.open(str(path), "rb") as wav_file:
            frame_rate = wav_file.getframerate()
            frame_count = wav_file.getnframes()
            if frame_rate <= 0:
                return None
            return int(frame_count / frame_rate * 1000)
    except Exception:
        return None
