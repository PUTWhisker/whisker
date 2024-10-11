#! /bin/bash

ln proto/authentication.proto backend/src/proto/authentication/authentication.proto
ln proto/sound_transfer.proto backend/src/proto/sound_transfer/sound_transfer.proto


mkdir backend/src/keys
openssl ecparam -name prime256v1 -genkey -noout -out backend/src/keys/ecdsa_private_key.pem
openssl ec -in backend/src/keys/ecdsa_private_key.pem -pubout -out backend/src/keys/ecdsa_public_key.pem


mkdir mobile/src/main/proto/io/grpc