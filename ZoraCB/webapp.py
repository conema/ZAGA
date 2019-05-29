from flask import Flask, request
from flask_restful import Resource, Api
from json import dumps
from flask_jsonpify import jsonify
import chatbot

app = Flask(__name__)
app.config['DEBUG'] = False
api = Api(app)


class Chatbot(Resource):
	def post(self):
		message = str(request.data)
		print ("MESSAGE:"+ message)
		answer = chatbot.reply(message)
		return jsonify(answer)

api.add_resource(Chatbot, '/chatbot')

if __name__ == '__main__':
	app.run(host= '0.0.0.0', port=4003,threaded=False)
 
