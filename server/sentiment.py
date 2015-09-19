from socket import *
from alchemyapi import AlchemyAPI
alchemyapi = AlchemyAPI()
import json
import pandas
import requests

HOST = "10.21.51.114" #local host
PORT = 8000 #open port 7000 for connection
s = socket(AF_INET, SOCK_STREAM)
s.bind((HOST, PORT))
s.listen(1) #how many connections can it receive at one time
conn, addr = s.accept() #accept the connection
print "Connected by: " , addr #print the address of the person connected
while True:
    data = conn.recv(1024) #how many bytes of data will the server receive
    print "Received: ", repr(data)
    url = 'http://access.alchemyapi.com/calls/text/TextGetTextSentiment'

    params = dict(
    	apikey='df798c566bb0d721db86139d2107b3f7f686dcab',
    	text=review,
    	outputMode='json'
    )
    resp = requests.get(url=url, params=params)
    reply = json.loads(resp.text)
    if "docSentiment" not in reply:
    	reply = "none"
    if 'score' in reply['docSentiment']:
        reply = float(reply['docSentiment']['score'])
    else:
        reply = reply['docSentiment']
    conn.sendall(raw_input(reply))
conn.close()
