FROM python:3.12-slim AS main-image

WORKDIR /builder
COPY requirements.txt .
RUN pip install -r /builder/requirements.txt

RUN apt-get update && apt-get install -y \
                        ffmpeg