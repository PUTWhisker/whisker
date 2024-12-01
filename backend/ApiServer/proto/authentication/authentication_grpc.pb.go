// Code generated by protoc-gen-go-grpc. DO NOT EDIT.
// versions:
// - protoc-gen-go-grpc v1.2.0
// - protoc             v5.26.0
// source: backend/ApiServer/proto/authentication/authentication.proto

package authentication

import (
	context "context"
	grpc "google.golang.org/grpc"
	codes "google.golang.org/grpc/codes"
	status "google.golang.org/grpc/status"
)

// This is a compile-time assertion to ensure that this generated file
// is compatible with the grpc package it is being compiled against.
// Requires gRPC-Go v1.32.0 or later.
const _ = grpc.SupportPackageIsVersion7

// ClientServiceClient is the client API for ClientService service.
//
// For semantics around ctx use and closing/ending streaming RPCs, please refer to https://pkg.go.dev/google.golang.org/grpc/?tab=doc#ClientConn.NewStream.
type ClientServiceClient interface {
	Login(ctx context.Context, in *UserCredits, opts ...grpc.CallOption) (*LoginResponse, error)
	GetTranslation(ctx context.Context, in *Empty, opts ...grpc.CallOption) (ClientService_GetTranslationClient, error)
	Register(ctx context.Context, in *UserCredits, opts ...grpc.CallOption) (*StatusResponse, error)
}

type clientServiceClient struct {
	cc grpc.ClientConnInterface
}

func NewClientServiceClient(cc grpc.ClientConnInterface) ClientServiceClient {
	return &clientServiceClient{cc}
}

func (c *clientServiceClient) Login(ctx context.Context, in *UserCredits, opts ...grpc.CallOption) (*LoginResponse, error) {
	out := new(LoginResponse)
	err := c.cc.Invoke(ctx, "/ClientService/Login", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *clientServiceClient) GetTranslation(ctx context.Context, in *Empty, opts ...grpc.CallOption) (ClientService_GetTranslationClient, error) {
	stream, err := c.cc.NewStream(ctx, &ClientService_ServiceDesc.Streams[0], "/ClientService/GetTranslation", opts...)
	if err != nil {
		return nil, err
	}
	x := &clientServiceGetTranslationClient{stream}
	if err := x.ClientStream.SendMsg(in); err != nil {
		return nil, err
	}
	if err := x.ClientStream.CloseSend(); err != nil {
		return nil, err
	}
	return x, nil
}

type ClientService_GetTranslationClient interface {
	Recv() (*TextHistory, error)
	grpc.ClientStream
}

type clientServiceGetTranslationClient struct {
	grpc.ClientStream
}

func (x *clientServiceGetTranslationClient) Recv() (*TextHistory, error) {
	m := new(TextHistory)
	if err := x.ClientStream.RecvMsg(m); err != nil {
		return nil, err
	}
	return m, nil
}

func (c *clientServiceClient) Register(ctx context.Context, in *UserCredits, opts ...grpc.CallOption) (*StatusResponse, error) {
	out := new(StatusResponse)
	err := c.cc.Invoke(ctx, "/ClientService/Register", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

// ClientServiceServer is the server API for ClientService service.
// All implementations must embed UnimplementedClientServiceServer
// for forward compatibility
type ClientServiceServer interface {
	Login(context.Context, *UserCredits) (*LoginResponse, error)
	GetTranslation(*Empty, ClientService_GetTranslationServer) error
	Register(context.Context, *UserCredits) (*StatusResponse, error)
	mustEmbedUnimplementedClientServiceServer()
}

// UnimplementedClientServiceServer must be embedded to have forward compatible implementations.
type UnimplementedClientServiceServer struct {
}

func (UnimplementedClientServiceServer) Login(context.Context, *UserCredits) (*LoginResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method Login not implemented")
}
func (UnimplementedClientServiceServer) GetTranslation(*Empty, ClientService_GetTranslationServer) error {
	return status.Errorf(codes.Unimplemented, "method GetTranslation not implemented")
}
func (UnimplementedClientServiceServer) Register(context.Context, *UserCredits) (*StatusResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method Register not implemented")
}
func (UnimplementedClientServiceServer) mustEmbedUnimplementedClientServiceServer() {}

// UnsafeClientServiceServer may be embedded to opt out of forward compatibility for this service.
// Use of this interface is not recommended, as added methods to ClientServiceServer will
// result in compilation errors.
type UnsafeClientServiceServer interface {
	mustEmbedUnimplementedClientServiceServer()
}

func RegisterClientServiceServer(s grpc.ServiceRegistrar, srv ClientServiceServer) {
	s.RegisterService(&ClientService_ServiceDesc, srv)
}

func _ClientService_Login_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(UserCredits)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ClientServiceServer).Login(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/ClientService/Login",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ClientServiceServer).Login(ctx, req.(*UserCredits))
	}
	return interceptor(ctx, in, info, handler)
}

func _ClientService_GetTranslation_Handler(srv interface{}, stream grpc.ServerStream) error {
	m := new(Empty)
	if err := stream.RecvMsg(m); err != nil {
		return err
	}
	return srv.(ClientServiceServer).GetTranslation(m, &clientServiceGetTranslationServer{stream})
}

type ClientService_GetTranslationServer interface {
	Send(*TextHistory) error
	grpc.ServerStream
}

type clientServiceGetTranslationServer struct {
	grpc.ServerStream
}

func (x *clientServiceGetTranslationServer) Send(m *TextHistory) error {
	return x.ServerStream.SendMsg(m)
}

func _ClientService_Register_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(UserCredits)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ClientServiceServer).Register(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/ClientService/Register",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ClientServiceServer).Register(ctx, req.(*UserCredits))
	}
	return interceptor(ctx, in, info, handler)
}

// ClientService_ServiceDesc is the grpc.ServiceDesc for ClientService service.
// It's only intended for direct use with grpc.RegisterService,
// and not to be introspected or modified (even as a copy)
var ClientService_ServiceDesc = grpc.ServiceDesc{
	ServiceName: "ClientService",
	HandlerType: (*ClientServiceServer)(nil),
	Methods: []grpc.MethodDesc{
		{
			MethodName: "Login",
			Handler:    _ClientService_Login_Handler,
		},
		{
			MethodName: "Register",
			Handler:    _ClientService_Register_Handler,
		},
	},
	Streams: []grpc.StreamDesc{
		{
			StreamName:    "GetTranslation",
			Handler:       _ClientService_GetTranslation_Handler,
			ServerStreams: true,
		},
	},
	Metadata: "backend/ApiServer/proto/authentication/authentication.proto",
}