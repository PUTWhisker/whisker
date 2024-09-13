FROM golang:1.22 AS golang-builder

WORKDIR /builder

COPY ./src .


RUN go mod download
RUN CGO_ENABLED=0 GOOS=linux go build -o golang-server

############################### UNCOMMENT THIS ################################

FROM python:3.12-slim AS python-builder

WORKDIR /builder
COPY requirements.txt .
RUN pip install -r /builder/requirements.txt

############################### UNCOMMENT THIS ################################

FROM python:3.12-slim AS main-image

WORKDIR /server

COPY --from=golang-builder /builder/golang-server .
COPY --from=golang-builder /builder/keys /server/keys
COPY --from=python-builder /usr/local /usr/local

RUN apt-get update && apt-get install -y \
                        ffmpeg
 
RUN mkdir -p "/server/SOUND_FILES" \
    && chmod -R 0666 "/server/SOUND_FILES"

ARG SERVER_PORT=9900
EXPOSE ${SERVER_PORT}
ENV SERVER_PORT=${SERVER_PORT}


ENV JWT_PUBLIC_KEY_PATH=/server/keys/ecdsa_public_key.pem
ENV JWT_PRIVATE_KEY_PATH=/server/keys/ecdsa_private_key.pem
ENV DATABASE_URL=postgres://postgres:mysecretpassword@host.docker.internal:5432/whisperDatabase


CMD ["./golang-server"]

# ###############################################################################

# FROM debian:stable-slim 

# WORKDIR /server

# COPY --from=golang-builder /builder/golang-server .


# RUN apt-get update && apt-get install -y \
#          python3 \
#          

# COPY --from=python-builder /builder/venv ./venv

# RUN mkdir -p "/server/SOUND_FILES" \
#     && chmod -R 0666 "/server/SOUND_FILES"
# ENV PATH="/server/venv/bin:$PATH"
# ENV MODEL_PATH="/server/venv/bin/whisper"
# ENV SOUND_FILES_FOLDER_PATH="/golang/SOUND_FILES"

# EXPOSE 9900

# CMD ["./golang-server"]

################################################################
