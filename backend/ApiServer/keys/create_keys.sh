#!/bin/bash
openssl ecparam -genkey -name prime256v1 -noout -out ecdsa_private_key.pem
openssl ec -in ecdsa_private_key.pem -pubout -out ecdsa_public_key.pem