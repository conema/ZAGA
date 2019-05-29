import requests
import json

endpoint = "http://127.0.0.1:5002/chatbot"
message = "how are you?"
result = requests.post(endpoint, data = message)
text = json.loads(str(result.text))
print text