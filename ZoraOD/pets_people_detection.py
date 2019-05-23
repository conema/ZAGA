#!/usr/bin/env python2.7

from scipy import misc
import numpy as np
from object_detection.utils import visualization_utils as vis_util
from object_detection.utils import label_map_util
from hdfs import InsecureClient
import socket
import os

# Metodo per trovare le labels associate ad un'immagine in input nei modelli basati sui dataset specifici:
# - people model (modello basato sul dataset di uomini e donne)
# - pets model (modello basato sul dataset delle razze di cani e gatti)
def find_labels(image_path, image_name, stub, request, model, n):
    """
    Args:
        image_path: path dell'immagine in input
        image_name: nome dell'immagine ottenuto con la funzione time di python
        stub: viene utilizzato per la comunicazione client-server
        request: richiesta da inviare al server
        model: nome del modello di object detection, puo' essere pet model o people model
        n: numero massimo delle labels che si vogliono considerare
    """
    labels = []  # vettore con le labels del dataset specifico
    bbx = []  # vettore con le coordinate dei bounding box trovati
    request.model_spec.name = model
    result = stub.Predict(request, 10.0)  # risultati della richiesta di prediction, 10 secs timeout
    classes = result.outputs['detection_classes'].float_val  # id delle classi trovate, in ordine dalla classe con score piu' alto
    scores = result.outputs['detection_scores'].float_val  # score delle classi,dallo score piu' alto
    #print zip(classes, scores)
    boxes = result.outputs['detection_boxes'].float_val  # posizione dei bounding box
    # trasformo il vettore in modo che ogni elemento sia una quadrupla che identifica il bounding box
    boxes = np.reshape(boxes, [100, 4])

    # per salvare l'immagine con i bounding box, dobbiamo aprire l'immagine e sfruttare la libreria vis_util di tensorflow
    im = misc.imread(image_path)  # legge l'immagine come un array multidimensionale
    if(model == "pets_model"):
        label_map_path = "Label_maps/pets_label_map.pbtxt" # mappa delle label
        label_map = label_map_util.load_labelmap(label_map_path)
        categories = label_map_util.convert_label_map_to_categories(label_map=label_map, max_num_classes=37)
    else:
        label_map_path = "Label_maps/people_label_map.pbtxt"
        label_map = label_map_util.load_labelmap(label_map_path)
        categories = label_map_util.convert_label_map_to_categories(label_map=label_map, max_num_classes=2)
    category_index = label_map_util.create_category_index(categories)  # dizionario coppie chiave ("id"), valore ("nome classe")

    # viene creato un array (img_height, img_width, 3) con i bounding box sovrapposti
    image_vis = vis_util.visualize_boxes_and_labels_on_image_array(
        im,
        boxes,
        np.squeeze(classes).astype(np.int32),
        np.squeeze(scores),
        category_index,
        max_boxes_to_draw=10,  # num max di bounding box da visualizzare
        min_score_thresh=.6,  # soglia minima dei bounding box da visualizzare
        use_normalized_coordinates=True,
        line_thickness=5)  # larghezza linea del contorno dei box

    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    port_result = sock.connect_ex(('localhost', 50070))
    client_hdfs = InsecureClient('http://localhost:50070')  # client per accedere al HDFS
    if(model == "pets_model"):
        misc.imsave("Images_bbx/{}_pets.jpg".format(image_name),image_vis)  # salva l'array in locale come un'immagine JPEG
        if port_result == 0:  # se l'HDFS e' connesso, vi sposto l'immagine
            client_hdfs.upload('/zora-object-detection/images/{}_pets.jpg'.format(image_name),'Images_bbx/{}_pets.jpg'.format(image_name))
            os.remove("Images_bbx/{}_pets.jpg".format(image_name))
    else:
        misc.imsave("Images_bbx/{}_people.jpg".format(image_name),image_vis)
        if port_result == 0:
            client_hdfs.upload('/zora-object-detection/images/{}_people.jpg'.format(image_name),'Images_bbx/{}_people.jpg'.format(image_name))
            os.remove("Images_bbx/{}_people.jpg".format(image_name))

    # inseriamo le labels trovate nella detection in un vettore da passare allo script obj_detection per formare la stringa
    # da far pronunciare al robot. Le coordinate del bounding box invece verranno salvate nel file log dell'HDFS.
    boxes=boxes.tolist()  # trasforma l'array multidimensionale in una lista
    for i in range(0,n):
        # considero solo le labels con uno score >= 0.6 ed escludo quelle che identificano un bounding box gia' inserito
        # con uno score piu' alto
        if(scores[i] >= 0.6 and boxes[i] not in bbx):
            bbx.append(boxes[i])
            labels.append(str(category_index[int(classes[i])]['name']))

    return labels, bbx
