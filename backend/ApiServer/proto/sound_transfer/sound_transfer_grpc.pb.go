// Code generated by protoc-gen-go-grpc. DO NOT EDIT.
// versions:
// - protoc-gen-go-grpc v1.5.1
// - protoc             v5.26.0
// source: backend/ApiServer/proto/sound_transfer/sound_transfer.proto

package sound_transfer

import (
	context "context"
	grpc "google.golang.org/grpc"
	codes "google.golang.org/grpc/codes"
	status "google.golang.org/grpc/status"
)

// This is a compile-time assertion to ensure that this generated file
// is compatible with the grpc package it is being compiled against.
// Requires gRPC-Go v1.64.0 or later.
const _ = grpc.SupportPackageIsVersion9

const (
	SoundService_TestConnection_FullMethodName = "/SoundService/TestConnection"
	SoundService_TranscribeFile_FullMethodName = "/SoundService/TranscribeFile"
	SoundService_TranscribeLive_FullMethodName = "/SoundService/TranscribeLive"
	SoundService_TranslateFile_FullMethodName  = "/SoundService/TranslateFile"
	SoundService_DiarizateFile_FullMethodName  = "/SoundService/DiarizateFile"
)

// SoundServiceClient is the client API for SoundService service.
//
// For semantics around ctx use and closing/ending streaming RPCs, please refer to https://pkg.go.dev/google.golang.org/grpc/?tab=doc#ClientConn.NewStream.
type SoundServiceClient interface {
	TestConnection(ctx context.Context, in *TextMessage, opts ...grpc.CallOption) (*TextMessage, error)
	TranscribeFile(ctx context.Context, in *TranscriptionRequest, opts ...grpc.CallOption) (*SoundResponse, error)
	TranscribeLive(ctx context.Context, opts ...grpc.CallOption) (grpc.BidiStreamingClient[TranscirptionLiveRequest, SoundStreamResponse], error)
	TranslateFile(ctx context.Context, in *TranslationRequest, opts ...grpc.CallOption) (grpc.ServerStreamingClient[SoundResponse], error)
	DiarizateFile(ctx context.Context, in *TranscriptionRequest, opts ...grpc.CallOption) (*SpeakerAndLineResponse, error)
}

type soundServiceClient struct {
	cc grpc.ClientConnInterface
}

func NewSoundServiceClient(cc grpc.ClientConnInterface) SoundServiceClient {
	return &soundServiceClient{cc}
}

