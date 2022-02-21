## Base Image
FROM registry.access.redhat.com/ubi7/nodejs-12 AS builder

## Folder to put Source Code and run commands on
WORKDIR /opt/app-root/src

COPY package.json /opt/app-root/src
RUN npm install @angular/cli && npm install
COPY . /opt/app-root/src
EXPOSE 4200

## Run application, listening on all interfaces and making it accessible from the host machine
CMD ["ng", "serve","--configuration", "development", "--host", "0.0.0.0","--port","4200","--disable-host-check","--poll"]