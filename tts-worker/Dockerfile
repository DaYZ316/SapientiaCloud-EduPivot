FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PIP_INDEX_URL=https://pypi.tuna.tsinghua.edu.cn/simple
ENV PIP_TRUSTED_HOST=pypi.tuna.tsinghua.edu.cn

WORKDIR /app

COPY requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r /app/requirements.txt

COPY app /app/app

RUN mkdir -p /models /opt/piper /tmp/tts-worker

EXPOSE 32111

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "32111"]
