FROM docker.io/python:3.8

WORKDIR /app/

COPY requirements.txt requirements.txt
RUN pip install -r requirements.txt && python -m spacy download en_core_web_md

COPY src/server.py /app/src/server.py

EXPOSE 5000

ENTRYPOINT ["python", "-X", "utf8", "-u", "src/server.py"]