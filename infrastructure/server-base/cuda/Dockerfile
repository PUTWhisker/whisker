FROM nvidia/cuda:12.1.1-cudnn8-devel-ubuntu22.04 AS server-base

WORKDIR /builder

RUN apt-get update && \
    apt-get install -y \
    libasound-dev \
    libportaudio2 \
    libportaudiocpp0 \
    portaudio19-dev \
    gcc 

RUN apt-get install -y python3 python3-pip && \
    python3 -m pip install --upgrade pip


COPY requirements.txt .

RUN pip install --no-cache-dir -r /builder/requirements.txt