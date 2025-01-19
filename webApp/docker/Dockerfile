FROM node:alpine

WORKDIR /usr/src/app/webApp

COPY package*.json ./
RUN npm install -D webpack

COPY . .

RUN npx webpack --mode=development

WORKDIR /usr/src/app
RUN npm install -g http-server

EXPOSE 8080
# Command to run the web server
CMD ["http-server", "-p", "8080"]