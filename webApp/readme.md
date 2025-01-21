# Generate new proto files
`protoc sound_transfer.proto --js_out=import_style=commonjs:proto/sound_transfer --grpc-web_out=import_style=commonjs,mode=grpcwebtext:proto/sound_transfer --proto_path=./proto/sound_transfer`  
`protoc authentication.proto --js_out=import_style=commonjs:proto/authentication --grpc-web_out=import_style=commonjs,mode=grpcwebtext:proto/authentication --proto_path=./proto/authentication`  

# Install necessery libraries
`npm install`

# Generate code that can be used in web browser
`npx webpack --mode=development`