func (c *soundServiceClient) TestConnection(ctx context.Context, in *TextMessage, opts ...grpc.CallOption) (*TextMessage, error) {
	cOpts := append([]grpc.CallOption{grpc.StaticMethod()}, opts...)
	out := new(TextMessage)
	err := c.cc.Invoke(ctx, SoundService_TestConnection_FullMethodName, in, out, cOpts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *soundServiceClient) TranscribeFile(ctx context.Context, in *TranscriptionRequest, opts ...grpc.CallOption) (*SoundResponse, error) {
	cOpts := append([]grpc.CallOption{grpc.StaticMethod()}, opts...)
	out := new(SoundResponse)
	err := c.cc.Invoke(ctx, SoundService_TranscribeFile_FullMethodName, in, out, cOpts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *soundServiceClient) TranscribeLive(ctx context.Context, opts ...grpc.CallOption) (grpc.BidiStreamingClient[TranscirptionLiveRequest, SoundStreamResponse], error) {
	cOpts := append([]grpc.CallOption{grpc.StaticMethod()}, opts...)
	stream, err := c.cc.NewStream(ctx, &SoundService_ServiceDesc.Streams[0], SoundService_TranscribeLive_FullMethodName, cOpts...)
	if err != nil {
		return nil, err
	}
	x := &grpc.GenericClientStream[TranscirptionLiveRequest, SoundStreamResponse]{ClientStream: stream}
	return x, nil
}

// This type alias is provided for backwards compatibility with existing code that references the prior non-generic stream type by name.
type SoundService_TranscribeLiveClient = grpc.BidiStreamingClient[TranscirptionLiveRequest, SoundStreamResponse]

func (c *soundServiceClient) TranslateFile(ctx context.Context, in *TranslationRequest, opts ...grpc.CallOption) (grpc.ServerStreamingClient[SoundResponse], error) {
	cOpts := append([]grpc.CallOption{grpc.StaticMethod()}, opts...)
	stream, err := c.cc.NewStream(ctx, &SoundService_ServiceDesc.Streams[1], SoundService_TranslateFile_FullMethodName, cOpts...)
	if err != nil {
		return nil, err
	}
	x := &grpc.GenericClientStream[TranslationRequest, SoundResponse]{ClientStream: stream}
	if err := x.ClientStream.SendMsg(in); err != nil {
		return nil, err
	}
	if err := x.ClientStream.CloseSend(); err != nil {
		return nil, err
	}
	return x, nil
}

// This type alias is provided for backwards compatibility with existing code that references the prior non-generic stream type by name.
type SoundService_TranslateFileClient = grpc.ServerStreamingClient[SoundResponse]

func (c *soundServiceClient) DiarizateFile(ctx context.Context, in *TranscriptionRequest, opts ...grpc.CallOption) (*SpeakerAndLineResponse, error) {
	cOpts := append([]grpc.CallOption{grpc.StaticMethod()}, opts...)
	out := new(SpeakerAndLineResponse)
	err := c.cc.Invoke(ctx, SoundService_DiarizateFile_FullMethodName, in, out, cOpts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

// SoundServiceServer is the server API for SoundService service.
// All implementations must embed UnimplementedSoundServiceServer
// for forward compatibility.
type SoundServiceServer interface {
	TestConnection(context.Context, *TextMessage) (*TextMessage, error)
	TranscribeFile(context.Context, *TranscriptionRequest) (*SoundResponse, error)
	TranscribeLive(grpc.BidiStreamingServer[TranscirptionLiveRequest, SoundStreamResponse]) error
	TranslateFile(*TranslationRequest, grpc.ServerStreamingServer[SoundResponse]) error
	DiarizateFile(context.Context, *TranscriptionRequest) (*SpeakerAndLineResponse, error)
	mustEmbedUnimplementedSoundServiceServer()
}

// UnimplementedSoundServiceServer must be embedded to have
// forward compatible implementations.
//
// NOTE: this should be embedded by value instead of pointer to avoid a nil
// pointer dereference when methods are called.
type UnimplementedSoundServiceServer struct{}

func (UnimplementedSoundServiceServer) TestConnection(context.Context, *TextMessage) (*TextMessage, error) {
	return nil, status.Errorf(codes.Unimplemented, "method TestConnection not implemented")
}
func (UnimplementedSoundServiceServer) TranscribeFile(context.Context, *TranscriptionRequest) (*SoundResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method TranscribeFile not implemented")
}
func (UnimplementedSoundServiceServer) TranscribeLive(grpc.BidiStreamingServer[TranscirptionLiveRequest, SoundStreamResponse]) error {
	return status.Errorf(codes.Unimplemented, "method TranscribeLive not implemented")
}
func (UnimplementedSoundServiceServer) TranslateFile(*TranslationRequest, grpc.ServerStreamingServer[SoundResponse]) error {
	return status.Errorf(codes.Unimplemented, "method TranslateFile not implemented")
}
func (UnimplementedSoundServiceServer) DiarizateFile(context.Context, *TranscriptionRequest) (*SpeakerAndLineResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method DiarizateFile not implemented")
}
func (UnimplementedSoundServiceServer) mustEmbedUnimplementedSoundServiceServer() {}
func (UnimplementedSoundServiceServer) testEmbeddedByValue()                      {}

// UnsafeSoundServiceServer may be embedded to opt out of forward compatibility for this service.
// Use of this interface is not recommended, as added methods to SoundServiceServer will
// result in compilation errors.
type UnsafeSoundServiceServer interface {
	mustEmbedUnimplementedSoundServiceServer()
}

func RegisterSoundServiceServer(s grpc.ServiceRegistrar, srv SoundServiceServer) {
	// If the following call pancis, it indicates UnimplementedSoundServiceServer was
	// embedded by pointer and is nil.  This will cause panics if an
	// unimplemented method is ever invoked, so we test this at initialization
	// time to prevent it from happening at runtime later due to I/O.
	if t, ok := srv.(interface{ testEmbeddedByValue() }); ok {
		t.testEmbeddedByValue()
	}
	s.RegisterService(&SoundService_ServiceDesc, srv)
}

func _SoundService_TestConnection_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(TextMessage)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(SoundServiceServer).TestConnection(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: SoundService_TestConnection_FullMethodName,
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(SoundServiceServer).TestConnection(ctx, req.(*TextMessage))
	}
	return interceptor(ctx, in, info, handler)
}

func _SoundService_TranscribeFile_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(TranscriptionRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(SoundServiceServer).TranscribeFile(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: SoundService_TranscribeFile_FullMethodName,
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(SoundServiceServer).TranscribeFile(ctx, req.(*TranscriptionRequest))
	}
	return interceptor(ctx, in, info, handler)
}

func _SoundService_TranscribeLive_Handler(srv interface{}, stream grpc.ServerStream) error {
	return srv.(SoundServiceServer).TranscribeLive(&grpc.GenericServerStream[TranscirptionLiveRequest, SoundStreamResponse]{ServerStream: stream})
}

// This type alias is provided for backwards compatibility with existing code that references the prior non-generic stream type by name.
type SoundService_TranscribeLiveServer = grpc.BidiStreamingServer[TranscirptionLiveRequest, SoundStreamResponse]

func _SoundService_TranslateFile_Handler(srv interface{}, stream grpc.ServerStream) error {
	m := new(TranslationRequest)
	if err := stream.RecvMsg(m); err != nil {
		return err
	}
	return srv.(SoundServiceServer).TranslateFile(m, &grpc.GenericServerStream[TranslationRequest, SoundResponse]{ServerStream: stream})
}

// This type alias is provided for backwards compatibility with existing code that references the prior non-generic stream type by name.
type SoundService_TranslateFileServer = grpc.ServerStreamingServer[SoundResponse]

func _SoundService_DiarizateFile_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(TranscriptionRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(SoundServiceServer).DiarizateFile(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: SoundService_DiarizateFile_FullMethodName,
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(SoundServiceServer).DiarizateFile(ctx, req.(*TranscriptionRequest))
	}
	return interceptor(ctx, in, info, handler)
}

// SoundService_ServiceDesc is the grpc.ServiceDesc for SoundService service.
// It's only intended for direct use with grpc.RegisterService,
// and not to be introspected or modified (even as a copy)
var SoundService_ServiceDesc = grpc.ServiceDesc{
	ServiceName: "SoundService",
	HandlerType: (*SoundServiceServer)(nil),
	Methods: []grpc.MethodDesc{
		{
			MethodName: "TestConnection",
			Handler:    _SoundService_TestConnection_Handler,
		},
		{
			MethodName: "TranscribeFile",
			Handler:    _SoundService_TranscribeFile_Handler,
		},
		{
			MethodName: "DiarizateFile",
			Handler:    _SoundService_DiarizateFile_Handler,
		},
	},
	Streams: []grpc.StreamDesc{
		{
			StreamName:    "TranscribeLive",
			Handler:       _SoundService_TranscribeLive_Handler,
			ServerStreams: true,
			ClientStreams: true,
		},
		{
			StreamName:    "TranslateFile",
			Handler:       _SoundService_TranslateFile_Handler,
			ServerStreams: true,
		},
	},
	Metadata: "backend/ApiServer/proto/sound_transfer/sound_transfer.proto",
}
