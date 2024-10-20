FROM grpc-whisper:latest AS main-image

WORKDIR /server

COPY . .

ENV PYTHONUNBUFFERED=1

CMD ["python", "/server/server.py"]

