# Generate new proto files
`protoc sound_transfer.proto --js_out=import_style=commonjs:proto --grpc-web_out=import_style=commonjs,mode=grpcwebtext:proto --proto_path=./proto/`  

# Install necessery libraries
`npm install`

# Generate code that can be used in web browser
`npx webpack --mode=development ./client.js`

