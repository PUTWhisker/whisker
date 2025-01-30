# LLM runner
Code for container that runs LLM's.

## Recompile python gRPC classes
```
python -m grpc_tools.protoc -I ..\..\proto --python_out=. --pyi_out=. --grpc_python_out=. ..\..\proto\sound_transfer.proto
```

## How to run
1. Go to `backend/whiserp` directory
1. Run `pip install -r requirements. txt` to download openAI whisper
1. Run `cp .env.example .env` and fill values in `.env`
1. Start server using `python server.py`