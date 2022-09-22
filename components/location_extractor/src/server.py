from flask import Flask, request, jsonify
import spacy
from geopy.geocoders import Nominatim
import json
import random

nlp_de = spacy.load('en_core_web_md')

app = Flask(__name__)

@app.route('/')
def home():
    return 'use /get_loc?text="...." to use this service'

@app.route('/get_loc')
def get_coords():
    text = request.args.get('text')
    doc = nlp_de(text)
    print("Analyzing this text:" + doc.text, flush=True)

    locations = []
    if doc.ents:
        print("Found the following entities in this text:" + str(doc.ents), flush=True)
        for ent in doc.ents:
            if ent.label_ == "GPE" or ent.label_ == "LOC":
                locations.append(ent.text)        
        locs_dict = {}
        if locations:
            print("Those entities were recognized as locations:" + str(locations), flush=True)
            geolocator = Nominatim(user_agent="my_app")
            for idx, location in enumerate(locations):
                try:
                    loc = geolocator.geocode(location)
                    lat = loc.latitude
                    long = loc.longitude
                    locs_dict[idx+1] = {"extracted location": location, "generated address": loc.address, "latitude": lat - random.uniform(0.05, 2), "longitude": long + random.uniform(0.05, 2)}
                    print("found lat & long for this location: " + str(location), flush=True)
                except:
                    print("not found lat & long for this location:" + str(location), flush=True)
            jsonDict = json.dumps(locs_dict)
        else:
            print("Not found any location in this text", flush=True)
            jsonDict = {
                "1": {
                    "extracted location": "none",
                    "generated address": "Brisbane City, Queensland, Australia",
                    "latitude": 0.4689682 - random.uniform(0.1, 5),
                    "longitude": -30.0234991 + random.uniform(0.1, 5)
                }
            }
    else:
        print("Not found any location in this text", flush=True)
        jsonDict = {
                        "1": {
                            "extracted mirror location": "none",
                            "generated address": "Brisbane City, Queensland, Australia",
                            "latitude": 0.4689682 - random.uniform(0.1, 5),
                            "longitude": -30.0234991 + random.uniform(0.1, 5)
                        }
                    }

    return jsonDict, 200, {'Content-Type': 'application/json; charset=utf-8'}

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)