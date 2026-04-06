from __future__ import annotations

import os
from dataclasses import dataclass


@dataclass(frozen=True)
class Settings:
    host: str = os.getenv("TTS_WORKER_HOST", "0.0.0.0")
    port: int = int(os.getenv("TTS_WORKER_PORT", "32111"))
    piper_bin: str = os.getenv("PIPER_BIN", "/opt/piper/piper")
    default_voice_code: str = os.getenv("PIPER_DEFAULT_VOICE_CODE", "teacher-default")
    default_model_path: str = os.getenv("PIPER_MODEL_PATH", "/models/teacher-default.onnx")
    default_config_path: str = os.getenv("PIPER_CONFIG_PATH", "")
    output_dir: str = os.getenv("TTS_OUTPUT_DIR", "/tmp/tts-worker")
    max_text_length: int = int(os.getenv("TTS_MAX_TEXT_LENGTH", "2000"))
    synthesize_timeout_seconds: int = int(os.getenv("TTS_SYNTHESIZE_TIMEOUT_SECONDS", "60"))


settings = Settings()
