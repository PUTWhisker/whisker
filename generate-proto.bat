@echo off

where protoc >nul 2>nul
if errorlevel 1 (
    echo ERROR: protoc is not installed. Please install protoc before running this script.
    exit /b 1
)

python -m venv venv

pip install grpcio-tools

python -m grpc_tools.protoc -I ./proto --python_out=./CLI/proto/sound_transfer --pyi_out=./CLI/proto/sound_transfer --grpc_python_out=./CLI/proto/sound_transfer ./proto/sound_transfer.proto
python -m grpc_tools.protoc -I ./proto --python_out=./CLI/proto/authentication --pyi_out=./CLI/proto/authentication --grpc_python_out=./CLI/proto/authentication ./proto/authentication.proto

python -m grpc_tools.protoc -I ./proto --python_out=./backend/whisper/proto --pyi_out=./backend/whisper/proto --grpc_python_out=./backend/whisper/proto ./proto/sound_transfer.proto

protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative ./backend/ApiServer/proto/sound_transfer/sound_transfer.proto
protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative ./backend/ApiServer/proto/authentication/authentication.proto

protoc sound_transfer.proto --js_out=import_style=commonjs:webApp/proto/sound_transfer --grpc-web_out=import_style=commonjs,mode=grpcwebtext:webApp/proto/sound_transfer --proto_path=./proto
protoc authentication.proto --js_out=import_style=commonjs:webApp/proto/authentication --grpc-web_out=import_style=commonjs,mode=grpcwebtext:webApp/proto/authentication --proto_path=./proto

xcopy .\proto\sound_transfer.proto .\mobile\protos\src\main\proto\io\grpc\soundtransfer\sound_transfer.proto /Y
xcopy .\proto\authentication.proto .\mobile\protos\src\main\proto\io\grpc\authentication\authentication.proto /Y

rmdir /S /Q venv

echo Setup completed successfully.
