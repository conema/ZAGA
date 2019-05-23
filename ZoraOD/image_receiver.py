#Rest API per la comunicazione HTTP con il robot Zora

from flask import Flask, request
from flask_restful import Resource, Api
import obj_detection
import time
from hdfs import InsecureClient
import socket
import os

app = Flask(__name__)
api = Api(app)


# risorsa per l'object detection di un'immagine scattata dal robot
class ObjectDetection(Resource):
    def post(self):
        image_name = int(time.time()) #per avere sempre immagini con nomi diversi utilizzo la funzione time di python
        image_path="/root/Zora-Object-Detection/Images_bbx/{}.jpg".format(image_name)
        with open(image_path, 'wb') as image:
            image.write(request.data) #l'immagine contenuta in request.data viene salvata in locale
        # result e' il risultato dell'object detection. Puo' essere la stringa che deve pronunciare il robot,
        # oppure le possibili labels che identificano un oggetto nell'immagine se lo score e' compreso tra due soglie.
        # Viene restituito il vettore nullo se non e' stato trovato nessun oggetto
        result = obj_detection.find_result(image_path, image_name)

        # se l'hdfs e' connesso vi salvo l'immagine
        # uso il modulo socket per controllare se la connessione con la porta dell'hdfs e'attiva
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        port_result = sock.connect_ex(('localhost', 50070))
        # se la porta e' aperta restituisce 0, altrimenti restituisce un valore diverso da 0
        if port_result == 0:
            client_hdfs = InsecureClient('http://localhost:50070')
            # sposto l'immagine nel HDFS
            client_hdfs.upload('/zora-object-detection/images/{}.jpg'.format(image_name), image_path)
            os.remove(image_path)

        return result #il risultato viene inviato al robot


api.add_resource(ObjectDetection, '/image-receiver') #aggiunge la risorsa all'API


if __name__ == '__main__':
	app.run(host="0.0.0.0", port=4002)
