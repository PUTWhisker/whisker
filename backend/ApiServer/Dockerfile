FROM golang:1.22 AS golang-builder

WORKDIR /builder

COPY . .

RUN go mod download
RUN CGO_ENABLED=0 GOOS=linux go build -o golang-server

FROM debian:stable-slim as main-stage

WORKDIR /server

COPY --from=golang-builder /builder/golang-server .
COPY --from=golang-builder /builder/keys /server/keys

# Can be deleted???
RUN mkdir -p "/server/SOUND_FILES" \
    && chmod -R 0666 "/server/SOUND_FILES"

RUN chmod -R 0666 "/server/keys"

ENV JWT_PUBLIC_KEY_PATH=/server/keys/ecdsa_public_key.pem
ENV JWT_PRIVATE_KEY_PATH=/server/keys/ecdsa_private_key.pem
ENV SOUND_FILES_FOLDER_PATH=/server/SOUND_FILES

CMD ["./golang-server"]
