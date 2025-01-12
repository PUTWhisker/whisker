FROM swaggerapi/swagger-ui AS swagger
# Perhaps it tries to copy to dest file from env path, so copy the file to different one and make an env path to

COPY ./api/gen/proto.swagger.json /usr/share/nginx/proto.swagger.json

ENV SWAGGER_JSON=/usr/share/nginx/proto.swagger.json

EXPOSE 8080