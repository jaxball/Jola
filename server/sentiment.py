from flask import Flask, request
import json
import requests

app = Flask(__name__)


@app.route('/', methods=['POST'])
def hello_world():
    url = 'http://access.alchemyapi.com/calls/text/TextGetTextSentiment'
    params = dict(
        apikey='df798c566bb0d721db86139d2107b3f7f686dcab',
        text=request.args.get('data'),
        outputMode='json'
    )

    resp = requests.get(url=url, params=params)
    reply = json.loads(resp.text)
    if 'docSentiment' not in reply:
        reply = 'none'
    if 'score' in reply['docSentiment']:
        reply = float(reply['docSentiment']['score'])
    else:
        reply = reply['docSentiment']
    return str(reply)

if __name__ == '__main__':
    app.run()
