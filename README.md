# ZAGA
NAO/**Z**ora **A**rchitecture for **G**oogle **A**ssistant

This repository contains the source of a [Choregraphe](https://community.ald.softbankrobotics.com/en/resources/faq/developer/what-choregraphe) project that allows robots by [Softbank Robotics](https://www.softbankrobotics.com/) to behave like a Google Assistant, refereed as _GA_ from now, it responds to normal voice command/questions (eg. "Who is Obama?", "What time is it?", "Turn off the light", etc...) like a normal Google Home would do.

<a href="http://hri.unica.it/"><img src="https://raw.githubusercontent.com/conema/hri-unica/master/img/logos/HRILab.png" align="right" width="300px"></a>

The script provides visual feedback to the user, eyes change colors according to the robot states: red indicates an error, blue indicates that it's listening, white indicate when it's idle. This was tested on a real robot, named [Zora](http://www.zorarobotics.be/index.php/en/zorabot-zora), that is based on the Nao robot by Softbank.

The robot should have [ALSA](https://en.wikipedia.org/wiki/Advanced_Linux_Sound_Architecture) installed, note that in Zora it's already installed.

More than the default behavior of a GA device, the robot, with this project can run custom Choregraphe projects, by saying something like `execute object recognition`. This projects have 4 sub-projects: Action Commands, Object Detection, Sentiment Analysis and Bingo.
All the following points are mandatory to use this project 

## Prerequisites
1. Go in the folder where you want to clone the project
1. Clone the project
```
   git clone https://github.com/conema/ZAGA.git
```
3. Enter into the cloned folder

# Table of contents
- Automatic procedure
   - [Dockerfile](#dockerfile)
- Manual procedure
   - [Google Assistant](#1-ga-server-by-conema)
      - [Prerequisites](#prerequisites-1)
      - [Usage with NAO/Zora](#usage-with-naozora)
   - [Humanoid robot action commands with an ontology](#2-humanoid-robot-action-commands-with-an-ontology-by-fspiga13)
      - [Prerequisites](#prerequisites-2)
      - [Usage](#usage)
      - [Usage with NAO/Zora](#usage-with-naozora-1)
   - [Object recognition](#3-object-recognition-by-fabseulo)
      - [Prerequisites](#prerequisites-3)
      - [Usage](#usage-1)
      - [Usage with NAO/Zora](#usage-with-naozora-2)
   - [Sentiment analysis](#4-sentiment-analysis-by-Mattia-Atzeni)
      - [Prerequisites](#prerequisites-4)
      - [Usage with NAO/Zora](#usage-with-naozora-3)
   - [Bingo](#5-bingo)
      - [Usage with NAO/Zora](#usage-with-naozora-4)
## Automatic precedure
### Dockerfile
1. Go in the `Dockerfile` folder
1. `docker build --tag="build1:ZAGA" .`
1. Wait for the end of the build (it can take some time)
1. Create/open a project in the [Actions Console](http://console.actions.google.com/)
1.  [Register a device model](https://developers.google.com/assistant/sdk/guides/service/python/embed/register-device)
1.  Download `credentials.json`
1. Use the [`google-oauthlib-tool`](https://github.com/GoogleCloudPlatform/google-auth-library-python-oauthlib) to generate credentials:

```
google-oauthlib-tool --client-secrets credentials.json \
                             --credentials devicecredentials.json \
                             --scope https://www.googleapis.com/auth/assistant-sdk-prototype \
                             --save

```
8. To start do the box follow:
    1. [Start GA](#usage-with-naozora)
    1. [Start Action Ontology](#usage-with-naozora-1)
    1. [Start Object Recognition](#usage-with-naozora-2)
    1. [Start Sentiment Analysis](#usage-with-naozora-3)

**Note:** Use `docker run -it build1:ZAGA` to start the container
## Manual procedure
### [1] GA Server (By [conema](https://github.com/conema/GA-Server)) 
[🡅 TOP](#zaga)

GA-Server is a simple script that works as a server, it receives audio chunks from a client and it forwards them to Google Assistant. This script can be used (with a client) when you want to integrate GA into a device that is not powerful enough or in a device where the SDK couldn't be installed.

**Note:** NodeJS >= 11 is needed, you can install it by following the guide in the [NodeJs website](https://nodejs.org/en/)

#### Prerequisites
Steps from 1 to 5 are needed only if you don't have a registered project or the Google Assistant SDK credentials

1.  Create/open a project in the [Actions Console](http://console.actions.google.com/)
1.  [Register a device model](https://developers.google.com/assistant/sdk/guides/service/python/embed/register-device)
1.  Download `credentials.json`
1.  Install the [`google-oauthlib-tool`](https://github.com/GoogleCloudPlatform/google-auth-library-python-oauthlib) in a [Python 3](https://www.python.org/downloads/) virtual environment:

```
python3 -m venv env && \
env/bin/python -m pip install --upgrade pip setuptools && \
env/bin/pip install --upgrade "google-auth-oauthlib[tool]"

```

5.  Use the [`google-oauthlib-tool`](https://github.com/GoogleCloudPlatform/google-auth-library-python-oauthlib) to generate credentials:

```
env/bin/google-oauthlib-tool --client-secrets credentials.json \
                             --credentials devicecredentials.json \
                             --scope https://www.googleapis.com/auth/assistant-sdk-prototype \
                             --save

```

6.  `git clone https://github.com/conema/GA-Server.git`
1. `cd GA-Server/`
1.  Run `npm install`
1.  Open `index.js` and edit the host, port, input/output sample rate if needed (Default settings are: accept connections from all IPv4 addresses on the local machine on port 4000, audio with 16000Hz sample rate)
1. Register [Custom Device Actions](https://developers.google.com/assistant/sdk/guides/library/python/extend/custom-actions)
   1. Download the [gactions-cli](https://developers.google.com/actions/tools/gactions-cli) and move the executable in `../CDA/`
   1. Update custom actions to GA and set the testing mode. Change *<project_id>* with the [ID of the Google Assistant model](https://cloud.google.com/resource-manager/docs/creating-managing-projects#identifying_projects) (the one created in point 2)
   ```
   cd ../CDA/ && \
   ./gactions update --action_package actions.json --project <project_id> && \
   ./gactions test --action_package actions.json --project <project_id>
   ```

#### Usage with NAO/Zora
1. Go into `GA-Server` folder
1.  Run `node index.js`
If all it's working, it should appear a message with "TCP server listen on address: x.w.y.z:p". That means that the server is ready to receive audio chunks from a client.
1.   Open the Choregraphe project, right click on the **GA** box, click **Set parameter** and set as **IP** the IP of the computer and as port 4000.
1.   Start the behavior, and after been said `Hey Zora`, wait for the beep and for eyes becoming blue, and say `What time is it?`


### [2] Humanoid robot action commands with an ontology (By [fspiga13](https://github.com/Fspiga13/Humanoid-Robot-Obeys-Human-Action-Commands-through-a-Robot-Action-Ontology))
[🡅 TOP](#zaga)

This engine allows the NAO/Zora robot to **execute natural language commands** spoken by the user. To provide the robot with knowledge, we have defined an **action robot ontology**. The ontology is fed to a NLP engine that performs a machine reading of the input text (in natural language) given by a user and tries to identify action commands for the robot to execute. 

A video showing the proposed system and the knowledge that is possible to extract from the human interaction with Zora is available [here](https://youtu.be/CC9NzlbF0gQ).

More information about this project can be found [here](https://github.com/Fspiga13/Humanoid-Robot-Obeys-Human-Action-Commands-through-a-Robot-Action-Ontology).

#### Prerequisites
The project should works with **Python 2.7** and Python 3, but only Python 2.7 is tested. **Java 8** is also needed.

**Note:** this project use the python env variable for starting scripts, so the variable *python* should be present.
1. Install required modules
```
pip install requests && \
pip install hdfs && \
pip install bs4 && \
pip install rdflib && \
pip install nltk && \
pip install graphviz && \
pip install stanfordcorenlp
```
2. Download [Stanford CoreNLP](http://nlp.stanford.edu/software/stanford-corenlp-full-2018-10-05.zip) and move the unzipped folder into the `textToRdf` folder

#### Usage
1. Run the CoreNLP server
```
cd ZoraAC/textToRdf/stanford-corenlp-full-2018-10-05/ && \
java -mx6g -cp "*" edu.stanford.nlp.pipeline.StanfordCoreNLPServer -port 9000 -timeout 15000
```
**Note:** Using Stanford locally requires about 4GB of free RAM

2. Test the operation of the RDF creation tool, it should return an RDF. Change `<sentence>` with the an input sequence, like: *Zora, raise your right arm*. Note that this can be very slow.
```
cd ../src/ && \
python xproject.py -t "<sentence>"
```
3. Start ZoraNlpReasoner.jar and use test Actions without NAO/Zora
```
cd ../../ && \
java -jar ZoraNlpReasoner.jar
```
4. Write commands, like "Hey Zora, raise your right arm" or "Hey Zora, move your head to the left"

#### Usage with NAO/Zora
1. Run the CoreNLP server
```
cd ZoraAC/textToRdf/stanford-corenlp-full-2018-10-05/ && \
java -mx4g -cp "*" edu.stanford.nlp.pipeline.StanfordCoreNLPServer -port 9000 -timeout 15000
```
**Note:** Using Stanford locally requires about 4GB of free RAM

2. Start ZoraNlpReasoner as server
```
cd ../../ && \
java -jar ZoraNlpReasoner.jar use zora
```
3.  Open the Choregraphe project, right click on the **AC** box, click **Set parameter** and set as **URL** the url of the precedent server (something like `http://<IP>:5003`, where IP is the internet address of the computer where ZoraNlpReasoner.jar is running)
1.  Start the behavior, and after been said `Hey Zora`, wait for the beep and for eyes becoming blue, and say `execute ontology`
1. Say or write the command in the dialog console, like "Hey Zora, raise your right arm" or "Hey Zora, move your head to the left"

### [3] Object recognition (By [fabseulo](https://github.com/fabseulo/Zora-Object-Detection))
[🡅 TOP](#zaga)

This application is a **server** that performs **object detection** with NAO/Zora, using models loaded in the **TensorFlow ModelServer**. The robot takes a picture and sends it to the server that answers with the recognized object. The robot asks the user if its bet is right, if so, it makes happy gestures, otherwise it makes sad ones. The box stops when the user say "stop".

**Note**: the script found in this repository is a modified version of fabseulo's one, the original version will not work with this.
#### Prerequisites
**Python 3** is required to run this script. Tested in Ubuntu.

1. Install required modules
```
pip3 install tensorflow && \
pip3 install grpcio && \
pip3 install numpy && \
pip3 install scipy && \
pip3 install object-detection && \
pip3 install hdfs && \
pip3 install tensorflow-serving-api && \
pip3 install flask
```
2. Install TFX via APT ([More installation alternative here](https://www.tensorflow.org/tfx/serving/setup))
    1. Add TensorFlow Serving distribution URI as a package source
    ```
    echo "deb [arch=amd64] http://storage.googleapis.com/tensorflow-serving-apt stable tensorflow-model-server tensorflow-model-server-universal" | tee /etc/apt/sources.list.d/tensorflow-serving.list && \
    curl https://storage.googleapis.com/tensorflow-serving-apt/tensorflow-serving.release.pub.gpg | apt-key add
    ```

   1. Install and update TensorFlow ModelServer
   ```
   apt-get update && apt-get install tensorflow-model-server
   ```
 
 3. Compile [protobuf files](https://github.com/tensorflow/models/)
    1. Install **protobuf compiler** ([more builds here](https://github.com/protocolbuffers/protobuf))
    ```
    wget https://github.com/google/protobuf/releases/download/v3.3.0/protoc-3.3.0-linux-x86_64.zip && \
    unzip protoc-3.3.0-linux-x86_64.zip
    ```

    1. Go in the folder where you want to download the protobuf files and clone them
    ```
    git clone https://github.com/tensorflow/models.git
    ```
   
    1. Enter into the folder where the files are been cloned and compile them. Change **<PROTOC_FOLDER>** with the absolute path of where you unzipped `protoc-3.3.0-linux-x86_64.zip`
    ```
    cd models/research/  && \
    <PROTOC_FOLDER>/bin/protoc object_detection/protos/*.proto --python_out=.
    ```

#### Usage
1. Go to `ZoraOD/`
1. Edit `model_server.config`, in rows 4, 9, 14 you'll need change `<PATH_TO_PROJECT>` with the **absolute path** of the folder where you downloaded/cloned this project
1. Start TFX, remember to change `PATH_TO_PROJECT`
```
tensorflow_model_server --port=4001 --model_config_file='<PATH_TO_PROJECT>/ZoraOD/model_server.config'
```
4. Run the test.py to check that all is working.
```
python3 test.py --image_path=Images_test/harry_meghan.jpg
```
If the execution is without errors, the script should return a predicted string and the image with bounding boxes should been created in the `Images_bbx` folder.
If Hadoop is running, the application saves a log file and the predicted images into HDFS.

#### Usage with NAO/Zora
1. Go into the `ZoraOD/` folder
1. Edit `model_server.config`, in rows **4**, **9** and **14**, you'll need change `<PATH_TO_PROJECT>` with the **absolute path** of the folder where you downloaded/cloned this project
1. Start TFX, remember to change `PATH_TO_PROJECT`
```
tensorflow_model_server --port=4001 --model_config_file='<PATH_TO_PROJECT>/ZoraOD/model_server.config'
```
3. Start the image receiver server located in the Object detection folder
```
python3 image_receiver.py
```
4. Open the Choregraphe project, right click on the **OD** box, click **Set parameter** and set as **URL** the url of the preceded server (something like `http://<IP>:4002`, where IP is the internet address of the computer where image_receiver.py is running)
1. Start the behavior, and after been said  `Hey Zora`, wait for the beep and for eyes becoming blue, and say `execute object detection`
1. Say or write in the dialog box the text and follow the NAO/Zora commands.

### [4] Sentiment analysis (by Mattia Atzeni)
[🡅 TOP](#zaga)

NAO/Zora can automatically understand the **polarity** of what the user says. Based on the sentiment, the robot will make a neutral, positive or negative animation.

#### Prerequisites
1. [Apache Maven](https://maven.apache.org/download.cgi)  needs to be installed.
1. Download [glove.6B](http://nlp.stanford.edu/data/glove.6B.zip) and place all the text files into `ZoraSA/WordVectors/glove.6B/`
1. Download SentiWordNet_3.0
```
cd ZoraSA/ &&\
wget -O SentiWordNet_3.0.txt https://github.com/aesuli/SentiWordNet/blob/master/data/SentiWordNet_3.0.0.txt
```
4. Download opennlp files
```
cd ZoraSA/BUPPolarityDetection/opennlp/ &&\
wget http://opennlp.sourceforge.net/models-1.5/en-ner-date.bin &&\
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
```

#### Usage with NAO/Zora
1. Go into the `ZoraSA` folder
1. Start the polarity detection service. `<PATH_TO_MAVEN>` should be changed with the directory to the local installed Maven.
```
cd ZoraSA/BUPPolarityDetection/  && \
<PATH_TO_MAVEN>/bin/mvn jetty:run -Djetty.http.port=8080 -Xms1G -Xmx2G -Dorg.bytedeco.javacpp.maxbytes=8G -Dorg.bytedeco.javacpp.maxphysicalbytes=10G
```
2.  Open the Choregraphe project, right click on the **SA** box, click **Set parameter** and set as **URL** the url of the preceded server (something like `http://<IP>:8080/sa/service`, where IP is the internet address of the computer where Jetty is running)
1.  Start the behavior, and after been said `Hey Zora`, wait for the beep and for eyes becoming blue, and say `execute sentiment analysis`
1.  Say or write in the dialog box the text

### [5] Bingo
[🡅 TOP](#zaga)

Bingo is a **self-contained package**, which works **out-of-the-box** without any configuration, that plays Bingo with the user. It behaves as follows:
1. NAO/Zora explains the rules to play with her;
1. She starts to say random bingo numbers until she hears "*bingo*", "*line*", "*stop*" or "*repeat*": 
   1. If **bingo** or **line** are said, NAO/Zora asks to dictate the numbers and after 5 (for line) or 15 (for bingo) are said, she stop the user and she repeat, for confirmation, all the numbers that the user said.
    If the user confirms the numbers, NAO/Zora checks if these numbers are in the extracted ones: if so, the user win, otherwise NAO/Zora starts saying the numbers again.
    Otherwise, if the user didn't confirm the numbers, NAO/Zora asks to dictate them again;
    1. If **stop** is said, the game stops;
    1. If **repeat** is said, NAO/Zora repeats the last number that she said and the game continues.

#### Usage with NAO/Zora
 1. Start the behavior, and after been said  `hey Zora`, wait for the beep and for eyes becoming blue, and say `execute bingo`
 1. Say or write in the dialog box the text


