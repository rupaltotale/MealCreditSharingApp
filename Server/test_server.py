import urllib.request
import unittest

class TestLab1(unittest.TestCase):
    """def test_00_server(self):
        print("TESTING SERVER IS UP ON LOCALHOST")
        stuff = urllib.request.urlopen('http://127.0.0.1:8000/')
        self.assertEqual(stuff.read(), b'Start')

    def test_01_availabilty(self):
        print("BASIC TEST FOR AVAILABILTY LIST")
        stuff = urllib.request.urlopen('http://127.0.0.1:8000/availability-list')
        print(stuff.read())"""
        """print("SIMPLE DATABASE QUERIES")
        print("LOCATION")
        stuff = urllib.request.urlopen('http://127.0.0.1:8000/availability-list/-1/Mustang/false/false/false/false')
        self.assertEqual(stuff.read(), b'[{"user_id":4,"asking_price":3,"location":"Mustang","start_time":"2019-07-02T20:31:00.000Z","end_time":"2019-07-02T23:31:00.000Z"},{"user_id":7,"asking_price":5.5,"location":"Mustang","start_time":"2019-09-27T09:15:00.000Z","end_time":"2019-09-27T12:15:00.000Z"}]')
        print("USERNAME")
        stuff = urllib.request.urlopen('http://127.0.0.1:8000/availability-list/2/false/inj_xzi6/false/false/false')
        self.assertEqual(stuff.read(), b'[{"user_id":7,"asking_price":3,"location":"Einstein","start_time":"2019-12-26T01:41:00.000Z","end_time":"2019-12-26T04:41:00.000Z"},{"user_id":7,"asking_price":5.5,"location":"Mustang","start_time":"2019-09-27T09:15:00.000Z","end_time":"2019-09-27T12:15:00.000Z"}]')
        print("PRICE")
        stuff = urllib.request.urlopen('http://127.0.0.1:8000/availability-list/-1/false/false/false/false/2.75')
        self.assertEqual(stuff.read(), b'[{"user_id":6,"asking_price":2,"location":"Avenue","start_time":"2019-01-25T01:14:00.000Z","end_time":"2019-01-25T04:14:00.000Z"},{"user_id":8,"asking_price":2.5,"location":"Campus Market","start_time":"2019-07-17T21:57:00.000Z","end_time":"2019-07-18T00:57:00.000Z"}]')
        print("START TIME")
        stuff = urllib.request.urlopen('http://127.0.0.1:8000/availability-list/-1/false/false/00:00:09/00:00:17/false')
        print(stuff.read())
        print("END TIME")
        stuff = urllib.request.urlopen('http://127.0.0.1:8000/availability-list/-1/false/false/00:00:00/00:00:07/false')
        print(stuff.read())"""

if __name__ == '__main__': 
    unittest.main()