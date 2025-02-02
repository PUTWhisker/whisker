# Whisker - app for transcription

## How to build server
1. Create keys for Api server. Go to `backend/ApiServer/keys` directory and run `create_keys.sh`
1. Build base server for LLM runner container. Go to `infrastructure/server-base` directory and run `docker build -t grpc-whisper .`
1. Build the rest of containers. Go to `infrastructure/server-base` directory and run `docker-compose build`
1. Configure enviromental variables in `docker-compose.yaml` file. For `HUGGING_FACE_API_KEY` you need to register to [hugging face](https://huggingface.co/) service, accept [pyannote/speaker-diarization-3.1](https://huggingface.co/pyannote/speaker-diarization-3.1) terms of service and generate key for your account.
1. Launch containers using `docker-compose up`


## Changing or modifying proto files
1. Change schema in the `proto/` directory.
1. Run `generate-proto.sh` or `generate-proto.bat`

## Web application
To preview web application visit [our website](https://wmsd.cs.put.poznan.pl)