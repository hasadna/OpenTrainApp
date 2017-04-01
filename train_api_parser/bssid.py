#!/usr/bin/env python

from firebase import firebase
from os import environ
import requests

"""
    Migrate stops list from old server
"""

OLD_BSSIDS_URL = 'http://gtfs.otrain.org/api/data/bssids/?format=json'
FIREBASE_URL = 'https://opentrain-eca68.firebaseio.com/'

FIREBASE_ADMIN_EMAIL = ('firebase-adminsdk-f8787@opentrain-eca68.iam'
                        '.gserviceaccount.com')

def move_old_ssids_to_database():
    print('Authenticating in Firebase...')
    auth = firebase.FirebaseAuthentication(
        environ['FIREBASE_SECRET'],
        FIREBASE_ADMIN_EMAIL)
    app = firebase.FirebaseApplication(FIREBASE_URL, authentication=auth)
    print('Getting stops from old server...')
    old_json = requests.get(OLD_BSSIDS_URL).json()
    print('Putting into Firebase')
    app.put('/data/', 'bssids', old_json)


if __name__ == "__main__":
    move_old_ssids_to_database()