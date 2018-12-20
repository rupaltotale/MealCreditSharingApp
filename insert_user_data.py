import requests as req

def insertion():
    stuff = req.post('http://127.0.0.1:8000/user/ben/glossner/bgslide/hello')

insertion()