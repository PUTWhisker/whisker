FROM node:alpine AS webapp

WORKDIR /webApp

COPY package*.json ./ 
RUN npm install -D webpack

COPY . .

RUN npx webpack --mode=development


FROM nginx:alpine

COPY --from=webapp /webApp /usr/share/nginx/html/webApp

COPY ./swagger /usr/share/nginx/html/swagger

COPY ./nginx.conf /etc/nginx/conf.d/default.conf

