from grpc.beta import implementations
import tensorflow as tf
from scipy import misc
import numpy as np
import pets_people_detection
from collections import Counter
from hdfs import InsecureClient
from object_detection.utils import visualization_utils as vis_util
from object_detection.utils import label_map_util
from tensorflow_serving.apis import predict_pb2
from tensorflow_serving.apis import prediction_service_pb2
import os
import imageio
import socket

# Metodo per trovare il risultato dell'object detection.
# Il metodo effettua una chiamata gRPC al model server di Tensorflow, dove sono caricati i modelli di object detection.
# Il metodo puo' restituire una stringa contenente gli oggetti trovati se lo score e' superiore ad una soglia massima,
# restituire un vettore con le varie possibilita' nel caso di valori compresi tra una soglia mimina e una soglia massima
# o restituire un vettore nullo per valori inferiori alla soglia minima.
# Viene effettuata prima una detection sul COCO Model di oggetti generali e, nel caso di presenza di persone o cani e
# gatti, si effettua una detection nei rispettivi modelli specifici Pets Model e People Model, per riconoscere il sesso
# delle persone o la razza dei cani e dei gatti.
def find_result(image_path, image_name):

    result = []  # risultato dell'object detection
    with open(image_path, 'rb') as f:
        data = f.read()  # legge l'immagine di input

    channel = implementations.insecure_channel('0.0.0.0', 4001)  # crea un canale non sicuro con l'host, numero di porta 4001

    # crea il servizio di prediction che permette di accedere ai modelli caricati nel model server
    stub = prediction_service_pb2.beta_create_PredictionService_stub(channel)

    request = predict_pb2.PredictRequest()  # richiesta da mandare al server
    # PredictRequest specifica quale modello TensorFlow eseguire, quali sono i tensori di input e come sono filtrati
    # gli output prima di essere restituiti all'utente
    request.model_spec.name = 'coco_model'
    request.model_spec.signature_name = 'detection_signature'  # signature del modello
    request.inputs['inputs'].CopyFrom(tf.contrib.util.make_tensor_proto(data, shape=[1]))  # tensori di input
    res = stub.Predict(request, 10.0)  # risultati della richiesta di prediction, 10 secs timeout

    scores = res.outputs['detection_scores'].float_val  # score degli oggetti trovati in ordine decrescente
    classes = res.outputs['detection_classes'].float_val  # id delle classi trovate, ordinate con score decrescente
    # print zip(classes, scores)
    # vettore con la posizione normalizzata dei bounding box dell'immagine: ymin, xmin, ymax, xmax
    # i bounding box sono ordinati dal bbx dell'oggetto con score maggiore
    boxes = res.outputs['detection_boxes'].float_val
    # trasformo il vettore in modo che ogni elemento sia una quadrupla che identifica il bounding box
    boxes = np.reshape(boxes, [100, 4])

    # per salvare l'immagine con i bounding box, dobbiamo aprire l'immagine e sfruttare la libreria vis_util di tensorflow
    im = imageio.imread(image_path)  # legge l'immagine come un array multidimensionale
    label_map_path = "Label_maps/mscoco_label_map.pbtxt"
    label_map = label_map_util.load_labelmap(label_map_path) # mappa id-label
    categories = label_map_util.convert_label_map_to_categories(label_map=label_map, max_num_classes=90)  # lista di dizionari
    category_index = label_map_util.create_category_index(categories)  # dizionario coppie chiave ("id"), valore ("nome classe")

    # viene creato un array (img_height, img_width, 3) con i bounding box sovrapposti
    image_vis = vis_util.visualize_boxes_and_labels_on_image_array(
        im,
        boxes,
        np.squeeze(classes).astype(np.int32),
        np.squeeze(scores),
        category_index,
        max_boxes_to_draw=10, # num max di bounding box da visualizzare
        min_score_thresh=.2,  # soglia minima dei bounding box da visualizzare
        use_normalized_coordinates=True,
        line_thickness=5)  # larghezza linea del contorno dei box

    imageio.imwrite("Images_bbx/{}_coco.jpg".format(image_name), image_vis)  # salva l'array come un'immagine JPEG

    client_hdfs = InsecureClient('http://localhost:50070')  # client per accedere al HDFS

    #tramite il modulo socket verifichiamo se la porta dell'hdfs e' connessa
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM) #crea un nuovo socket
    port_result = sock.connect_ex(('localhost', 50070))
    # se la porta e' attiva restituisce 0, altrimenti un valore diverso da 0
    if port_result == 0:  # se l'HDFS e' connesso, vi sposto l'immagine con i bounding box predetti
        client_hdfs.upload('/zora-object-detection/images/{}_coco.jpg'.format(image_name),"Images_bbx/{}_coco.jpg".format(image_name))
        os.remove("Images_bbx/{}_coco.jpg".format(image_name))
    vowels = ("a", "e", "i", "o", "u")
    bbx_coco = []  # vettore con le coordinate dei bounding box trovati con il COCO Model

    # se lo score dell'oggetto con score piu' alto nell'immagine e' maggiore o uguale a 0.6,
    # si effettua la detection e si costruisce la stringa da passare al robot
    if (scores[0] >= 0.6):
        labels_coco = []  # vettore delle labels trovate con il COCO Model
        labels_pets = []  # vettore delle labels trovate con il Pets Model
        labels_people = []  # vettore delle labels trovate con il People Model
        bbx_pets = []  # coordinate dei bounding box relative al Pets Model
        bbx_people = []  # coordinate dei bounding box relative al modello people
        boxes=boxes.tolist()  # trasforma l'array multidimensionale in lista
        j = 0
        for i in range(0, len(classes)):
            # consideriamo tutte le labels con uno score >= 0.6 e i cui bounding box non corrispondono a quelli gia'
            # inseriti con score piu' alto
            if (scores[i] >= 0.6 and boxes[i] not in bbx_coco):
                labels_coco.append(str(category_index[int(classes[i])]['name']))
                bbx_coco.append(boxes[i])
                j = j + 1

        # se tra le labels ci sono persone si cerca il sesso con un object detection tramite il People Model
        if ("person" in labels_coco):
            n_people = labels_coco.count("person")
            labels_people, bbx_people = pets_people_detection.find_labels(image_path, image_name, stub, request,
                                                                          "people_model", n_people)
        # se tra le labels ci sono cani o gatti si cercano le razze con un object detection tramite il Pets Model
        if ("cat" in labels_coco or "dog" in labels_coco):
            n_cat = labels_coco.count("cat")  # numero di gatti
            n_dog = labels_coco.count("dog")  # numero di cani
            n_pets = n_cat + n_dog
            # labels e bounding box trovati con il pets model
            labels_pets, bbx_pets = pets_people_detection.find_labels(image_path, image_name, stub, request, "pets_model", n_pets)

        # dizionario avente come chiavi i nomi delle labels e valori le rispettive occorrenze in labels_coco
        counter = Counter(labels_coco)
        counter = list(counter.items())  # converte il dizionario in una lista di coppie con nome della label e occorrenza

        #costruisco la stringa "string" da passare al robot
        string = "I see "
        for i in range(0, len(counter)):  # la lunghezza del counter corrisponde al numero di labels diverse
            if counter[i][1] == 1:  # se la classe ha solo un'occorrenza
                if (counter[i][0].startswith(vowels)):  # se il nome della classe inizia per vocale
                    string += "an " + counter[i][0] + ", "
                else:
                    string += "a " + counter[i][0] + ", "
            else: # se la classe ha piu' occorrenze
                s = ["sheep", "scissors", "skis"]
                es = ["sandwich", "toothbrush", "wine glass", "bus", "bench", "couch"]
                if (counter[i][0] in s):
                    string += str(counter[i][1]) + " " + counter[i][0] + ", "
                elif (counter[i][0] in es):
                    string += str(counter[i][1]) + " " + counter[i][0] + "es, "
                elif (counter[i][0] == "knife"):
                    string += str(counter[i][1]) + " knives, "
                elif (counter[i][0] == "person"):
                    string += str(counter[i][1]) + " people, "
                else:
                    string += str(counter[i][1]) + " " + counter[i][0] + "s, "
        string = string.rstrip(", ")  # se la frase termina con una virgola, devo trascurare quest'ultima
        if ("," in string):
            k = string.rfind(",")
            string = string[:k] + " and" + string[k + 1:]  # sostituisce l'ultima virgola della frase con "and"
        string += "!"
        if (labels_people != []):  # se trovo il sesso della/e persona/e
            n_man = labels_people.count("man")
            n_woman = labels_people.count("woman")
            if len(labels_people) == 1 and n_people == 1:  # nell'immagine c'e' una persona
                if(n_man == 1):
                    string += " He is a man"
                else:
                    string += " She is a woman"
            elif len(labels_people) == 1 and n_people > 1:
                # nell'immagine ci sono piu' persone ma ho riconosciuto il sesso di una
                string += " A person is a " + labels_people[0]
            else:  # ho riconosciuto il sesso di piu' persone
                if n_man > 1 and n_woman == 0:
                    string += " There are " + str(n_man) + " men"
                elif n_man == 0 and n_woman > 1:
                    string += " There are " + str(n_woman) + " women"
                elif n_man == 1 and n_woman == 1:
                    string += " There is a man and a woman"
                elif n_man > 1 and n_woman == 1:
                    string += " There is a woman and " + str(n_man) + " men"
                elif n_man == 1 and n_woman > 1:
                    string += " There is a man and " + str(n_woman) + " women"
                else:
                    string += " There are " + str(n_man) + " men and " + str(n_woman) + " women"
            string += "."
        if (labels_pets != []):  # se trovo le razze dei cani e/o dei gatti
            cat_breeds = []
            dog_breeds = []
            for i in range(0,len(labels_pets)):
                # separo le razze in due liste in base all'iniziale del nome: maiuscolo per la razza di un gatto e
                # minuscolo per la razza di un cane
                if labels_pets[i][0].isupper():
                    cat_breeds.append(labels_pets[i])
                else:
                    dog_breeds.append(labels_pets[i])
            if (n_cat == 1 and n_dog == 0):  # nell'immagine c'e' un gatto ma non ci sono cani
                pet_string = " The cat breed is " + labels_pets[0]
            elif (n_cat == 0 and n_dog == 1):  # nell'immagine c'e' un cane ma non ci sono gatti
                pet_string = " The dog breed is " + labels_pets[0]
            elif (n_cat > 1 and n_dog == 0):  # nell'immagine ci sono piu' gatti, ma non ci sono cani
                if (len(labels_pets) == 1):  # ho trovato la razza di uno solo dei gatti
                    pet_string = " The breed of a cat is " + labels_pets[0]
                else:  # ho trovato la razza dei gatti
                    pet_string = " The cat breeds are "
                    for i in range(0, len(labels_pets)):
                        pet_string += labels_pets[i] + ", "
            elif (n_cat == 0 and n_dog > 1):  # nell'immagine ci sono piu' cani, ma non ci sono gatti
                if (len(labels_pets) == 1):  # ho trovato la razza di un solo cane
                    pet_string = " The breed of a dog is " + labels_pets[0]
                else:  # ho trovato la razza dei cani
                    pet_string = " The dog breeds are "
                    for i in range(0, len(labels_pets)):
                        pet_string += labels_pets[i] + ", "
            else: #nell'immagine ci sono sia cani che gatti
                if (len(labels_pets) == 1):  # se con il Pets Model ho trovato solo il nome di una razza
                    if not dog_breeds:  # se non ci sono razze di cani
                        if n_cat == 1:
                            pet_string = " The cat breed is " + labels_pets[0]
                        else:
                            pet_string = " The breed of a cat is " + labels_pets[0]
                    else:  # se non ci sono razze di gatti
                        if n_dog == 1:
                            pet_string = " The dog breed is " + labels_pets[0]
                        else:
                            pet_string = " The breed of a dog is " + labels_pets[0]
                else:  # ho trovato piu' razze
                    if not cat_breeds and n_dog > 1:  # se non ci sono razze di gatti e ci sono piu' cani nell'immagine
                        pet_string = " The dog breeds are "
                    elif not dog_breeds and n_cat > 1:  # se non ci sono razze di cani e ci sono piu' gatti
                        pet_string = " The cat breeds are "
                    else:  # se ci sono razze di cani e di gatti
                        pet_string = " The dog and cat breeds are "
                    for i in range(0, len(labels_pets)):
                        pet_string += labels_pets[i] + ", "
            pet_string = pet_string.rstrip(", ")
            if ("," in pet_string):  # sostituisco l'ultima virgola della stringa con "and"
                k = pet_string.rfind(",")
                pet_string = pet_string[:k] + " and" + pet_string[k + 1:]
            pet_string += "."
            string += pet_string
        result = [string]

        log_string = string + '\n'  # stringa da passare al file di log salvato in hdfs

        # consideramo anche l'oggetto con score piu' alto compreso tra 0.5 e 0.6 se non coincide con un oggetto
        # gia' considerato
        if(scores[j]<0.6 and scores[j]>0.5 and boxes[j] not in bbx_coco):
            class_name=str(category_index[int(classes[j])]['name'])
            bbx_coco.append(boxes[j])
            if(class_name in labels_coco):
                result.append("Is there also another " + class_name + "?")
                log_string += "Maybe there is also another " + class_name + ".\n"
            else:
                if class_name.startswith(vowels):
                    result.append("Is there also an " + class_name + "?")
                    log_string += "Maybe there is also an " + class_name + ".\n"
                else:
                    result.append("Is there also a " + class_name + "?")
                    log_string += "Maybe there is also a " + class_name + ".\n"

        log_string = add_bbx_log(log_string, bbx_coco, '{}_coco.jpg'.format(image_name))
        if(labels_people != []):
            log_string = add_bbx_log(log_string, bbx_people, '{}_people.jpg'.format(image_name))
        if (labels_pets != []):
            log_string = add_bbx_log(log_string, bbx_pets, '{}_pets.jpg'.format(image_name))

    # se la label con lo score piu' alto ha score compreso tra 0.2 e 0.6, inserisco in un vettore tutte le labels
    # con lo score compreso tra queste soglie
    elif (scores[0] < 0.6 and scores[0] > 0.2):
        # stringa da inserire nel file di log con le ipotesi su possibili oggetti presenti nell'immagine
        log_string = "I'm not sure what's in the picture.\nMaybe there is"
        for i in range(0, len(classes)):
            if (scores[i] < 0.6 and scores[i] > 0.2):
                class_name = str(category_index[int(classes[i])]['name'])
                if (class_name not in result):
                    result.append(class_name)
                    bbx_coco.append(boxes[i])
                    if class_name.startswith(vowels):
                        log_string += " an " + class_name + ","
                    else:
                        log_string += " a " + class_name + ","
        log_string = log_string.rstrip(", ")
        if ("," in log_string):  # sostituisco l'ultima virgola della stringa con "or"
            k = log_string.rfind(",")
            log_string = log_string[:k] + " or" + log_string[k + 1:]
        log_string += ".\n"
        log_string = add_bbx_log(log_string, bbx_coco, '{}_coco.jpg'.format(image_name))

    # se la label con lo score piu' alto ha uno score <= 0.2 passo al robot il vettore "result" nullo
    else:
        log_string = "I don't know what's in the picture!"

    if port_result == 0:  # se l'HDFS e' connesso, creo il file di log con la stringa
        with client_hdfs.write('/zora-object-detection/logs/{}.log'.format(image_name)) as writer:
            writer.write(log_string)

    return result


# aggiunge le coordinate dei bounding box alla stringa da inserire nel file di log
def add_bbx_log(log_string, box, image):
    log_string += "\nBounding box " + image + ":\n"
    for i in range(0,len(box)):
       log_string += str(box[i]) + '\n'
    return log_string
