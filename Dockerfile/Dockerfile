FROM ubuntu:16.04
LABEL maintainer="Emanuele Concas" website="https://github.com/conema"

# GA 
EXPOSE 4000 

# Stanford CoreNLP
EXPOSE 9000

# ZoraNlpReasoner
EXPOSE 5003

# Tensorflow
EXPOSE 4001

# image_receiver
EXPOSE 4002

# jetty
EXPOSE 8080

RUN apt-get update
RUN apt-get -y upgrade

RUN apt-get install -y git
#RUN apt-get install -y python2.7 python-pip
RUN apt-get install -y python3.5 python3-pip
RUN apt-get install -y curl
RUN curl -sL https://deb.nodesource.com/setup_11.x | bash -
RUN apt-get install -y nodejs
RUN apt-get install -y openjdk-8-jdk
RUN apt-get install -y wget
RUN apt-get install -y unzip

WORKDIR /root/
RUN update-alternatives --install /usr/bin/python python /usr/bin/python3.5 1
RUN git clone https://github.com/conema/ZAGA.git

# GA
RUN python3 -m pip install --upgrade pip setuptools
RUN pip3 install --upgrade "google-auth-oauthlib[tool]"

WORKDIR /root/ZAGA/
RUN git clone https://github.com/conema/GA-Server.git
WORKDIR /root/ZAGA/GA-Server/
RUN npm install

# AC
RUN pip3 install requests &&\
pip3 install hdfs &&\
pip3 install bs4 &&\
pip3 install rdflib &&\
pip3 install nltk &&\ 
pip3 install graphviz &&\
pip3 install stanfordcorenlp &&\
pip3 install networkx &&\
pip3 install matplotlib

WORKDIR /root/ZAGA/ZoraAC/textToRdf/
RUN wget http://nlp.stanford.edu/software/stanford-corenlp-full-2018-10-05.zip
RUN unzip stanford-corenlp-full-2018-10-05.zip
RUN rm stanford-corenlp-full-2018-10-05.zip

# Object detection
RUN pip3 install tensorflow &&\
pip3 install grpcio &&\
pip3 install numpy &&\
pip3 install scipy &&\
pip3 install object-detection &&\
pip3 install hdfs &&\
pip3 install tensorflow-serving-api &&\
pip3 install flask &&\
pip3 install flask_restful &&\
pip3 install imageio

RUN echo "deb [arch=amd64] http://storage.googleapis.com/tensorflow-serving-apt stable tensorflow-model-server tensorflow-model-server-universal" | tee /etc/apt/sources.list.d/tensorflow-serving.list && \
curl https://storage.googleapis.com/tensorflow-serving-apt/tensorflow-serving.release.pub.gpg | apt-key add

RUN apt-get update && apt-get install -y tensorflow-model-server

WORKDIR /root/
RUN git clone https://github.com/tensorflow/models.git

RUN mkdir protobuf
WORKDIR /root/protobuf
RUN wget https://github.com/google/protobuf/releases/download/v3.3.0/protoc-3.3.0-linux-x86_64.zip
RUN unzip protoc-3.3.0-linux-x86_64.zip

WORKDIR /root/models/research/
RUN /root/protobuf/bin/protoc object_detection/protos/*.proto --python_out=.

RUN echo "export PYTHONPATH=$PYTHONPATH:/root/models/research:/root/models/research/slim" >> ~/.bashrc
RUN source ~/.bashrc

WORKDIR /root/ZAGA/ZoraOD/
RUN mkdir -p Models/coco_model/1/
RUN mkdir -p Models/people_model/1/
RUN mkdir -p Models/pets_model/1/

RUN wget https://github.com/hri-unica/Zora-Object-Detection/raw/master/Models/coco_model/1/saved_model.pb -O Models/coco_model/1/saved_model.pb
RUN wget https://github.com/hri-unica/Zora-Object-Detection/raw/master/Models/people_model/1/saved_model.pb -O Models/people_model/1/saved_model.pb
RUN wget https://github.com/hri-unica/Zora-Object-Detection/raw/master/Models/pets_model/1/saved_model.pb -O Models/pets_model/1/saved_model.pb

# SA
WORKDIR /root/
RUN wget http://it.apache.contactlab.it/maven/maven-3/3.6.1/binaries/apache-maven-3.6.1-bin.tar.gz
RUN tar -xvf apache-maven-3.6.1-bin.tar.gz
RUN rm apache-maven-3.6.1-bin.tar.gz

RUN echo 'export MAVEN_OPTS="-Xmx2G -Dorg.bytedeco.javacpp.maxbytes=10G -Dorg.bytedeco.javacpp.maxphysicalbytes=10G"' >> ~/.bashrc
RUN source ~/.bashrc

WORKDIR /root/ZAGA/ZoraSA/
RUN wget -O SentiWordNet_3.0.txt https://github.com/aesuli/SentiWordNet/blob/master/data/SentiWordNet_3.0.0.txt

WORKDIR /root/ZAGA/ZoraSA/BUPPolarityDetection/opennlp/
RUN wget http://opennlp.sourceforge.net/models-1.5/en-ner-date.bin &&\
wget http://opennlp.sourceforge.net/models-1.5/en-ner-location.bin &&\
wget http://opennlp.sourceforge.net/models-1.5/en-ner-organization.bin &&\
wget http://opennlp.sourceforge.net/models-1.5/en-parser-chunking.bin &&\
wget http://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin &&\
wget http://opennlp.sourceforge.net/models-1.5/en-pos-perceptron.bin &&\
wget http://opennlp.sourceforge.net/models-1.5/en-sent.bin &&\
wget http://opennlp.sourceforge.net/models-1.5/en-token.bin &&\
wget https://github.com/aciapetti/opennlp-italian-models/raw/master/models/it/it-pos-maxent.bin &&\
wget https://github.com/aciapetti/opennlp-italian-models/raw/master/models/it/it-pos_perceptron.bin &&\
wget https://github.com/aciapetti/opennlp-italian-models/blob/master/models/it/it-sent.bin &&\
wget https://github.com/aciapetti/opennlp-italian-models/blob/master/models/it/it-token.bin


WORKDIR /root/ZAGA/ZoraSA/WordVectors/glove.6B/
RUN wget http://nlp.stanford.edu/data/glove.6B.zip
RUN unzip glove.6B.zip
RUN rm glove.6B.zip

WORKDIR /root/
ENTRYPOINT [ "/bin/bash" ]