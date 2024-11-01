FROM node:alpine

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install -g http-server

COPY . .
EXPOSE 8080

# Command to run the web server
CMD ["http-server", "-p", "8080"]