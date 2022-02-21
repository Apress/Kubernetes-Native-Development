FROM registry.access.redhat.com/ubi7/nodejs-12 AS builder
# Stage 1: build the app
WORKDIR /opt/app-root/src

COPY package.json /opt/app-root/src
RUN npm install
COPY . /opt/app-root/src

RUN ng build --configuration production

# Stage 2: serve it with nginx
FROM nginx AS deploy

LABEL maintainer="Max Dargatz"

COPY --from=builder /opt/app-root/src/dist/news-frontend /usr/share/nginx/html

EXPOSE 80

CMD ["/bin/sh",  "-c",  "envsubst < /usr/share/nginx/html/assets/settings.template.json > /usr/share/nginx/html/assets/settings.json && exec nginx -g 'daemon off;'"]
