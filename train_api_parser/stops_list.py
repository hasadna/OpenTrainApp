#!/usr/bin/env python

from firebase import firebase
from os import environ
import requests

"""
    Migrate stops list from old server
"""

OLD_STOPS_URL = 'http://otrain.org/api/v1/stops/'
FIREBASE_URL = 'https://opentrain-eca68.firebaseio.com/'
FIREBASE_ADMIN_EMAIL = ('firebase-adminsdk-f8787@opentrain-eca68.iam'
                        '.gserviceaccount.com')

def move_old_stations_to_firebase():
    print('Authenticating in Firebase...')
    auth = firebase.FirebaseAuthentication(
        environ['FIREBASE_SECRET'],
        FIREBASE_ADMIN_EMAIL)
    app = firebase.FirebaseApplication(FIREBASE_URL, authentication=auth)
    print('Getting stops from old server...')
    old_json = requests.get(OLD_STOPS_URL).json()
    print('Putting into Firebase')
    app.put('/', 'stops', old_json)


if __name__ == "__main__":
    move_old_stations_to_firebase()