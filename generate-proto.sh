#! /bin/bash

python -m venv venv

# For correct venv activation
if [ "$(uname)" == "Darwin" ]; then
    echo "Mac"
    # Mac OS X platform        
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    # GNU/Linux platform
    source ./venv/bin/activate
elif [[ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" || "$(expr substr $(uname -s) 1 10)" == "MINGW64_NT" ]]; then
    # Windows platform
    source ./venv/Scripts/activate
fi

pip install grpcio-tools

# CLI 
python -m grpc_tools.protoc -I ./proto --python_out=./CLI/proto/sound_transfer --pyi_out=./CLI/proto/sound_transfer --grpc_python_out=./CLI/proto/sound_transfer ./proto/sound_transfer.proto
python -m grpc_tools.protoc -I ./proto --python_out=./CLI/proto/authentication --pyi_out=./CLI/proto/authentication --grpc_python_out=./CLI/proto/authentication ./proto/authentication.proto

# Whisper server
python -m grpc_tools.protoc -I ./proto --python_out=./backend/whisper/proto --pyi_out=./backend/whisper/proto --grpc_python_out=./backend/whisper/proto ./proto/sound_transfer.proto

# ServerAPI
cp ./proto/sound_transfer.proto ./backend/src/proto/sound_transfer
cp ./proto/authentication.proto ./backend/src/proto/authentication
protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative ./backend/src/proto/sound_transfer/sound_transfer.proto
protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative ./backend/src/proto/authentication/authentication.proto

#TODO: Add Web and Mobile's protoc (And maybe protoc installing if possible)

rm -r venv