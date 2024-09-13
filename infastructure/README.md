#  whisper-server

##  How to run

- If you have Debian Python libraries, specify their path in the docker-compose file. Otherwise, uncomment the two lines in the `./backend/Dockerfile`.
- Run `docker-compose build` & `docker-compose up`
- Now make gRPC calls to proxy server port (default 50051)