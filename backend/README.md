# gRPC whisper server

## Recompile gRPC classes
```
protoc --go_out=. --go_opt=paths=source_relative \
    --go-grpc_out=. --go-grpc_opt=paths=source_relative \
    [name_of_proto_file.pb]
```


## How to run
1. Run `pip install -r requirements. txt` to download openAI whisper
1. Run `cp .env.example .env` and fill values in `.env`
1. Go to `/src` and run `go run .`


## Run docker container
1. Build image `docker build -t grpc-whisper .`
1. Run container `docker run -p 9900:9900 grpc-whisper`