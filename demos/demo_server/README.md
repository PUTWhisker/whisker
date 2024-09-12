# GRPC tutorial 
[link](https://grpc.io/docs/languages/go/basics/)

## Create protobuf message

``` proto
//declare syntax
syntax = "proto3";
//name of the output golang package
option go_package = "example.com/hello_world";
//each endpoint needs a service to connect to 
service testServer {
    rpc TestConnection(HelloWorld) returns (HelloWorld) {}
}
// each send and return need a package
message HelloWorld {
    string text = 1;
}
```

## Compile it to go module
```
protoc --go_out=. --go_opt=paths=source_relative \
    --go-grpc_out=. --go-grpc_opt=paths=source_relative \
    [name_of_proto_file.pb]
```    

