package services

import (
	"context"
	"fmt"
	"log"
	"os"

	"github.com/golang-jwt/jwt/v5"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/metadata"
	"google.golang.org/grpc/status"
)

var (
	errMissingMetadata = status.Errorf(codes.InvalidArgument, "missing metadata")
	errInvalidToken    = status.Errorf(codes.Unauthenticated, "invalid token")
	nonAuthMethods     = map[string]bool{
		"/ClientService/Login":        true,
		"/ClientService/Register":     true,
		"/ClientService/RefreshToken": true,
	}
	nonAuthServices = map[string]bool{
		"/SoundService": true,
	}
)

func parseJWT(tokenString string) (*jwt.Token, error) {
	publicKey, err := loadPublicECDSAKeyFromFile(os.Getenv("JWT_PUBLIC_KEY_PATH"))

	if err != nil {
		log.Panicf("Failed to load public key %v", err)
	}

	token, err := jwt.ParseWithClaims(tokenString, &UserClaims{}, func(token *jwt.Token) (interface{}, error) {
		return publicKey, nil
	})
	if err != nil {
		return nil, err
	} else if _, ok := token.Claims.(*UserClaims); ok {
	} else {
		return nil, status.Error(1, "unknown claims type, cannot proceed")
	}

	// Check if the token is valid
	if !token.Valid {
		return nil, errInvalidToken
	}

	// Return the verified token
	return token, nil
}

func getUserId(authorization []string) (string, error) {

	if len(authorization) < 1 {
		return "", errInvalidToken
	}
	token, err := parseJWT(authorization[0])
	if err != nil {
		return "", errInvalidToken
	}
	claims := token.Claims.(*UserClaims)
	fmt.Print(claims)
	return claims.User_id, nil
}

func JwtUnaryInterceptor(ctx context.Context, req any, info *grpc.UnaryServerInfo, handler grpc.UnaryHandler) (any, error) {
	// authentication (token verification)
	methodName := info.FullMethod
	if nonAuthMethods[methodName] {
		return handler(ctx, req)
	}
	if nonAuthServices[methodName[0:len("/SoundService")]] {
		return handler(ctx, req)
	}
	md, ok := metadata.FromIncomingContext(ctx)
	if !ok {
		return nil, errMissingMetadata
	}
	user_id, err := getUserId(md["jwt"])
	if err != nil {
		return nil, err
	}
	ctx = context.WithValue(ctx, "user_id", user_id)
	m, err := handler(ctx, req)
	if err != nil {
		log.Printf("RPC failed with error: %v", err)
	}
	return m, err
}

type wrappedStream struct {
	grpc.ServerStream
	ctx context.Context
}

func (w *wrappedStream) Context() context.Context {
	return w.ctx
}

func newWrappedStream(s grpc.ServerStream, ctx context.Context) grpc.ServerStream {
	return &wrappedStream{s, ctx}
}

func JwtStreamInterceptor(srv any, ss grpc.ServerStream, info *grpc.StreamServerInfo, handler grpc.StreamHandler) error {
	// authentication (token verification)
	methodName := info.FullMethod
	if nonAuthServices[methodName[0:len("/SoundService")]] {
		return handler(srv, grpc.ServerStream(ss))
	}
	md, ok := metadata.FromIncomingContext(ss.Context())
	if !ok {
		return errMissingMetadata
	}
	user_id, err := getUserId(md["jwt"])
	ctx := ss.Context()
	new_ctx := context.WithValue(ctx, "user_id", user_id)
	if err != nil {
		return err
	}

	err = handler(srv, newWrappedStream(ss, new_ctx))
	if err != nil {
		fmt.Printf("RPC failed with error: %v", err)
	}
	return err
}

func GetUserNameFromMetadata(metadata metadata.MD) (string, error) {
	// no metadata attached
	if metadata["jwt"] == nil {
		return "", nil
	}
	if metadata["jwt"][0] == "" {
		return "", nil
	}
	unverifiedToken := metadata["jwt"][0]
	token, err := parseJWT(unverifiedToken)
	if err != nil {
		return "", err
	}
	claims := token.Claims.(*UserClaims)
	return claims.User_id, nil
}
