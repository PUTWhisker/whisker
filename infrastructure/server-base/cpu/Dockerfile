FROM python:3.12-slim AS server-base

WORKDIR /builder

RUN apt-get update
RUN apt-get install libasound-dev libportaudio2 libportaudiocpp0 portaudio19-dev -y
RUN apt-get install gcc -y

COPY requirements.txt .
RUN pip install -r /builder/requirements.txt


