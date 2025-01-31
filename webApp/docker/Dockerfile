FROM node:alpine AS webapp

WORKDIR /webApp

COPY package*.json ./ 
RUN npm install -D webpack

COPY . .

RUN npx webpack --mode=development


FROM nginx:alpine

COPY --from=webapp /webApp /usr/share/nginx/html/webApp

COPY ./swagger/swagger /usr/share/nginx/html/swagger

COPY nginx.conf /etc/nginx/conf.d/default.conf

# EXPOSE 8080

# WORKDIR /usr/share/nginx

# CMD ["nginx", "-g", "daemon off;"]

# FROM swaggerapi/swagger-ui AS swagger

# COPY --from=webapp /usr/src/app/webApp /usr/share/nginx/html/webApp
# COPY ./swagger/api/gen/proto.swagger.json /usr/share/nginx/proto.swagger.json

# # Set the environment variable for Swagger UI to pick up your custom swagger file
# ENV SWAGGER_JSON=/usr/share/nginx/proto.swagger.json

# # Copy the custom nginx configuration to the correct directory
# COPY default.conf /etc/nginx/conf.d

# EXPOSE 8080
