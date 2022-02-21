FROM docker.io/python:3.8 as builddeploy

WORKDIR /app

RUN apt-get update -y
RUN apt-get install -y --no-install-recommends \
        libatlas-base-dev gfortran nginx supervisor

RUN pip3 install uwsgi && \
    useradd --no-create-home nginx && \
    rm /etc/nginx/sites-enabled/default && \
    rm -r /root/.cache

COPY ./requirements.txt /app/requirements.txt

RUN pip3 install -r /app/requirements.txt && \
    python -m spacy download en_core_web_md

COPY server-conf/nginx.conf /etc/nginx/
COPY server-conf/flask-site-nginx.conf /etc/nginx/conf.d/
COPY server-conf/uwsgi.ini /etc/uwsgi/
COPY server-conf/supervisord.conf /etc/supervisor/

COPY src /app/src

CMD ["/usr/bin/supervisord"]
