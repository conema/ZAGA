import rdflib
import ast
import json
import datetime
import string
import re
from collections import Counter
import sys
import os
import subprocess
from nltk.stem import WordNetLemmatizer
import graphviz as gv
from rdflib import Literal, XSD


os.environ["PATH"] += os.pathsep + './graphviz'#graph viz path

SEMAFOR_DIR = 'semafor-master/'


#OWL DESCRIPTORS https://www.w3.org/TR/2004/REC-owl-features-20040210/#Class
OWL_TYPE = rdflib.term.URIRef(u'http://www.w3.org/1999/02/22-rdf-syntax-ns#type')
OWL_PROPERTY = rdflib.term.URIRef(u'http://www.w3.org/2002/07/owl#Property')
OWL_OBJECT_PROPERTY = rdflib.term.URIRef(u'http://www.w3.org/2002/07/owl#ObjectProperty')
OWL_CLASS = rdflib.term.URIRef(u'http://www.w3.org/2002/07/owl#Class')
OWL_SUB_CLASS_OF = rdflib.term.URIRef(u'http://www.w3.org/2000/01/rdf-schema#subClassOf')
OWL_DOMAIN = rdflib.term.URIRef(u'http://www.w3.org/2000/01/rdf-schema#domain')
OWL_DATATYPE_PROPERTY = rdflib.term.URIRef(u'http://www.w3.org/2002/07/owl#DatatypeProperty')
#OWL_INDIVIDUAL = rdflib.term.URIRef(u'http://www.w3.org/2002/07/owl#Individual')


#CLASSES
XPROJECT_CLASS_WORD  = rdflib.term.URIRef('it.unica/xproject#Word')
XPROJECT_CLASS_COMPOUND_WORD  = rdflib.term.URIRef('it.unica/xproject#CompoundWord')
UKB_CLASS_WSD_CONCEPT  = rdflib.term.URIRef('it.unica/ukb#Concept')
SEMAFOR_CLASS_FRAME = rdflib.term.URIRef('it.unica/semafor#Frame')
SEMAFOR_CLASS_FRAME_ELEMENT = rdflib.term.URIRef('it.unica/semafor#FrameElement')


#PROPERTIES
EVOKE_FRAME = 'http://it.unica.xproject/semafor#evokeFrame'					#semafor
HAS_FRAME_ELEMENT = 'http://it.unica.xproject/semafor#hasFrameElement'		#semafor
IS_FRAME_ON = 'http://it.unica.xproject/semafor#isFrameElementOn'			#semafor
#IS_FRAME_OF = 'http://#isFrameElementOf'									#semafor
HAS_DEPENDENCY = 'http://it.unica.xproject/corenlp#hasDependency:'			#corenlp
HAS_POS = 'http://it.unica.xproject/corenlp#postype'						#corenlp
HAS_ENTITY_TYPE = 'http://it.unica.xproject/corenlp#hasEntityType'			#corenlp
HAS_COREF = 'http://it.unica.xproject/corenlp#hasCoref'						#corenlp
IS_COREF = 'http://it.unica.xproject/corenlp#isCorefOf'						#corenlp
BEGIN = 'http://it.unica.xproject/corenlp#begin'							#corenlp
END = 'http://it.unica.xproject/corenlp#end'								#corenlp
IS_COMPOSED_BY = 'http://it.unica.xproject/#isComposedBy'					#this
HAS_WSD_CONCEPT = 'http://it.unica.xproject/ukb#hasWSDConcept'				#ukb
HAS_NAME = 'http://it.unica.xproject/ukb#hasWSDName'						#ukb


XPROJECT_OFFSET = 'http://it.unica/xproject#offset_'
FRAME = 'http://it.unica/xproject/semafor:frame#'
FRAME_ELEMENT = 'http://it.unica/xproject/semafor:frameElement#'
UKB_CONCEPT = 'http://it.unica/xproject/ukb:concept#'

class Utils:
	
	@staticmethod
	def mapPosTags(pos):
		if pos in ['NN','NNS','NNP','NNPS']:
			return 'n'
		elif pos in ['VB','VBD','VBG','VBN','VBP','VBZ']:
			return 'v'
		elif pos in ['JJ','JJR','JJS']:
			return 'a'
		elif pos in ['RB','RBR','RBS']:
			return 'r'
		else:
			return ''
			
	@staticmethod
	def get_lemma(word, pos):
		wordnet_lemmatizer = WordNetLemmatizer()
		return wordnet_lemmatizer.lemmatize(word, pos=pos)
		
	#compute an offset startinf from corenlp tokens list
	@staticmethod
	def get_offset(tokens, begin_index, end_index):
		begin = 0
		end = 0
		for token in tokens:
			if token['index'] == begin_index:
				begin = token['characterOffsetBegin']
			if token['index'] == end_index - 1:
				end = token['characterOffsetEnd']
				break
		return str(begin) + '_' + str(end)
		

